package com.example.noteapp.models;

public class Note {

    private String id;
    private String title;
    private String content;
    private String label;
    private String color;
    private long reminderTime; // Thời gian nhắc ghi chú (milliseconds)
    private String userId;

    public Note() {} // Required for Firebase

    public Note(String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.label = label;
        this.color = color;
        this.reminderTime = reminderTime;
        this.userId = userId;
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
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public long getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(long reminderTime) {
        this.reminderTime = reminderTime;
    }
    private boolean isPrivate;


    public Note(String title, String content, String label, String color, long reminderTime, boolean isPrivate) {
        this.title = title;
        this.content = content;
        this.label = label;
        this.color = color;
        this.reminderTime = reminderTime;
        this.isPrivate = isPrivate;
    }

    // Getter Setter
    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }

}
