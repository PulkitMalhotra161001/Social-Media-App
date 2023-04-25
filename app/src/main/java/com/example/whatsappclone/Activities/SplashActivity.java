package com.example.whatsappclone.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.whatsappclone.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();

        boolean hasLoggedIn = PhoneNumberActivity.PREFS_NAME;

        new Handler().postDelayed(() -> {
            if (hasLoggedIn) {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(SplashActivity.this, PhoneNumberActivity.class);
                startActivity(intent);
                finish();
            }
        }, 500);

    }
}