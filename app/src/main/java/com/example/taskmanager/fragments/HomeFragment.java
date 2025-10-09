package com.example.taskmanager.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.taskmanager.R;
import com.example.taskmanager.adapters.TaskAdapter;
import com.example.taskmanager.database.AppDatabase;
import com.example.taskmanager.models.Task;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    RecyclerView recyclerView;
    TaskAdapter adapter;
    ArrayList<Task> taskList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerPendingTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // initial load
        loadTasks();

        return view;
    }

    private void loadTasks() {
        AppDatabase db = AppDatabase.getInstance(getContext());
        List<Task> dbTasks = db.taskDao().getPendingTasks();
        taskList = new ArrayList<>(dbTasks);
        adapter = new TaskAdapter(taskList, true); // show complete button
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // refresh when returning from AddTaskActivity or other changes
        if (adapter != null) {
            taskList.clear();
            taskList.addAll(AppDatabase.getInstance(getContext()).taskDao().getPendingTasks());
            adapter.notifyDataSetChanged();
        } else {
            loadTasks();
        }
    }
}
