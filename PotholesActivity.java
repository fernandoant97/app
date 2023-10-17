package com.example.roomcoord;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PotholesActivity extends AppCompatActivity {
    private PotholeViewModel potholeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pothole_list);

        // Estrai l'ID della sessione dall'Intent
        Intent intent = getIntent();
        int sessionId = intent.getIntExtra("SESSION_ID", -1);
        if (sessionId == -1) {
            Log.d(TAG, "onCreate: ERRORE NELL'ID SESSIONE");
            return;
        }

        RecyclerView recyclerView = findViewById(R.id.potholeRecyclerView);
        final PotholeListAdapter adapter = new PotholeListAdapter(new PotholeListAdapter.PotholeDiff());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        potholeViewModel = new ViewModelProvider(this).get(PotholeViewModel.class);
        potholeViewModel.getPotholesForSession(sessionId).observe(this, potholes -> {
            // Aggiorna la copia cached delle buche nell'adapter
            adapter.submitList(potholes);
        });
    }
}
