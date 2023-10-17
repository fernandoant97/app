package com.example.roomcoord;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton = findViewById(R.id.session_open);
        Button viewSessionButton = findViewById(R.id.view_session);



        // Azione per il bottone "Start"
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Apri qui la nuova activity per "Start"
                Intent intent = new Intent(MainActivity.this, StartActivity.class);
                startActivity(intent);
            }
        });

        // Azione per il bottone "View Session"
        viewSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Apri la MonitoringSessionActivity
                Intent intent = new Intent(MainActivity.this, MonitoringSessionActivity.class);
                startActivity(intent);
            }
        });
    }


}
