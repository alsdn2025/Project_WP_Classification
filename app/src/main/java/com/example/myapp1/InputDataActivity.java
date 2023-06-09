package com.example.myapp1;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp1.Cam.CameraActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * usage : 갤러리에서 사진을 꺼내어 추론 요청, 결과 표시
 * @author MW
 * */
public class InputDataActivity extends FragmentActivity {
    public static final int GALLERY_IMAGE_REQUEST_CODE = 1;
    public static final int CAMERA_IMAGE_REQUEST_CODE = 2;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 3;
    private static final String TAG = "InputDataActivity";
    private ImageView imageView;
    private TextView output_textView;
    private ImageView imageView_endangered;
    private PlantOrgans organ;
    private ClassifierWithTFLiteSupport classifier;
    private List<Map.Entry<String, Float>> outputList;
    private String fileName = "no file";
    private String filePath;
    private List<String> list_title;
    private List<String> list_endangered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_data);

        Button selectBtn = findViewById(R.id.selectBtn);
        selectBtn.setOnClickListener(view -> getImageFromGallery());

        Button cameraBtn = findViewById(R.id.cameraBtn);
        cameraBtn.setOnClickListener(view -> getImageFromCamera());

        Button dictBtn = findViewById(R.id.dictBtn);
        dictBtn.setOnClickListener(view -> getDictionaryDialog());

        Button detailBtn = findViewById(R.id.detailBtn);
        detailBtn.setOnClickListener(view -> getDetailedOutputDialog());

        Button postingBtn = findViewById(R.id.postBtn);
        postingBtn.setOnClickListener(view -> getPostDialog());

        imageView = findViewById(R.id.imageView);
        output_textView = findViewById(R.id.output_textView);
        TextView organ_textView = findViewById(R.id.organ_textView);
        imageView_endangered = findViewById(R.id.imageView2);

        Intent intent = getIntent();
        // Get organ from SearchFragment( MainActivity )
        organ = (PlantOrgans) intent.getSerializableExtra("organ");
        if(organ == null){
            Log.e(TAG, "Organ is empty, Create classifier with default constructor");
        }
        setOrganTextView(organ_textView);


        classifier = new ClassifierWithTFLiteSupport(this, organ);
        try{
            classifier.init();
        }catch (IOException e){
            Log.e(TAG,"Classifier initiating error");
            e.printStackTrace();
        }

        // 멸종위기종일 경우 체크 표시
        list_title = new ArrayList<>();
        list_endangered = new ArrayList<>();
        String json = null;

        try {
            // json파일 접근
            InputStream is = getResources().getAssets().open("dictionary.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

            // json파일 안의 객체와 배열에 접근
            JSONObject jsonObject = new JSONObject(json);
            JSONArray array = jsonObject.getJSONArray("plant");

            for(int i = 0; i < array.length(); i++) {

                JSONObject o = array.getJSONObject(i);

                String item = new String(
                        o.getString("endangered")
                );

                list_endangered.add(item);

                String item2 = new String(
                        o.getString("title")
                );

                list_title.add(item2);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @SuppressLint("SetTextI18n")
    private void setOrganTextView(TextView textView){
        switch (this.organ){
            case FLOWER:
                textView.setText("Flower");
                break;
            case FRUIT:
                textView.setText("Fruit");
                break;
            case LEAF:
                textView.setText("Leaf");
                break;
            default:
                textView.setText("Default = Leaf(1_to_30)");
        }
    }

    private void getImageFromGallery(){
        // Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
        // 경로 가져올시 Recent Images 에서 오류가 발생하여 아래처럼 MediaStore 를 이용하는 형태로 변경
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try{
            startActivityForResult(intent, GALLERY_IMAGE_REQUEST_CODE); // deprecated
        }catch (Exception e){
            Log.e(TAG, "Failed to get Image from Gallery", e);
        }
    }


    private void getImageFromCamera(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            Log.e(TAG, "need to get Permissions");
            String[] permissions = new String[1];
            permissions[0] = android.Manifest.permission.CAMERA;
            ActivityCompat.requestPermissions(this, permissions, CAMERA_PERMISSION_REQUEST_CODE );
            Toast.makeText(this, "카메라 권한 승인 후 다시 눌러주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("organ", organ);
        try{
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST_CODE);
        }catch (Exception e){
            Log.e(TAG, "Failed to get Image from Camera", e);
        }
    }

    // 사전 기능 추가
    private void getDictionaryDialog(){
        if(outputList == null){
            Toast.makeText(this, "아직 추론을 하지 않았어요!", Toast.LENGTH_SHORT).show();
            return;
        }

        String result = outputList.get(0).getKey();
        FragmentManager fragmentManager = getSupportFragmentManager();
        DictionaryFragment dictionaryFragment = DictionaryFragment.newInstance(result);
        dictionaryFragment.show(fragmentManager, PostingFragment.TAG);
    }

    // 자세한 확률을 표시하는 Dialog 생성
    // 추론 결과로 받은 List<Entry>를 하나의 String 으로 변환하여 Dialog 에 전달
    private void getDetailedOutputDialog(){
        if(outputList == null){
            Toast.makeText(this, "아직 추론을 하지 않았어요!", Toast.LENGTH_SHORT).show();
            return;
        }

        try{
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Float> entry: outputList){
                sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append(System.lineSeparator());
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            DetailedOutputFragment detailedOutputFragment =  DetailedOutputFragment.newInstance(fileName, sb.toString());
            detailedOutputFragment.show(fragmentManager, DetailedOutputFragment.TAG);
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
    }

    // DB 및 메인 포스트에 추가하는 포스팅 다이얼로그 생성
    // 추론 후에만 생성 가능
    private void getPostDialog(){
        if(outputList == null){
            Toast.makeText(this, "아직 추론을 하지 않았어요!", Toast.LENGTH_SHORT).show();
            return;
        }
        String result = outputList.get(0).getKey();
        FragmentManager fragmentManager = getSupportFragmentManager();
        PostingFragment postingFragment = PostingFragment.newInstance(this.filePath, result, this.organ);
        postingFragment.show(fragmentManager, PostingFragment.TAG);
    }

    // uri 객체로부터 파일 이름을 가져와 filename_text 뷰에 setText 하는 메서드
    // https://developer.android.com/training/secure-file-sharing/retrieve-info?hl=ko#java
//    @SuppressLint("SetTextI18n")
//    private void displayImageNameFromUri(Uri uri){
//        /*
//         * Get the file's content URI from the incoming Intent,
//         * then query the server app to get the file's display name
//         * and size.
//         */
//        Cursor returnCursor =
//                getContentResolver().query(uri, null, null, null, null);
//        /*
//         * Get the column indexes of the data in the Cursor,
//         * move to the first row in the Cursor, get the data,
//         * and display it.
//         */
//        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
//        returnCursor.moveToFirst();
//        TextView filename_textView = findViewById(R.id.filename_textview);
//        this.fileName = returnCursor.getString(nameIndex);
//
//        filename_textView.setText("File name : " + fileName);
//        returnCursor.close();
//    }

    // Uri 객체로부터 파일 경로와 파일 이름을 가져와 필드에 저장
    @SuppressLint("SetTextI18n")
    private void displayImageNameFromUri(Uri uri){
        TextView filename_textView = findViewById(R.id.filename_textview);
        try{
            filePath = UriPathProvider.getRealPathFromURI(this, uri);
            fileName = filePath.substring(filePath.lastIndexOf("/")+1);
            filename_textView.setText("File name : " + fileName);
        }catch (NullPointerException e){
            Log.e(TAG + "   displayImageNameFromUri() : ", e.getMessage());
        }
    }

    // Gallery 에서 이미지 선택 후 or Camera 로 이미지 촬영 후 호출
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        // 갤러리
        if(resultCode == Activity.RESULT_OK && requestCode == GALLERY_IMAGE_REQUEST_CODE){
            if(data == null) return;

            Uri selectedImage = data.getData();
            displayImageNameFromUri(selectedImage);
            requestClassifying(selectedImage);
            printEndangered();

            // 카메라
        }else if(resultCode == Activity.RESULT_OK && requestCode == CAMERA_IMAGE_REQUEST_CODE){
            if(data == null) return;

            Uri selectedImage;
            if((selectedImage = data.getBundleExtra("bundle").getParcelable("imageUri")) != null){
                displayImageNameFromUri(selectedImage);
                try{
                    requestClassifying(selectedImage);
                }catch (NullPointerException e){
                    Log.e(TAG + " requestClassifying() : ", "NULL POINTER EXCEPTION");
                }
            }
        }
    }

    // Classifier 에게 분류(추론) 요청
    private void requestClassifying(Uri selectedImage){
        Bitmap bitmap = null;

        // Image Uri -> Bitmap Object
        try{
            if(Build.VERSION.SDK_INT >= 29){
                ImageDecoder.Source src =
                        ImageDecoder.createSource(getContentResolver(), selectedImage);
                bitmap = ImageDecoder.decodeBitmap(src);
            } else{
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
            }
        }catch (IOException e ){
            Log.e(TAG, "Failed to read Image", e);
        }

        // Inference
        if(bitmap != null){
            outputList = classifier.classify(bitmap);
            String resultStr = String.format(Locale.ENGLISH,
                    "Inferred class : %s " + System.lineSeparator() + "Probability : %.2f%%",
                    outputList.get(0).getKey(), outputList.get(0).getValue() * 100);

            imageView.setImageBitmap(bitmap);
            output_textView.setText(resultStr);

        }else{
            Log.e(TAG,"Bitmap is null Object");
        }
    }

    // 멸종위기종 표시
    private void printEndangered(){

        for (int i = 0; i < list_title.size(); i++)
        {
            if (list_title.get(i).equals(outputList.get(0).getKey()))
            {
                if (list_endangered.get(i).equals("Y"))
                {
                    imageView_endangered.setVisibility(View.VISIBLE);
                }

                break;
            }
        }
    }
}