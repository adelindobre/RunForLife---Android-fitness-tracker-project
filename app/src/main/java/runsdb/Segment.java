package runsdb;

/**
 * Created by AdelinGDobre on 6/4/2017.
 */

import android.content.ContentValues;
import android.location.Location;

public class Segment {
    public long startId;
    public long endId;
    public double distance;
    public long timeInterval;
    public double velocity;
    public double heightInterval;
    public long runId;

    public Segment(long startId, long endId, double distance, long timeInterval, double velocity, double heightInterval, long runId) {
        this.startId = startId;
        this.endId = endId;
        this.distance = distance;
        this.timeInterval = timeInterval;
        this.velocity = velocity;
        this.heightInterval = heightInterval;
        this.runId = runId;
    }
    public Segment (long startId, long endId, Location startLocation, Location endLocation, long runId){
        this.startId = startId;
        this.endId = endId;
        this.distance = computeDistance(startLocation, endLocation);
        this.timeInterval = computeTimeInterval(startLocation, endLocation);
        this.velocity = computeVelocity(timeInterval, distance);
        this.heightInterval = computeHeightInterval(startLocation, endLocation);
        this.runId = runId;
    }

    private double computeDistance(Location startLocation, Location endLocation) {
        return startLocation.distanceTo(endLocation);
    }

    private long computeTimeInterval(Location startLocation, Location endLocation){
        long start = startLocation.getTime();
        long end = endLocation.getTime();
        long interval = end - start;
        if (interval < 0){
            interval = -interval;
        }
        return interval;
    }

    private double computeVelocity(long timeInterval, double distance){
        return distance / (timeInterval / 1000);
    }

    public double computeHeightInterval(Location startLocation, Location endLocation){
        double start = startLocation.getAltitude();
        double end = endLocation.getAltitude();
        return end - start;
    }

    public ContentValues toContentValues(){
        ContentValues values = new ContentValues();
        values.put(RunsContract.Sections.COLUMN_NAME_START_ID, startId);
        values.put(RunsContract.Sections.COLUMN_NAME_END_ID, endId);
        values.put(RunsContract.Sections.COLUMN_NAME_DISTANCE, distance);
        values.put(RunsContract.Sections.COLUMN_NAME_TIME_INTERVAL, timeInterval);
        values.put(RunsContract.Sections.COLUMN_NAME_VELOCITY, velocity);
        values.put(RunsContract.Sections.COLUMN_NAME_HEIGHT_INTERVAL, heightInterval);
        values.put(RunsContract.Sections.COLUMN_NAME_RUN_ID, runId);
        return values;
    }

}
