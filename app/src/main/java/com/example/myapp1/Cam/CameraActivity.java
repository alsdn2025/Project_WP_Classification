package com.example.myapp1.Cam;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapp1.PlantOrgans;
import com.example.myapp1.R;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {
    private PlantOrgans selectedOrgan = PlantOrgans.LEAF; // MW : record selected organ for naming the file

    //초점 보기
    private OverCameraView mOverCameraView;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    //이미지 임시 저장
    private byte[] imageDada;
    //사진 표시
    private boolean isTakePhoto;
    //초점 여부
    private boolean isFoucing;

    public static final int CHOOSE_PHOTO =2;
    private CameraPreview preview;
    private FrameLayout mPreviewLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mPreviewLayout = (FrameLayout) findViewById(R.id.camera_preview_layout);
        ImageView imgBack = (ImageView) findViewById(R.id.imgBack);
        ImageView imgCameraLeft = (ImageView) findViewById(R.id.imgCameraLeft);
        ImageView imgCameraCenter = (ImageView) findViewById(R.id.imgCameraCenter);
        ImageView imgCameraRight = (ImageView) findViewById(R.id.imgCameraRight);
        ImageView imgSrc = (ImageView) findViewById(R.id.imgSrc);
        TextView tvLeaf = (TextView) findViewById(R.id.tvLeaf);
        TextView tvFlower = (TextView) findViewById(R.id.tvFlower);
        TextView tvFruit = (TextView) findViewById(R.id.tvFruit);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        imgCameraLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preview.switchCamera();
            }
        });
        imgCameraCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isTakePhoto) {
                    takePhoto();
                }
            }
        });
        imgCameraRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aa();
            }
        });
        tvLeaf.setSelected(true);
        tvLeaf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedOrgan = PlantOrgans.LEAF; // MW : record selected organ for naming the file
                tvLeaf.setSelected(true);
                tvFlower.setSelected(false);
                tvFruit.setSelected(false);
                imgSrc.setImageResource(R.drawable.ic_yezi);
            }
        });
        tvFlower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedOrgan = PlantOrgans.FLOWER;
                tvLeaf.setSelected(false);
                tvFlower.setSelected(true);
                tvFruit.setSelected(false);
                imgSrc.setImageResource(R.drawable.ic_hua);
            }
        });
        tvFruit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedOrgan = PlantOrgans.FRUIT;
                tvLeaf.setSelected(false);
                tvFlower.setSelected(false);
                tvFruit.setSelected(true);
                imgSrc.setImageResource(R.drawable.ic_guo);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Camera mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        preview = new CameraPreview(this,mCamera);
        mOverCameraView = new OverCameraView(this);
        mPreviewLayout.addView(preview);
        mPreviewLayout.addView(mOverCameraView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseCamera() {
        if (preview.getCamera() != null){
            preview.getCamera().setPreviewCallback(null);
            preview.getCamera().stopPreview();
            preview.getCamera().release();        // release the camera for other applications
            preview.setCameraNull();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCameraAndPreview();
    }

    private void releaseCameraAndPreview() {
        if (preview.getCamera() != null) {
            preview.getCamera().stopPreview();
            preview.getCamera().release();
            preview.setCameraNull();
        }
    }

    //사진첩에 사진을 찍고 그림을 저장
    private void takePhoto(){
        isTakePhoto = true;
        //사진을 찍기 위해 카메라를 호출
        preview.getCamera().takePicture(null,null,null,(data,camera)->{
            imageDada = data;
            Log.e("imageDada  ","imageDada.length "+imageDada.length);
            preview.getCamera().stopPreview();//미리 보기 중지
            //저장 후 커팅
            savePhoto();
        });
    }

    //사진 저장
    private void savePhoto(){
        String fileName;
        if(Build.BRAND .equals("Xiaomi")){
            Log.e("手机品牌","Xiaomi"); //휴대폰 브랜드 사진 저장 경로 테스트
            //현재 프로그램 경로
            String name = DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA))+".jpg";
            //절대 경로
            String printTxtPath = Environment.getExternalStorageDirectory() + "/DCIM/Camera/";
            fileName = printTxtPath+name;
        }else{
            Log.e("手机品牌","qita"); // MW: 手机品牌 = '브랜드' 라는 의미인듯
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
            timeStamp = timeStamp.replaceAll(":", "."); // MW: ':' causes some error, replace to '.'
            String bitName = selectedOrganToString() + "_" + timeStamp + ".jpg"; // MW : Add organ info into image file name

            // MW: if an error occur, 'try File.pathSeparator' instead of '/'
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ "/DCIM/WP_Classification/");
            if(!dir.exists()){
                dir.mkdir(); // MW: Create directory if it does not exist
            }

            fileName = dir.getAbsolutePath() + '/' + bitName ;
            Log.e("CameraActivity", fileName);
        }
        File file = new File(fileName);
        if(file.exists()){
            file.delete();
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(imageDada,0, imageDada.length);
        }catch (Exception e){
            e.printStackTrace();
            Log.e("Exception  ","e "+e);
        }finally {
            Log.e("Exception  ","e -- ");
            if (fos != null){
                Log.e("Exception  ","e ++ ");
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + fileName)));
    }

    /**
     * 라이브러리
     * */
    public void aa(){
        //SD 카드 읽기 권한을 동적으로 신청
        if (ContextCompat.checkSelfPermission(CameraActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CameraActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            openAlbum();
        }
    }

    private void openAlbum() {
        //미리 보기 시작
        preview.getCamera().startPreview();
        imageDada = null;
        isTakePhoto = false;

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO); //사진첩 열기
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //휴대폰 시스템 버전 번호 판단
            if (Build.VERSION.SDK_INT >= 19) {
                //4.4 이상의 시스템은 이 방법을 사용하여 사진을 처리
                handleImageOnKitKat(data);
            } else {
                handleImageBeforeKitKat(data);
            }
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data){
        String imagePath = null;
        Uri uri = data.getData();
        if(DocumentsContract.isDocumentUri(this,uri)){
            /*document 타입의 Uri인 경우 document id로 처리*/
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1]; //숫자 형식의 아이디를 해석해 내다
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath = getImagePath(contentUri,null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            //content 타입의 uri라면 일반 방식으로 처리
            imagePath = getImagePath(uri,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            //file 타입의 uri라면, 직접 이미지 경로를 얻으면 되다
            imagePath = uri.getPath();
        }
        //그림 경로에 따라 그림 보이기
        //upload(imagePath);
    }

    private void handleImageBeforeKitKat(Intent data){
        Uri uri = data.getData();
        String imagePath = getImagePath(uri,null);
        //upload(imagePath);
    }

    @SuppressLint("Range")
    private String getImagePath(Uri uri, String selection){
        String path = null;
        //Uri와 selection을 통해 실제 사진 경로 가져오기
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if(cursor != null){
            if (cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!isFoucing) {
                float x = event.getX();
                float y = event.getY();
                isFoucing = true;
                if (preview.getCamera() != null && !isTakePhoto) {
                    mOverCameraView.setTouchFoucusRect(preview.getCamera(), autoFocusCallback, x, y);
                }
                mRunnable = () -> {
                    Toast.makeText(CameraActivity.this, "오토포커스 타임아웃이니, 적절한 위치를 조정하여 촬영해 주십시오.！", Toast.LENGTH_SHORT);
                    isFoucing = false;
                    mOverCameraView.setFoucuing(false);
                    mOverCameraView.disDrawTouchFocusRect();
                };
                //초점 시간 초과 설정
                mHandler.postDelayed(mRunnable, 3000);
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 설명: 자동 초점 콜백
     */
    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            isFoucing = false;
            mOverCameraView.setFoucuing(false);
            mOverCameraView.disDrawTouchFocusRect();
            //포커스 타임아웃 콜백 중지
            mHandler.removeCallbacks(mRunnable);
        }
    };

    @NonNull
    @Contract(pure = true)
    private String selectedOrganToString(){
        switch (selectedOrgan){
            case FLOWER:
                return "Flower";
            case FRUIT:
                return "Fruit";
            case LEAF:
                return "Leaf";
            default:
                return "null";
        }
    }
}