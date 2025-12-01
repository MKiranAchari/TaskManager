package com.example.taskmanager.ui.home;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taskmanager.R;
import com.example.taskmanager.data.local.entities.Task;
import com.example.taskmanager.viewmodel.TaskViewModel;
import com.google.android.material.snackbar.Snackbar;

public class HomeFragment extends Fragment {

    private TaskViewModel vm;
    private TaskAdapter adapter;
    private RecyclerView recycler;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recycler = view.findViewById(R.id.recyclerPendingTasks);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new TaskAdapter(false, new TaskAdapter.TaskActionListener() {
            @Override public void onDelete(Task task) {
                vm.delete(task);
                Snackbar.make(requireActivity().findViewById(android.R.id.content),
                                "Task deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> vm.insert(new Task(task.getTitle(), task.getDescription(), task.getDateTime(), task.isCompleted(), task.getPriority())))
                        .show();
            }
            @Override public void onComplete(Task task) {
                task.setCompleted(true);
                vm.update(task);
                Snackbar.make(requireActivity().findViewById(android.R.id.content),
                        "Task moved to History", Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onEditClick(Task task) {
                vm.update(task); // Save changes
            }
        }, getParentFragmentManager());
        recycler.setAdapter(adapter);

        vm = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        vm.getPendingTasks().observe(getViewLifecycleOwner(), tasks -> adapter.submitList(tasks));

        return view;
    }
}
