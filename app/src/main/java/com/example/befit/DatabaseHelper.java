package com.example.befit;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Calendar;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database info
    private static final String DATABASE_NAME = "befit.db";
    private static final int DATABASE_VERSION = 3;

    // Table names and column names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_EXERCISES = "exercises";
    private static final String TABLE_EXERCISE_RATINGS = "exercise_ratings";
    private static final String TABLE_PROFILES = "profiles";

    // Common column names
    private static final String COLUMN_ID = "id";

    // Users table column names
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // Profiles table column names
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DATE_OF_BIRTH = "date_of_birth";
    public static final String COLUMN_COUNTRY = "country";
    public static final String COLUMN_GENDER = "gender";
    public static final String COLUMN_PHONE = "phone_number";
    public static final String COLUMN_BIO = "bio";
    // Exercises table column names
    private static final String COLUMN_EXERCISE_NAME = "name";
    private static final String COLUMN_EXERCISE_IMAGE = "image";
    private static final String COLUMN_EXERCISE_DURATION = "duration";

    private static final String COLUMN_EXERCISE_ID = "exercise_id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_RATING = "rating";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_DAILY_TARGET = "daily_target";
    public static final String COLUMN_WEEKLY_TARGET = "weekly_target";
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_USER_ID = "user_id";


    // Tag for logging
    private static final String TAG = "DatabaseHelper";

    // Create users table query
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USERNAME + " TEXT UNIQUE, " +
            COLUMN_PASSWORD + " TEXT)";

    // Create exercises table query
    private static final String CREATE_TABLE_EXERCISES = "CREATE TABLE " + TABLE_EXERCISES + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_EXERCISE_NAME + " TEXT, " +
            COLUMN_EXERCISE_IMAGE + " TEXT, " +
            COLUMN_EXERCISE_DURATION + " INTEGER)";

    // Create exercise ratings table query
    private static final String CREATE_TABLE_EXERCISE_RATINGS = "CREATE TABLE " + TABLE_EXERCISE_RATINGS + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_EXERCISE_ID + " INTEGER, " +
            COLUMN_USER_ID + " INTEGER, " +
            COLUMN_RATING + " REAL, " +
            COLUMN_TIMESTAMP + " INTEGER, " +
            "FOREIGN KEY(" + COLUMN_EXERCISE_ID + ") REFERENCES " + TABLE_EXERCISES + "(" + COLUMN_ID + "), " +
            "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";

    // Create profiles table query
    private static final String CREATE_TABLE_PROFILES = "CREATE TABLE " + TABLE_PROFILES + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USER_ID + " INTEGER, " +
            COLUMN_NAME + " TEXT, " +
            COLUMN_DATE_OF_BIRTH + " TEXT, " +
            COLUMN_COUNTRY + " TEXT, " +
            COLUMN_GENDER + " TEXT, " +
            COLUMN_PHONE + " TEXT, " +
            COLUMN_BIO + " TEXT," +
            COLUMN_DAILY_TARGET + " INTEGER, " +
            COLUMN_WEEKLY_TARGET + " INTEGER, " +
            "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // Log the database path
        String databasePath = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
        Log.d(TAG, "Database path: " + databasePath);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_EXERCISES);
        db.execSQL(CREATE_TABLE_EXERCISE_RATINGS);
        db.execSQL(CREATE_TABLE_PROFILES);

        Log.d(TAG, "Tables created successfully.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISE_RATINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILES);
        // Create tables again
        onCreate(db);
    }

    // User operations

    public long insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Log the user information being passed
        Log.d(TAG, "Inserting user: Username=" + username + ", Password=" + password);

        // Check if the username already exists
        if (userExists(username)) {
            // Handle duplication (e.g., reject insertion or update existing record)
            return -1; // Return -1 to indicate failure
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        long userId = db.insert(TABLE_USERS, null, values);

        // Log the user ID, username, and password
        Log.d(TAG, "User registered successfully - ID: " + userId + ", Username: " + username + ", Password: " + password);

        // Create a profile for the user
        if (userId != -1) {
            createProfile(userId);
        }
        return userId;
    }

    private void createProfile(long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues profileValues = new ContentValues();
        profileValues.put(COLUMN_USER_ID, userId);
        // You can add default values or leave them null for other fields
        // For example:
        profileValues.put(COLUMN_NAME, ""); // Default name
        profileValues.put(COLUMN_DATE_OF_BIRTH, ""); // Default date of birth
        profileValues.put(COLUMN_COUNTRY, ""); // Default country
        profileValues.put(COLUMN_GENDER, ""); // Default gender
        profileValues.put(COLUMN_PHONE, ""); // Default phone number
        profileValues.put(COLUMN_BIO, ""); // Default bio
        profileValues.put(COLUMN_DAILY_TARGET, 0); // Default daily target
        profileValues.put(COLUMN_WEEKLY_TARGET, 0); // Default weekly target
        long newProfileId = db.insert(TABLE_PROFILES, null, profileValues);

        // Log the creation of the profile
        if (newProfileId != -1) {
            Log.d(TAG, "Profile created successfully for userId " + userId + " - ID: " + newProfileId);
        } else {
            Log.e(TAG, "Failed to create profile for userId " + userId);
        }
    }

    public boolean userExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }


    public boolean isValidLogin(String username, String password) {
        Log.d(TAG, "Validating login: Username=" + username + ", Password=" + password);

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS +
                " WHERE " + COLUMN_USERNAME + " = ?" +
                " AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});
        int count = cursor.getCount();
        cursor.close();

        // Log the login attempt
        if (count > 0) {
            Log.d(TAG, "Login successful - Username: " + username + ", Password: " + password);
        } else {
            Log.d(TAG, "Login failed - Username: " + username + ", Password: " + password);
        }

        return count > 0;
    }



    public long insertProfile(long userId, String name, String dateOfBirth, String country, String gender, String phoneNumber, String bio, int dailyTarget, int weeklyTarget) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if the provided user ID exists in the profiles table
        if (!profileExists(userId)) {
            // Profile doesn't exist, create it
            ContentValues profileValues = new ContentValues();
            profileValues.put(COLUMN_USER_ID, userId);
            // You can add default values or leave them null for other fields
            // For example:
            profileValues.put(COLUMN_NAME, ""); // Default name
            profileValues.put(COLUMN_DATE_OF_BIRTH, ""); // Default date of birth
            profileValues.put(COLUMN_COUNTRY, ""); // Default country
            profileValues.put(COLUMN_GENDER, ""); // Default gender
            profileValues.put(COLUMN_PHONE, ""); // Default phone number
            profileValues.put(COLUMN_BIO, ""); // Default bio
            profileValues.put(COLUMN_DAILY_TARGET, dailyTarget); // Default daily target
            profileValues.put(COLUMN_WEEKLY_TARGET, weeklyTarget); // Default weekly target
            long newProfileId = db.insert(TABLE_PROFILES, null, profileValues);

            // Log the creation of the profile
            if (newProfileId != -1) {
                Log.d(TAG, "Profile created successfully for userId " + userId + " - ID: " + newProfileId);
            } else {
                Log.e(TAG, "Failed to create profile for userId " + userId);
                db.close();
                return -1; // Return -1 to indicate failure
            }
        }

        // Now that the profile exists (or was created), proceed with inserting the profile data
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_DATE_OF_BIRTH, dateOfBirth);
        values.put(COLUMN_COUNTRY, country);
        values.put(COLUMN_GENDER, gender);
        values.put(COLUMN_PHONE, phoneNumber);
        values.put(COLUMN_BIO, bio); // Insert bio
        values.put(COLUMN_DAILY_TARGET, dailyTarget); // Insert daily target
        values.put(COLUMN_WEEKLY_TARGET, weeklyTarget); // Insert weekly target
        long profileId = db.insert(TABLE_PROFILES, null, values);

        // Check if the insertion was successful
        if (profileId != -1) {
            Log.d(TAG, "Profile inserted successfully - ID: " + profileId);
        } else {
            Log.e(TAG, "Failed to insert profile.");
        }

        return profileId;
    }


    public Cursor getProfile(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_PROFILES +
                " WHERE " + COLUMN_USER_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    // Add methods to update daily and weekly targets for a user
    public boolean updateDailyTarget(long userId, int dailyTarget) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (!profileExists(userId)) {
            Log.e(TAG, "Profile does not exist for user ID: " + userId);
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_DAILY_TARGET, dailyTarget);
        String selection = COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        Log.d(TAG, "Updating daily target for user ID: " + userId);
        return db.update(TABLE_PROFILES, values, selection, selectionArgs) > 0;
    }
    public boolean updateWeeklyTarget(long userId, int weeklyTarget) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (!profileExists(userId)) {
            Log.e(TAG, "Profile does not exist for user ID: " + userId);
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(COLUMN_WEEKLY_TARGET, weeklyTarget);
        String selection = COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        Log.d(TAG, "Updating weekly target for user ID: " + userId);
        return db.update(TABLE_PROFILES, values, selection, selectionArgs) > 0;
    }


    public boolean updateProfile(long userId, String name, String dateOfBirth, String country, String gender, String phoneNumber, String bio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_DATE_OF_BIRTH, dateOfBirth);
        values.put(COLUMN_COUNTRY, country);
        values.put(COLUMN_GENDER, gender);
        values.put(COLUMN_PHONE, phoneNumber);
        values.put(COLUMN_BIO, bio); // Update bio
        Log.d(TAG, "Updating profile for userId=" + userId + ": Name=" + name + ", DateOfBirth=" + dateOfBirth + ", Country=" + country + ", Gender=" + gender + ", PhoneNumber=" + phoneNumber + ", Bio=" + bio);


        // Define the WHERE clause to update the profile for the given user ID
        String selection = COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(TABLE_PROFILES, null, selection, selectionArgs, null, null, null);
        boolean profileExists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }

        if (!profileExists) {
            Log.e(TAG, "updateProfile: Profile with userId " + userId + " does not exist.");
            db.close();
            return false;
        }

        try {
            // Perform the update operation
            int rowsAffected = db.update(TABLE_PROFILES, values, selection, selectionArgs);

            // Check if the update was successful
            boolean success = rowsAffected > 0;

            if (success) {
                Log.d(TAG, "Updating profile for user ID: " + userId);
                Log.d(TAG, "New profile values: Name=" + name + ", Date of Birth=" + dateOfBirth + ", Country=" + country + ", Gender=" + gender + ", Phone Number=" + phoneNumber + ", Bio=" + bio);
                Log.d(TAG, "updateProfile: Profile updated successfully");
            } else {
                Log.e(TAG, "updateProfile: Failed to update profile. Rows affected: " + rowsAffected);
            }

            return success;
        } catch (Exception e) {
            Log.e(TAG, "updateProfile: Exception occurred while updating profile for user ID " + userId, e);
            return false;
        } finally {
            // Close the database connection
            db.close();
        }
    }

    public boolean profileExists(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_PROFILES + " WHERE " + COLUMN_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        int count = cursor.getCount();
        cursor.close();
        boolean exists = count > 0;
        Log.d(TAG, "Profile with userId " + userId + " exists: " + exists);
        return exists;
    }
    public static String getColumnBio() {
        return COLUMN_BIO;
    }

    public static String getColumnName() {
        return COLUMN_NAME;
    }

    public static String getColumnDateOfBirth() {
        return COLUMN_DATE_OF_BIRTH;
    }

    public static String getColumnCountry() {
        return COLUMN_COUNTRY;
    }
    public long getUserIdFromAuthenticationSystem(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        long userId = -1;

        // Define the columns you want to retrieve from the database
        String[] projection = {COLUMN_ID};

        // Define the selection criteria
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        // Query the database to find the user with the given username and password
        Cursor cursor = db.query(
                TABLE_USERS,          // The table to query
                projection,           // The columns to return
                selection,            // The columns for the WHERE clause
                selectionArgs,        // The values for the WHERE clause
                null,                 // Don't group the rows
                null,                 // Don't filter by row groups
                null                  // The sort order
        );

        // If a user is found, get their ID
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(COLUMN_ID);
            if (columnIndex != -1) {
                userId = cursor.getLong(columnIndex);
            }
            cursor.close();
        }

        // Return the user ID
        return userId;
    }

    // Exercise operations

    public long insertExercise(String name, String image, long duration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXERCISE_NAME, name);
        values.put(COLUMN_EXERCISE_IMAGE, image);
        values.put(COLUMN_EXERCISE_DURATION, duration);
        return db.insert(TABLE_EXERCISES, null, values);
    }

    // Exercise ratings operations

    public long insertExerciseRating(long exerciseId, long userId, float rating) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXERCISE_ID, exerciseId);
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_RATING, rating);
        return db.insert(TABLE_EXERCISE_RATINGS, null, values);
    }

    // Reset exercise ratings and progress tracking
    public void resetDailyData() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Get the timestamp for the start of the current day
        long startOfDayTimestamp = calculateStartOfDay(System.currentTimeMillis());
        // Delete exercise ratings for the current day
        db.delete(TABLE_EXERCISE_RATINGS, COLUMN_TIMESTAMP + " >= ?", new String[]{String.valueOf(startOfDayTimestamp)});
    }

    // Method to reset data every day at 12:00 AM
    public void resetDataAtMidnight() {
        // Get current time
        Calendar calendar = Calendar.getInstance();
        long currentTime = calendar.getTimeInMillis();
        // Calculate time until midnight
        long timeUntilMidnight = calculateTimeUntilMidnight(currentTime);
        // Schedule reset task at midnight
        scheduleResetTask(timeUntilMidnight);
    }

    // Calculate time until midnight
    private long calculateTimeUntilMidnight(long currentTime) {
        // Get calendar instance and set time to midnight
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        // Calculate time until midnight
        return calendar.getTimeInMillis() + (24 * 60 * 60 * 1000) - currentTime;
    }

    // Schedule reset task at midnight
    private void scheduleResetTask(long delayMillis) {
        // Create a runnable task to reset data
        Runnable resetTask = new Runnable() {
            @Override
            public void run() {
                resetDailyData(); // Reset daily data at midnight
                resetDataAtMidnight(); // Schedule next reset task
            }
        };
        // Execute reset task after delay
        new android.os.Handler().postDelayed(resetTask, delayMillis);
    }

    // Calculate the start of the day for a given timestamp
    private long calculateStartOfDay(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public void resetProgressForDay(long startOfDayInMillis, long endOfDayInMillis) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Perform the reset operation for the current day, e.g., delete or update records within the specified time range
        // For example:
        db.delete(TABLE_EXERCISE_RATINGS, COLUMN_TIMESTAMP + " >= ? AND " + COLUMN_TIMESTAMP + " <= ?", new String[]{String.valueOf(startOfDayInMillis), String.valueOf(endOfDayInMillis)});
        db.close();
    }



}
