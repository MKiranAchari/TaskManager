package com.example.taskmanager.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.taskmanager.models.Task;

@Database(entities = {Task.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract TaskDao taskDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "task_db"
                            )
                            .allowMainThreadQueries() // ok for learning; use background threads in prod
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
