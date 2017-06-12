package runsdb;

/**
 * Created by AdelinGDobre on 6/4/2017.
 */

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class Waypoint {
    public double longtitude;
    public double latitude;
    public double height;
    public long timestamp;
    public long runId;

    public Waypoint(double longtitude, double latitude, double height, long timestamp, long runId) {
        this.longtitude = longtitude;
        this.latitude = latitude;
        this.height = height;
        this.timestamp = timestamp;
        this.runId = runId;
    }
    public Waypoint(Location location, long runId) {
        longtitude = location.getLongitude();
        latitude = location.getLatitude();
        height = location.getAltitude();
        timestamp = location.getTime();
        this.runId = runId;
    }

    public LatLng toLatLng() {
        return new LatLng(latitude, longtitude);
    }

}
