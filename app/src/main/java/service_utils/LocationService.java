package service_utils;

/**
 * Created by AdelinGDobre on 6/5/2017.
 */
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import runsdb.LocationDBHelper;
import runsdb.LocationDBReader;
import runsdb.LocationDBWriter;
import runsdb.Run;
import runsdb.Waypoint;
import runsdb.Segment;

public class LocationService extends Service implements LocationListener {
    private LocationHelper locHelp;
    private LocationDBHelper dbHelp;
    private LocationDBWriter dbWrite;
    private LocationDBReader dbRead;
    private long runId;
    private Location latestLocation;
    private Location secondLatestLocation;
    private Long latestWaypointId;
    private Long secondLatestWaypointId;
    private Long firstSegmentId;
    private Run currentRun;
    private boolean isFirstSegment = true;
    private final IBinder mBinder = new LocationBinder();
    private ArrayList<OnDataChangedListener> listeners = new ArrayList<OnDataChangedListener>();
    private Bundle runBundle;

    private String usermail;

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        locHelp.removeLocationUpdates();
        return true;
    }

    public boolean initialize(String mail){
        locHelp = new LocationHelper(this);
        dbHelp = new LocationDBHelper(this);
        dbWrite = new LocationDBWriter(dbHelp);
        dbRead = new LocationDBReader(dbHelp);
        runId = dbRead.getLatestRunId() + 1;
        runBundle = new Bundle();
        boolean successful = locHelp.addLocationUpdates();
        usermail = mail;

        return successful;
    }

    @Override
    public void onLocationChanged(Location location) {
        secondLatestWaypointId = latestWaypointId;
        secondLatestLocation = latestLocation;
        latestLocation = location;
        latestWaypointId = storeWaypoint(location);
        //in the very first update, there is only one Location, so we can't create a segment
        if (secondLatestWaypointId != null && secondLatestLocation != null ){
            Segment sg = new Segment(secondLatestWaypointId, latestWaypointId, secondLatestLocation, latestLocation, runId);
            long insertedSegmentId = storeSegment(sg);
            if (isFirstSegment){
                firstSegmentId = insertedSegmentId;
                isFirstSegment = false;
                storeRun();
            } else {
                updateRun(runId);
            }
        }
        triggerOnDataChangedListener();
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //TODO: add code in case GPS is not available
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        //TODO: add handling in case GPS is disabled

    }

    private long storeWaypoint(Location location) {
        Waypoint wp = new Waypoint(location, runId);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        runBundle.putParcelable("position", latLng);
        return dbWrite.storeWaypoint(wp);
    }

    private long storeSegment(Segment sg){
        runBundle.putDouble("velocity", sg.velocity);
        return dbWrite.storeSegment(sg);
    }

    private void updateRun(long runId){
        currentRun = dbWrite.updateRun(runId, dbRead);
        double heightInterval = currentRun.ascendInterval + currentRun.descendInterval;
        runBundle.putDouble("distance", currentRun.distance);
        runBundle.putLong("timeInterval", currentRun.timeInterval);
        runBundle.putDouble("heightInterval", heightInterval);
    }

    private long storeRun(Run run){
        return dbWrite.storeRun(run);
    }

    private long storeRun(){
        Run run = new Run(runId, usermail);
        runBundle.putLong("id", runId);
        return storeRun(run);
    }

    //To be called from displaying activity
    public void addOnDataChangedListener(OnDataChangedListener listener){
        listeners.add(listener);
    }

    public void triggerOnDataChangedListener(){
        for (int i = 0; i < listeners.size(); i++){
            listeners.get(i).onDataChanged(runBundle);
        }
    }

    public class LocationBinder extends Binder {
        public LocationService getService(){
            return LocationService.this;
        }
    }
    public interface OnDataChangedListener{
        //interface methods are always public
        void onDataChanged(Bundle run);
    }
}
