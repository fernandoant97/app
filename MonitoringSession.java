package com.example.roomcoord;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "session_table")
public class MonitoringSession {
    @PrimaryKey(autoGenerate = true)
    private int sessionId;

    @NonNull
    @ColumnInfo(name = "session_name")
    private String sessionName;

    @NonNull
    @ColumnInfo(name = "session_date_time")
    private String sessionDateTime;

    public MonitoringSession(@NonNull String sessionName, @NonNull String sessionDateTime) {
        this.sessionName = sessionName;
        this.sessionDateTime = sessionDateTime;
    }
    @ColumnInfo(name = "is_completed")
    private boolean isCompleted;

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public static String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Getter e Setter

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    @NonNull
    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(@NonNull String sessionName) {
        this.sessionName = sessionName;
    }

    @NonNull
    public String getSessionDateTime() {
        return sessionDateTime;
    }

    public void setSessionDateTime(@NonNull String sessionDateTime) {
        this.sessionDateTime = sessionDateTime;
    }
}

