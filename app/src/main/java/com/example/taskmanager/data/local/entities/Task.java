package com.example.taskmanager.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String description;
    private String dateTime;
    private boolean completed;
    private String priority;

    public Task(String title, String description, String dateTime, boolean completed, String priority) {
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
        this.completed = completed;
        this.priority = priority;
    }

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDateTime() { return dateTime; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public String getPriority() { return priority; }
}
