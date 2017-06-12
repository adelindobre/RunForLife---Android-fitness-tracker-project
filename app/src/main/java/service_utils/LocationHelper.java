package service_utils;

/**
 * Created by AdelinGDobre on 6/5/2017.
 */
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;

public class LocationHelper {
    LocationManager locMan;
    LocationListener locLis;

    String LOCATION_PROVIDER = "gps";
    private int minUpdateInterval = 1000;
    private int minUpdateDistance = 0;

    public LocationHelper(Context context){
        //requesting LocationManager
        locMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //Setting up locLis
        locLis = (LocationListener) context;
    }

    protected boolean addLocationUpdates(){
        return addLocationUpdates(locLis);
    }

    protected boolean addLocationUpdates(LocationListener locLis) {
        boolean successful = false;
        if (locMan.isProviderEnabled(LOCATION_PROVIDER)){
            try {
                locMan.requestLocationUpdates(LOCATION_PROVIDER, minUpdateInterval, minUpdateDistance, locLis);
                successful = true;
            } catch (SecurityException ex){
                System.out.println(ex);
            }
        }
        return successful;
    }

    protected void removeLocationUpdates(){
        removeLocationUpdates(locLis);
    }
    protected void removeLocationUpdates(LocationListener locLis){
        try{
            locMan.removeUpdates(locLis);
        } catch (SecurityException ex){
            System.out.println(ex);
        }
    }
}
