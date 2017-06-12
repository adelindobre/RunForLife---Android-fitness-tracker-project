package runsdb;

/**
 * Created by AdelinGDobre on 6/5/2017.
 */

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class LocationDBReader {
    private LocationDBHelper dbHelp;


    public LocationDBReader(LocationDBHelper dbHelp) {
        this.dbHelp = dbHelp;
    }

    public long getLatestRunId(){
        SQLiteDatabase db = dbHelp.getReadableDatabase();
        String[] projection = {RunsContract.Runs._ID};
        String sortOrder = RunsContract.Runs._ID + " DESC";
        Cursor c = db.query(RunsContract.Runs.TABLE_NAME, projection, null, null, null, null, sortOrder, "1");
        //moveToFirst returns false if there is no row
        boolean firstRun = !(c.moveToFirst());
        if (firstRun){
            c.close();
            db.close();
            return 0;
        } else {
            long runId = c.getLong(c.getColumnIndexOrThrow(RunsContract.Runs._ID));
            c.close();
            db.close();
            return runId;
        }
    }
    public ArrayList<Run> getRuns(Long firstDate, Long lastDate, String order){
        SQLiteDatabase db = dbHelp.getReadableDatabase();
        String sortOrder = RunsContract.Runs.COLUMN_NAME_TIMESTAMP + " " + order;
        String selection = RunsContract.Runs.COLUMN_NAME_TIMESTAMP + " > ? AND " + RunsContract.Runs.COLUMN_NAME_TIMESTAMP + " < ?";
        String[] selectionArgs = {firstDate.toString(), lastDate.toString()};
        Cursor c = db.query(RunsContract.Runs.TABLE_NAME, null, selection, selectionArgs, null, null, sortOrder);
        ArrayList <Run> runs = new ArrayList<Run>();
        c.moveToFirst();
        while (!c.isAfterLast()){
            long id = c.getLong(c.getColumnIndexOrThrow(RunsContract.Runs._ID));
            long timestamp = c.getLong(c.getColumnIndexOrThrow(RunsContract.Runs.COLUMN_NAME_TIMESTAMP));
            double distance = c.getDouble(c.getColumnIndexOrThrow(RunsContract.Runs.COLUMN_NAME_DISTANCE));
            long timeInterval = c.getLong(c.getColumnIndexOrThrow(RunsContract.Runs.COLUMN_NAME_TIME_INTERVAL));
            runs.add(new Run(id, distance, timeInterval,timestamp));
            c.moveToNext();
        }
        c.close();
        db.close();
        return runs;
    }

    public ArrayList<Run> getRuns(Long firstDate, Long lastDate, String order, String mail){
        SQLiteDatabase db = dbHelp.getReadableDatabase();
        String sortOrder = RunsContract.Runs.COLUMN_NAME_TIMESTAMP + " " + order;
        String selection = RunsContract.Runs.COLUMN_NAME_TIMESTAMP + " > ? AND " + RunsContract.Runs.COLUMN_NAME_TIMESTAMP + " < ? AND " + RunsContract.Runs.COLUMN_NAME_USER + " = ?";
        String[] selectionArgs = {firstDate.toString(), lastDate.toString(), mail.toString()};
        Cursor c = db.query(RunsContract.Runs.TABLE_NAME, null, selection, selectionArgs, null, null, sortOrder);
        ArrayList <Run> runs = new ArrayList<Run>();
        c.moveToFirst();
        while (!c.isAfterLast()){
            long id = c.getLong(c.getColumnIndexOrThrow(RunsContract.Runs._ID));
            long timestamp = c.getLong(c.getColumnIndexOrThrow(RunsContract.Runs.COLUMN_NAME_TIMESTAMP));
            double distance = c.getDouble(c.getColumnIndexOrThrow(RunsContract.Runs.COLUMN_NAME_DISTANCE));
            long timeInterval = c.getLong(c.getColumnIndexOrThrow(RunsContract.Runs.COLUMN_NAME_TIME_INTERVAL));
            runs.add(new Run(id, distance, timeInterval,timestamp));
            c.moveToNext();
        }
        c.close();
        db.close();
        return runs;
    }
    public Run calculateRun(long id){
        ArrayList<Segment> segments = getSegments(id);
        String usermail = getUsermail(id);
        return calculateRun(segments, id, usermail);
    }

    protected Run calculateRun(final ArrayList<Segment> segments, long id, String usermail){
        double distance = calculateDistance(segments);
        long timeInterval = calculateTimeInterval(segments);
        double maxVelocity = calculateMaxVelocity(segments);
        double medVelocity = calculateMedVelocity(segments);
        double ascendInterval = calculateInterval(segments, 1);
        double descendInterval = - calculateInterval(segments, 2);
        long breakTime = calculateBreakTime(segments);
        long timestamp =  calculateTimestamp(segments);
        return new Run(id, distance, timeInterval, maxVelocity, medVelocity, ascendInterval, descendInterval, breakTime, timestamp, usermail);
    }

    private double calculateDistance(ArrayList<Segment> segments){
        double totalDistance = 0;
        for (int i = 0; i < segments.size(); i++){
            totalDistance += segments.get(i).distance;
        }
        return totalDistance;
    }

    private long calculateTimeInterval(ArrayList<Segment> segments){
        long totalTimeInterval = 0;
        for (int i = 0; i < segments.size(); i++){
            totalTimeInterval += segments.get(i).timeInterval;
        }
        return totalTimeInterval;
    }

    private double calculateMaxVelocity(ArrayList<Segment> segments){
        double maxVelocity = 0;
        for (int i = 0; i < segments.size(); i++){
            double velocity = segments.get(i).velocity;
            if (velocity > maxVelocity){
                maxVelocity = velocity;
            }
        }
        return maxVelocity;
    }

    private double calculateMedVelocity(ArrayList<Segment> segments){
        int size = segments.size();
        double sum = 0;
        for (int i = 0; i < size; i++){
            sum += segments.get(i).velocity;
        }
        if (size != 0){
            return sum / size;
        } else {
            return 0;
        }
    }

    private double calculateInterval(ArrayList<Segment> segments, int mode){
        double totalInterval = 0;
        for (int i = 0; i < segments.size(); i++){
            double interval = segments.get(i).heightInterval;
            switch (mode){
                case 0:
                    totalInterval += interval;
                    break;
                case 1:
                    if (interval > 0){
                        totalInterval += interval;
                    }
                    break;
                case 2:
                    if (interval < 0){
                        totalInterval += -interval;
                    }
                    break;
            }
        }
        return totalInterval;
    }

    private long calculateBreakTime(ArrayList<Segment> segments){
        long breakTime = 0;
        for (int i = 0; i < segments.size(); i++){
            if( segments.get(i).distance == 0 ){
                breakTime += segments.get(i).timeInterval;
            }
        }
        return breakTime;
    }

    private long calculateTimestamp(ArrayList<Segment> segments){
        if (segments.size() != 0){
            long startWaypointId = segments.get(0).startId;
            Waypoint wp = getWaypoint(startWaypointId);
            return wp.timestamp;
        } else {
            return 0;
        }
    }

    public String getUsermail(long runId){
       Run run = getRun(runId);
       return run.usermail;
    }
    public ArrayList<Segment> getSegments(long runId){
        SQLiteDatabase db = dbHelp.getReadableDatabase();
        String sortOrder = RunsContract.Sections._ID + " ASC";
        String selection = RunsContract.Sections.COLUMN_NAME_RUN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(runId)};
        Cursor c = db.query(RunsContract.Sections.TABLE_NAME, null, selection, selectionArgs, null, null, sortOrder);
        c.moveToFirst();
        ArrayList<Segment> segments = new ArrayList<Segment>();
        while (!c.isAfterLast()){
            Segment sg = getSegment(c);
            segments.add(sg);
            c.moveToNext();
        }
        c.close();
        db.close();
        return segments;

    }
    protected Segment getSegment(Cursor c){
        //long id = c.getLong(c.getColumnIndexOrThrow(RunsContract.Sections._ID));
        long startId = c.getLong(c.getColumnIndexOrThrow(RunsContract.Sections.COLUMN_NAME_START_ID));
        long endId = c.getLong(c.getColumnIndexOrThrow(RunsContract.Sections.COLUMN_NAME_END_ID));
        double distance = c.getDouble(c.getColumnIndexOrThrow(RunsContract.Sections.COLUMN_NAME_DISTANCE));
        long timeInterval = c.getLong(c.getColumnIndexOrThrow(RunsContract.Sections.COLUMN_NAME_TIME_INTERVAL));
        double velocity = c.getDouble(c.getColumnIndexOrThrow(RunsContract.Sections.COLUMN_NAME_VELOCITY));
        double heightInterval = c.getDouble(c.getColumnIndexOrThrow(RunsContract.Sections.COLUMN_NAME_HEIGHT_INTERVAL));
        long runId = c.getLong(c.getColumnIndexOrThrow(RunsContract.Sections.COLUMN_NAME_RUN_ID));
        return new Segment(startId, endId, distance, timeInterval, velocity, heightInterval, runId);
    }

    public ArrayList<Waypoint> getWaypoints(long id){
        ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
        SQLiteDatabase db = dbHelp.getReadableDatabase();
        String selection = RunsContract.Waypoints.COLUMN_NAME_RUN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        Cursor c = db.query(RunsContract.Waypoints.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            waypoints.add(getWaypoint(c));
            c.moveToNext();
        }
        c.close();
        db.close();
        return waypoints;
    }
    public Waypoint getWaypoint(long id){
        SQLiteDatabase db = dbHelp.getReadableDatabase();
        String selection = RunsContract.Waypoints._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        Cursor c = db.query(RunsContract.Waypoints.TABLE_NAME, null, selection, selectionArgs, null, null, null, "1");
        c.moveToFirst();
        Waypoint wp = getWaypoint(c);
        c.close();
        db.close();
        return wp;
    }
    protected Waypoint getWaypoint(Cursor c){
        double longtitude = c.getDouble(c.getColumnIndexOrThrow(RunsContract.Waypoints.COLUMN_NAME_LONGTITUDE));
        double latitude = c.getDouble(c.getColumnIndexOrThrow(RunsContract.Waypoints.COLUMN_NAME_LATITUDE));
        double height = c.getDouble(c.getColumnIndexOrThrow(RunsContract.Waypoints.COLUMN_NAME_HEIGHT));
        long timestamp = c.getLong(c.getColumnIndexOrThrow(RunsContract.Waypoints.COLUMN_NAME_TIMESTAMP));
        long runId = c.getLong(c.getColumnIndexOrThrow(RunsContract.Waypoints.COLUMN_NAME_RUN_ID));
        return new Waypoint(longtitude, latitude, height, timestamp, runId);
    }

    public Run getRun(long id){
        SQLiteDatabase db = dbHelp.getReadableDatabase();
        String selection = RunsContract.Runs._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        Cursor c = db.query(RunsContract.Runs.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        c.moveToFirst();
        Run run = getRun(c);
        c.close();
        db.close();
        return run;
    }

    protected Run getRun(Cursor c){
        long id = c.getLong(c.getColumnIndexOrThrow(RunsContract.Runs._ID));
        double distance = c.getDouble(c.getColumnIndexOrThrow(RunsContract.Runs.COLUMN_NAME_DISTANCE));
        long timeInterval = c.getLong(c.getColumnIndexOrThrow(RunsContract.Runs.COLUMN_NAME_TIME_INTERVAL));
        double maxVelocity = c.getDouble(c.getColumnIndexOrThrow(RunsContract.Runs.COLUMN_NAME_MAX_VELOCITY));
        double medVelocity = c.getDouble(c.getColumnIndexOrThrow(RunsContract.Runs.COLUMN_NAME_MED_VELOCITY));
        double ascInterval = c.getDouble(c.getColumnIndexOrThrow(RunsContract.Runs.COLUMN_NAME_ASCEND_INTERVAL));
        double descInterval = c.getDouble(c.getColumnIndexOrThrow(RunsContract.Runs.COLUMN_NAME_DESCEND_INTERVAL));
        long breakTime = c.getLong(c.getColumnIndexOrThrow(RunsContract.Runs.COLUMN_NAME_BREAK_TIME));
        long timestamp = c.getLong(c.getColumnIndexOrThrow(RunsContract.Runs.COLUMN_NAME_TIMESTAMP));
        String usermail = c.getString(c.getColumnIndexOrThrow(RunsContract.Runs.COLUMN_NAME_USER));
        return new Run(id, distance, timeInterval, maxVelocity, medVelocity, ascInterval, descInterval, breakTime, timestamp, usermail);
    }

    public Long[] getRunIds(){
        SQLiteDatabase db = dbHelp.getReadableDatabase();
        String[] projection = {RunsContract.Runs._ID};
        String sortOrder = RunsContract.Runs._ID + " ASC";
        Cursor c = db.query(RunsContract.Runs.TABLE_NAME, projection, null, null, null, null, sortOrder);
        ArrayList <Long> runIds = new ArrayList<>();
        c.moveToFirst();
        while (!c.isAfterLast()){
            long id = c.getLong(c.getColumnIndexOrThrow(RunsContract.Runs._ID));
            runIds.add(id);
            c.moveToNext();
        }
        c.close();
        db.close();
        Long[] runIdsArray = new Long[0];
        return runIds.toArray(runIdsArray);
    }
}
