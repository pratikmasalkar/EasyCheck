package com.example.easycheck;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Spalsh_Screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spalsh_screen);
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Intent intent = new Intent(Spalsh_Screen.this, Registration.class);
            startActivity(intent);
            finish();
        },3000);
    }
}