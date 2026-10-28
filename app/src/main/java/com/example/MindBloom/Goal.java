package com.example.MindBloom;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Goal model class representing a user's personal wellness goal.
 * Follows Java POJO design compatible with Firebase Realtime Database (Lab Manual Week 11).
 */
public class Goal {
    private String id;
    private String title;
    private String description;
    private long targetDate;
    private int progress;
    private long createdAt;
    private long updatedAt;

    // Required public no-argument constructor for Firebase serialization
    public Goal() {
    }

    public Goal(String id, String title, String description, long targetDate, int progress, long createdAt, long updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.targetDate = targetDate;
        this.progress = progress;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(long targetDate) {
        this.targetDate = targetDate;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Helper to check if goal is 100% completed.
     */
    public boolean isCompleted() {
        return progress >= 100;
    }

    /**
     * Formats target date as human-readable string (e.g., "15 Nov 2026").
     */
    public String getTargetDateFormatted() {
        if (targetDate <= 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(new Date(targetDate));
    }

    /**
     * Formats creation timestamp.
     */
    public String getCreatedAtFormatted() {
        if (createdAt <= 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(createdAt));
    }

    /**
     * Formats last updated timestamp.
     */
    public String getUpdatedAtFormatted() {
        if (updatedAt <= 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(updatedAt));
    }
}
