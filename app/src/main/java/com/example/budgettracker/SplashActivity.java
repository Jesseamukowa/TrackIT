package com.example.budgettracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            // This logic ensures Jesse sees onboarding ONLY the first time
            SharedPreferences pref = getSharedPreferences("TrackitPrefs", MODE_PRIVATE);
            boolean isFirstTime = pref.getBoolean("isFirstTime", true);

            if (isFirstTime) {
                startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, loginActivity.class));
            }
            finish();
        }, 3000); // Show splash for 6 second
    }
}