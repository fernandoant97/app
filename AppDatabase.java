package com.example.roomcoord;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {MonitoringSession.class, Pothole.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract MonitoringDao monitoringDao();
    public abstract PotholeDao potholeDao();
    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {
                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                    super.onOpen(db);

                    databaseWriteExecutor.execute(() -> {
                        // Ottieni i riferimenti ai DAO
                        MonitoringDao monitoringDao = INSTANCE.monitoringDao();
                        PotholeDao potholeDao = INSTANCE.potholeDao();

                        // Cancella tutti i dati esistenti nel database (opzionale)
                        monitoringDao.delete();

                        // Crea alcune sessioni di esempio
                        MonitoringSession session1 = new MonitoringSession("Sessione 1", MonitoringSession.getCurrentDateTime());
                        MonitoringSession session2 = new MonitoringSession("Sessione 2", MonitoringSession.getCurrentDateTime());

                        // Inserisce le sessioni nel database
                        long id1 = monitoringDao.insert(session1);  // Assumiamo che il metodo insert ritorni l'ID generato
                        long id2 = monitoringDao.insert(session2);

                        // Crea alcune "buche" di esempio e le associa alle sessioni
                        Pothole pothole1 = new Pothole((int)id1, 40.712776, -74.005974, "Indirizzo 1");
                        Pothole pothole2 = new Pothole((int)id1, 41.902782, 12.496366, "Indirizzo 2");

                        // Inserisce le "buche" nel database
                        potholeDao.insert(pothole1);
                        potholeDao.insert(pothole2);
                    });
                }
            };



}
