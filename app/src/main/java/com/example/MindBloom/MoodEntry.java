package com.example.MindBloom;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * MoodEntry model class representing a recorded mood check-in.
 * Follows Java POJO design compatible with Firebase Realtime Database (Lab Manual Week 11).
 */
public class MoodEntry {
    private String id;
    private String mood;
    private int intensity;
    private String note;
    private long timestamp;

    // Required default no-argument constructor for Firebase serialization
    public MoodEntry() {
    }

    public MoodEntry(String id, String mood, int intensity, String note, long timestamp) {
        this.id = id;
        this.mood = mood;
        this.intensity = intensity;
        this.note = note;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public int getIntensity() {
        return intensity;
    }

    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Helper method to format numeric timestamp into human-readable date and time.
     */
    public String getFormattedDate() {
        if (timestamp <= 0) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
