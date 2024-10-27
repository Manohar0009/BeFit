package com.example.befit;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExerciseActivity extends AppCompatActivity {

    private Button startButton;
    private List<ImageView> exerciseImages = new ArrayList<>();
    private ProgressBar exerciseProgressBar;
    private TextView exerciseDurationTextView;
    private RatingBar exerciseRatingBar;
    private BottomNavigationView bottomNavigationView;

    private int currentExerciseIndex = 0;
    private CountDownTimer exerciseTimer;
    private Map<Integer, Float> exerciseRatings = new HashMap<>();
    private boolean isExercisePaused;
    private long remainingTimeMillis;
    private long exerciseDurationMillis;
    private long elapsedMillis;

    private long exerciseStartTime;

    private Button resetButton;

    private ProgressTracker progressTracker;
    private long pauseDurationMillis;

    private DatabaseHelper databaseHelper;
    private static final int CALORIES_PER_EXERCISE = 5;

    private long pauseStartTime;
    private long totalExerciseDuration = 0;

    private static final int PROGRESS_ACTIVITY_REQUEST_CODE = 1;
    private Intent progressIntent;
    private long individualExerciseDurationMillis;

    private long exerciseEndTime;

    private long durationMillis;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        // Initialize the DatabaseHelper instance
        databaseHelper = new DatabaseHelper(this);
//        long userId = getUserIdFromSharedPreferences();
        // Initialize the ProgressTracker instance
//        progressTracker = ProgressTracker.getInstance(this, databaseHelper);
//        progressTracker = ProgressTracker.getInstance(this, userId);
        progressTracker = ProgressTracker.getInstance(this, getCurrentUserId());
        // Initialize UI elements
        startButton = findViewById(R.id.startButton);
        // Initialize reset button
        resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetTimer();
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide the button and start the exercise
                v.setVisibility(View.GONE);
                startExerciseAfterDelay(2000); // Start after 5 seconds (for demonstration)
                // startExercise(); // Start the exercise immediately

            }
        });

        // Add exercise images to the list
        exerciseImages.add(findViewById(R.id.exerciseImage1));
        exerciseImages.add(findViewById(R.id.exerciseImage2));
        exerciseImages.add(findViewById(R.id.exerciseImage3));
        exerciseImages.add(findViewById(R.id.exerciseImage4));
        exerciseImages.add(findViewById(R.id.exerciseImage5));
        exerciseImages.add(findViewById(R.id.exerciseImage6));
        exerciseImages.add(findViewById(R.id.exerciseImage7));

        exerciseProgressBar = findViewById(R.id.exerciseProgressBar);
        exerciseDurationTextView = findViewById(R.id.exerciseDurationTextView);
        exerciseRatingBar = findViewById(R.id.exerciseRatingBar);

        // Set initial progress and rating
        exerciseProgressBar.setProgress(0);
        exerciseRatingBar.setRating(0);

        // Hide all exercise images initially
        hideAllExerciseImages();

        // Initialize bottom navigation view
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            // Handle navigation item clicks here
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                // Start HomeActivity
                startActivity(new Intent(ExerciseActivity.this, HomeActivity.class));
                finish(); // Close the current activity if necessary
                return true;
            }
            if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(ExerciseActivity.this, ProfileActivity.class));
                finish(); // Close the current activity if necessary
                return true;
            } else {
                return false;
            }
        });


        // Set the exercise duration (in milliseconds)
        exerciseDurationMillis = 6000;
        Button pauseButton = findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isExercisePaused) {
                    pauseExercise();
                } else {
                    resumeExercise();
                }
            }
        });

        Button exitButton = findViewById(R.id.exitButton);
        exitButton.setOnClickListener(v -> {
            // Create an intent to navigate to the ProgressActivity
            Intent intent = createProgressIntent();

            Log.d("ProgressLogging", "Exiting ExerciseActivity. Starting ProgressActivity...");

            // Set the result to indicate success and include the intent with progress data
            setResult(Activity.RESULT_OK, intent);

            // Finish the ExerciseActivity
            finish();
        });
    }
    private long getCurrentUserId() {
        // Retrieve the current user ID from SharedPreferences or any other authentication mechanism
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return sharedPreferences.getLong("user_id", -1); // Return -1 if user ID is not found
    }


    public Intent createProgressIntent() {
        // Get the instance of ProgressTracker
//        ProgressTracker progressTracker = ProgressTracker.getInstance(this); // Provide the appropriate context
        progressTracker = ProgressTracker.getInstance(this, getCurrentUserId());
        // Retrieve the required progress data
        int totalExercisesCompleted = progressTracker.getTotalExercisesCompleted();
        long totalPauseTimeMillis = progressTracker.getTotalPauseTimeMillis();
        double totalCaloriesBurned = progressTracker.getTotalCaloriesBurned();
        long totalExerciseDurationMillis = progressTracker.getTotalExerciseDurationMillis();

        // Create an intent to navigate to the ProgressActivity
        Intent intent = new Intent(ExerciseActivity.this, ProgressActivity.class);

        // Pass the progress data to ProgressActivity
        intent.putExtra("totalExercisesCompleted", totalExercisesCompleted);
        intent.putExtra("totalPauseTimeMillis", totalPauseTimeMillis);
        intent.putExtra("totalCaloriesBurned", totalCaloriesBurned);
        intent.putExtra("totalExerciseDurationMillis", totalExerciseDurationMillis);

        return intent;
    }


    private void startExerciseAfterDelay(long delayMillis) {
        // Show countdown message with delay
        TextView countdownTextView = findViewById(R.id.countdownTextView);
        countdownTextView.setVisibility(View.VISIBLE);

        new CountDownTimer(delayMillis, 1000) {
            int count = (int) (delayMillis / 1000);

            @Override
            public void onTick(long millisUntilFinished) {
                // Update countdown message
                countdownTextView.setText("The exercise is going to start in " + count + "...");
                count--;
            }


            @Override
            public void onFinish() {
                // Start the exercise after delay
                countdownTextView.setVisibility(View.GONE); // Hide countdown message
                startExercise();
            }
        }.start();
    }


    private void startExercise() {
        Button pauseButton = findViewById(R.id.pauseButton);
        pauseButton.setEnabled(false);

        long currentTime = System.currentTimeMillis();
        Log.d("ExerciseActivity", "Exercise start time: " + currentTime);
        // Set the exercise start time only if it's the first exercise or after shuffling
//        if (currentExerciseIndex == 0) {
//            exerciseStartTime = System.currentTimeMillis();
//        }
// Record the exercise start time just before starting the timer
        exerciseStartTime = System.currentTimeMillis();

        if (currentExerciseIndex < exerciseImages.size()) {
            if (currentExerciseIndex == 0) {
                Collections.shuffle(exerciseImages);
            }

            hideAllExerciseImages();

            ImageView currentExerciseImage = exerciseImages.get(currentExerciseIndex);
            currentExerciseImage.setVisibility(View.VISIBLE);

            // Record the exercise start time just before starting the timer
            exerciseStartTime = System.currentTimeMillis();

            exerciseRatingBar.setRating(0);

            // Set the exercise start time
//            exerciseStartTime = currentTime;

            // Calculate the remaining time for the exercise
            long remainingTime = exerciseDurationMillis;
            Log.d("ExerciseActivity", "Remaining time: " + remainingTime);

            startTimer(remainingTime);
            // Set remaining time to exercise duration
//            remainingTimeMillis = exerciseDurationMillis;

            // Start countdown timer for the remaining time
//            startTimer(remainingTime);


            pauseButton.setEnabled(true);
        } else {
            exerciseDurationTextView.setText("Exercise Completed for today");
            exerciseDurationTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        }
    }


    private void startTimer(long durationMillis) {
        this.durationMillis = durationMillis;
        exerciseStartTime = System.currentTimeMillis(); // Record start time just before starting the timer

        exerciseTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Calculate elapsed time since the start of the exercise
                long elapsedTime = System.currentTimeMillis() - exerciseStartTime;

                // Update countdown timer text
                updateTimerText(millisUntilFinished);

                // Update progress bar
                int progress = (int) (((durationMillis - millisUntilFinished) * 100) / durationMillis);
                exerciseProgressBar.setProgress(progress);

                // Log remaining time
                Log.d("ExerciseActivity", "Remaining time: " + millisUntilFinished);

                // Log the elapsed time for debugging
                Log.d("ExerciseActivity", "Elapsed time: " + elapsedTime);
            }


            @Override
            public void onFinish() {
                // Hide the current exercise image
                if (currentExerciseIndex < exerciseImages.size()) {
                    exerciseImages.get(currentExerciseIndex).setVisibility(View.GONE);
                }

                // Move to the next exercise when timer finishes
                currentExerciseIndex++;
                elapsedMillis = 0; // Reset elapsed time for next exercise
                startExercise();
                onExerciseCompleted();
            }
        };
        exerciseTimer.start();
    }


    private void updateTimerText(long millisUntilFinished) {
        // Convert milliseconds to minutes and seconds
        long minutes = millisUntilFinished / 60000;
        long seconds = (millisUntilFinished % 60000) / 1000;

        // Format the time as a string
        String timeString = String.format("%02d:%02d", minutes, seconds);

        // Update the countdown timer text
        exerciseDurationTextView.setText(timeString);
    }

    private void hideAllExerciseImages() {
        for (ImageView exerciseImage : exerciseImages) {
            exerciseImage.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseHelper.close();
        // Cancel the exercise timer to prevent memory leaks
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
//            passProgressDataToProgressActivity();
        }
    }

    public void submitRating(View view) {
        // Get the rating provided by the user
        float rating = exerciseRatingBar.getRating();

        // Save the rating for the current exercise
        exerciseRatings.put(currentExerciseIndex, rating);

        // Save the rating to the database
        saveRatingToDatabase(currentExerciseIndex, rating);
    }

    private void saveRatingToDatabase(int exerciseIndex, float rating) {
        // Retrieve the current user ID from SharedPreferences or any other authentication mechanism
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        long userId = sharedPreferences.getLong("user_id", -1); // Replace "user_id" with the key used to store the user ID

        // Ensure that the user ID is valid
        if (userId != -1) {
            // Save the rating for the current exercise to the database
            long exerciseId = exerciseIndex + 1; // Example: Use the exercise index as exercise ID
            long ratingId = databaseHelper.insertExerciseRating(exerciseId, userId, rating);

            if (ratingId != -1) {
                // Rating inserted successfully
                Toast.makeText(this, "Rating submitted successfully", Toast.LENGTH_SHORT).show();
            } else {
                // Error inserting rating
                Toast.makeText(this, "Failed to submit rating", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle case where user ID is not found or invalid
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
        }
    }


//    private void onExerciseCompleted() {
//        long elapsedTime = System.currentTimeMillis() - exerciseStartTime; // Calculate exercise duration
//
//        // Log the elapsed time
//        Log.d("ExerciseActivity", "Elapsed time: " + elapsedTime);
//
//        // Add exercise duration to the total exercise duration
//        totalExerciseDuration += elapsedTime;
//
//        // Log the exercise completion details for debugging
//        Log.d("ExerciseActivity", "Exercise completed. Duration: " + elapsedTime);
//        // Set exerciseCompleted flag to true
//        progressTracker.setExerciseCompleted(true);
//
//        // Add exercise duration to the progress tracker
//        progressTracker.addExerciseDuration(elapsedTime);
//
//
//        // Increment total exercises completed
//        progressTracker.completeExercise();
//
//
//        // Add fixed calories burned for each exercise completed (5 calories per exercise)
//        progressTracker.addCaloriesBurned(5);
//
//        Toast.makeText(this, "Exercise completed!", Toast.LENGTH_SHORT).show();
//
//        // Log calories burned for debugging
//        Log.d("ExerciseActivity", "Calories Burned: " + 5); // Fixed value of 5 calories per exercise
//    }


    private void onExerciseCompleted() {
        // Calculate elapsed time since the start of the exercise
        long elapsedTime = System.currentTimeMillis() - exerciseStartTime;

        // Log the elapsed time for debugging
        Log.d("ExerciseActivity", "Elapsed time: " + elapsedTime);

        // Add exercise duration to the total exercise duration
        totalExerciseDuration += elapsedTime;

        // Log the exercise completion details for debugging
        Log.d("ExerciseActivity", "Exercise completed. Duration: " + elapsedTime);
        // Set exerciseCompleted flag to true
        progressTracker.setExerciseCompleted(true);

        // Add exercise duration to the progress tracker
        progressTracker.addExerciseDuration(elapsedTime);

        // Increment total exercises completed
        progressTracker.completeExercise();

        // Add fixed calories burned for each exercise completed (5 calories per exercise)
        progressTracker.addCaloriesBurned(5);

        Toast.makeText(this, "Exercise completed!", Toast.LENGTH_SHORT).show();

        // Log calories burned for debugging
        Log.d("ExerciseActivity", "Calories Burned: " + 5); // Fixed value of 5 calories per exercise
    }


    private void pauseExercise() {
        if (!isExercisePaused) {
            // Record the start time of pause only if the exercise is not already paused
            pauseStartTime = System.currentTimeMillis();
            isExercisePaused = true;
            // Cancel the exercise timer if it's running
            if (exerciseTimer != null) {
                exerciseTimer.cancel();
            }
            // Update the UI to reflect the pause state
            Button pauseButton = findViewById(R.id.pauseButton);
            pauseButton.setText("Resume Exercise");
            pauseButton.setEnabled(true);

            // Calculate the remaining time left for the current exercise
//            remainingTimeMillis = exerciseDurationMillis - elapsedMillis;

            remainingTimeMillis = exerciseDurationMillis - (System.currentTimeMillis() - exerciseStartTime);


            // Notify the progress tracker about the pause
            progressTracker.setExercisePaused(true);

        }

    }


    private void resumeExercise() {
        if (exerciseTimer != null && isExercisePaused) {
            long pauseEndTime = System.currentTimeMillis(); // Get the end time of pause
            long pauseDurationMillis = pauseEndTime - pauseStartTime; // Calculate pause duration


//            exerciseStartTime += pauseDurationMillis;

            exerciseStartTime = System.currentTimeMillis() - (exerciseDurationMillis - remainingTimeMillis);

            // Resume timer with the corrected remaining time
            startTimer(remainingTimeMillis);

            // Update UI and flags
            Button pauseButton = findViewById(R.id.pauseButton);
            pauseButton.setText("Pause Exercise");
            isExercisePaused = false;
            progressTracker.setExercisePaused(false);

            progressTracker.addPauseTime(pauseDurationMillis);
            Log.d("ExerciseActivity", "Exercise resumed. Pause duration: " + pauseDurationMillis);
        }
        // Exercise start time should not be updated here
    }

    private void resetTimer() {
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
        }
        // Reset timer-related variables
        elapsedMillis = 0;
        remainingTimeMillis = exerciseDurationMillis;
        exerciseStartTime = System.currentTimeMillis(); // Reset exercise start time
        // Reset progress tracker
        progressTracker.completeExercise(); // Increment total exercises completed
        progressTracker.resetExerciseDuration(); // Reset exercise duration
        // Update UI to initial state
        exerciseProgressBar.setProgress(0);
        updateTimerText(exerciseDurationMillis);
        hideAllExerciseImages();
        if (currentExerciseIndex < exerciseImages.size()) {
            ImageView currentExerciseImage = exerciseImages.get(currentExerciseIndex);
            currentExerciseImage.setVisibility(View.VISIBLE);
        }
        // Start the exercise if it's not paused
        if (!isExercisePaused) {
            startTimer(remainingTimeMillis);
        }


    }
}


