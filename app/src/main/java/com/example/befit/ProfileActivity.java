package com.example.befit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private EditText nameEditText, dateOfBirthEditText, countryEditText, genderEditText, phoneNumberEditText, bioEditText;
    private Button saveButton, editProfileButton;
    private ImageButton linkedinButton;
    private DatabaseHelper databaseHelper;
    private long userId;
    private boolean editMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profilesection);

        // Retrieve userId from SharedPreferences
        userId = getUserId();
        Log.d(TAG, "UserId: " + userId);

        initializeViews();
        databaseHelper = new DatabaseHelper(this);
        loadProfileData(userId);

        // Initialize bottom navigation view
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.navigation_profile) {
                Toast.makeText(ProfileActivity.this, "You are already at the Profile page", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

//        bioEditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (editMode) {
//                    saveProfileData();
//                }
//            }
//        });

        linkedinButton.setOnClickListener(v -> openLinkedInProfile());
        saveButton.setOnClickListener(v -> onSaveProfileButtonClick());
    }

    private void initializeViews() {
        nameEditText = findViewById(R.id.nameEditText);
        dateOfBirthEditText = findViewById(R.id.dateOfBirthEditText);
        countryEditText = findViewById(R.id.countryEditText);
        genderEditText = findViewById(R.id.genderEditText);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        bioEditText = findViewById(R.id.bioEditText);
        saveButton = findViewById(R.id.saveButton);
        editProfileButton = findViewById(R.id.editprofileButton);
        linkedinButton = findViewById(R.id.linkedinButton);

        // Initially disable the input fields
        setFieldsEnabled(false);

        // Initially hide the save button
        saveButton.setVisibility(View.GONE);

        // Set OnClickListener for the edit profile button
        editProfileButton.setOnClickListener(v -> toggleEditMode(!editMode));
    }

    private void loadProfileData(long userId) {
        Cursor cursor = databaseHelper.getProfile(userId);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME);
            int dateOfBirthIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE_OF_BIRTH);
            int countryIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_COUNTRY);
            int genderIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_GENDER);
            int phoneNumberIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PHONE);
            int bioIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BIO);

            nameEditText.setText(cursor.getString(nameIndex));
            dateOfBirthEditText.setText(cursor.getString(dateOfBirthIndex));
            countryEditText.setText(cursor.getString(countryIndex));
            genderEditText.setText(cursor.getString(genderIndex));
            phoneNumberEditText.setText(cursor.getString(phoneNumberIndex));
            bioEditText.setText(cursor.getString(bioIndex));
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    private long getUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return sharedPreferences.getLong("user_id", -1);
    }

    private void saveProfileData() {
        String name = nameEditText.getText().toString();
        String dateOfBirth = dateOfBirthEditText.getText().toString();
        String gender = genderEditText.getText().toString();
        String phoneNumber = phoneNumberEditText.getText().toString();
        String country = countryEditText.getText().toString();
        String bio = bioEditText.getText().toString();

        // Check if the profile exists before updating
        Cursor cursor = databaseHelper.getProfile(userId);
        if (cursor != null && cursor.moveToFirst()) {
            // Profile exists, proceed with the update
            if (databaseHelper.updateProfile(userId, name, dateOfBirth, gender, phoneNumber, country, bio)) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to update profile data");
            }
        } else {
            // Profile does not exist, inform the user
            Toast.makeText(this, "Profile does not exist", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Profile with user ID " + userId + " does not exist");
        }
        // Close the cursor
        if (cursor != null) {
            cursor.close();
        }
    }


    private void toggleEditMode(boolean enable) {
        setFieldsEnabled(enable);
        editMode = enable;

        // Show or hide the save button based on the edit mode
        saveButton.setVisibility(enable ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(enable);
    }

    private void setFieldsEnabled(boolean enabled) {
        nameEditText.setEnabled(enabled);
        dateOfBirthEditText.setEnabled(enabled);
        countryEditText.setEnabled(enabled);
        genderEditText.setEnabled(enabled);
        phoneNumberEditText.setEnabled(enabled);
        bioEditText.setEnabled(enabled);
    }

    // Inside your onSaveProfileButtonClick method

    private void onSaveProfileButtonClick() {
        String name = nameEditText.getText().toString();
        String dateOfBirth = dateOfBirthEditText.getText().toString();
        String country = countryEditText.getText().toString();
        String gender = genderEditText.getText().toString();
        String phoneNumber = phoneNumberEditText.getText().toString();
        String bio = bioEditText.getText().toString();

        // Check if the profile exists before deciding whether to insert or update
        if (databaseHelper.userExists(String.valueOf(userId))) { // Using the public userExists method
            // Profile exists, update it
            if (databaseHelper.updateProfile(userId, name, dateOfBirth, country, gender, phoneNumber, bio)) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to update profile data");
            }
        } else {
            // Profile does not exist, insert it
            long profileInsertResult = databaseHelper.insertProfile(userId, name, dateOfBirth, country, gender, phoneNumber, bio,0, 0);
            if (profileInsertResult != -1) {
                Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show();
            }
        }
    }





    private void openLinkedInProfile() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/eerav-koirala/"));
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload profile data to ensure it's up to date
        loadProfileData(userId);
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveProfileData(); // Call method to save profile data when the activity is stopped
    }
}
