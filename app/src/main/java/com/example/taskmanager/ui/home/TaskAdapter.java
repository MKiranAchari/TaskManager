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
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.data.local.entities.Task;

public class TaskAdapter extends ListAdapter<Task, TaskAdapter.VH> {

    public interface TaskActionListener {
        void onDelete(Task task);
        void onComplete(Task task);
    }

    private final boolean isHistory;
    private final TaskActionListener listener;

    public TaskAdapter(boolean isHistory, TaskActionListener listener) {
        super(DIFF);
        this.isHistory = isHistory;
        this.listener = listener;
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
            showCustomDialog(v.getContext(), t);
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

    private void showCustomDialog(Context context, Task t) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog);

        // Optional: remove default title bar
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Dismiss when tapping outside
        dialog.setCancelable(true);

        // Get views
        TextView tvTitle = dialog.findViewById(R.id.dialog_Title);
        Button btnEdit = dialog.findViewById(R.id.btnEdit);
        Button btnDelete = dialog.findViewById(R.id.btnDelete);
        TextView tvDesc = dialog.findViewById(R.id.dialog_Desc);
        TextView tvTime = dialog.findViewById(R.id.dialog_Time);

        tvTitle.setText(t.getTitle());
        tvDesc.setText(t.getDescription());
        tvTime.setText(t.getDateTime());

        btnEdit.setOnClickListener(v -> {
            Toast.makeText(context, "Edit clicked", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            Toast.makeText(context, "Delete clicked", Toast.LENGTH_SHORT).show();
            if (listener != null) listener.onDelete(t);
            dialog.dismiss();
        });

        dialog.show();
    }

}
