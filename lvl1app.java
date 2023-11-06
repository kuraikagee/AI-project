package com.example.betaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class lvl1app extends AppCompatActivity {

    private Button alphadetbtn;
    private Button wordsbtn;
    private Button aboutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lvl1app);

        alphadetbtn = findViewById(R.id.alphadetbtn);
        wordsbtn = findViewById(R.id.wordsbtn);
        aboutBtn = findViewById(R.id.ideabtn);

        alphadetbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the alpha detection button click
                openAlphaDetectionPage();
            }
        });

        wordsbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the words detection button click
                openWordsDetectionPage();
            }
        });

        aboutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the about button click
                openAboutPage();
            }
        });
    }

    private void openAlphaDetectionPage() {
        // Start the activity for the alpha detection page
        Intent intent = new Intent(getApplicationContext(), scroll_page.class);
        startActivity(intent);
    }

    private void openWordsDetectionPage() {
        // Start the activity for the words detection page
        Intent intent = new Intent(getApplicationContext(), Activity_camera.class);
        startActivity(intent);
    }

    private void openAboutPage() {
        // Start the activity for the about page
        Intent intent = new Intent(getApplicationContext(), about.class);
        startActivity(intent);
    }
}

