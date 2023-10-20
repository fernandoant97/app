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
    boolean isListening = false;
    float differenza_limite = 10F;
    boolean shouldAddPothole = false;
    private boolean canStartLocation = false;
    private int potholeCounter = 0;
    private static final int MAX_POTHOLES = 1;
    private SwitchCompat EnergySaver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);
        Button startButton = findViewById(R.id.start);
        appRepository = new AppRepository(getApplication());
        
        monitoringSessionViewModel = new ViewModelProvider(this).get(MonitoringSessionViewModel.class);
        potholeViewModel = new ViewModelProvider(this).get(PotholeViewModel.class);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        EnergySaver = findViewById(R.id.switch1);

     
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        ImageButton mybutton = findViewById(R.id.question_tips);
        TooltipCompat.setTooltipText(mybutton, "OFF: WiFi + cella + GPS\n" +
                "ON: Wi-Fi + cella (poco uso del GPS)" );
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
                // Aggiungi qui altri permessi se necessario, come quello per la memoria
        };

        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
        Executor executor = Executors.newSingleThreadExecutor();
        decideLocationRequest();
        EnergySaver.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Mostra un dialog per confermare il risparmio energetico
                    new AlertDialog.Builder(StartActivity.this)
                            .setTitle("Risparmio energetico")
                            .setMessage("Il risparmio energetico porterà una minore accuratezza, continuare? ")
                            .setPositiveButton("Sì", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    decideLocationRequest();  // Aggiunta di questa chiamata
                                    // Abilita la modalità di risparmio energetico
                                    restartLocationUpdates();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Reimposta lo stato dello switch a OFF
                                    EnergySaver.setChecked(false);
                                }
                            })
                            .show();
                } else {
                    decideLocationRequest();
                    // Disabilita la modalità di risparmio energetico
                    restartLocationUpdates();
                }
            }
        });


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(StartActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permesso non concesso
                    if (ActivityCompat.shouldShowRequestPermissionRationale(StartActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                        // Mostra una spiegazione all'utente, poi richiedi il permesso
                        ActivityCompat.requestPermissions(StartActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_CODE);
                    } else {
                    
                        new AlertDialog.Builder(StartActivity.this)
                                .setMessage("Questa app richiede il permesso di accesso alla posizione. Vai alle impostazioni per abilitarlo.")
                                .setPositiveButton("Impostazioni", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("Annulla", null)
                                .show();
                    }
                    return;  
                }
                if (!isListening) {
                   
                    sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                    isListening = true;
                } else {
                
                    sensorManager.unregisterListener(sensorEventListener);
                    isListening = false;
                }
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
                                                startLocation();
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
    private void decideLocationRequest() {
        if (EnergySaver.isChecked()) {
            Log.d("LocationRequest", "Creating low power location request");
            createLowPowerLocationRequest();
        } else {
            Log.d("LocationRequest", "Creating high accuracy location request");
            createHighAccuracyLocationRequest();
        }
    }
    private void restartLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.removeLocationUpdates(locationCallback); // Rimuovi gli aggiornamenti precedenti
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper()); // Avvia nuovi aggiornamenti
        }
    }

    private void createHighAccuracyLocationRequest() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .build();
        // Ricomincia il rilevamento della posizione con le nuove impostazioni
        restartLocationUpdates();
    }

    private void createLowPowerLocationRequest() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_LOW_POWER, 5000)
                .build();
        // Ricomincia il rilevamento della posizione con le nuove impostazioni
        restartLocationUpdates();
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
            int orientation = getResources().getConfiguration().orientation;
            switch (orientation) {

                case (2):
                    if (x_acc_lin_curr > 11 || x_acc_lin_curr < -11) {
                        shouldAddPothole = true;
                        potholeCounter = 0;
                    } else {
                        potholeCounter++;
                    }
                   
                        break;
                case (1):
                    if (y_acc_lin_curr > 11 || y_acc_lin_curr < -11) {
                        shouldAddPothole = true;
                        potholeCounter = 0;
                    } else {
                        potholeCounter++;
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
        if (shouldAddPothole && currentLocation != null && potholeCounter >= MAX_POTHOLES) {
            databaseWriteExecutor.execute(() -> {
                String addressString;
                if(Geocoder.isPresent()) {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    List<Address> addresses;
                    try {
                        addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
                        if (addresses != null && addresses.size() > 0) {
                            Address address = addresses.get(0);
                            addressString = address.getAddressLine(0);
                        } else {
                            addressString = "Indirizzo sconosciuto";
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        addressString = "Errore nella geocodifica";
                    }
                } else {
                    addressString = "Geocodifica assente. Impossibile trovare l'indirizzo";
                }

                Pothole newPothole = new Pothole((int) currentSessionId, currentLocation.getLatitude(), currentLocation.getLongitude(), addressString);
                potholeViewModel.insert(newPothole);
                shouldAddPothole = false;
                potholeCounter = 0;
            });
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

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 250)
                .build();
    }
    private void startLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
            return;
        }
        decideLocationRequest();
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

    }



    private final LocationCallback locationCallback = new LocationCallback() {
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
        stopAccelerometer();
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
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


