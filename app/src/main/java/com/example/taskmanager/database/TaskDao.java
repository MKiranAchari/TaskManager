package com.example.taskmanager.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.taskmanager.models.Task;
import java.util.List;

@Dao
public interface TaskDao {

    // return generated id when inserting
    @Insert
    long insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY id DESC")
    List<Task> getPendingTasks();

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY id DESC")
    List<Task> getCompletedTasks();
}
