package com.example.budgettracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etUser, etAnswer, etNewPass;
    private Button btnReset;
    private DatabaseHelper db; // This is our bridge to the database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize the helper
        db = new DatabaseHelper(this);

        etUser = findViewById(R.id.etUsernameRecovery);
        etAnswer = findViewById(R.id.etSecurityAnswer);
        etNewPass = findViewById(R.id.etNewPasscode);
        btnReset = findViewById(R.id.btnResetPassword);

        btnReset.setOnClickListener(v -> {
            String user = etUser.getText().toString().trim();
            String answer = etAnswer.getText().toString().trim();
            String newPass = etNewPass.getText().toString().trim();

            if (user.isEmpty() || answer.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // STEP 1: READ from Database (Validation)
            if (db.validateRecovery(user, answer)) {

                // STEP 2: WRITE to Database (Update Password)
                if (db.resetPassword(user, newPass)) {
                    Toast.makeText(this, "Success! Password updated.", Toast.LENGTH_LONG).show();
                    finish(); // Close activity and return to Login
                } else {
                    Toast.makeText(this, "Database Error: Update failed", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Validation Failed: Wrong answer or username", Toast.LENGTH_SHORT).show();
            }
        });
    }
}