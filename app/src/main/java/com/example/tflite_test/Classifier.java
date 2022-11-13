package com.example.tflite_test;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Pair;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/*
* NO MORE USAGE(11/13~)
* Use 'ClassifierWithTFLiteSupport' instead
* */
public class Classifier {
    Context context;
    private static final String MODEL_NAME = "MobileNetV2_leaf(front)_1_to_30.tflite";

    public Classifier(Context context){
        this.context = context;
    }

    // ByteBuffer 모델을 받아 초기화하는 메서드
    public void init() throws IOException {
        ByteBuffer model = loadModelFile(MODEL_NAME);
        model.order(ByteOrder.nativeOrder());
        Interpreter interpreter = new Interpreter(model);
    }

    private Pair<Integer, Float> argmax(float[] arr){
        int argmax = 0;
        float max = arr[0];
        for (int i =1; i<arr.length; i++){
            float f = arr[i];
            if(f > max){
                argmax = i;
                max = f;
            }
        }
        return new Pair<>(argmax,max);
    }

    // assets에서 tflite를 읽어와서 ByteBuffer로 변환시켜 리턴해주는 메서드
    private ByteBuffer loadModelFile(String modelName) throws IOException {
        AssetManager assetManager = context.getAssets();
        AssetFileDescriptor assetFileDescriptor = assetManager.openFd(modelName);
        FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = assetFileDescriptor.getStartOffset();
        long declaredLength = assetFileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

}
