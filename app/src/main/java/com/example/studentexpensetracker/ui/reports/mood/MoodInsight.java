package com.example.studentexpensetracker.ui.reports.mood;

public class MoodInsight {
    private final String title;
    private final String description;
    private final String emoji;

    public MoodInsight(String title, String description, String emoji) {
        this.title = title;
        this.description = description;
        this.emoji = emoji != null ? emoji : "💡";
    }

    public MoodInsight(String title, String description) {
        this(title, description, "💡");
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getEmoji() {
        return emoji;
    }
}
