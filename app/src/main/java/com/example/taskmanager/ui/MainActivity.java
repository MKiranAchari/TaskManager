package com.example.taskmanager.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.taskmanager.R;
import com.example.taskmanager.ui.add.AddTaskActivity;
import com.example.taskmanager.ui.home.HomeFragment;
import com.example.taskmanager.ui.home.HistoryFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager2 viewPager;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // keep your layout

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        fab = findViewById(R.id.fabAddTask);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull @Override
            public Fragment createFragment(int position) {
                return position == 0 ? new HomeFragment() : new HistoryFragment();
            }
            @Override public int getItemCount() { return 2; }
        });

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, pos) -> tab.setText(pos == 0 ? "Pending" : "History")
        ).attach();

        fab.setOnClickListener(v -> startActivity(new Intent(this, AddTaskActivity.class)));
    }
}
