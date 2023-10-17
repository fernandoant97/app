package com.example.roomcoord;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PotholeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Pothole pothole);

    @Query("DELETE FROM pothole_table WHERE sessionId = :sessionId")
    void deletePotholesForSession(int sessionId);
    @Query("DELETE FROM pothole_table")
    void deleteAll();

    @Query("SELECT * FROM pothole_table")
    LiveData<List<Pothole>> getAllPotholes();

    @Query("SELECT * FROM pothole_table WHERE sessionId = :sessionId")
    LiveData<List<Pothole>> getPotholesForSession(int sessionId);
}

