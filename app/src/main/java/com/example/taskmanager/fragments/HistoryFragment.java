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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Task> dbTasks = AppDatabase.getInstance(getContext()).taskDao().getPendingTasks();
            getActivity().runOnUiThread(() -> {
                taskList = new ArrayList<>(dbTasks);
                adapter = new TaskAdapter(taskList, false);
                recyclerView.setAdapter(adapter);
            });
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            taskList.clear();
            Executors.newSingleThreadExecutor().execute(() -> taskList.addAll(AppDatabase.getInstance(getContext()).taskDao().getCompletedTasks()));
            adapter.notifyDataSetChanged();
        } else {
            loadTasks();
        }
    }
}
