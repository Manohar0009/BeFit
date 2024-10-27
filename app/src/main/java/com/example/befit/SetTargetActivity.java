package com.example.befit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SetTargetActivity extends AppCompatActivity {

    private EditText dailyTargetEditText;
    private EditText weeklyTargetEditText;
    private TextView dailyTargetProgressTextView;
    private TextView weeklyTargetProgressTextView;
    private SharedPreferences sharedPreferences;

    private BottomNavigationView bottomNavigationView;
    private DatabaseHelper databaseHelper;
    private ProgressTracker progressTracker;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_target);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("targets", MODE_PRIVATE);

        // Retrieve current user ID (you need to implement this method in your app)
        userId = getCurrentUserId();

        // Get instance of ProgressTracker
        progressTracker = ProgressTracker.getInstance(getApplicationContext(), userId);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);


        dailyTargetEditText = findViewById(R.id.dailyTargetEditText);
        dailyTargetEditText.setText(""); // Set initial value to empty string
        weeklyTargetEditText = findViewById(R.id.weeklyTargetEditText);
        weeklyTargetEditText.setText(""); // Set initial value to empty string
        dailyTargetProgressTextView = findViewById(R.id.dailyTargetProgressTextView);
        weeklyTargetProgressTextView = findViewById(R.id.weeklyTargetProgressTextView);
        Button saveTargetButton = findViewById(R.id.saveTargetButton);

        // Retrieve previously saved targets, defaulting to 0 if not found
        int savedDailyTarget = sharedPreferences.getInt("dailyTarget", 0);
        int savedWeeklyTarget = sharedPreferences.getInt("weeklyTarget", 0);
        dailyTargetEditText.setText(String.valueOf(savedDailyTarget));
        weeklyTargetEditText.setText(String.valueOf(savedWeeklyTarget));

        // Retrieve and display current targets for the user
//        displayCurrentTargets();
        // Display initial progress as 0/0
        dailyTargetProgressTextView.setText("Exercise done today: 0/" + savedDailyTarget);
        weeklyTargetProgressTextView.setText("Exercise done this week: 0/" + savedWeeklyTarget);

        displayCurrentTargets();

        // Initialize bottom navigation view
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            // Handle navigation item clicks here
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                // Start HomeActivity
                startActivity(new Intent(SetTargetActivity.this, HomeActivity.class));
                finish(); // Close the current activity if necessary
                return true;
            }
            if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(SetTargetActivity.this, ProfileActivity.class));
                finish(); // Close the current activity if necessary
                return true;
            } else {
                return false;
            }
        });

        // Set click listener for the "Save Target" button
        saveTargetButton.setOnClickListener(v -> {
            // Get the entered daily and weekly targets
            String dailyTargetStr = dailyTargetEditText.getText().toString().trim();
            String weeklyTargetStr = weeklyTargetEditText.getText().toString().trim();

            // Check if both fields are filled
            if (!dailyTargetStr.isEmpty() && !weeklyTargetStr.isEmpty()) {
                // Parse the targets to integers
                int dailyTarget = Integer.parseInt(dailyTargetStr);
                int weeklyTarget = Integer.parseInt(weeklyTargetStr);

                // Save the targets to the database
                saveTargetsToDatabase(dailyTarget, weeklyTarget);
                dailyTargetProgressTextView.setText("Exercise done today: 0/" + dailyTarget);
                weeklyTargetProgressTextView.setText("Exercise done this week: 0/" + weeklyTarget);
//                finish();
            } else {
                // Display a toast message if any field is empty
                Toast.makeText(SetTargetActivity.this, "Please enter both targets", Toast.LENGTH_SHORT).show();
            }
        });

        Button resetTargetsButton = findViewById(R.id.resetTargetButton);
        resetTargetsButton.setOnClickListener(this::resetTargets);


        // Update progress TextViews based on saved targets
//        updateProgressTextViews(savedDailyTarget, savedWeeklyTarget);
    }


    private void updateProgressTextViews() {
        // Retrieve current targets
        String dailyTargetStr = dailyTargetEditText.getText().toString().trim();
        String weeklyTargetStr = weeklyTargetEditText.getText().toString().trim();

        // Check if EditText fields are not empty
        if (!dailyTargetStr.isEmpty() && !weeklyTargetStr.isEmpty()) {
            int dailyTarget = Integer.parseInt(dailyTargetStr);
            int weeklyTarget = Integer.parseInt(weeklyTargetStr);

            // Retrieve current progress from stored ProgressTracker instance
            ProgressTracker progressTracker = ProgressTracker.getInstance(SetTargetActivity.this, userId);
            int exercisesCompletedToday = progressTracker.getTotalExercisesCompleted();
            int exercisesCompletedThisWeek = progressTracker.getTotalExercisesCompleted();

            // Update TextViews
            dailyTargetProgressTextView.setText("Exercise done today: " + exercisesCompletedToday + "/" + dailyTarget);
            weeklyTargetProgressTextView.setText("Exercise done this week: " + exercisesCompletedThisWeek + "/" + weeklyTarget);
        } else {
            // Handle case when EditText fields are empty
            dailyTargetProgressTextView.setText("Exercise done today: 0/0");
            weeklyTargetProgressTextView.setText("Exercise done this week: 0/0");
        }
    }

    public void resetTargets(View view) {
        // Reset daily and weekly targets
        dailyTargetEditText.setText("");
        weeklyTargetEditText.setText("");

        // Save the targets to the database
        saveTargetsToDatabase(0, 0);

        // Update progress TextViews with new targets and exercises completed
        updateProgressTextViews();

        // Show toast message
        Toast.makeText(SetTargetActivity.this, "Targets reset", Toast.LENGTH_SHORT).show();
    }


    private void displayCurrentTargets() {
        // Retrieve current targets from the database
        Cursor profileCursor = databaseHelper.getProfile(userId);
        if (profileCursor != null && profileCursor.moveToFirst()) {
            int dailyTargetIndex = profileCursor.getColumnIndex(DatabaseHelper.COLUMN_DAILY_TARGET);
            int weeklyTargetIndex = profileCursor.getColumnIndex(DatabaseHelper.COLUMN_WEEKLY_TARGET);
            int savedDailyTarget = profileCursor.getInt(dailyTargetIndex);
            int savedWeeklyTarget = profileCursor.getInt(weeklyTargetIndex);
            dailyTargetEditText.setText(String.valueOf(savedDailyTarget));
            weeklyTargetEditText.setText(String.valueOf(savedWeeklyTarget));
            profileCursor.close();
        }
    }
    private void saveTargetsToDatabase(int dailyTarget, int weeklyTarget) {
        // Check if the profile exists for the user
        if (databaseHelper.profileExists(userId)) {
            // Update daily and weekly targets in the database
            boolean success = databaseHelper.updateDailyTarget(userId, dailyTarget);
            success &= databaseHelper.updateWeeklyTarget(userId, weeklyTarget);
            if (success) {
                Toast.makeText(SetTargetActivity.this, "Targets saved successfully", Toast.LENGTH_SHORT).show();
                // Update progress TextViews with new targets and exercises completed
                updateProgressTextViews();
            } else {
                Toast.makeText(SetTargetActivity.this, "Failed to save targets", Toast.LENGTH_SHORT).show();
            }
        } else {
            // If the profile doesn't exist, log an error message
            Log.e("SetTargetActivity", "Failed to save targets - Profile does not exist for user ID: " + userId);
            Toast.makeText(SetTargetActivity.this, "Failed to save targets - Profile does not exist", Toast.LENGTH_SHORT).show();
        }
    }




    // You need to implement a method to retrieve the current user's ID
    private long getCurrentUserId() {
        // Retrieve the current user ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return sharedPreferences.getLong("user_id", -1); // Return -1 if user ID is not found
    }
    protected void onResume() {
        super.onResume();
        // Retrieve and display current targets for the user
        displayCurrentTargets();
        // Update progress TextViews based on saved targets
        updateProgressTextViews();
    }


}
