package com.example.myapp1.navigation.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp1.DBHelper;
import com.example.myapp1.PostingFragment;
import com.example.myapp1.R;
import com.example.myapp1.navigation.CollectionManager;

import java.util.ArrayList;


/**
 * Adapter pattern for Gridview items ( GridItemDAO )
 * @author MW
 */
public class CollectionImgAdapter extends BaseAdapter {
    private static final String TAG = "CollectionImgAdapter";
    private static final int ITEM_NUMBER = 30;
    private Context context;
    private ArrayList<GridItemDTO> items = new ArrayList<>();

    public ImageView imageView;
    private TextView textView;

    private LayoutInflater layoutInflater;
    private GridView gridView;

    private SQLiteDatabase database;
    private DBHelper dbHelper;

    public CollectionImgAdapter(GridView gridView, Context context, ArrayList<Integer> idList, ArrayList<String> nameList) {
        this.gridView = gridView;
        this.context = context;
        dbHelper = new DBHelper(context, PostingFragment.DB_NAME, null, 1);
        database = dbHelper.getWritableDatabase();
        int drawableId;

        if(idList.size() != ITEM_NUMBER){
            Log.e(TAG, "       idList.size() : " + idList.size() + ", ITEM_NUMBER  : " + ITEM_NUMBER);
            return;
        }

        for(int i=0; i<ITEM_NUMBER; i++){
            Log.e(TAG + "collection 이 만들어짐",  i+": " + idList.get(i) + ", name :" + nameList.get(i));
            if(dbHelper.classNameExists(nameList.get(i))){
                drawableId = findDrawableIdByString(context, "ic_"+ String.format( "%03d" , idList.get(i)),"drawable");
            }else{
                drawableId = R.drawable.ic_hua;
            }

            items.add(new GridItemDTO(i+1, drawableId, nameList.get(i), idList.get(i),dbHelper.classNameExists(nameList.get(i))));
            Log.e(TAG, "SIZE: "+items.size() + " (30 if done right )"); // SIZE 가 30 이어야 정상
        }
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }


    @Override
    public long getItemId(int i) {
        return 0;
    }

    public int getItemClassId(int position) {
        return items.get(position).getClassId();
    }

    // mw: method for get Drawable Object code ( R.drawable.blarblar.. ) by String( resource name )
    public static int findDrawableIdByString(Context context, String resourceName, String type) {
        int result = context.getResources().getIdentifier(resourceName, type, context.getPackageName());
        if(result==0){
            Log.e(TAG, "  findDrawableIdByString is 0");
            Log.e(TAG, "  String(resourceName) : " + resourceName);
            return 0;
        }
        return result;
    }

    // 임시 메서드. 호출 시 해당 아이템이 컬렉트 된 것으로 처리.
//    public void refreshImg(int position) {
//        GridItemDAO item = items.get(position);
//        if(!item.isCollected()){
//            item.setCollected(true);
//            Log.e(TAG, "  refreshImg() : " + "items.get(position).getImage() : " + item.getImage());
//            item.setImage(findDrawableIdByString(context, "ic_"+ String.format( "%03d" , item.getClassId()),"drawable"));
//            Log.e(TAG, "  refreshImg() : " + "setImage : " + "ic_"+ String.format( "%03d" , item.getClassId()));
//        }
//
////        Log.e(TAG, "  refreshImg() : " + "items.get(position).getImage() : " + item.getImage());
//        imageView.setImageResource(item.getImage());
//        CollectionManager.getInstance().setStatementAlreadyExist(item.getClassName()); // 해당 컬렉션의 state 만 업데이트
//
//        gridView.invalidateViews();
//    }

    // mw: refresh selected img, called when user select the grid Items
    public void refreshImg(int position) {
        GridItemDTO item = items.get(position);

        if(CollectionManager.getInstance().getState(item.getClassName()) == CollectionManager.NOT_ACQUIRED){
            // 개발자( 치트 ) 기능, 선택한 아이템을 수집된 것으로 변경
            Toast.makeText(context, "(Developer only)" + item.getClassName() + " Clicked, set state ALREADY_EXIT", Toast.LENGTH_SHORT).show();
            CollectionManager.getInstance().setStatementAlreadyExist(item.getClassName()); // 해당 컬렉션의 state 만 업데이트
            item.setCollected(true);
            item.setImage(findDrawableIdByString(context, "ic_"+ String.format( "%03d" , item.getClassId()),"drawable"));

        }else if(CollectionManager.getInstance().getState(item.getClassName()) == CollectionManager.NEW_ACQUIRED){
            Toast.makeText(context, "" + item.getClassName() + "이(가) 컬렉션에 추가되었습니다!", Toast.LENGTH_SHORT).show();
            CollectionManager.getInstance().setStatementAlreadyExist(item.getClassName()); // 해당 컬렉션의 state 만 업데이트

        }else { // ALREADY_EXIT 라면 Dialog 를 띄운다.
            Toast.makeText(context, "" + item.getClassName() + "은(는) 이미 컬렉션에 존재합니다!", Toast.LENGTH_SHORT).show();

        }

        imageView.setImageResource(item.getImage());
        CollectionManager.getInstance().refreshProgressCount(); // ALREADY_EXIT 의 수를 센다.

        gridView.invalidateViews();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(R.layout.single_element, parent, false);
        imageView=(ImageView)convertView.findViewById(R.id.imageview);

        // item 의 텍스트 뷰(label), 필요시 single_element.xml 의 textview 주석 해제 후 사용
        //textView=(TextView)convertView.findViewById(R.id.textview);
        //textView.setText(String.valueOf(items.get(position).gridIndex) + "|code:" + items.get(position).getClassId());

        if(CollectionManager.getInstance().isNewlyAcquired(items.get(position).getClassName())){
            imageView.setImageResource(R.drawable.ic_star);
        }else
            imageView.setImageResource(items.get(position).getImage());


//        if(dbHelper.classNameExists(items.get(position).getClassName())){
//            Log.e("getView :: " , "DBHelper.exist (" + items.get(position).getClassName() + ") = true");
//        }

        return convertView;
    }
}