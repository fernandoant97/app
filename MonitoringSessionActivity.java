package com.example.roomcoord;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MonitoringSessionActivity extends AppCompatActivity {
    private MonitoringSessionViewModel monitoringSessionViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoringsession_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sessioni di monitoraggio");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        RecyclerView recyclerView = findViewById(R.id.monitoringSessionRecyclerView);
        final MonitoringSessionListAdapter adapter = new MonitoringSessionListAdapter(new MonitoringSessionListAdapter.SessionDiff(), getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        monitoringSessionViewModel = new ViewModelProvider(this).get(MonitoringSessionViewModel.class);
        monitoringSessionViewModel.getAllMonitoringSessions().observe(this, monitoringSession -> {
            // Update the cached copy of the words in the adapter.
            adapter.submitList(monitoringSession);
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();  // Chiude l'Activity corrente e torna a MainActivity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}