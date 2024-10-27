package com.example.befit;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProgressActivity extends AppCompatActivity {

    private TextView exercisesCompletedTextView;
    private BottomNavigationView bottomNavigationView;
    private TextView pauseTimeTextView;
    private TextView caloriesBurnedTextView;
    private TextView exerciseDurationTextView;

    private Button resetButton;
    private long userId;

    private ProgressTracker progressTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_activity);

        // Obtain user ID (you need to implement this method)
        userId = getCurrentUserId();

        // Initialize views
        exercisesCompletedTextView = findViewById(R.id.totalExercisesTextView);
        pauseTimeTextView = findViewById(R.id.totalPauseTimeTextView);
        caloriesBurnedTextView = findViewById(R.id.totalCaloriesBurnedTextView);
        exerciseDurationTextView = findViewById(R.id.totalExerciseDurationTextView);
        resetButton = findViewById(R.id.resetProgress);

        // Get instance of ProgressTracker
        progressTracker = ProgressTracker.getInstance(getApplicationContext(), userId);

       //  Initialize bottom navigation view
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                // Start HomeActivity
                startActivity(new Intent(ProgressActivity.this, HomeActivity.class));
                //finish(); // Optional: Close the current activity if necessary
                return true;
            }
            if (item.getItemId() == R.id.navigation_profile) {
                // Start profileActivity
                startActivity(new Intent(ProgressActivity.this, ProfileActivity.class));
                //finish(); // Optional: Close the current activity if necessary
                return true;
            }
            else {
                return false;
            }

        });

        // Display initial progress data
        displayProgressData();

        // Set onClickListener for reset button
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetConfirmationDialog();
            }
        });
    }

        private void displayProgressData() {
            long pauseDurationMillis = getIntent().getLongExtra("pauseDurationMillis", 0);
            exercisesCompletedTextView.setText("Total Exercise completed: " + progressTracker.getTotalExercisesCompleted());
            pauseTimeTextView.setText("Total Pause Time: " + progressTracker.getTotalPauseTimeMillis()/1000 + " Sec");
            caloriesBurnedTextView.setText("Total Calories Burned: " + progressTracker.getTotalCaloriesBurned());

            exerciseDurationTextView.setText("Total Exercise Duration: " + (progressTracker.getTotalExerciseDurationMillis())+" Sec");

        }

    // Method to show the reset confirmation dialog
    private void showResetConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Would you like to reset the progress?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetProgress();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    // Method to reset the progress
    private void resetProgress() {
        // Reset progress data in ProgressTracker
        progressTracker.resetProgress();

        // Update UI to display reset progress data
        displayProgressData();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ProgressLogging", "ProgressActivity resumed.");
    }
    private long getCurrentUserId() {
        // Retrieve the current user ID from SharedPreferences or any other authentication mechanism
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return sharedPreferences.getLong("user_id", -1); // Return -1 if user ID is not found
    }

}
