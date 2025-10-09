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

public class HistoryFragment extends Fragment {

    RecyclerView recyclerView;
    TaskAdapter adapter;
    ArrayList<Task> taskList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.recyclerHistoryTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadTasks();

        return view;
    }

    private void loadTasks() {
        AppDatabase db = AppDatabase.getInstance(getContext());
        List<Task> dbTasks = db.taskDao().getCompletedTasks();
        taskList = new ArrayList<>(dbTasks);
        adapter = new TaskAdapter(taskList, false); // hide complete button
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            taskList.clear();
            taskList.addAll(AppDatabase.getInstance(getContext()).taskDao().getCompletedTasks());
            adapter.notifyDataSetChanged();
        } else {
            loadTasks();
        }
    }
}
