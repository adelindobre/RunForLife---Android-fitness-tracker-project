package com.adelingdobre.runforlife;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;
import runsdb.LocationDBHelper;
import runsdb.LocationDBReader;
import runsdb.LocationDBWriter;
import utils.LineColors;
import utils.User;
import utils.UsersDB;
import utils.ValueFormatter;

public class StartFragment extends Fragment implements OnMapReadyCallback {

    private static GoogleMap gMap;
    private MainActivity drawAct;
    private TextView distanceView;
    private TextView durationView;
    private TextView velocityView;
    private TextView kaburnedView;
    private TextView kaestimView;
    private Polyline path;
    private ArrayList<LatLng> waypoints;
    private PolylineOptions plOpt;
    private boolean buttonBlocked = false;
    private Button button;
    private boolean isMapsEnabled = true;
    private ValueFormatter vf;
    private Bundle run;

    String usermail, activity_type;
    OnButtonPressed mcallback;

    WalkingCalculator walk_calculator;
    RunningCalculator run_calculator;
    CyclingCalculator ride_calculator;

    boolean details_received = false;
    boolean inital_route_drawn = false; //pt desenarea traseului din fisierul gpx
    boolean first_time_current_drawing = false; //pt golirea hartii la start-ul GPS-ului

    UsersDB usersDB;
    LocationDBReader dbReader;
    LocationDBWriter dbWriter;
    LocationDBHelper dbHelper;

    static GPXParser mParser = new GPXParser();
    static Gpx parsedGpx = null;

    public interface OnButtonPressed {
        public void onRegisteredRun();
    }

    public StartFragment() {
        // Required empty public constructor
    }

    public static StartFragment newInstance(String usermail) {
        StartFragment fragment = new StartFragment();
        Bundle args = new Bundle();
        args.putString("mail", usermail);
        fragment.setArguments(args);
        return fragment;
    }

//        Log.d("test", user.getUsername() + " " + user.getEmail() + " " + user.getPassword() + " " +
//                user.getWeight() + " " + user.getHeight() + " " + user.getHeartRate() + " " + user.getAge());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        this.usermail = args.getString("mail");

        drawAct = (MainActivity) this.getActivity();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(drawAct.getBaseContext());
        isMapsEnabled = sharedPref.getBoolean("pref_maps", true);
        vf = new ValueFormatter(drawAct.getApplicationContext());
        waypoints = new ArrayList<>();

        usersDB = new UsersDB(getActivity());
        dbHelper = new LocationDBHelper(getActivity());
        dbReader = new LocationDBReader(dbHelper);
        dbWriter = new LocationDBWriter(dbHelper);

        walk_calculator = new WalkingCalculator();
        run_calculator = new RunningCalculator();
        ride_calculator = new CyclingCalculator();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_start, container, false);
        button = (Button) view.findViewById(R.id.controlButton);
        button.setText(getString(R.string.start));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!buttonBlocked) {
                    if (!StartFragment.this.drawAct.isLocationServiceBound() && checkProfile()) {
                        if(!details_received) {
                            walk_calculator.reset();
                            run_calculator.reset();
                            ride_calculator.reset();
                            Intent intent = new Intent(getActivity(), OptionsActivity.class);
                            getActivity().startActivity(intent);
                        }
                        //toastIt("details_received este = "  + details_received);
                        if(details_received){
                            buttonBlocked = true;
                            StartFragment.this.drawAct.startLocationService(StartFragment.this);
                            button.setText(getString(R.string.searching_signal));
                        }
                    } else {
                        StartFragment.this.drawAct.stopLocationService();
                        button.setText(getString(R.string.start));
                        mcallback.onRegisteredRun();
                    }
                }
            }
        });
        kaburnedView = (TextView) view.findViewById(R.id.caloriesBurnedView);
        kaestimView  = (TextView) view.findViewById(R.id.caloriesEstimatedView);
        distanceView = (TextView) view.findViewById(R.id.distanceView);
        durationView = (TextView) view.findViewById(R.id.durationView);
        velocityView = (TextView) view.findViewById(R.id.currentVelocityView);
        velocityView.setText(vf.formatVelocity(0.0d));

        if (isMapsEnabled) {
            //get SupportMapFragment (wrapping most of the Map logic)
            SupportMapFragment smf = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
            //calls onMapReady();
            smf.getMapAsync(this);
        }
        return view;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        if (isMapsEnabled) {
            //check if gMap had been instantiated successfully
            if (gMap != null) {
                //remove gMap from FragmentManager, otherwise next time the fragment is loaded, the application crashes
                //getChildFragmentManager().beginTransaction().remove(getChildFragmentManager().findFragmentById(R.id.mapFragment)).commit();
                gMap = null;
            }
        }
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        Activity activity = getActivity();
        try{
            mcallback = (OnButtonPressed)activity;
        } catch(ClassCastException e){
            throw new ClassCastException(activity.toString());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            ArrayList<Parcelable> parcelable = savedInstanceState.getParcelableArrayList("waypoints");
            if (waypoints.size() == 0 && parcelable != null) {
                if (parcelable.size() > 0) {
                    for (int i = 0; i < parcelable.size(); i++) {
                        waypoints.add((LatLng) parcelable.get(i));
                    }
                    updatePolyline(waypoints, waypoints.get(waypoints.size() - 1));
                }
            }
            if (savedInstanceState.containsKey("run")) {
                Bundle run = savedInstanceState.getBundle("run");
                if (run != null) {
                    update(savedInstanceState.getBundle("run"));
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        if (drawAct.mBound){
            if (run != null){
                outState.putBundle("run", run);
            }
            if (waypoints != null) {
                outState.putParcelableArrayList("waypoints", waypoints);
            }
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //Enable button to show "my location" (using GMaps API v2)
            gMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        if(first_time_current_drawing){
            gMap.clear();
            first_time_current_drawing = false;
        }
        if(inital_route_drawn){
            gMap.clear();
            LatLng first = null;
            ArrayList<LatLng> waypoints = getInitialPointsGpx();

            if(waypoints.size() > 0){
                first = waypoints.get(0);
            }

            PolylineOptions options = new PolylineOptions()
                    .width(5)
                    .color(LineColors.getColor(2))
                    .addAll(waypoints);
            gMap.addPolyline(options);

            if (first != null){
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(first, 14.0f);
                gMap.animateCamera(cameraUpdate);
            }
            inital_route_drawn = false;
        }
        //Create PolylineOptions with a 25px thick, blue line
        plOpt = new PolylineOptions()
                .width(5)
                .color(Color.BLUE);
        //addPolyline through adding the PolylineOptions, waypoints are added in updateMap()
        path = gMap.addPolyline(plOpt);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, proceed to the normal flow.
                    gMap.setMyLocationEnabled(true);
                }
                break;
        }
    }

    protected void unblockButton(){
        buttonBlocked = false;
        details_received = false;
    }

    public void update(Bundle updatedRun, LatLng latestPosition){
        update(updatedRun);
        if (isMapsEnabled) {
            updateMap(latestPosition);
        }
    }

    protected void update(Bundle updatedRun){
        button.setText(R.string.stop);
        buttonBlocked = false;
        updateViews(updatedRun);
        run = updatedRun;
    }

    public void updateViews(Bundle updatedRun){
        //timeInterval in miliseconds
        //distance in meters
        // velocity in meters per second
        long id = updatedRun.getLong("id");
        long timeInterval = updatedRun.getLong("timeInterval", 0);
        double distance = updatedRun.getDouble("distance", 0.0d);
        double velocity = updatedRun.getDouble("velocity", 0.0d);
        double heightInterval = updatedRun.getDouble("heightInterval");
        double caloriesBurned = 0;

        if(activity_type.compareTo("walk") == 0){
            walk_calculator.setCurrentParameters(distance, timeInterval, velocity, heightInterval);
            walk_calculator.setCurrentCaloriesBurned();
            walk_calculator.setEstimatedCalories();
            kaburnedView.setText(vf.formatCalories(walk_calculator.current_calories_burned));
            kaestimView.setText(vf.formatCalories(walk_calculator.estimated_calories));
            caloriesBurned = walk_calculator.current_calories_burned;
        }
        if(activity_type.compareTo("run") == 0){
            run_calculator.setCurrentParameters(distance, timeInterval, heightInterval);
            run_calculator.setCurrentCaloriesBurned();
            run_calculator.setEstimatedCalories();
            kaburnedView.setText(vf.formatCalories(run_calculator.current_calories_burned));
            kaestimView.setText(vf.formatCalories(run_calculator.estimated_calories));
            caloriesBurned = run_calculator.current_calories_burned;
        }
        if(activity_type.compareTo("ride") == 0){
            ride_calculator.setCurrentParameters(timeInterval);
            ride_calculator.setCurrentCaloriesBurned();
            ride_calculator.setEstimatedCalories();
            kaburnedView.setText(vf.formatCalories(ride_calculator.current_calories_burned));
            kaestimView.setText(vf.formatCalories(ride_calculator.total_calories_burned));
            caloriesBurned = ride_calculator.current_calories_burned;
        }
        durationView.setText(vf.formatTimeInterval(timeInterval));
        distanceView.setText(vf.formatDistance(distance));
        velocityView.setText(vf.formatVelocity(velocity));

        dbWriter.updateCaloriesRun(id, caloriesBurned, dbReader);
    }

    protected void updateMap(LatLng latestPosition){
        if (waypoints == null){
            waypoints = new ArrayList<>();
        }
        waypoints.add(latestPosition);
        updatePolyline(waypoints, latestPosition);
    }

    private void updatePolyline (final ArrayList<LatLng> points, final LatLng latestPosition){
        if (gMap != null) {
            if (path == null) {
                path = gMap.addPolyline(plOpt);
            }
            path.setPoints(points);
            float currentZoomLevel = gMap.getCameraPosition().zoom;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latestPosition, currentZoomLevel);
            gMap.animateCamera(cameraUpdate);
        } else {
            SupportMapFragment smf = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
            smf.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    StartFragment.this.onMapReady(googleMap);
                    updatePolyline(points, latestPosition);
                }
            });
        }
    }

    public void reset(){
        resetViews();
        run = null;
        if (isMapsEnabled) {
            resetMap();
        }
    }

    protected void resetViews(){
        button.setText(R.string.start);
        durationView.setText(getString(R.string.duration_dummy));
        distanceView.setText(getString(R.string.distance_dummy));
        velocityView.setText(vf.formatVelocity(0.0d));
        kaburnedView.setText("0.0 Ka");
        kaestimView.setText("0.0 Ka");
    }

    protected void resetMap(){
        if (waypoints != null){
            waypoints.clear();
        }
        if (path != null) {
            path.remove();
            path = null;
        }
    }

    private void toastIt(String message){
        Toast.makeText(getActivity(), message,
                Toast.LENGTH_SHORT).show();
    }

    public boolean checkProfile(){
        User user = usersDB.getUserByEmail(usermail);

        if(user.getWeight() == null || user.getHeight() == null ||
                user.getAge() == null || user.getHeartRate() == null || user.getGender() == null){
            toastIt("Complete your profile before starting");
            return false;
        }

        if((user.getWeight().compareTo("") == 0) || (user.getHeight().compareTo("") == 0) ||
                (user.getAge().compareTo("") == 0) || (user.getHeartRate().compareTo("") == 0) ||
                (user.getGender().compareTo("") == 0)) {
            toastIt("Complete your profile before starting");
            return false;
        }
        return true;
    }

    public void getInitialData(Bundle args){
        details_received = true;
        User user = usersDB.getUserByEmail(usermail);
        activity_type = args.getString("type");
        double total_dist, total_time;
        double level, treadmill;
        double weight, height, age, heartrate;
        boolean gpxTest = args.getBoolean("gpx");
        String gender;

        if(activity_type.compareTo("walk") == 0){
            //total_dist in km, total_time in minutes
            // level selected item position, weight in kg
            total_dist = Double.parseDouble(args.getString("total_distance"));
            total_time = Double.parseDouble(args.getString("total_time"));
            level = (double)args.getInt("level");
            weight = Double.parseDouble(user.getWeight());
            //toastIt(weight + " " + total_dist + " " + total_time + " " + level);
            if(gpxTest == false){
                parsedGpx = null;
                first_time_current_drawing = true; //pt golirea hartii la inceperea cursei
                if (isMapsEnabled) {
                    //get SupportMapFragment (wrapping most of the Map logic)
                    SupportMapFragment smf = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
                    //calls onMapReady();
                    smf.getMapAsync(this);
                }
                walk_calculator.setInitialParameters(weight, total_dist, total_time, level);
                walk_calculator.setTotalCaloriesBurned();
            } else
                computeInitialWalkRoute(weight);

            toastIt("Total calories to burn: " + vf.formatCalories(walk_calculator.total_calories_burned));
        }

        if(activity_type.compareTo("run") == 0){
            //total_dist in km, total_time in minutes, level - item pos
            //weight in kg, height in cm
            total_dist = Double.parseDouble(args.getString("total_distance"));
            total_time = Double.parseDouble(args.getString("total_time"));
            level = (double)args.getInt("level");
            treadmill = (double)args.getInt("treadmill");
            age = Double.parseDouble(user.getAge());
            weight = Double.parseDouble(user.getWeight());
            height = Double.parseDouble(user.getHeight());
            heartrate = Double.parseDouble(user.getHeartRate());
            gender = user.getGender();

            if(gpxTest == false){
                parsedGpx = null;
                first_time_current_drawing = true; //pt golirea hartii la inceperea cursei
                if (isMapsEnabled){
                    //get SupportMapFragment (wrapping most of the Map logic)
                    SupportMapFragment smf = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
                    //calls onMapReady();
                    smf.getMapAsync(this);
                }
                run_calculator.setInitialParameters(age, weight, height, heartrate, treadmill, level,
                        total_dist, total_time, gender);
                run_calculator.setTotalCaloriesBurned();
            } else
                computeInitialRunRoute(age, weight, height, heartrate, treadmill, gender);

            toastIt("Total calories to burn: " + vf.formatCalories(run_calculator.total_calories_burned));
        }

        if(activity_type.compareTo("ride") == 0){
            //total_time in minutes, height in cm
            //level selected item position, weight in kg
            total_time = Double.parseDouble(args.getString("total_time"));
            level = (double)args.getInt("level");
            age = Double.parseDouble(user.getAge());
            weight = Double.parseDouble(user.getWeight());
            height = Double.parseDouble(user.getHeight());
            gender = user.getGender();

            if(gpxTest == false){
                parsedGpx = null;
                first_time_current_drawing = true;
                if(isMapsEnabled){
                    //get SupportMapFragment (wrapping most of the Map logic)
                    SupportMapFragment smf = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
                    //calls onMapReady();
                    smf.getMapAsync(this);
                }
                ride_calculator.setInitialParameters(gender, age, weight, height, total_time, level);
                ride_calculator.setTotalCaloriesBurned();
            } else
                computeInitialBikeRoute(gender, age, weight, height, total_time, level);

            toastIt("Total calories to burn: " + vf.formatCalories(ride_calculator.total_calories_burned));
        }
    }

    public void computeCaloriesPerSegment(List<TrackPoint> ltp, int type){

        GeodeticCalculator geoCalc = new GeodeticCalculator();
        Ellipsoid reference = Ellipsoid.WGS84;
        GlobalPosition first;
        GlobalPosition second;

        double segment_dist = 0; // in meters
        double segment_time = 0; // in minutes
        double segment_slope = 0; // item position

        for(int i  = 1; i < ltp.size(); i++){
            first = new GlobalPosition(ltp.get(i-1).getLatitude(), ltp.get(i-1).getLongitude(), 0);
            second = new GlobalPosition(ltp.get(i).getLatitude(), ltp.get(i).getLongitude(), 0);
            segment_dist += geoCalc.calculateGeodeticCurve(reference, first, second).getEllipsoidalDistance();
        }
        if(ltp.get(0).getTime() != null){
            segment_time = ltp.get(ltp.size() - 1).getTime().toDate().getTime() - ltp.get(0).getTime().toDate().getTime();
            segment_time = segment_time / (double)1000 / (double)60;
        }
        //heightInterval in meters
        double heightInterval = ltp.get(ltp.size() - 1).getElevation() - ltp.get(0).getElevation();

        if(type == 0){
            segment_slope = walk_calculator.computeSlope(heightInterval, segment_dist);
            walk_calculator.total_calories_burned += walk_calculator.getCalories(segment_dist, segment_time, segment_slope);
        }
        if(type == 1){
            segment_slope = run_calculator.computeSlope(heightInterval, segment_dist);
            segment_dist = segment_dist / (double)1000; // in km
            run_calculator.total_calories_burned += run_calculator.getGrossCalories(run_calculator.getCalories(segment_dist, segment_slope), segment_time);
        }
    }

    public void computeCalories(List<TrackSegment> segments, int type){

        List<TrackPoint> seg_ltp= null;

        if(segments.size() == 1){
            List<TrackPoint> ltp = segments.get(0).getTrackPoints();
            int nr_tenpoints_segments = ltp.size() / 10;

            for(int i = 0; i < nr_tenpoints_segments; i++){
                seg_ltp = new ArrayList<TrackPoint>();
                for(int j = i * 10; j < (i*10 + 10); j++)
                    seg_ltp.add(ltp.get(j));
                computeCaloriesPerSegment(seg_ltp, type);
            }
            seg_ltp = new ArrayList<TrackPoint>();
            for(int j = nr_tenpoints_segments * 10; j < ltp.size(); j++)
                seg_ltp.add(ltp.get(j));

            if(seg_ltp.size() > 1)
                computeCaloriesPerSegment(seg_ltp, type);
        }
        if(segments.size() > 1){
            for(int i = 0; i < segments.size(); i++){
                seg_ltp = segments.get(i).getTrackPoints();
                computeCaloriesPerSegment(seg_ltp, type);
            }
        }
    }

    public void computeInitialWalkRoute(double weight){
        if(parsedGpx != null) {
            if (isMapsEnabled) {
                //get SupportMapFragment (wrapping most of the Map logic)
                SupportMapFragment smf = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
                inital_route_drawn = true;
                //calls onMapReady();
                smf.getMapAsync(this);
            }
            //get the only one track from gpx
            Track track = parsedGpx.getTracks().get(0);
            List<TrackSegment> segments = track.getTrackSegments();

            walk_calculator.setInitialParameters(weight);
            computeCalories(segments, 0);
        }
    }

    public void computeInitialRunRoute(double age, double weight, double height, double heartrate, double treadmill,
                                             String gender){
        if(parsedGpx != null){
            if (isMapsEnabled) {
                //get SupportMapFragment (wrapping most of the Map logic)
                SupportMapFragment smf = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
                inital_route_drawn = true;
                //calls onMapReady();
                smf.getMapAsync(this);
            }
            //get the only one track from gpx
            Track track = parsedGpx.getTracks().get(0);
            List<TrackSegment> segments = track.getTrackSegments();

            run_calculator.setInitialParameters(age, weight, height, heartrate, treadmill, gender);
            computeCalories(segments, 1);
        }
    }

    public void computeInitialBikeRoute(String gender, double age, double weight, double height, double total_time, double level){
        if(parsedGpx != null){
            if (isMapsEnabled) {
                //get SupportMapFragment (wrapping most of the Map logic)
                SupportMapFragment smf = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
                inital_route_drawn = true;
                //calls onMapReady();
                smf.getMapAsync(this);
            }
            //get the only one track from gpx
            Track track = parsedGpx.getTracks().get(0);
            List<TrackSegment> segments = track.getTrackSegments();

            ride_calculator.setInitialParameters(gender, age, weight, height, total_time, level);
            ride_calculator.setTotalCaloriesBurned();
        }
    }

    public ArrayList<LatLng> getInitialPointsGpx() {

        ArrayList<LatLng> tp = new ArrayList<LatLng>();;

        if (parsedGpx != null) {
            List<Track> tracks = parsedGpx.getTracks();
            for (int i = 0; i < tracks.size(); i++) {
                Track track = tracks.get(i);
                List<TrackSegment> segments = track.getTrackSegments();
                for (int j = 0; j < segments.size(); j++) {
                    TrackSegment segment = segments.get(j);
                    for (TrackPoint trackPoint : segment.getTrackPoints()) {
                        tp.add(new LatLng(trackPoint.getLatitude(), trackPoint.getLongitude()));
                    }
                }
            }
        }
        return tp;
    }
}
