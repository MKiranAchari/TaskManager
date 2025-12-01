package com.example.taskmanager.ui.home;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.data.local.entities.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class TaskAdapter extends ListAdapter<Task, TaskAdapter.VH> {

    public interface TaskActionListener {
        void onDelete(Task task);
        void onComplete(Task task);
        void onEditClick(Task task);
    }

    private final boolean isHistory;
    private final TaskActionListener listener;
    private final FragmentManager fragmentManager;
    public TaskAdapter(boolean isHistory, TaskActionListener listener, FragmentManager fragmentManager) {
        super(DIFF);
        this.isHistory = isHistory;
        this.listener = listener;
        this.fragmentManager = fragmentManager;
    }

    private static final DiffUtil.ItemCallback<Task> DIFF = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getId() == newItem.getId();
        }
        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getDateTime().equals(newItem.getDateTime())
                    && oldItem.isCompleted() == newItem.isCompleted();
        }
    };

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Task t = getItem(position);
//        holder.id.setText("#" + t.getId());
        holder.title.setText(t.getTitle());
        holder.time.setText(t.getDateTime());

        holder.complete.setVisibility(isHistory ? View.GONE : View.VISIBLE);

        holder.complete.setOnClickListener(v -> {
            if (listener != null) listener.onComplete(t);
        });

        holder.delete.setVisibility(isHistory ? View.VISIBLE : View.GONE);
        holder.delete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(t);
        });

        holder.itemView.setOnLongClickListener(v -> {
            EditTaskDialogFragment dialog = EditTaskDialogFragment.newInstance(t.getId());
            dialog.setListener(new EditTaskDialogFragment.EditTaskListener() {
                @Override
                public void onTaskUpdated(Task updatedTask) {
                    notifyItemChanged(holder.getAdapterPosition());
                }

                @Override
                public void onTaskDeleted(Task deletedTask) {
                    notifyItemRemoved(holder.getAdapterPosition());
                }
            });

            // Pass FragmentManager from Activity/Fragment
            dialog.show(fragmentManager, "edit_task");
            return true;
        });


    }

    static class VH extends RecyclerView.ViewHolder {
        TextView id, title, time;
        ImageButton delete, complete;

        VH(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.taskId);
            title = itemView.findViewById(R.id.taskTitle);
            time = itemView.findViewById(R.id.taskTime);
            complete = itemView.findViewById(R.id.btnComplete);
            delete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private void showCustomDialog(Context context, Task task) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog);

        // Remove default title bar
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Dismiss when tapping outside
        dialog.setCancelable(true);

        // Views
        TextView tvTitle = dialog.findViewById(R.id.dialog_Title);
        TextView tvDesc = dialog.findViewById(R.id.dialog_Desc);
        TextView tvTime = dialog.findViewById(R.id.dialog_Time);
        Button btnEdit = dialog.findViewById(R.id.btnEdit);
        Button btnDelete = dialog.findViewById(R.id.btnDelete);
        TextInputLayout editLayout = dialog.findViewById(R.id.dialog_Desc_edit);
        TextInputEditText editText = dialog.findViewById(R.id.dialog_edit_task);

        // Initialize UI
        tvTitle.setText(task.getTitle());
        tvDesc.setText(task.getDescription());
        tvTime.setText(task.getDateTime());
        editText.setText(task.getDescription());
        editLayout.setVisibility(View.GONE); // hide edit text initially

        // Edit button logic
        btnEdit.setOnClickListener(v -> {
            if (editLayout.getVisibility() == View.GONE) {
                // First click → enable editing
                tvDesc.setVisibility(View.GONE);
                editLayout.setVisibility(View.VISIBLE);
                btnEdit.setText("Save");
            } else {
                // Second click → save changes
                String newDesc = editText.getText().toString().trim();
                if (!newDesc.isEmpty()) {
                    task.setDescription(newDesc);

                    // Use listener to update task via ViewModel in Fragment
                    if (listener != null) listener.onEditClick(task);

                    tvDesc.setText(newDesc);
                    tvDesc.setVisibility(View.VISIBLE);
                    editLayout.setVisibility(View.GONE);
                    btnEdit.setText("Edit");

                    Toast.makeText(context, "Task updated", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    editText.setError("Description cannot be empty");
                }
            }
        });

        // Delete button
        btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(task);
            Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }


}
