package com.fablapps.gpxexporterformifit.models;

public class DataActivity {

    private final int id, trackId, type, distance, costTime, endTime;

    private final String date;

    public int getId() {
        return this.id;
    }

    public int getTrackId() {
        return this.trackId;
    }

    public int getType() {
        return this.type;
    }

    public int getDistance() {
        return this.distance;
    }

    public int getCostTime() {
        return this.costTime;
    }

    public int getEndTime() {
        return this.endTime;
    }

    public String getDate() {
        return this.date;
    }

    public DataActivity(int id, int trackId, int type, int distance, int costTime, int endTime, String date) {
        this.id = id;
        this.trackId = trackId;
        this.type = type;
        this.distance = distance;
        this.costTime = costTime;
        this.endTime = endTime;
        this.date = date;


    }
}