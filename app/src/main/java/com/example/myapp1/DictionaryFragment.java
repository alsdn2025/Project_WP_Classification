package com.example.myapp1;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for Dictionary,
 * used an external party's library, See readme.md
 * @author MW
 */
public class DictionaryFragment extends DialogFragment implements View.OnClickListener {
    public static final String TAG = "DictionaryFragment";
    private static final String OUTPUT = "OUTPUT";

    private String output;

    private List<String> list_title;
    private List<String> list_desc;

    public DictionaryFragment(){}

    public static DictionaryFragment newInstance(String output) {
        DictionaryFragment fragment = new DictionaryFragment();
        Bundle args = new Bundle();
        args.putString(OUTPUT, output);
        fragment.setArguments(args);
        return fragment;
    }

    // 프래그먼트 생성시 호출
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            output = getArguments().getString(OUTPUT);
        }

        list_title = new ArrayList<>();
        list_desc = new ArrayList<>();
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

                String item1 = new String(
                        o.getString("title")
                );

                list_title.add(item1);

                String item2 = new String(
                        o.getString("desc")
                );

                list_desc.add(item2);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e(TAG, "Created Dictionary");
    }

    // 프래그먼트 생성 후 호출
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dictionary, container, false);

        Button closeBtn = view.findViewById(R.id.dict_closeBtn);
        closeBtn.setOnClickListener(this);
        TextView dictView = view.findViewById(R.id.dict_textview);

        for (int i = 0; i < list_title.size(); i++)
        {
            if (list_title.get(i).equals(output))
            {
                dictView.setText(list_desc.get(i));
                break;
            }
        }

        return view;
    }

    @Override
    public void onClick(View view) {
        dismiss();
    }
}