package com.example.roomcoord;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class PotholeViewModel extends AndroidViewModel {

    private AppRepository mRepository;
    private LiveData<List<Pothole>> mPotholesForSession;

    public PotholeViewModel(Application application) {
        super(application);
        mRepository = new AppRepository(application);
    }

    LiveData<List<Pothole>> getPotholesForSession(int sessionId) {
        if (mPotholesForSession == null) {
            mPotholesForSession = mRepository.getPotholesForSession(sessionId);
        }
        return mPotholesForSession;
    }

    public void insert(Pothole pothole) {
        mRepository.insert(pothole);
    }
}

