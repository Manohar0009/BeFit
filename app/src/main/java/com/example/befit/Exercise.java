package com.example.befit;

import android.app.Activity;

public class Exercise extends Activity {
    private String name;
    private int imageResource;
    private long durationMillis;

    // Default constructor
    public Exercise() {
        // Default values can be set here if needed
    }

    // Parameterized constructor
    public Exercise(String name, int imageResource, long durationMillis) {
        this.name = name;
        this.imageResource = imageResource;
        this.durationMillis = durationMillis;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
    }
}
