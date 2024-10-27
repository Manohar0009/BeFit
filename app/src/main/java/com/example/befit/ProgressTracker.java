package com.example.befit;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ProgressTracker {
    private static ProgressTracker instance;
    private int totalExercisesCompleted;
    private boolean isExercisePaused;
    private long totalPauseTimeMillis;
    private int totalCaloriesBurned;
    private long totalExerciseDurationMillis;
    private List<ExerciseEntry> exerciseHistory;

    private boolean exerciseCompleted;

    private SharedPreferences sharedPreferences;
    private static final String KEY_EXERCISES_COMPLETED = "exercises_completed";
    private static final String PREF_NAME = "progress_data";
    private long userId; // Add user ID field

//    private ProgressTracker(Context context, long userId) {
//        this.userId = userId;
//        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        exerciseHistory = new ArrayList<>(); // Initialize exerciseHistory
//        loadProgress(); // Load progress data from SharedPreferences
//
//    }
private ProgressTracker(Context context, long userId) {
    // Use user ID as part of shared preferences file name
    String prefFileName = PREF_NAME + userId;
    sharedPreferences = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
    exerciseHistory = new ArrayList<>(); // Initialize exerciseHistory
    loadProgress(); // Load progress data from SharedPreferences
}

//    public static synchronized ProgressTracker getInstance(Context context) {
//        if (instance == null) {
//            instance = new ProgressTracker(context.getApplicationContext());
//        }
//        return instance;
//    }

    public static synchronized ProgressTracker getInstance(Context context, long userId) {
        if (instance == null || instance.userId != userId) {
            instance = new ProgressTracker(context.getApplicationContext(), userId);
        }
        return instance;
    }

    public int getTotalExercisesCompleted() {
        return totalExercisesCompleted;
    }

    public long getTotalPauseTimeMillis() {
        return totalPauseTimeMillis;
    }

    public int getTotalCaloriesBurned() {
        return totalCaloriesBurned;
    }

    public long getTotalExerciseDurationMillis() {
        return totalExerciseDurationMillis;
    }
    public void setExerciseCompleted(boolean exerciseCompleted) {
        this.exerciseCompleted = exerciseCompleted;
    }


//    public void completeExercise() {
//        exerciseCompleted = true;
//        int exercisesCompleted = getExercisesCompleted();
//        totalExercisesCompleted++;
//        ExerciseEntry entry = new ExerciseEntry(ExerciseType.EXERCISE, System.currentTimeMillis());
//        exerciseHistory.add(entry);
//        sharedPreferences.edit().putInt(KEY_EXERCISES_COMPLETED, exercisesCompleted).apply();
//        Log.d("ProgressTracker", "Exercise completed. Total exercises completed: " + totalExercisesCompleted);
//        saveProgress(); // Save progress data
//    }

    public void completeExercise() {
        exerciseCompleted = true; // Set exerciseCompleted flag to true
        totalExercisesCompleted++;
        ExerciseEntry entry = new ExerciseEntry(ExerciseType.EXERCISE, System.currentTimeMillis());
        exerciseHistory.add(entry);
        sharedPreferences.edit().putInt(KEY_EXERCISES_COMPLETED, totalExercisesCompleted).apply(); // Update totalExercisesCompleted in SharedPreferences
        Log.d("ProgressTracker", "Exercise completed. Total exercises completed: " + totalExercisesCompleted);
        saveProgress(); // Save progress data
    }


    private void saveProgress() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("totalExercisesCompleted", totalExercisesCompleted);
        editor.putLong("totalPauseTimeMillis", totalPauseTimeMillis);
        editor.putInt("totalCaloriesBurned", totalCaloriesBurned);
        editor.putLong("totalExerciseDurationMillis", totalExerciseDurationMillis);
        editor.apply();
    }

    private void loadProgress() {
        totalExercisesCompleted = sharedPreferences.getInt("totalExercisesCompleted", 0);
        totalPauseTimeMillis = sharedPreferences.getLong("totalPauseTimeMillis", 0);
        totalCaloriesBurned = sharedPreferences.getInt("totalCaloriesBurned", 0);
        totalExerciseDurationMillis = sharedPreferences.getLong("totalExerciseDurationMillis", 0);
    }
    public void addExerciseDuration(long exerciseDurationMillis) {
        if (exerciseCompleted && exerciseDurationMillis > 0) {
//        if (exerciseDurationMillis > 0) {
            totalExerciseDurationMillis += exerciseDurationMillis;
            // Log message to indicate exercise duration added
            Log.d("ProgressTracker", "Exercise duration added: " + exerciseDurationMillis + " ms. Total exercise duration: " + totalExerciseDurationMillis);
        } else {
            // Log a warning if trying to add duration for an incomplete or invalid exercise duration
            Log.w("ProgressTracker", "Trying to add invalid duration for an incomplete exercise.");
        }
    }

    public void resetExerciseDuration() {
        totalExerciseDurationMillis = 0;
    }

    public void addCaloriesBurned(long caloriesBurned) {
        // Add calories burned only if an exercise was completed
        if (exerciseCompleted && caloriesBurned > 0) {
            totalCaloriesBurned += caloriesBurned;

            // Log calories burned for debugging
            Log.d("ProgressTracker", "Calories Burned: " + totalCaloriesBurned);
        }
    }


    public void addPauseTime(long pauseTimeMillis) {
        // Add pause time only if the exercise was completed
        if (exerciseCompleted && pauseTimeMillis > 0) {
            totalPauseTimeMillis += pauseTimeMillis;
            ExerciseEntry entry = new ExerciseEntry(ExerciseType.PAUSE, System.currentTimeMillis() - pauseTimeMillis);
            exerciseHistory.add(entry);

            // Log message to indicate pause time added
            Log.d("ProgressTracker", "Pause time added: " + pauseTimeMillis + " ms. Total pause time: " + totalPauseTimeMillis);
        }
    }

    public int getExercisesCompletedToday() {
        // Get the current date
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        // Count exercises completed today
        int count = 0;
        for (ExerciseEntry entry : exerciseHistory) {
            Calendar exerciseDate = Calendar.getInstance();
            exerciseDate.setTimeInMillis(entry.getTimestamp());

            if (exerciseDate.after(today) || exerciseDate.equals(today)) {
                if (entry.getType() == ExerciseType.EXERCISE) {
                    count++;
                }
            }
        }
        return count;
    }

    public void resetProgress() {
        // Reset all progress data
        totalExercisesCompleted = 0;
        totalPauseTimeMillis = 0;
        totalCaloriesBurned = 0;
        totalExerciseDurationMillis = 0;
        exerciseHistory.clear(); // Clear exercise history
        saveProgress(); // Save reset progress data
    }
    public int getExercisesCompleted() {
        return sharedPreferences.getInt(KEY_EXERCISES_COMPLETED, 0);
    }

    public int getExercisesCompletedThisWeek() {
        // Get the current week of the year
        Calendar calendar = Calendar.getInstance();
        int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);

        // Count exercises completed this week
        int count = 0;
        for (ExerciseEntry entry : exerciseHistory) {
            Calendar entryCalendar = Calendar.getInstance();
            entryCalendar.setTimeInMillis(entry.getTimestamp());
            int entryWeek = entryCalendar.get(Calendar.WEEK_OF_YEAR);
            if (entryWeek == currentWeek && entry.getType() == ExerciseType.EXERCISE) {
                count++;
            }
        }
        return count;
    }

    public void setExercisePaused(boolean paused) {
        isExercisePaused = paused;
    }



    private static class ExerciseEntry {
        private ExerciseType type;
        private long timestamp;
        private long durationMillis;

        public ExerciseEntry(ExerciseType type, long timestamp) {
            this.type = type;
            this.timestamp = timestamp;
            this.durationMillis = 0; // Set duration to 0 for exercise without duration
        }
        public ExerciseType getType() {
            return type;
        }

        // Getter method for timestamp
        public long getTimestamp() {
            return timestamp;
        }
    }

    private enum ExerciseType {
        EXERCISE, PAUSE
    }
}
