package com.example.myapp1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class detailedOutputActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_output);

        TextView textView = findViewById(R.id.textView);
        TextView filenameTextView = findViewById(R.id.filename_textview);

        textView.setText(getIntent().getStringExtra("output"));
        filenameTextView.setText(getIntent().getStringExtra("fileName"));
    }
}