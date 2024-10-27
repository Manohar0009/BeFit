package com.example.befit;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.befit.DatabaseHelper;
import com.example.befit.R;

public class RegisterActivity extends AppCompatActivity {

    // Views
    private EditText usernameEditText;
    private EditText passwordEditText;

    // DatabaseHelper instance
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Initialize register button
        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // Check if the username and password are not empty
        if (!username.isEmpty() && !password.isEmpty()) {
            // Attempt to register the user
            long rowId = databaseHelper.insertUser(username, password);
            if (rowId != -1) {
                // Registration successful, log user details
                Log.d("RegisterActivity", "User registered successfully - ID: " + rowId + ", Username: " + username + ", Password: " + password);
                // Show success message
                Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                finish(); // Finish RegisterActivity
            } else {
                // Registration failed, show error message
                Log.e("RegisterActivity", "Failed to register user");

                Toast.makeText(RegisterActivity.this, "Failed to register user", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Username or password is empty, show error message
            Toast.makeText(RegisterActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Any logic needed when the activity is resumed
    }

    @Override
    protected void onPause() {
        // Release any resources or unregister receivers
        super.onPause();
    }

    @Override
    protected void onStop() {
        // Save persistent data or perform cleanup operations
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // Release any remaining resources
        super.onDestroy();
    }
}
