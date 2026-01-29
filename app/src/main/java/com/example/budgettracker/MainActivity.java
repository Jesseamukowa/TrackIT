package com.example.budgettracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rvTransactions;
    private TransactionAdapter transactionAdapter;
    private ImageView imgProfile;
    private TextView tvWelcome, tvBalance, tvIncome, tvExpense;
    private FloatingActionButton btnAdd;
    private DatabaseHelper db;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize Views
        db = new DatabaseHelper(this);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvBalance = findViewById(R.id.tvBalance);
        tvIncome = findViewById(R.id.tvTotalIncome);
        tvExpense = findViewById(R.id.tvTotalExpense);
        btnAdd = findViewById(R.id.btnAddTransaction);
        imgProfile = findViewById(R.id.imgDashProfile);

        // 2. Income Layout Click Listener
        LinearLayout incomeLayout = findViewById(R.id.incomeLayout);
        if (incomeLayout != null) {
            incomeLayout.setOnClickListener(v -> showEditIncomeDialog());
        }

        // 3. RecyclerView Setup
        rvTransactions = findViewById(R.id.rvTransactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));

        List<Transaction> initialTransactions = db.getAllTransactionsList();
        transactionAdapter = new TransactionAdapter(initialTransactions);
        rvTransactions.setAdapter(transactionAdapter);

        // 4. User Data & Intent Handling
        username = getIntent().getStringExtra("USERNAME");
        if (username == null) username = "User";

        loadUserData();

        // 5. FAB Logic
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
            startActivity(intent);
        });

        // 6. Swipe to Delete Logic
        setupSwipeToDelete();

        // 7. FIXED: Single Bottom Navigation Listener
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_stats) {
                startActivity(new Intent(MainActivity.this, StatsActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("USERNAME", username); // Essential for loading the right user
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_home) {
                return true;
            }
            return false;
        });
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Transaction swipedTransaction = transactionAdapter.getTransactionAt(position);

                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Transaction")
                        .setMessage("Delete " + swipedTransaction.getCategory() + " record?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            db.deleteExpense(swipedTransaction.getId());
                            updateFinancials();
                            Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            transactionAdapter.notifyItemChanged(position);
                        })
                        .show();
            }
        }).attachToRecyclerView(rvTransactions);
    }

    private void loadUserData() {
        Cursor cursor = db.getUserData(username);
        if (cursor != null && cursor.moveToFirst()) {
            // Set Username
            int userIndex = cursor.getColumnIndex("username");
            if (userIndex != -1) {
                tvWelcome.setText("Hi, " + cursor.getString(userIndex) + "!");
            }

            // Set Image (using Uri parsing instead of Base64)
            int imgIndex = cursor.getColumnIndex("profile_image");
            if (imgProfile != null && imgIndex != -1) {
                String imageStr = cursor.getString(imgIndex);
                if (imageStr != null && !imageStr.isEmpty()) {
                    try {
                        imgProfile.setImageURI(Uri.parse(imageStr));
                    } catch (Exception e) {
                        imgProfile.setImageResource(R.drawable.ic_user_placeholder);
                    }
                }
            }
            cursor.close();
        }
    }

    private void showEditIncomeDialog() {
        FrameLayout container = new FrameLayout(this);
        final EditText etInput = new EditText(this);
        etInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etInput.setHint("Enter monthly budget");

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 60; params.rightMargin = 60;
        etInput.setLayoutParams(params);
        container.addView(etInput);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Update Budget")
                .setView(container)
                .setPositiveButton("Save", (dialog, which) -> {
                    String amountStr = etInput.getText().toString();
                    if (!amountStr.isEmpty()) {
                        db.updateMonthlyIncome("January", Double.parseDouble(amountStr));
                        updateFinancials();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFinancials();
    }

    private void updateFinancials() {
        double totalIncome = db.getTotalIncome();
        double totalExpenses = db.getTotalExpenses();
        double balance = totalIncome - totalExpenses;

        tvIncome.setText("KES " + String.format("%.2f", totalIncome));
        tvExpense.setText("KES " + String.format("%.2f", totalExpenses));
        tvBalance.setText("KES " + String.format("%.2f", balance));

        if (transactionAdapter != null) {
            transactionAdapter.updateData(db.getAllTransactionsList());
        }
    }
}