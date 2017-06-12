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

import java.util.ArrayList;

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
    boolean details_received = false;
    UsersDB usersDB;

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

        usersDB = MainActivity.usersDB;
        walk_calculator = new WalkingCalculator();
        run_calculator = new RunningCalculator();
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
        mcallback.onRegisteredRun();
        buttonBlocked = false;
        updateViews(updatedRun);
        run = updatedRun;
    }

    public void updateViews(Bundle updatedRun){
        //timeInterval in miliseconds
        //distance in meters
        // velocity in meters per second
        long timeInterval = updatedRun.getLong("timeInterval", 0);
        double distance = updatedRun.getDouble("distance", 0.0d);
        double velocity = updatedRun.getDouble("velocity", 0.0d);
        if(activity_type.compareTo("walk") == 0){
            walk_calculator.setCurrentParameters(distance, timeInterval, velocity);
            walk_calculator.setCurrentCaloriesBurned();
            walk_calculator.setEstimatedCalories();
            kaburnedView.setText(vf.formatCalories(walk_calculator.current_calories_burned));
            kaestimView.setText(vf.formatCalories(walk_calculator.estimated_calories));
        }
        if(activity_type.compareTo("run") == 0){
            run_calculator.setCurrentParameters(distance, timeInterval);
            run_calculator.setCurrentCaloriesBurned();
            run_calculator.setEstimatedCalories();
            kaburnedView.setText(vf.formatCalories(run_calculator.current_calories_burned));
            kaestimView.setText(vf.formatCalories(run_calculator.estimated_calories));
        }
        durationView.setText(vf.formatTimeInterval(timeInterval));
        distanceView.setText(vf.formatDistance(distance));
        velocityView.setText(vf.formatVelocity(velocity));
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

        if((user.getWeight().compareTo("") == 0) || (user.getHeight().compareTo("") == 0) ||
                (user.getAge().compareTo("") == 0) || (user.getHeartRate().compareTo("") == 0) ||
                (user.getGender().compareTo("") == 0))
        {
            toastIt("Complete your profile before starting");
            return false;
        }else
            return true;
    }

    public void getInitialData(Bundle args){
        details_received = true;
        User user = usersDB.getUserByEmail(usermail);
        activity_type = args.getString("type");
        double total_dist, total_time;
        double level, treadmill;
        double weight, height, age, heartrate;
        String gender;
        if(activity_type.compareTo("walk") == 0){
            total_dist = Double.parseDouble(args.getString("total_distance"));
            total_time = Double.parseDouble(args.getString("total_time"));
            level = (double)args.getInt("level");
            weight = Double.parseDouble(user.getWeight());
            //toastIt(weight + " " + total_dist + " " + total_time + " " + level);
            walk_calculator.setInitialParameters(weight, total_dist, total_time, level);
            walk_calculator.setTotalCaloriesBurned();
            toastIt("Total calories to burn: " + vf.formatCalories(walk_calculator.total_calories_burned));
        }
        if(activity_type.compareTo("run") == 0){
            total_dist = Double.parseDouble(args.getString("total_distance"));
            total_time = Double.parseDouble(args.getString("total_time"));
            level = (double)args.getInt("level");
            treadmill = (double)args.getInt("treadmill");
            age = Double.parseDouble(user.getAge());
            weight = Double.parseDouble(user.getWeight());
            height = Double.parseDouble(user.getHeight());
            heartrate = Double.parseDouble(user.getHeartRate());
            gender = user.getGender();
            run_calculator.setInitialParameters(age, weight, height, heartrate, treadmill, level,
                    total_dist, total_time, gender);
            run_calculator.setTotalCaloriesBurned();
            toastIt("Total calories to burn: " + vf.formatCalories(run_calculator.total_calories_burned));
        }
    }
}
