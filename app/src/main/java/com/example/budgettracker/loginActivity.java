package com.example.budgettracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class loginActivity extends AppCompatActivity {

    // 1. Declare variables matching your modern UI IDs
    private EditText etUser, etPassword;
    private Button btnSignIn;
    private TextView tvRegister, tvForgot;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 2. Initialize Database and UI elements
        db = new DatabaseHelper(this);

        // Note: Using the IDs from the clean UI layout we built
        etUser = findViewById(R.id.etEmail); // This is your username field
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvRegister = findViewById(R.id.tvRegister);

        // Use the ID from your Forgot Password TextView
        tvForgot = findViewById(R.id.tvForgot);

        // 3. Login Button Logic
        btnSignIn.setOnClickListener(v -> {
            String username = etUser.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (username.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            } else {
                // Check credentials against the 'users' table in DatabaseHelper
                // Your DB method: checkLogin(String username, String password)
                if (db.checkLogin(username, pass)) {
                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();

                    // Navigate to Dashboard (MainActivity)
                    Intent intent = new Intent(loginActivity.this, MainActivity.class);

                    // Pass the username so MainActivity can greet the user
                    intent.putExtra("USERNAME", username);

                    startActivity(intent);
                    finish(); // Closes Login screen so Jesse can't go back to it
                } else {
                    Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 4. Navigate to Register Screen
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(loginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // 5. Navigate to Forgot Password Screen
        if (tvForgot != null) {
            tvForgot.setOnClickListener(v -> {
                // Ensure ForgotPasswordActivity is created in your project
                Intent intent = new Intent(loginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            });
        }
    }
}