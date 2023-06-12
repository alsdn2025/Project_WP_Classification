package com.example.myapp1;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 *Fragment for: Post inference results
 *@authorMW
 */
public class PostingFragment extends DialogFragment implements View.OnClickListener {
    public static final int REQUEST_LOCATION_PERMISSION_CODE = 100;
    public static final String TAG = "POSTING_FRAGMENT";
    public static final String DB_NAME = "mw_temp.db"; // DB 이름, 임의로 지정함
    private static final String FILE_NAME = "FILE_NAME";
    private static final String ORGAN = "ORGAN";
    private static final String RESULT = "RESULT";

    private SQLiteDatabase database;
    private DBHelper dbHelper;
    TextInputEditText textInput;
    private FusedLocationProviderClient fusedLocationProviderClient; // local location Provider

    private String fileName;
    private String organ;
    private String result;
    private String classNumber;
    private String className;
    private double latitude = 0.0;
    private double longitude = 0.0;

    public PostingFragment() {}

    /**
     * factory method
     * @param fileName Image File Name
     * @param organ Inferred Plant Organ of Image file
     * @param result Inferred output, format of "className_classNumber"
     * @return A new instance of fragment PostingFragment.
     */
    public static PostingFragment newInstance(String fileName, String result, PlantOrgans organ) {
        PostingFragment fragment = new PostingFragment();
        Bundle args = new Bundle();
        args.putString(FILE_NAME, fileName);
        args.putString(RESULT, result);
        args.putString(ORGAN, organ.name());
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.fileName = getArguments().getString(FILE_NAME);
            this.result = getArguments().getString(RESULT);
            this.organ = getArguments().getString(ORGAN);
        }

        checkPermissions(); // 위치 권한 요청
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
    }

    /**
     * View 가 만들어진 이후 호출됨
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posting, container, false);

        Button postingBtn = view.findViewById(R.id.postBtn);
        postingBtn.setOnClickListener(this);
        TextView filenameTextView = view.findViewById(R.id.posting_filenameTextview);
        TextView organTextView = view.findViewById(R.id.posting_Organ_textview);
        TextView classNumberTextView = view.findViewById(R.id.posting_ClassNumber_textview);
        this.textInput = view.findViewById(R.id.posting_EditText);

        filenameTextView.setText(fileName);
        organTextView.setText(organ);
        classNumberTextView.setText(result);

        StringTokenizer tokenizer = new StringTokenizer(result, "_");
        this.className = tokenizer.nextToken();
        this.classNumber = tokenizer.nextToken();

        // 임시로 DB 데이터 확인차 만들어 놓음
        // 버튼 클릭시 DB 데이터를 하나씩 읽어 Log 로 출력
        Button checkDBBtn = view.findViewById(R.id.checkBtn);
        checkDBBtn.setOnClickListener(tempView -> checkDB());

        // DB 초기화
        // DB 이름 static 으로 임의로 지정
        dbHelper = new DBHelper(this.getContext(), DB_NAME, null, 1);
        database = dbHelper.getWritableDatabase();

        getLocation(); // 위치 정보 요청
        return view;
    }

    /**
     * 위치 권한을 체크하고 없으면 요청하는 메서드
     */
    private void checkPermissions(){
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            String[] permissions = new String[2];
            permissions[0] = android.Manifest.permission.ACCESS_FINE_LOCATION;
            permissions[1] = android.Manifest.permission.ACCESS_COARSE_LOCATION;
            ActivityCompat.requestPermissions(getActivity(), permissions,REQUEST_LOCATION_PERMISSION_CODE );
        }
    }

    /**
     * DB 확인을 위해 임시로 제작
     * DB에 저장된 데이터를 모두 읽어오는 메서드, Logcat 에서 파라미터로 Verbose 선택해야 보임
     */
    @SuppressLint("Range")
    public void checkDB() {
        Log.i(TAG, "DB를 읽어옵니다!");
        List<Integer> idList = new ArrayList<>();
        List<Double> latList = new ArrayList<>(); // 위도
        List<Double> longList = new ArrayList<>(); // 경도
        List<String> fileNameList = new ArrayList<>(); // 파일명
        List<String> classNameList = new ArrayList<>(); // 클래스명
        List<String> commentList = new ArrayList<>();

        String query = "SELECT * FROM location";
        Cursor cursor = database.rawQuery(query, null);
        while (cursor.moveToNext()) {
            idList.add(cursor.getInt(cursor.getColumnIndex("id")));
            latList.add(cursor.getDouble(cursor.getColumnIndex("lat"))); //위도용 테이블에 이번 위도값 저장
            longList.add(cursor.getDouble(cursor.getColumnIndex("long"))); //경도용 테이블에 이번 경도값 저장
            fileNameList.add(cursor.getString(cursor.getColumnIndex("filename")));
            classNameList.add(cursor.getString(cursor.getColumnIndex("class")));
            commentList.add(cursor.getString(cursor.getColumnIndex("comment")));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("------------------------").append(System.lineSeparator());
        try {
            for (int i = 0; i < latList.size(); i++) {
                sb.append(idList.get(i)).append(" ")
                        .append(latList.get(i)).append(" ")
                        .append(longList.get(i)).append(" ")
                        .append(fileNameList.get(i)).append(" ")
                        .append(classNameList.get(i)).append(" ")
                        .append("| comment : ")
                        .append(commentList.get(i)).append(System.lineSeparator());
            }
            Log.i(TAG, sb.toString());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            cursor.close();
        }

        Toast.makeText(this.getContext(), "DB Check 완료, logcat 을 확인하세요", Toast.LENGTH_SHORT).show();
        cursor.close();
    }

    /**
     * get last location( latitude, longitude )
     */
    private void getLocation(){
        try{
            checkPermissions();
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(getActivity(), location -> {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }else {
                    throw new SecurityException();
                }
            });
        }catch (SecurityException e){
            Log.e(TAG, "-getLocation() : failed to get location");
            Log.e(TAG, e.getMessage());
            Toast.makeText(this.getContext(), "위치를 가져올 수 없습니다. 위치 기능이 꺼져 있는지, 위치 권한을 수락했는지 확인해주세요.",Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }


    /**
     * delete latest data from db
     */
    public void deleteData(){
        int lastIndex = dbHelper.getLastIndex();
        if(lastIndex <= 0){
            Toast.makeText(this.getContext(), "삭제할 데이터가 없습니다.",Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this.getContext(), "(Developer only)DB 데이터를 삭제합니다. id=" + lastIndex +"행이 삭제됩니다.",Toast.LENGTH_SHORT).show();
        dbHelper.deleteData(String.valueOf(lastIndex));
        database = dbHelper.getWritableDatabase();
    }

    /**
     *  포스팅 버튼 클릭시 DB에 결과 데이터를 저장하는 메서드
     */
    @Override
    public void onClick(View view) {
        String comment = Objects.requireNonNull(this.textInput.getText()).toString(); // comment

        // delete last raw data ( ONLY FOR DEVELOPMENT)
        if(comment.equals("delete")){
            deleteData();
            return;
        }

        // 위도 경도가 모두 0.0 이면 제대로 위치를 받아오지 못했다고 판단, DB에 저장하지 않고 종료
        if(latitude == 0.0 && longitude == 0.0){
            Toast.makeText(this.getContext(), "위치 경도가 0.0입니다. 위치 기능이 꺼져 있는지, 위치 권한을 수락했는지 확인해주세요.",Toast.LENGTH_SHORT).show();
            dismiss();
        }


        // 지도상 아예 겹치는 것을 방지하기 위해 임시로 random값 이용
        ContentValues contentValues = new ContentValues();
        contentValues.put("lat", latitude + (Math.random()-0.5)*0.001); // 위도
        contentValues.put("long", longitude + (Math.random()-0.5)*0.001); // 경도
        contentValues.put("filename", fileName); // 파일 이름
        contentValues.put("class", className); // 클래스명
        contentValues.put("comment", comment); // 사용자가 입력한 코멘트
        database.insert("location", null, contentValues ); // location 테이블에 저장

        Toast.makeText(this.getContext(), "DB에 추가 완료!",Toast.LENGTH_SHORT).show();

        dismiss();
    }
}