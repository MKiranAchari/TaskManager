package com.example.taskmanager.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.MainActivity;
import com.example.taskmanager.R;
import com.example.taskmanager.database.AppDatabase;
import com.example.taskmanager.models.Task;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private ArrayList<Task> taskList;
    private Context context;
    private boolean showCompleteButton;

    public TaskAdapter(ArrayList<Task> taskList, boolean showCompleteButton) {
        this.taskList = taskList;
        this.showCompleteButton = showCompleteButton;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskId.setText("#" + task.getId());
        holder.taskTitle.setText(task.getTitle());
        holder.taskTime.setText(task.getEndTime());

        // show/hide complete button based on where adapter is used
        holder.btnComplete.setVisibility(showCompleteButton ? View.VISIBLE : View.GONE);

        holder.btnDelete.setOnClickListener(v -> showDeleteDialog(task, position));
        holder.btnComplete.setOnClickListener(v -> showCompleteDialog(task, position));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    private void showDeleteDialog(Task task, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to permanently delete \"" + task.getTitle() + "\"?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    AppDatabase db = AppDatabase.getInstance(context);

                    // Remove from list immediately
                    if (!taskList.isEmpty()){
                        taskList.remove(position);
                        notifyItemRemoved(position);
                    }

                    // Delete in background
                    Executors.newSingleThreadExecutor().execute(() -> db.taskDao().deleteTask(task));

                    // Undo Snackbar
                    Snackbar.make(((MainActivity) context).findViewById(android.R.id.content),
                                    "Task deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO", v -> Executors.newSingleThreadExecutor().execute(() -> {
                                db.taskDao().insertTask(task);
                                ((MainActivity) context).runOnUiThread(() -> {
                                    taskList.add(position, task);
                                    notifyItemInserted(position);
                                });
                            }))
                            .show();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }


    private void showCompleteDialog(Task task, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Mark as Completed")
                .setMessage("Mark \"" + task.getTitle() + "\" as completed and move to History?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    task.setCompleted(true);
                    Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase db = AppDatabase.getInstance(context);
                        db.taskDao().updateTask(task);

                        ((MainActivity) context).runOnUiThread(() -> {
                            taskList.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Task moved to History", Toast.LENGTH_SHORT).show();
                        });
                    });
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskId, taskTitle, taskTime;
        ImageButton btnDelete, btnComplete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskId = itemView.findViewById(R.id.taskId);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskTime = itemView.findViewById(R.id.taskTime);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnComplete = itemView.findViewById(R.id.btnComplete);
        }
    }
}
