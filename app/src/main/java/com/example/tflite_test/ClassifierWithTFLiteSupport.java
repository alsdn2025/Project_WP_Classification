package com.example.tflite_test;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ClassifierWithTFLiteSupport {
    private final static String TAG = "ClassifierWithTFLiteSupport";
    private final String MODEL_NAME;
    private final String LABEL_FILE;

    int modelInputWidth, modelInputHeight, modelInputChannel;
    Context context;
    TensorImage inputImage; // 이미지 관련 데이터 저장용 인스턴스
    TensorBuffer outputBuffer; // 출력 결과를 담을 버퍼
    private List<String> labels; // 라벨 정보를 담을 리스트, assets의 txt 파일에서 read

    Model model; // tflite 로드부터 추론까지 모두 수행 가능한 model 개체

    // context 만 전달시 테스트라고 간주, 1_to_30 모델&라벨 파일 설정
    public ClassifierWithTFLiteSupport(Context context){
        this.context = context;
        MODEL_NAME = "MobileNetV2_leaf(front)_1_to_30.tflite";
        LABEL_FILE = "labels_leaf_1_to_30.txt";
    }

    // 객체 생성시 전달받은 식물 부위(기관)에 따라 사용할 모델 및 라벨 파일 설정
    public ClassifierWithTFLiteSupport(Context context, PlantOrgans organ){
        this.context = context;
        switch (organ){
            case FLOWER:
                MODEL_NAME = "Flowers_ResNet50.tflite";
                LABEL_FILE = "labels_Flower.txt";
                break;
            case FRUIT:
                MODEL_NAME = "Fruits_ResNet50.tflite";
                LABEL_FILE = "labels_Fruit.txt";
                break;
            case LEAF:
                MODEL_NAME = "Leaf(front)_ResNet50.tflite";
                LABEL_FILE = "labels_leaf.txt";
                break;
            default:
                Log.e(TAG, "생성자 파라미터가 적합하지 않음, default 값으로 세팅");
                MODEL_NAME = "MobileNetV2_leaf(front)_1_to_30.tflite";
                LABEL_FILE = "labels_leaf_1_to_30.txt";
        }
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


    // classify()에서 호출
    // 모델로부터 받은 Output 맵의 value 를 정규화하고, value 를 기준으로 정렬된 Entry List 로 만들어 리턴
    private List<Entry<String, Float>> getSortedEntryList(Map<String, Float> map){
        float sumVal = 0;

        List<Entry<String, Float>> list_entries = new ArrayList<>(map.entrySet());
        Collections.sort(list_entries, (obj1, obj2) -> {
            // 내림 차순 정렬
            return obj2.getValue().compareTo(obj1.getValue());
        });

        for (Entry<String, Float> entry: list_entries){
            // logging, 잘 보이게 Error 로 표시
            String str = "String:" + entry.getKey() + " Val: " + entry.getValue();
            Log.e(TAG, str);

            sumVal += entry.getValue();
        }

        for (Entry<String, Float> entry: list_entries){
            entry.setValue(entry.getValue()/sumVal);
        }

        return list_entries;
    }

    // 추론 메서드.
    public List<Entry<String, Float>> classify(Bitmap image){
        inputImage = loadImage(image);

        // outputBuffer 는 rewind 해서 전달
        Object[] inputs = new Object[]{inputImage.getBuffer()};
        Map<Integer, Object> outputs = new HashMap<>();
        outputs.put(0, outputBuffer.getBuffer().rewind());

        // 추론
        model.run(inputs, outputs);

        // 모델이 추론한 결과와 label 리스트를 매핑
        Map<String, Float> output = new TensorLabel(labels, outputBuffer).getMapWithFloatValue();

        return getSortedEntryList(output);
    }

    public void finish(){
        if(model != null)
            model.close();
    }
}
