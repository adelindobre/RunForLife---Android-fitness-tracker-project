package runsdb;

/**
 * Created by AdelinGDobre on 6/5/2017.
 */

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;


public class LocationDBWriter {
    LocationDBHelper dbHelp;

    public LocationDBWriter(LocationDBHelper dbHelp){
        this.dbHelp = dbHelp;
    }

    public long storeWaypoint(Waypoint wp) {
        SQLiteDatabase db = dbHelp.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(RunsContract.Waypoints.COLUMN_NAME_LONGTITUDE, wp.longtitude);
        values.put(RunsContract.Waypoints.COLUMN_NAME_LATITUDE, wp.latitude);
        values.put(RunsContract.Waypoints.COLUMN_NAME_HEIGHT, wp.height);
        values.put(RunsContract.Waypoints.COLUMN_NAME_TIMESTAMP, wp.timestamp);
        values.put(RunsContract.Waypoints.COLUMN_NAME_RUN_ID, wp.runId);
        long newRowId = db.insert(RunsContract.Waypoints.TABLE_NAME, null, values);
        if (newRowId == -1) {
            System.out.println("Inserting waypoint " + wp.longtitude + " " + wp.latitude + " failed!");
        }
        db.close();
        return newRowId;
    }
    public long storeSegment(Segment sg){
        SQLiteDatabase db = dbHelp.getWritableDatabase();
        ContentValues values = sg.toContentValues();
        long newRowId = db.insert(RunsContract.Sections.TABLE_NAME, null, values);
        if (newRowId == -1) {
            System.out.println("Inserting segment " + sg.startId + " " + sg.endId + " failed!");
        }
        db.close();
        //System.out.println(values.toString());
        return newRowId;
    }
    public long storeRun(Run run){
        SQLiteDatabase db = dbHelp.getWritableDatabase();
        ContentValues values = run.toContentValues(false);
        long newRowID = db.insert(RunsContract.Runs.TABLE_NAME, null, values);
        if (newRowID == -1){
            System.out.println("Inserting run failed!");
        }
        db.close();
        //System.out.println(values.toString());
        return newRowID;

    }

    public Run updateRun(long id, LocationDBReader dbRead){
        Run updatedRun = dbRead.calculateRun(id);
        updateRun(updatedRun);
        return updatedRun;
    }

    private long updateRun(Run run){
        SQLiteDatabase db = dbHelp.getWritableDatabase();
        ContentValues values = run.toContentValues(false);
        String selection = RunsContract.Runs._ID  + " = ?";
        String[] selectionArgs = {String.valueOf(run.id)};
        int affectedRows = db.update(RunsContract.Runs.TABLE_NAME, values, selection, selectionArgs);
        db.close();
        return affectedRows;
    }

}
