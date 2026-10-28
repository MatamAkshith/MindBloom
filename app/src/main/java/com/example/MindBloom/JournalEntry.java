package com.example.MindBloom;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * JournalEntry model class representing a user's reflective journal entry.
 * Follows Java POJO design compatible with Firebase Realtime Database (Lab Manual Week 11).
 */
public class JournalEntry {
    private String id;
    private String title;
    private String content;
    private String prompt;
    private long timestamp;

    // Required public default no-argument constructor for Firebase serialization
    public JournalEntry() {
    }

    public JournalEntry(String id, String title, String content, String prompt, long timestamp) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.prompt = prompt;
        this.timestamp = timestamp;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Helper method to format numeric timestamp into a human-readable date and time.
     */
    public String getFormattedDate() {
        if (timestamp <= 0) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Returns the title or a fallback if the title is empty.
     */
    public String getDisplayTitle() {
        if (title != null && !title.trim().isEmpty()) {
            return title.trim();
        }
        return "Untitled Reflection";
    }
}
