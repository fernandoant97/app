package com.example.roomcoord;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.internal.location.zzau;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StartActivity extends AppCompatActivity {
    private AppRepository appRepository;
    private long currentSessionId;
    private MonitoringSessionViewModel monitoringSessionViewModel;
    private PotholeViewModel potholeViewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_CODE = 1;
    private LocationRequest locationRequest;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener sensorEventListener;

    private Location currentLocation;
    float x_smoothed = 0, smoothing = 1.5F, deltacc_x = 0, deltacc_y = 0, prev_val_acc_lin_x = 0, prev_val_acc_lin_y = 0, deltacc2;
    float y_smoothed = 0;
    float z_smoothed = 0;
    float starting_value = 0;
    boolean flag = false;
    float differenza_limite = 10F;
    boolean shouldAddPothole = false;
    private boolean canStartLocation = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);
        Button startButton = findViewById(R.id.start);
        appRepository = new AppRepository(getApplication());
        // Inizializza il ViewModel
        monitoringSessionViewModel = new ViewModelProvider(this).get(MonitoringSessionViewModel.class);
        potholeViewModel = new ViewModelProvider(this).get(PotholeViewModel.class);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        // Inizializzare FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Array dei permessi da richiedere
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
                // Aggiungi qui altri permessi se necessario, come quello per la memoria
        };

        // Richiesta dei permessi
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
        Executor executor = Executors.newSingleThreadExecutor();

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
                builder.setTitle("Inserisci un nome per la sessione");

                final EditText input = new EditText(StartActivity.this);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String sessionName = input.getText().toString();

                        if (sessionName.isEmpty()) {
                            Toast.makeText(StartActivity.this, "Il nome della sessione non può essere vuoto", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                int existingSessions = appRepository.countSessionsWithName(sessionName);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (existingSessions > 0) {
                                            Toast.makeText(StartActivity.this, "Il nome della sessione già esiste", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        // Avvia una nuova sessione di monitoraggio
                                        MonitoringSession newSession = new MonitoringSession(sessionName, MonitoringSession.getCurrentDateTime());
                                        monitoringSessionViewModel.insert(newSession, new AppRepository.InsertCallback() {
                                            @Override
                                            public void insertFinished(long sessionId) {
                                                currentSessionId = sessionId;
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(StartActivity.this, "Sessione avviata", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                                // Inizia ad osservare le coordinate
                                                //startLocation();
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });

                builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });


        // Pulsante Stop
        Button stopButton = findViewById(R.id.end);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStopMonitoring(v);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Mostra un Toast per informare l'utente che la sessione è stata avviata
                        Toast.makeText(StartActivity.this, "Sessione terminata", Toast.LENGTH_LONG).show();
                    }
                });

            }
        });

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    mostraValoriacc_lin(event);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Implementazione opzionale
            }
        };
    }
    private void mostraValoriacc_lin(SensorEvent sensorEvent) {
        if (sensorEvent != null) {
            getSmoothing(sensorEvent, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
            float x_acc_lin_curr = sensorEvent.values[0];
            float y_acc_lin_curr = sensorEvent.values[1];
            float z_acc_lin_curr = sensorEvent.values[2];   //il valore cartesiano z non è utile per questo tipo di rilevazione

            //Trovo il valore della differenza tra il valore corrente e quello precedente nelle direzioni x e y
            deltacc_x = x_acc_lin_curr - prev_val_acc_lin_x;
            prev_val_acc_lin_x = x_acc_lin_curr;

            deltacc_y = y_acc_lin_curr - prev_val_acc_lin_y;
            prev_val_acc_lin_y = y_acc_lin_curr;

            //Rendo positivo il valore trovato e così facendo posso sfruttarlo all'interno dell'algoritmo Z-Diff.
            float mod_delta_x = Math.abs(deltacc_x);
            float mod_delta_y = Math.abs(deltacc_y);
            Log.d("deltax", String.valueOf(deltacc_x));
            Log.d("deltay", String.valueOf(deltacc_y));
            int orientation = getResources().getConfiguration().orientation;
            switch (orientation) {
                //In landscape
                case (2):
                    ///*****ALGORITMO Z-TRASH*****///
                    if (x_acc_lin_curr > 8 || x_acc_lin_curr < -8) {
                        Log.d("variable", String.valueOf(shouldAddPothole));
                        shouldAddPothole = true;

                    }
                    ///*****ALGORITMO Z-DIFF*****///
                    if (mod_delta_x > differenza_limite) {
                        Log.d("variable", "z-diff" + shouldAddPothole);
                        shouldAddPothole = true;
                    }
                    break;

                    //In portrait
                case (1):
                    ///*****ALGORITMO Z-TRASH*****///
                    if (y_acc_lin_curr > 8 || y_acc_lin_curr < -8) {
                        Log.d("variable", "z-diff" + shouldAddPothole);
                        shouldAddPothole = true;
                    }

                    ///*****ALGORITMO Z-DIFF*****///
                    if (mod_delta_y > differenza_limite) {
                        Log.d("variable", "z-diff" + shouldAddPothole);
                        shouldAddPothole = true;
                    }
                    break;
            }
            if (shouldAddPothole) {
                addPothole();
            }
        }
    }


    public void getSmoothing(SensorEvent sensorEvent, float x, float y, float z){
        x_smoothed += (x - x_smoothed) / smoothing;
        y_smoothed += (y - y_smoothed) / smoothing;
        z_smoothed += (z - z_smoothed) / smoothing;

        sensorEvent.values[0] = x_smoothed;
        sensorEvent.values[1] = y_smoothed;
        sensorEvent.values[2] = z_smoothed;
    }

    private void addPothole() {
        if (shouldAddPothole && currentLocation != null) {
            Pothole newPothole = new Pothole((int) currentSessionId, currentLocation.getLatitude(), currentLocation.getLongitude(), "Indirizzo");
            // Assumendo che tu abbia un ViewModel o un altro metodo per inserire nel DB
            potholeViewModel.insert(newPothole);
            shouldAddPothole = false;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(accelerometer != null) {
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
    }

    private void createLocationRequest() {

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 50)
                .build();
    }
    private void startLocation() {
        // Codice per avviare il rilevamento delle coordinate
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Richiedi i permessi mancanti
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
            return;
        }
        createLocationRequest();
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // Tutti i permessi sono stati concessi
               // startLocation();
            } else {
                // Almeno un permesso è stato negato
                Toast.makeText(this, "Permessi negati. Alcune funzionalità potrebbero non essere disponibili.", Toast.LENGTH_LONG).show();
            }
        }
    }
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    Log.d("Location", "Latitudine: " + latitude + ", Longitudine: " + longitude);
                    // Fai quello che devi fare con le coordinate qui
                    currentLocation=location;
                }
            }
        }
    };

    public void onStopMonitoring(View view) {
        // Ferma il monitoraggio dell'accelerometro
        stopAccelerometer();
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        // Osserva la sessione di monitoraggio
        monitoringSessionViewModel.getMonitoringSessionById((int) currentSessionId).observe(this, sessionToComplete -> {
            if (sessionToComplete != null) {
                // Aggiorna i campi che vuoi, come ad esempio marcare la sessione come completata
                sessionToComplete.setSessionDateTime(MonitoringSession.getCurrentDateTime());  // Aggiorna il timestamp, se necessario
                sessionToComplete.setCompleted(true);
                appRepository.updateMonitoringSession(sessionToComplete);
                // Torna alla schermata principale (o fa qualcos'altro)
                finish();
            }
        });
    }


    private void stopAccelerometer() {
        // Codice per fermare il monitoraggio dell'accelerometro
    }
}


