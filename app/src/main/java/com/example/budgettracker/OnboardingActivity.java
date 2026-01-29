package com.example.budgettracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout layoutIndicators;
    private Button btnAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        layoutIndicators = findViewById(R.id.layoutIndicators);
        btnAction = findViewById(R.id.btnAction);

        viewPager.setAdapter(new OnboardingAdapter());
        setupIndicators(3);
        setCurrentIndicator(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setCurrentIndicator(position);
                if (position == 2) btnAction.setText("Get Started");
                else btnAction.setText("Next");
            }
        });

        btnAction.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() + 1 < 3) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                completeOnboarding();
            }
        });
    }

    private void setupIndicators(int count) {
        ImageView[] indicators = new ImageView[count];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(8, 0, 8, 0);
        for (int i = 0; i < count; i++) {
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(this, android.R.drawable.presence_invisible));
            indicators[i].setLayoutParams(params);
            layoutIndicators.addView(indicators[i]);
        }
    }

    private void setCurrentIndicator(int index) {
        int count = layoutIndicators.getChildCount();
        for (int i = 0; i < count; i++) {
            ImageView imageView = (ImageView) layoutIndicators.getChildAt(i);
            if (i == index) imageView.setColorFilter(ContextCompat.getColor(this, R.color.purple_500));
            else imageView.setColorFilter(ContextCompat.getColor(this, R.color.dot_inactive));
        }
    }

    private void completeOnboarding() {
        SharedPreferences.Editor editor = getSharedPreferences("TrackitPrefs", MODE_PRIVATE).edit();
        editor.putBoolean("isFirstTime", false);
        editor.apply();
        startActivity(new Intent(OnboardingActivity.this, loginActivity.class));
        finish();
    }
}