package com.example.budgettracker;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class StatsActivity extends AppCompatActivity {

    private PieChart pieChart;
    private RecyclerView rvCategoryStats;
    private DatabaseHelper db;
    private String username;
    private TextView tvTotalAmount;
    private TextView tvJan, tvFeb, tvMarch, tvApril;
    private TextView tvStatsTotalExpenses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        // 1. Initialize Database and Views
        db = new DatabaseHelper(this);
        pieChart = findViewById(R.id.pieChart);
        rvCategoryStats = findViewById(R.id.rvCategoryStats);
        tvStatsTotalExpenses = findViewById(R.id.tvStatsTotalExpenses);

        // Initialize Month TextViews
        tvJan = findViewById(R.id.tvJan);
        tvFeb = findViewById(R.id.tvFeb);
        tvMarch = findViewById(R.id.tvMarch);// Corrected ID from tvJune
        tvApril = findViewById(R.id.tvApril);

        // 2. Set Click Listeners
        tvJan.setOnClickListener(v -> updateStatsForMonth("Jan", tvJan));
        tvFeb.setOnClickListener(v -> updateStatsForMonth("Feb", tvFeb));
        tvMarch.setOnClickListener(v -> updateStatsForMonth("Jun", tvMarch));
        tvApril.setOnClickListener(v -> updateStatsForMonth("Jun", tvApril));

        // 3. Setup UI Components
        setupDonutChart();
        setupCategoryList(); // Initialize the RecyclerView adapter here

        // 4. Load Initial Data (January by default)
        // This replaces loadReportData() and fixes the "Cannot be applied to" error
        updateStatsForMonth("Jan", tvJan);

        // 5. Toolbar Setup
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Billing Reports");
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Statistics"); // Sets the header title
        }

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            // This closes the current activity and returns to the dashboard
            finish();
            // Adds a smooth slide-out animation
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

// 1. Highlight the Stats icon specifically
        bottomNav.setSelectedItemId(R.id.nav_stats);

// 2. Set the click logic
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                finish(); // Returns to MainActivity
                return true;
            } else if (id == R.id.nav_profile) {
                // Move to Profile
                Intent intent = new Intent(StatsActivity.this, ProfileActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
                finish(); // Close stats so it's not in the back-stack
                return true;
            }
            return true;
        });
    }

    private void setupDonutChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);

        // 1. Remove the background shadow/border
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.parseColor("#FAF9FF")); // Matches the Card background

        // 2. Disable the transparent circle outline
        pieChart.setTransparentCircleRadius(0f);

        // 3. Ensure no border is drawn around the chart
        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setEnabled(false);
    }

    private void loadReportData() {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        // This pulls the Category Name, Total, and Count from the DB
        Cursor cursor = db.getCategoryReport();
        double grandTotal = 0;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0); // Category Name
                float total = cursor.getFloat(1);  // Total Spent
                grandTotal += total;

                pieEntries.add(new PieEntry(total, name));

                // Assign specific colors to match your reference image
                colors.add(getCategoryColor(name));

            } while (cursor.moveToNext());
            cursor.close();
        }

        // Apply Data to Chart
        PieDataSet dataSet = new PieDataSet(pieEntries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        // Update the center total in the donut hole
        pieChart.setCenterText("Total\nKES " + String.format("%.0f", grandTotal));
        pieChart.invalidate(); // Refresh chart

        // Refresh the RecyclerView List
        setupCategoryList();

        // Update the summary bar at the bottom
        tvStatsTotalExpenses.setText("KES " + String.format("%.2f", grandTotal));
    }

    // Inside StatsActivity.java
    private CategoryReportAdapter categoryAdapter;

    private void setupCategoryList() {
        // 1. Find the RecyclerView from your activity_stats.xml
        rvCategoryStats = findViewById(R.id.rvCategoryStats);

        // 2. Set the LayoutManager (Vertical list)
        rvCategoryStats.setLayoutManager(new LinearLayoutManager(this));

        // 3. Fetch the grouping data from the database
        Cursor cursor = db.getCategoryReport();

        // 4. Initialize the adapter with the cursor
        categoryAdapter = new CategoryReportAdapter(cursor);

        // 5. Attach the adapter to the RecyclerView
        rvCategoryStats.setAdapter(categoryAdapter);
    }

    // Helper to assign consistent colors to categories

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back to MainActivity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshCategoryList(String month) {
        // Fetch fresh data for the specific month
        Cursor newCursor = db.getCategoryReportByMonth(month);

        // Update the adapter with the new cursor
        if (categoryAdapter != null) {
            categoryAdapter.swapCursor(newCursor);
        }
    }

    // Added "TextView selectedTv" as a parameter so the method knows what to highlight
    private void updateStatsForMonth(String monthShortName, TextView selectedTv) {

        // --- PART 1: UI VISUAL UPDATES ---

        // Reset all months to "dimmed" state
        tvJan.setTextColor(Color.parseColor("#B3FFFFFF"));
        tvFeb.setTextColor(Color.parseColor("#B3FFFFFF"));
        tvMarch.setTextColor(Color.parseColor("#B3FFFFFF"));

        // Reset styles (removing underline and bold)
        tvJan.setBackgroundResource(0);
        tvFeb.setBackgroundResource(0);
        tvMarch.setBackgroundResource(0);
        tvJan.setTypeface(null, Typeface.NORMAL);
        tvFeb.setTypeface(null, Typeface.NORMAL);
        tvMarch.setTypeface(null, Typeface.NORMAL);

        // Highlight the clicked month
        selectedTv.setTextColor(Color.WHITE);
        selectedTv.setTypeface(null, Typeface.BOLD);
        selectedTv.setBackgroundResource(R.drawable.selected_month_indicator);

        // --- PART 2: DATA & CHART UPDATES ---

        // Fetch data from Database (Only declare 'cursor' ONCE)
        Cursor cursor = db.getCategoryReportByMonth(monthShortName);

        // Refresh the RecyclerView List
        if (categoryAdapter != null) {
            categoryAdapter.swapCursor(cursor);
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        double monthTotal = 0;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                float amount = cursor.getFloat(1);
                monthTotal += amount;
                entries.add(new PieEntry(amount, cursor.getString(0)));
            } while (cursor.moveToNext());
        }

        ArrayList<Integer> chartColors = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                String catName = cursor.getString(0);
                chartColors.add(getCategoryColor(catName)); // Match the list!
            } while (cursor.moveToNext());
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(chartColors); // Set the dynamic list

        // Update the Chart Visuals
        PieDataSet dataset = new PieDataSet(entries, "");
        dataSet.setColors(chartColors); // Apply the dynamic colors
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        // Update Center Text and Bottom Summary
        pieChart.setCenterText(monthShortName + "\nTotal\nKES " + String.format("%.0f", monthTotal));
        tvStatsTotalExpenses.setText("KES " + String.format("%.2f", monthTotal));

        // Refresh visuals
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    // Inside StatsActivity.java
    public static int getCategoryColor(String category) {
        // Trim removes accidental spaces, toLowerCase handles typos
        switch (category.toLowerCase().trim()) {
            case "food": return Color.parseColor("#FFA500");        // Orange
            case "transport": return Color.parseColor("#4285F4");   // Blue
            case "health": return Color.parseColor("#FF4D4D");      // Red
            case "shopping": return Color.parseColor("#9C27B0");    // Purple
            case "beauty": return Color.parseColor("#4CAF50");       // Green
            case "social": return Color.parseColor("#795548");        // Brown
            case "education": return Color.parseColor("#FFEB3B");   // Yellow
            case "entertainment": return Color.parseColor("#E91E63"); // Pink
            default:
                // This creates a random-ish color if the category isn't on the list
                return Color.parseColor("#607D8B"); // Blue-Gray for unknown
        }
    }

}