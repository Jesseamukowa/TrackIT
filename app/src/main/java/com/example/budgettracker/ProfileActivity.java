package com.example.budgettracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfileLarge;
    private TextView tvProfileName, tvLTDSavings, tvTopCategory, tvJoinedDate;
    private DatabaseHelper db;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = new DatabaseHelper(this);

        // Initialize Views
        ivProfileLarge = findViewById(R.id.ivProfileLarge);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvLTDSavings = findViewById(R.id.tvLTDSavings);
        tvTopCategory = findViewById(R.id.tvTopCategory);
        tvJoinedDate = findViewById(R.id.tvJoinedDate);

        LinearLayout btnLogout = findViewById(R.id.btnLogout);
        LinearLayout btnReset = findViewById(R.id.btnResetPass);
        LinearLayout btnEditProfile = findViewById(R.id.btnEditProfile);
        LinearLayout btnExport = findViewById(R.id.btnExportData);

        username = getIntent().getStringExtra("USERNAME");
        if (username == null) username = "User";

        loadProfileData();

        // --- NAVIGATION LOGIC ---
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                finish();
                return true;
            } else if (id == R.id.nav_stats) {
                startActivity(new Intent(this, StatsActivity.class));
                finish(); // Close profile so we don't stack activities
                return true;
            }
            return true;
        });

        // --- FEATURE BUTTONS ---
        btnEditProfile.setOnClickListener(v ->
                Toast.makeText(this, "Opening Profile Editor...", Toast.LENGTH_SHORT).show());

        btnExport.setOnClickListener(v ->
                Toast.makeText(this, "Exporting CSV to Downloads...", Toast.LENGTH_SHORT).show());

        btnReset.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, loginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @SuppressLint("Range")
    private void loadProfileData() {
        // 1. Load Personal Identity
        Cursor cursor = db.getUserData(username);
        if (cursor != null && cursor.moveToFirst()) {
            tvProfileName.setText(cursor.getString(cursor.getColumnIndex("username")));
            String imageStr = cursor.getString(cursor.getColumnIndex("profile_image"));
            if (imageStr != null && !imageStr.isEmpty()) {
                ivProfileLarge.setImageURI(Uri.parse(imageStr));
            }
            cursor.close();
        }

        // 2. NEW: Load Life-to-Date Financial Summaries
        double savings = db.getLTDSavings(); // Ensure this method exists in DatabaseHelper
        String topCat = db.getTopSpendingCategory(); // Ensure this method exists in DatabaseHelper

        tvLTDSavings.setText("KES " + String.format("%.0f", savings));
        tvTopCategory.setText(topCat != null ? topCat : "N/A");

        // 3. Optional: Set a dynamic joined date (Hardcoded for now as Jan 2026)
        tvJoinedDate.setText("Member since January 2026");
    }
}