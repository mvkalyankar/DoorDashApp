package com.example.doordashapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class FoodDonationActivity extends AppCompatActivity {
    private ImageButton backBtn;
    private TextView phone1Tv,phone2Tv,phone3Tv,phone4Tv,phone5Tv,phone6Tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_donation);
        backBtn = findViewById(R.id.backBtn);
        phone1Tv = findViewById(R.id.phone1Tv);
        phone2Tv = findViewById(R.id.phone2Tv);
        phone3Tv = findViewById(R.id.phone3Tv);
        phone4Tv = findViewById(R.id.phone4Tv);
        phone5Tv = findViewById(R.id.phone5Tv);
        phone6Tv = findViewById(R.id.phone6Tv);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        phone1Tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String a = phone1Tv.getText().toString();
                dialPhone(a);
            }
        });
        phone2Tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String a = phone2Tv.getText().toString();
                dialPhone(a);
            }
        });
        phone3Tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String a = phone3Tv.getText().toString();
                dialPhone(a);
            }
        });
        phone4Tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String a = phone4Tv.getText().toString();
                dialPhone(a);
            }
        });
        phone5Tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String a = phone5Tv.getText().toString();
                dialPhone(a);
            }
        });
        phone6Tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String a = phone6Tv.getText().toString();
                dialPhone(a);
            }
        });



    }

    private void dialPhone(String shopPhone ) {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(shopPhone))));
        Toast.makeText(this, "" + shopPhone, Toast.LENGTH_SHORT).show();
    }
}