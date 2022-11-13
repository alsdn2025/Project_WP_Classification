package com.example.tflite_test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button FlowerBtn = findViewById(R.id.FlowerBtn);
        Button FruitBtn = findViewById(R.id.FruitBtn);
        Button LeafBtn = findViewById(R.id.LeafBtn);

        FlowerBtn.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this, InputDataActivity.class);
            i.putExtra("organ", PlantOrgans.FLOWER);
            startActivity(i);
        });
        FruitBtn.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this, InputDataActivity.class);
            i.putExtra("organ", PlantOrgans.FRUIT);
            startActivity(i);
        });
        LeafBtn.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this, InputDataActivity.class);
            i.putExtra("organ", PlantOrgans.LEAF);
            startActivity(i);
        });
    }
}