package com.example.befit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class LoginActivity extends AppCompatActivity {

    // Views
    private EditText usernameEditText;
    private EditText passwordEditText;

    // DatabaseHelper instance
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Initialize views
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Initialize login button
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> validateLogin());

        // Initialize register button
        Button registerButton = findViewById(R.id.loginRegisterButton);
        registerButton.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        // Initialize "Forgot Password" text
        TextView forgotPasswordText = findViewById(R.id.forgotPasswordText);
        forgotPasswordText.setMovementMethod(LinkMovementMethod.getInstance());
        Spannable spannable = new SpannableString(getResources().getString(R.string.forgot_password));
        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Handle the click event for "Forgot Password"
                // For example, open a new activity or show a dialog for password recovery
                // startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
                Toast.makeText(LoginActivity.this, "Forgot Password clicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);  // Disable underline
                ds.setColor(ContextCompat.getColor(LoginActivity.this, R.color.forgot_password_text_color));  // Set custom text color
                ;  // Set custom text color
            }
        }, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        forgotPasswordText.setText(spannable, TextView.BufferType.SPANNABLE);
        forgotPasswordText.setHighlightColor(Color.TRANSPARENT); // Set highlight color to transparent
    }

    private void validateLogin() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // Check if the username and password are valid
        if (databaseHelper.isValidLogin(username, password)) {
            // Successful login, retrieve userId and store it
            long userId = databaseHelper.getUserIdFromAuthenticationSystem(username, password);
            if (userId != -1) {
                // Save userId to SharedPreferences
                saveUserIdToSharedPreferences(userId);
                // Navigate to HomeActivity
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish(); // Finish LoginActivity
            } else {
                // Show error message if userId is not found
                Toast.makeText(LoginActivity.this, "User ID not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Invalid login, show error message
            Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserIdToSharedPreferences(long userId) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("user_id", userId);
        editor.apply();
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
