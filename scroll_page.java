package com.example.betaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class scroll_page extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scroll_page);

        // Find all the buttons by their IDs
        Button slActionsButton = findViewById(R.id.sl_actions);
        Button slNumbersButton = findViewById(R.id.sl_numbers_button);
        Button slSchoolButton = findViewById(R.id.sl_school_button);
        Button slAnimalsButton = findViewById(R.id.sl_animals_button);
        Button slColorsButton = findViewById(R.id.sl_colors_button);
        Button slBodyPartsButton = findViewById(R.id.sl_body_prts_button);
        Button slFoodButton = findViewById(R.id.sl_food_button);

        // Set click listeners for each button
        slActionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(scroll_page.this, ActionSlActivity.class));
            }
        });

        slNumbersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(scroll_page.this, NumbersSlActivity.class));
            }
        });

        slSchoolButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(scroll_page.this, SchoolSlActivity.class));
            }
        });

        slAnimalsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(scroll_page.this, AnimalsSlActivity.class));
            }
        });

        slColorsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(scroll_page.this, ColorsSlActivity.class));
            }
        });

        slBodyPartsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(scroll_page.this, BodyPartsSlActivity.class));
            }
        });

        slFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(scroll_page.this, FoodSlActivity.class));
            }
        });
    }
}
