package com.example.myapp1.Cam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.example.myapp1.InputDataActivity;
import com.example.myapp1.PlantOrgans;
import com.example.myapp1.R;
import com.example.myapp1.Cam.BitmapUtils;
import com.example.myapp1.Cam.CameraPreview;
import com.example.myapp1.Cam.OverCameraView;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * MW:  Modified logic flow [camera] -> [inference] Right away
 */
public class CameraActivity extends AppCompatActivity {
    // MW : record selected organ for naming the file
    private PlantOrgans selectedOrgan = PlantOrgans.LEAF;

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
                checkAlbumPermission();
            }
        });

        /////////////////////////////////////////////////////////////////////
        // MW: Get organ from InputDataActivity
        Intent intent = getIntent();
        if(intent.hasExtra("organ")){
            switch((PlantOrgans) intent.getSerializableExtra("organ")){
                case LEAF:
                    selectedOrgan = PlantOrgans.LEAF;
                    tvLeaf.setSelected(true);
                    imgSrc.setImageResource(R.drawable.ic_yezi);
                    break;
                case FLOWER:
                    selectedOrgan = PlantOrgans.FLOWER;
                    tvFlower.setSelected(true);
                    imgSrc.setImageResource(R.drawable.ic_hua);
                    break;
                case FRUIT:
                    selectedOrgan = PlantOrgans.FRUIT;
                    tvFruit.setSelected(true);
                    imgSrc.setImageResource(R.drawable.ic_guo);
                    break;
                default:
            }
        }else{
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
                    selectedOrgan = PlantOrgans.FLOWER; // MW : record selected organ for naming the file
                    tvLeaf.setSelected(false);
                    tvFlower.setSelected(true);
                    tvFruit.setSelected(false);
                    imgSrc.setImageResource(R.drawable.ic_hua);
                }
            });
            tvFruit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedOrgan = PlantOrgans.FRUIT; // MW : record selected organ for naming the file
                    tvLeaf.setSelected(false);
                    tvFlower.setSelected(false);
                    tvFruit.setSelected(true);
                    imgSrc.setImageResource(R.drawable.ic_guo);
                }
            });
        }
        //////////////////////////////////////////////////////////////////////////////
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

    // MW: Take a picture and save the picture to the gallery
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

    // MW: Store pictures
    private void savePhoto(){
        String fileName;
        if(Build.BRAND .equals("Xiaomi")){
            Log.e("手机品牌","Xiaomi"); // MW: 휴대폰 브랜드 사진 저장 경로 테스트
            // MW: current program path
            String name = DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA))+".jpg";
            // MW: abs path
            String printTxtPath = Environment.getExternalStorageDirectory() + "/DCIM/Camera/";
            fileName = printTxtPath+name;
        }else{
            Log.e("手机品牌","qita"); // MW: 手机品牌 = '브랜드' 라는 의미인듯
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
            timeStamp = timeStamp.replaceAll(":", "_"); // MW: ':' causes some error, replace to '_'
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

            //////////////////////////////////////////////////////
            // MW : set image uri for InputDataActivity
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putParcelable("imageUri", Uri.fromFile(file));
            intent.putExtra("bundle", bundle);
            setResult(RESULT_OK, intent);
            /////////////////////////////////////////////////////

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
        finish();
    }

    /**
     * MW: check album access permission( + sd card )
     * */
    public void checkAlbumPermission(){
        // SD 카드 읽기 권한을 동적으로 신청
        if (ContextCompat.checkSelfPermission(CameraActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CameraActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            openAlbum();
        }
    }

    private void openAlbum() {
        // start preview
        preview.getCamera().startPreview();
        imageDada = null;
        isTakePhoto = false;

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO); // open gallery
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
            // MW: Determining the mobile phone system version number
            if (Build.VERSION.SDK_INT >= 19) {
                // MW: 4.4 and higher systems process photos using this method
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
            /*In the case of document type Uri, process as document id*/
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1]; // CC: 숫자 형식의 아이디를 해석해 내다
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath = getImagePath(contentUri,null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            //If it is a content-type uri, it is handled in the normal way
            imagePath = getImagePath(uri,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            // If the uri is a file type, you can directly get the image path
            imagePath = uri.getPath();
        }
        //MW: Show pictures according to picture path
        //upload(imagePath);
    }

    private void handleImageBeforeKitKat(Intent data){
        Uri uri = data.getData();
        String imagePath = getImagePath(uri,null);
        //upload(imagePath);
    }

    /**
     * Get absolute file(photo) path through Uri and selection
     */
    @SuppressLint("Range")
    private String getImagePath(Uri uri, String selection){
        String path = null;
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
                // set focus timeout
                mHandler.postDelayed(mRunnable, 3000);
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 동 초점 콜백
     */
    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            isFoucing = false;
            mOverCameraView.setFoucuing(false);
            mOverCameraView.disDrawTouchFocusRect();
            // 포커스 타임아웃 콜백 중지
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