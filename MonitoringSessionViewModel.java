package com.example.roomcoord;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class MonitoringSessionViewModel extends AndroidViewModel {

    private AppRepository mRepository;
    private final LiveData<List<MonitoringSession>> mAllMonitoringSessions;

    public MonitoringSessionViewModel(Application application) {
        super(application);
        mRepository = new AppRepository(application);
        mAllMonitoringSessions = mRepository.getAllMonitoringSessions();
    }

    LiveData<List<MonitoringSession>> getAllMonitoringSessions() {
        return mAllMonitoringSessions;
    }

    public void insert(MonitoringSession monitoringSession, AppRepository.InsertCallback callback) {
        mRepository.insert(monitoringSession, callback);
    }
    public LiveData<MonitoringSession> getMonitoringSessionById(int sessionId) {
        return mRepository.getMonitoringSessionById(sessionId);
    }

}

