package com.example.noteapp.models;

public class Note {

    private String id;
    private String title;
    private String content;
    private boolean completed;
    private String imageUrl;

    // Constructor rỗng (Firebase yêu cầu)
    public Note() {
    }

    // Constructor tạo ghi chú không có ảnh
    public Note(String title, String content) {
        this.title = title;
        this.content = content;
        this.completed = false;
        this.imageUrl = null;
    }

    // Constructor có ảnh
    public Note(String title, String content, String imageUrl) {
        this.title = title;
        this.content = content;
        this.completed = false;
        this.imageUrl = imageUrl;
    }

    // Getter & Setter cho ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter & Setter cho Title
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Getter & Setter cho Content
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // Getter & Setter cho Completed
    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    // Getter & Setter cho ImageUrl
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
