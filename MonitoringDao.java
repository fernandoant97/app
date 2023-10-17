package com.example.roomcoord;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;


@Dao
public interface MonitoringDao {

    // allowing the insert of the same word multiple times by passing a
    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(MonitoringSession monitoringSession);

    @Query("DELETE FROM session_table")
    void delete();

    @Update
    void update(MonitoringSession monitoringSession);

    @Query("SELECT * FROM session_table WHERE sessionId = :sessionId LIMIT 1")
    LiveData<MonitoringSession> getMonitoringSessionById(int sessionId);

    @Query("DELETE FROM session_table WHERE sessionId = :sessionId")
    void deleteSessionById(int sessionId);
    @Query("SELECT * FROM session_table ORDER BY sessionId ASC")
    LiveData<List<MonitoringSession>> getAllMonitoringSessions();

    @Query("SELECT COUNT(*) FROM session_table WHERE session_name = :sessionName")
    int countSessionsWithName(String sessionName);

}