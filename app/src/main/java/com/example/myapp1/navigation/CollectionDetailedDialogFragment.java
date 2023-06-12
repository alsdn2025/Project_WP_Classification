package com.example.myapp1.navigation;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.myapp1.R;
import com.example.myapp1.navigation.model.CollectionImgAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


/**
 * Description dialog for the time user selects the collection already exist
 * @author MW
 */
public class CollectionDetailedDialogFragment extends DialogFragment implements View.OnClickListener {
    public static final String TAG = "COLLECTION_DETAILED_DIALOG_FRAGMENT";
    private static final String CLASS_NAME = "CLASS_NAME";
    private static final String CLASS_ID = "CLASS_ID";
    private static final String FILE_PATH = "FILE_PATH";

    private String className;
    private String classId;
    private String imgDrawable;
    private String filePath;

    public CollectionDetailedDialogFragment(){}

    // mw: factory method
    public static CollectionDetailedDialogFragment newInstance(String fileName, int classId, String filePath) {
        CollectionDetailedDialogFragment fragment = new CollectionDetailedDialogFragment();
        Bundle args = new Bundle();
        String id = String.format("%03d", classId);

        args.putString(CLASS_NAME, fileName);
        args.putString(CLASS_ID, id);
        args.putString(FILE_PATH, filePath);
        fragment.setArguments(args);
        return fragment;
    }

    // mw: called when this fragment created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            className = getArguments().getString(CLASS_NAME);
            classId = getArguments().getString(CLASS_ID);
            filePath = getArguments().getString(FILE_PATH);
            imgDrawable = "ic_" + classId;
        }
    }

    // mw: called after this fragment created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_collection_detailed_dialog, container, false);

        Button closeBtn = view.findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(this);
        ImageView imageView = view.findViewById(R.id.imageView);
        ImageView myImageView = view.findViewById(R.id.myImageView);

        TextView classNameTextView = view.findViewById(R.id.nameTextView);
        TextView descView = view.findViewById(R.id.descTextView);

        classNameTextView.setText(className);
        imageView.setImageResource(CollectionImgAdapter.findDrawableIdByString(getContext(),imgDrawable,"drawable"));
        descView.setText(String.format(findDesc(classId)));

        // mw: load img from device storage
        // mw: if there is no file, than load 'ic_hua.png' stored in res>drawable
        File file = new File(filePath);
        if(file.exists()){
            Glide.with(myImageView.getContext())
                    .load(filePath)
                    .into(myImageView);

        }else {
            myImageView.setImageResource(R.drawable.ic_hua);
        }

        imageView.invalidate();

        return view;
    }

    // mw: find 'classId's description from json file
    public String findDesc(String classId){
        int intId = Integer.parseInt(classId);
        // refer to TJ code(DictionaryFragment)
        try{
            InputStream is = getResources().getAssets().open("dictionary.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String buf = new String(buffer, "UTF-8");

            JSONObject jsonObject = new JSONObject(buf);
            JSONArray jsonArray = jsonObject.getJSONArray("plant");

            for(int i = 0; i < jsonArray.length(); i++) {
                String title = jsonArray.getJSONObject(i).getString("title");
                if(intId == Integer.parseInt(title.split("_")[1]) ) {
                    return jsonArray.getJSONObject(i).getString("desc");
                }
            }
        }catch (IOException | JSONException e) {
            Log.e(TAG, "    onCreate() ");
            e.printStackTrace();
        }
        return "no_description";
    }

    @Override
    public void onClick(View view) {
        dismiss();
    }
}