package com.example.taskmanager;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.taskmanager.database.AppDatabase;
import com.example.taskmanager.models.Task;

public class AddTaskActivity extends AppCompatActivity {

    EditText editTitle, editDesc, editDateTime;
    Button btnSaveTask;
    String finalDateTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        editTitle = findViewById(R.id.editTitle);
        editDesc = findViewById(R.id.editDesc);
        editDateTime = findViewById(R.id.editDateTime);
        btnSaveTask = findViewById(R.id.btnSaveTask);

        // Pick date & time
        editDateTime.setOnClickListener(v -> pickDateTime());

        // Save task (we’ll just show a Toast for now)
        btnSaveTask.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String desc = editDesc.getText().toString().trim();

            if (title.isEmpty() || desc.isEmpty() || finalDateTime.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                AppDatabase db = AppDatabase.getInstance(this);
                Task task = new Task(title, desc, finalDateTime, false);
                long newId = db.taskDao().insertTask(task);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Task Saved: " + title, Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });
    }

    private void pickDateTime() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePicker = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                finalDateTime = String.format("%02d-%02d-%04d %02d:%02d",
                                        dayOfMonth, month + 1, year, hourOfDay, minute);
                                editDateTime.setText(finalDateTime);
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true);
                    timePicker.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePicker.show();
    }
}
