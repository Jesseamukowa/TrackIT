package com.example.budgettracker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegUser, etRegPassword, etSecurityAnswer;
    private Button btnRegister;
    private ImageView ivProfilePicture;
    private RelativeLayout rlPhotoHolder;

    private DatabaseHelper db;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);

        // 1. Linking Java variables to XML IDs
        etRegUser = findViewById(R.id.etRegUser);
        etRegPassword = findViewById(R.id.etRegPassword);
        etSecurityAnswer = findViewById(R.id.etSecurityAnswer);
        btnRegister = findViewById(R.id.btnRegister);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        rlPhotoHolder = findViewById(R.id.rlPhotoHolder);

        // 2. Setting up Photo Picker click listener
        rlPhotoHolder.setOnClickListener(v -> openGallery());

        // 3. Registration Logic
        btnRegister.setOnClickListener(v -> {
            String user = etRegUser.getText().toString().trim();
            String pass = etRegPassword.getText().toString().trim();
            String answer = etSecurityAnswer.getText().toString().trim();

            // Convert URI to string to save in SQLite TEXT column
            String imagePath = (imageUri != null) ? imageUri.toString() : "";

            if (user.isEmpty() || pass.isEmpty() || answer.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Save user data to the database
                long id = db.addUser(user, pass, answer, imagePath);

                if (id != -1) {
                    Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Closes registration and returns to LoginActivity
                } else {
                    Toast.makeText(this, "Error: Username might already exist", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openGallery() {
        // Using OPEN_DOCUMENT is the modern way to get long-term file access
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        // Requesting flags to ensure we can read the file later
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();

            if (imageUri != null) {
                // PERSIST PERMISSION: This is the crucial step for the Profile screen!
                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                try {
                    // This line tells Android to keep this specific URI "alive" for our app
                    getContentResolver().takePersistableUriPermission(imageUri, takeFlags);

                    // Display the selected image immediately
                    ivProfilePicture.setImageURI(imageUri);
                } catch (SecurityException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to persist image permission", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}