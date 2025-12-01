package com.example.taskmanager.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.taskmanager.data.local.entities.Task;
import com.example.taskmanager.repository.TaskRepository;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {

    private final TaskRepository repository;
    private final LiveData<List<Task>> pendingTasks;
    private final LiveData<List<Task>> completedTasks;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application.getApplicationContext());
        pendingTasks = repository.getPendingTasks();
        completedTasks = repository.getCompletedTasks();
    }

    public LiveData<List<Task>> getPendingTasks() { return pendingTasks; }
    public LiveData<List<Task>> getCompletedTasks() { return completedTasks; }

    public void insert(Task task) { repository.insert(task); }
    public void update(Task task) { repository.update(task); }
    public void delete(Task task) { repository.delete(task); }
    public Task getTaskByIdSync(int id) {
        List<Task> allTasks = pendingTasks.getValue();
        if (allTasks != null) {
            for (Task t : allTasks) {
                if (t.getId() == id) return t;
            }
        }
        return null; // handle null case
    }

}
