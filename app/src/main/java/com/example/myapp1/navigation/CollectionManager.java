package com.example.myapp1.navigation;

import android.content.Context;
import android.util.Log;

import com.example.myapp1.DBHelper;
import com.example.myapp1.PostingFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 신규 컬렉션 추가를 감지하고 관리하기 위한 싱글턴 클래스
 * delete 기능은 구현하지 않는다고 전제
 * singleton class for new collection detect
 * Assuming that delete function is not implemented
 * @author MW
 */
public class CollectionManager {
    private static CollectionManager instance;
    private static String TAG = "CollectionManager";
    private static String JSON_FILE_NAME = "dictionary.json";
    // statement
    public static int NEW_ACQUIRED = 1;
    public static int ALREADY_EXIST= 0;
    public static int NOT_ACQUIRED = -1;

    // <className, statement>
    private HashMap<String, Integer> collections = new HashMap<>(30);
    private DBHelper dbHelper;
    private int progressCount = 0;

    public int getProgressCount() {
        Log.e(TAG, "컬렉션 수집 : " + progressCount);
        return progressCount;
    }

    public void refreshProgressCount(){
        progressCount = 0;
        for (int state: collections.values()) {
            if(state == ALREADY_EXIST){
                progressCount++;
            }
        }
    }

    private CollectionManager() {

    }

    public static CollectionManager getInstance() {
        if (instance == null) {
            synchronized(CollectionManager.class) {
                instance = new CollectionManager();
            }
        }
        return instance;
    }

    // initialize collections
    public void init(Context context){
        dbHelper = new DBHelper(context, PostingFragment.DB_NAME, null, 1);
        progressCount = 0;
        String name;
        int state;

        try{
            InputStream is = context.getResources().getAssets().open(JSON_FILE_NAME);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String buf = new String(buffer, "UTF-8");

            JSONObject jsonObject = new JSONObject(buf);
            JSONArray jsonArray = jsonObject.getJSONArray("plant");

            for(int i = 0; i < jsonArray.length(); i++) {
                String title = jsonArray.getJSONObject(i).getString("title");
                name = title.split("_")[0];
                // init() 호출 시점에 해당 식물이 db 에 저장되어 있다면
                if(dbHelper.classNameExists(name)){
                    collections.put(name, ALREADY_EXIST); // state = ALREADY_EXIST
                    progressCount++;
                }else
                    collections.put(name, NOT_ACQUIRED); // db에 없다면 state = NOT_ACQUIRED
            }
        }catch (IOException | JSONException e) {
            Log.e(TAG, "    init() Exception Occur");
            e.printStackTrace();
        }
    }

    // refresh collections
    public void refresh(Context context){
        try{
            progressCount = 0;
            InputStream is = context.getResources().getAssets().open(JSON_FILE_NAME);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String buf = new String(buffer, "UTF-8");

            JSONObject jsonObject = new JSONObject(buf);
            JSONArray jsonArray = jsonObject.getJSONArray("plant");

            for(int i = 0; i < jsonArray.length(); i++) {
                String title = jsonArray.getJSONObject(i).getString("title");
                String name = title.split("_")[0];

                // refresh() 호출 시점에 db 에는 존재하나 collections 의 statement 는 NOT_ACQUIRED 라면
                if(dbHelper.classNameExists(name) && collections.get(name) == NOT_ACQUIRED){
                    // Hashmap.replace() 함수는 API 24 부터 제공, 우리 앱의 최소 API 는 23, 따라서 put 으로 대체
                    collections.put(name, NEW_ACQUIRED); // state = NEW_ACQUIRED
                    progressCount++;
                }else if(collections.get(name) == ALREADY_EXIST){
                    progressCount++;
                }
            }
        }catch (IOException | JSONException e) {
            Log.e(TAG, "    refresh() ");
            e.printStackTrace();
        }
    }

    public void logAllCollectionsStatement(){
        for (String key:collections.keySet()) {
            Log.e(TAG, key + ": " + collections.get(key));
        }
        StringBuilder sb = new StringBuilder();
    }

    public boolean isNewlyAcquired(String className){
        return collections.get(className) == NEW_ACQUIRED;
    }

    public List<String> getAllNewlyAcquiredClassName(){
        ArrayList<String> resultList = new ArrayList<>();
        for (String name: collections.keySet()) {
            if(collections.get(name) == NEW_ACQUIRED){
                resultList.add(name);
            }
        }
        if(resultList.size() == 0){
            Log.e(TAG, "Newly Acquired Class is 0");
            return null;
        }
        return resultList;
    }

    public int getState(String className){
        if(!collections.containsKey(className)){
            Log.e(TAG, "  getState() :: className 을 잘못 입력했습니다");
            return -2;
        }
        return collections.get(className);
    }

    //
    public void setStatementAlreadyExist(String className){
        if(collections.get(className) != ALREADY_EXIST)
            collections.put(className, ALREADY_EXIST);
    }
}
