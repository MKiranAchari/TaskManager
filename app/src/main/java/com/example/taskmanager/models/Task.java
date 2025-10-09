package com.example.taskmanager.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String description;
    private String endTime;
    private boolean isCompleted;

    public Task(String title, String description, String endTime, boolean isCompleted) {
        this.title = title;
        this.description = description;
        this.endTime = endTime;
        this.isCompleted = isCompleted;
    }

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
