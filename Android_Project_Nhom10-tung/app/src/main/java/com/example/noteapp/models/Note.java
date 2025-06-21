package com.example.noteapp.models;

public class Note {
    private String id;
    private String title;
    private String content;
    private String label; // nhãn
    private String color; // màu (hex string, vd: #FFCDD2)

    public Note() {} // Constructor rỗng Firestore cần

    public Note(String title, String content, String label, String color) {
        this.title = title;
        this.content = content;
        this.label = label;
        this.color = color;
    }

    // Getter, setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getLabel() { return label; }
    public String getColor() { return color; }

    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setLabel(String label) { this.label = label; }
    public void setColor(String color) { this.color = color; }
}

