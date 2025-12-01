package com.example.taskmanager.ui.home;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.taskmanager.R;
import com.example.taskmanager.data.local.entities.Task;
import com.example.taskmanager.viewmodel.TaskViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class EditTaskDialogFragment extends DialogFragment {

    private static final String ARG_TASK_ID = "task_id";

    public interface EditTaskListener {
        void onTaskUpdated(Task updatedTask);
        void onTaskDeleted(Task deletedTask);
    }

    private int taskId;
    private Task task;
    private EditTaskListener listener;
    private TaskViewModel viewModel;

    private TextView tvTitle, tvDesc;
    private TextInputLayout editLayout;
    private TextInputEditText editText;
    private Button btnEdit, btnDelete;

    public static EditTaskDialogFragment newInstance(int taskId) {
        EditTaskDialogFragment fragment = new EditTaskDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TASK_ID, taskId);
        fragment.setArguments(args);
        return fragment;
    }

    public void setListener(EditTaskListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);

        if (getArguments() != null) {
            taskId = getArguments().getInt(ARG_TASK_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        task = viewModel.getTaskByIdSync(taskId); // Add a synchronous helper in ViewModel or Repository

        tvTitle = view.findViewById(R.id.dialog_Title);
        tvDesc = view.findViewById(R.id.dialog_Desc);
        editLayout = view.findViewById(R.id.dialog_Desc_edit);
        editText = view.findViewById(R.id.dialog_edit_task);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnDelete = view.findViewById(R.id.btnDelete);

        // Initialize views
        tvTitle.setText(task.getTitle());
        tvDesc.setText(task.getDescription());
        editText.setText(task.getDescription());
        editLayout.setVisibility(View.GONE);

        // Edit button toggles edit mode
        btnEdit.setOnClickListener(v -> {
            if (editLayout.getVisibility() == View.GONE) {
                tvDesc.setVisibility(View.GONE);
                editLayout.setVisibility(View.VISIBLE);
                btnEdit.setText("Save");
            } else {
                String newDesc = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(newDesc)) {
                    task.setDescription(newDesc);
                    viewModel.update(task);
                    if (listener != null) listener.onTaskUpdated(task);
                    dismiss();
                }
            }
        });

        // Delete button
        btnDelete.setOnClickListener(v -> {
            viewModel.delete(task);
            if (listener != null) listener.onTaskDeleted(task);
            dismiss();
        });

        setCancelable(true); // tap outside cancels
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}
