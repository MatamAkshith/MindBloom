package com.example.MindBloom;

/**
 * RelaxationItem model class representing a structured, beginner-friendly relaxation practice.
 */
public class RelaxationItem {
    private final String id;
    private final String title;
    private final String duration;
    private final String icon;
    private final String summary;
    private final String instructions;

    public RelaxationItem(String id, String title, String duration, String icon, String summary, String instructions) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.icon = icon;
        this.summary = summary;
        this.instructions = instructions;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDuration() {
        return duration;
    }

    public String getIcon() {
        return icon;
    }

    public String getSummary() {
        return summary;
    }

    public String getInstructions() {
        return instructions;
    }
}
