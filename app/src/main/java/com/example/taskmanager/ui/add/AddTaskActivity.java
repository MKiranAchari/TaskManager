package com.example.taskmanager.ui.add;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.taskmanager.R;
import com.example.taskmanager.data.local.entities.Task;
import com.example.taskmanager.viewmodel.TaskViewModel;

import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity {

    EditText editTitle, editDesc, editDateTime;
    Button btnSave;
    String finalDateTime = "";

    private TaskViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        editTitle = findViewById(R.id.editTitle);
        editDesc = findViewById(R.id.editDesc);
        editDateTime = findViewById(R.id.editDateTime);
        btnSave = findViewById(R.id.btnSaveTask);

        vm = new ViewModelProvider(this).get(TaskViewModel.class);

        editDateTime.setOnClickListener(v -> pickDateTime());
        btnSave.setOnClickListener(v -> saveTask());
    }

    private void saveTask() {
        String title = editTitle.getText().toString().trim();
        String desc = editDesc.getText().toString().trim();
        if (title.isEmpty() || desc.isEmpty() || finalDateTime.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        Task t = new Task(title, desc, finalDateTime, false);
        vm.insert(t);
        Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void pickDateTime() {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    TimePickerDialog tp = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                finalDateTime = String.format("%02d-%02d-%04d %02d:%02d",
                                        dayOfMonth, month + 1, year, hourOfDay, minute);
                                editDateTime.setText(finalDateTime);
                            },
                            c.get(Calendar.HOUR_OF_DAY),
                            c.get(Calendar.MINUTE),
                            true);
                    tp.show();
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));
        dp.show();
    }
}
