package com.example.roomcoord;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "pothole_table", foreignKeys = @ForeignKey(entity = MonitoringSession.class,
        parentColumns = "sessionId",
        childColumns = "sessionId",
        onDelete = ForeignKey.CASCADE))
public class Pothole {
    @PrimaryKey(autoGenerate = true)
    private int potholeId;

    @ColumnInfo(name = "sessionId")
    private int sessionId;  // Aggiungo questa colonna come chiave esterna
    @ColumnInfo(name = "latitude")
    private double latitude;

    @ColumnInfo(name = "longitude")
    private double longitude;

    @NonNull
    @ColumnInfo(name = "address")
    private String address;

    public Pothole(int sessionId, double latitude, double longitude, @NonNull String address) {
        this.sessionId= sessionId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    // Getter e Setter
    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }
    public int getPotholeId() {
        return potholeId;
    }

    public void setPotholeId(int potholeId) {
        this.potholeId = potholeId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @NonNull
    public String getAddress() {
        return address;
    }

    public void setAddress(@NonNull String address) {
        this.address = address;
    }
}
