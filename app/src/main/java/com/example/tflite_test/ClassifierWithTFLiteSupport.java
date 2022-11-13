package com.example.tflite_test;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;

import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassifierWithTFLiteSupport {
    private final static String TAG = "ClassifierWithTFLiteSupport";
    private final String MODEL_NAME;
    private final String LABEL_FILE;

    int modelInputWidth, modelInputHeight, modelInputChannel;
//    int modelOutputClasses;
    Context context;
    TensorImage inputImage; // 이미지 관련 데이터 저장용 인스턴스
    TensorBuffer outputBuffer; // 출력 결과를 담을 버퍼
    private List<String> labels; // 라벨 정보를 담을 리스트, assets의 txt 파일에서 read

    Model model; // tflite 로드부터 추론까지 모두 수행 가능한 model 개체

    public ClassifierWithTFLiteSupport(Context context){
        MODEL_NAME = "MobileNetV2_leaf(front)_1_to_30.tflite";
        LABEL_FILE = "labels_leaf_1_to_30.txt";
        this.context = context;
    }

    public ClassifierWithTFLiteSupport(Context context, String modelName, String labelFile){
        this.context = context;
        MODEL_NAME = modelName;
        LABEL_FILE = labelFile;
    }

    // modelInputShape 멤버변수를 초기화하는 메서드, init()에서 호출됨
    public void initModelShape(){
        Tensor inputTensor = model.getInputTensor(0);
        Tensor outputTensor = model.getOutputTensor(0);
        int[] inputShape = inputTensor.shape();
        modelInputChannel = inputShape[0];
        modelInputWidth = inputShape[1];
        modelInputHeight = inputShape[2];

        inputImage = new TensorImage(inputTensor.dataType()); // Float32 타입으로 TensorImage 개체 생성
        // 모델의 출력 텐서의 shape, data type 에 맞게 고정된 사이즈의 output 버퍼 개체 생성
        outputBuffer = TensorBuffer.createFixedSize(outputTensor.shape(), outputTensor.dataType());
    }

    // 생성자와 별도로 model 등 필드를 초기화하는 메서드
    public void init() throws IOException {
        model = Model.createModel(context, MODEL_NAME);
        Log.e(TAG, "model created");
//        interpreter = new Interpreter(model);
//        ByteBuffer model = FileUtil.loadMappedFile(context, MODEL_NAME);
//        model.order(ByteOrder.nativeOrder());

        initModelShape();
        labels = FileUtil.loadLabels(context, LABEL_FILE); // label 텍스트 파일 읽고 저장
    }

    // Bitmap 이미지를 입력받고, 이를 전처리하고 TensorImage 개체로 리턴하는 메서드, classify() 에서 호출됨
    private TensorImage loadImage(final Bitmap bitmap){
        // Tensor Image 는 ARGB_8888 포맷의 Bitmap 만을 받음, 따라서 확인 및 변환 필요
        if(bitmap.getConfig() != Bitmap.Config.ARGB_8888){
            inputImage.load(bitmap.copy(Bitmap.Config.ARGB_8888, true));
        }else {
            inputImage.load(bitmap);
        }

        // Image 전처리 과정, resizing, normalizing 등이 필요하면 여기에 작성
        // Resizing은 최근접 보간법(Nearest neighbor interpolation) 사용
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(
                        modelInputHeight,
                        modelInputWidth,
                        ResizeOp.ResizeMethod.NEAREST_NEIGHBOR)
                ).build();

        return imageProcessor.process(inputImage);
    }

    // 추론 결과인 Map 을 받아, 가장 높은 확률을 가진 원소를 <Sting, Float> 형태로 리턴시켜주는 메서드
    // classify()에서 호출
    private Pair<String, Float> argmax(Map<String, Float> map){
        String maxKey = "";
        float maxVal = -1;
        float sumVal = 0;

        for (Map.Entry<String, Float> entry: map.entrySet()){
            float f = entry.getValue();
            String str = "String:" + entry.getKey() + " Val: " + entry.getValue();
            Log.e(TAG, str);
            sumVal += f;
            if(f > maxVal){
                maxKey = entry.getKey();
                maxVal = f;
            }
        }
        Log.e(TAG,"sumVal : " + sumVal);
        return new Pair<>(maxKey, maxVal/sumVal);
    }

    // 추론 메서드.
    public Pair<String, Float> classify(Bitmap image){
        inputImage = loadImage(image);

        // outputBuffer 는 rewind 해서 전달
        Object[] inputs = new Object[]{inputImage.getBuffer()};
        Map<Integer, Object> outputs = new HashMap<>();
        outputs.put(0, outputBuffer.getBuffer().rewind());

        // 추론
        model.run(inputs, outputs);

        // 모델이 추론한 결과와 label 리스트를 매핑
        Map<String, Float> output = new TensorLabel(labels, outputBuffer).getMapWithFloatValue();

        return argmax(output);
    }

    public void finish(){
        if(model != null)
            model.close();
    }
}
