package com.example.myapp1;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Fragment to show detailed inference result
 * @author MW
 */
public class DetailedOutputFragment extends DialogFragment implements View.OnClickListener {
    public static final String TAG = "DETAILED_OUTPUT_FRAGMENT";
    private static final String FILE_NAME = "FILE_NAME";
    private static final String PROBABILITIES = "PROBABILITIES";

    private String fileName;
    private String probabilities;

    public DetailedOutputFragment(){}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param fileName name of Image file
     * @param probabilities inference output probability
     * @return A new instance of fragment DetailedOutputFragment.
     */
    public static DetailedOutputFragment newInstance(String fileName, String probabilities) {
        DetailedOutputFragment fragment = new DetailedOutputFragment();
        Bundle args = new Bundle();
        args.putString(FILE_NAME, fileName);
        args.putString(PROBABILITIES, probabilities);
        fragment.setArguments(args);
        return fragment;
    }

    // 프래그먼트 생성시 호출
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fileName = getArguments().getString(FILE_NAME);
            probabilities = getArguments().getString(PROBABILITIES);
        }
    }

    // 프래그먼트 생성 후 호출
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_detailed_output, container, false);

        Button closeBtn = view.findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(this);
        TextView filenameTextView = view.findViewById(R.id.filename_textview);
        TextView textView = view.findViewById(R.id.textView);

        filenameTextView.setText(fileName);
        textView.setText(probabilities);

        return view;
    }

    @Override
    public void onClick(View view) {
        dismiss();
    }
}