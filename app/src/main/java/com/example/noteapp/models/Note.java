package com.example.noteapp.models;

public class Note {

    private String id;
    private String title;
    private String content;

    public Note() {} // Required for Firebase

    public Note(String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
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

    public String getContent() {
        return content;
    }
}
