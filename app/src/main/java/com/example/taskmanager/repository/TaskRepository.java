package com.example.taskmanager.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.taskmanager.data.local.database.AppDatabase;
import com.example.taskmanager.data.local.dao.TaskDao;
import com.example.taskmanager.data.local.entities.Task;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {

    private final TaskDao taskDao;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    public TaskRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        taskDao = db.taskDao();
    }

    public LiveData<List<Task>> getPendingTasks() {
        return taskDao.getPendingTasks();
    }

    public LiveData<List<Task>> getCompletedTasks() {
        return taskDao.getCompletedTasks();
    }

    public void insert(Task task) {
        ioExecutor.execute(() -> taskDao.insertTask(task));
    }

    public void update(Task task) {
        ioExecutor.execute(() -> taskDao.updateTask(task));
    }

    public void delete(Task task) {
        ioExecutor.execute(() -> taskDao.deleteTask(task));
    }
}
