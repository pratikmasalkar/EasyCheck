package com.example.easycheck;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Spalsh_Screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spalsh_screen);
        Handler handler = new Handler();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            startActivity(new Intent(Spalsh_Screen.this, Dashboard.class));
            finish();
        }else {
            handler.postDelayed(() -> {
                Intent intent = new Intent(Spalsh_Screen.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }, 3000);
        }
    }
}