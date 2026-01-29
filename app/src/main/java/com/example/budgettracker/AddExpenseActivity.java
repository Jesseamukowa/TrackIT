package com.example.budgettracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private GridView gvCategories;
    private TextView tvSelectedCategory;
    private EditText etAmount;
    private Button btnSave;
    private DatabaseHelper db;
    private String selectedCatName = "";

    // FIX 1: Variable to store the dynamic ID
    private int selectedCategoryId = -1;

    private String[] categoryNames = {
            "Food", "Shopping", "Transport", "Entertainment", "Education", "Health", "Social", "Beauty"
    };

    private int[] categoryIcons = {
            R.drawable.ic_food, R.drawable.ic_shopping, R.drawable.ic_transport,
            R.drawable.ic_entertainment, R.drawable.ic_education, R.drawable.ic_health,
            R.drawable.ic_social, R.drawable.ic_beauty
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        db = new DatabaseHelper(this);
        gvCategories = findViewById(R.id.gvCategories);
        tvSelectedCategory = findViewById(R.id.tvSelectedCategory);
        etAmount = findViewById(R.id.etAmount);
        btnSave = findViewById(R.id.btnSaveExpense);

        CategoryAdapter adapter = new CategoryAdapter(this, categoryNames, categoryIcons);
        gvCategories.setAdapter(adapter);

        // FIX 2: Capture the actual position + 1 to match DB IDs
        gvCategories.setOnItemClickListener((parent, view, position, id) -> {
            selectedCatName = categoryNames[position];
            selectedCategoryId = position + 1; // Food (0) becomes ID 1, Shopping (1) becomes ID 2...
            tvSelectedCategory.setText("Selected: " + selectedCatName);
        });

        btnSave.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim();

            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            } else if (selectedCategoryId == -1) {
                Toast.makeText(this, "Please select a category icon", Toast.LENGTH_SHORT).show();
            } else {
                saveTransaction(Double.parseDouble(amountStr));
            }
        });
    }

    private void saveTransaction(double amount) {
        String currentDateTime = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(new Date());

        // FIX 3: Pass selectedCategoryId instead of the hardcoded 1
        long result = db.addExpense(amount, selectedCatName, currentDateTime, selectedCategoryId);

        if (result != -1) {
            Toast.makeText(this, "Transaction Saved!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Database Error!", Toast.LENGTH_SHORT).show();
        }
    }
}