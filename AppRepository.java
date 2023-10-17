package com.example.roomcoord;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class AppRepository {

    private final MonitoringDao mMonitoringDao;
    private final PotholeDao mPotholeDao;

    private final LiveData<List<MonitoringSession>> mAllMonitoringSessions;
    private final LiveData<List<Pothole>> mAllPotholes;

    public AppRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mMonitoringDao = db.monitoringDao();
        mPotholeDao = db.potholeDao();

        mAllMonitoringSessions = mMonitoringDao.getAllMonitoringSessions();
        mAllPotholes = mPotholeDao.getAllPotholes();
    }

    // Per MonitoringSession
    public LiveData<List<MonitoringSession>> getAllMonitoringSessions() {
        return mAllMonitoringSessions;
    }

    //public void insert(MonitoringSession monitoringSession) {
    //    AppDatabase.databaseWriteExecutor.execute(() -> mMonitoringDao.insert(monitoringSession));
    //}

    public void insert(MonitoringSession monitoringSession, InsertCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long sessionId = mMonitoringDao.insert(monitoringSession);
            callback.insertFinished(sessionId);
        });
    }

    public interface InsertCallback {
        void insertFinished(long sessionId);
    }

    // Metodo per cancellare una sessione per ID
    public void deleteSessionById(int sessionId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mMonitoringDao.deleteSessionById(sessionId);
        });
    }
    public interface FetchSessionCallback {
        void onSessionFetched(MonitoringSession session);
    }

    public LiveData<MonitoringSession> getMonitoringSessionById(int sessionId) {
        return mMonitoringDao.getMonitoringSessionById(sessionId);
    }


    public void updateMonitoringSession(MonitoringSession monitoringSession) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mMonitoringDao.update(monitoringSession);
        });
    }
    public int countSessionsWithName(String sessionName) {
        return mMonitoringDao.countSessionsWithName(sessionName);
    }


    // Per Pothole
    public LiveData<List<Pothole>> getAllPotholes() {
        return mAllPotholes;
    }

    public void insert(Pothole pothole) {
        AppDatabase.databaseWriteExecutor.execute(() -> mPotholeDao.insert(pothole));
    }

    public void deleteSessionAndPotholes(int sessionId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mMonitoringDao.deleteSessionById(sessionId);  // Assumiamo che tu abbia questo metodo nel tuo DAO
            mPotholeDao.deletePotholesForSession(sessionId);  // E anche questo
        });
    }

    // Metodo per ottenere tutte le Pothole per una specifica MonitoringSession
    public LiveData<List<Pothole>> getPotholesForSession(int sessionId) {
        return mPotholeDao.getPotholesForSession(sessionId);
    }
}
