package com.example.tflite_test;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.util.Pair;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Locale;

/*
* usage : 갤러리에서 사진을 꺼내어 추론
* */
public class InputDataActivity extends AppCompatActivity {
    public static final int GALLERY_IMAGE_REQUEST_CODE = 1;
    private static final String TAG = "InputDataActivity";
    private ClassifierWithTFLiteSupport classifier;
    private ImageView imageView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_data);

        Button selectBtn = findViewById(R.id.selectBtn);
        selectBtn.setOnClickListener(view -> getImageFromGallery());

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);

        classifier = new ClassifierWithTFLiteSupport(this);
        try{
            classifier.init();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void getImageFromGallery(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
        try{
            startActivityForResult(intent, GALLERY_IMAGE_REQUEST_CODE); // deprecated
        }catch (Exception e){
            Log.e(TAG, "Failed to get Image from Gallery", e);
        }
    }

    // uri 객체로부터 파일 이름을 가져와 filename_text 뷰에 setText 하는 메서드
    // https://developer.android.com/training/secure-file-sharing/retrieve-info?hl=ko#java
    private void displayImageNameFromUri(Uri uri){
        /*
         * Get the file's content URI from the incoming Intent,
         * then query the server app to get the file's display name
         * and size.
         */
        Cursor returnCursor =
                getContentResolver().query(uri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         * move to the first row in the Cursor, get the data,
         * and display it.
         */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        TextView nameView = (TextView) findViewById(R.id.filename_text);

        nameView.setText("Image file name : " + returnCursor.getString(nameIndex));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK && requestCode == GALLERY_IMAGE_REQUEST_CODE){
            if(data == null)
                return;

            Uri selectedImage = data.getData();
            displayImageNameFromUri(selectedImage);
            Bitmap bitmap = null;

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

            if(bitmap != null){
                Pair<String, Float> output = classifier.classify(bitmap);
                String resultStr = String.format(Locale.ENGLISH,
                        "Inferred class : %s, probability : %.2f%%",
                        output.first, output.second * 100);

                imageView.setImageBitmap(bitmap);
                textView.setText(resultStr);
            }else{
                Log.e(TAG,"Bitmap is null Object");
            }

        }
    }
}