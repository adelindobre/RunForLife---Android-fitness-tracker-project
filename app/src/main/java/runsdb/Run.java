package runsdb;

/**
 * Created by AdelinGDobre on 6/4/2017.
 */

import android.content.ContentValues;

public class Run {
    public long id;
    public double distance;
    public long timeInterval;
    public double maxVelocity;
    public double medVelocity;
    public double ascendInterval;
    public double descendInterval;
    public long breakTime;
    public long timestamp;
    public String usermail;
    public double calories;

    public Run(){}

    public Run(long id, double distance, long timeInterval, long timestamp){
        this.id = id;
        this.distance = distance;
        this.timeInterval = timeInterval;
        this.timestamp = timestamp;
    }

    public Run(long id, double distance, long timeInterval, long timestamp, String user){
        this.id = id;
        this.distance = distance;
        this.timeInterval = timeInterval;
        this.timestamp = timestamp;
        this.usermail = user;
    }
//
//    public Run(long id){
//        this.id = id;
//    }

    public Run(long id, String user){
        this.usermail = user;
        this.id = id;
    }

    public Run(long id, double distance, long timeInterval, double maxVelocity, double medVelocity, double ascendInterval, double descendInterval,
               long breakTime, long timestamp, String usermail, double calories){
        this. id = id;
        this.distance = distance;
        this.timeInterval = timeInterval;
        this.maxVelocity = maxVelocity;
        this.medVelocity = medVelocity;
        this.ascendInterval = ascendInterval;
        this.descendInterval = descendInterval;
        this.breakTime = breakTime;
        this.timestamp = timestamp;
        this.usermail = usermail;
        this.calories = calories;
    }

    public ContentValues toContentValues(boolean storeId){
        ContentValues values = new ContentValues();
        if (storeId){
            values.put(RunsContract.Runs._ID, id);
        }
        values.put(RunsContract.Runs.COLUMN_NAME_DISTANCE, distance);
        values.put(RunsContract.Runs.COLUMN_NAME_TIME_INTERVAL, timeInterval);
        values.put(RunsContract.Runs.COLUMN_NAME_MAX_VELOCITY, maxVelocity);
        values.put(RunsContract.Runs.COLUMN_NAME_MED_VELOCITY, medVelocity);
        values.put(RunsContract.Runs.COLUMN_NAME_ASCEND_INTERVAL, ascendInterval);
        values.put(RunsContract.Runs.COLUMN_NAME_DESCEND_INTERVAL, descendInterval);
        values.put(RunsContract.Runs.COLUMN_NAME_BREAK_TIME, breakTime);
        values.put(RunsContract.Runs.COLUMN_NAME_TIMESTAMP, timestamp);
        values.put(RunsContract.Runs.COLUMN_NAME_USER, usermail);
        values.put(RunsContract.Runs.COLUMN_NAME_CALORIES, calories);
        return values;
    }
}
