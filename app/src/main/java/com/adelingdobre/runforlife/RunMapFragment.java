package com.adelingdobre.runforlife;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import runsdb.Waypoint;
import utils.LineColors;


public class RunMapFragment extends Fragment implements OnMapReadyCallback {
    private static final String ARG_RUNIDS = "run_ids";

    private long[] runIds;
    private static GoogleMap gMap;
    private DetailsActivity detailsActivity;
    private boolean isMapsEnabled = true;

    public static RunMapFragment newInstance(long[] runIds) {
        RunMapFragment fragment = new RunMapFragment();
        Bundle args = new Bundle();
        args.putLongArray(ARG_RUNIDS, runIds);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        detailsActivity = (DetailsActivity) this.getActivity();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(detailsActivity.getBaseContext());
        isMapsEnabled = sharedPref.getBoolean("pref_maps", true);
        if (getArguments() != null) {
            runIds = getArguments().getLongArray(ARG_RUNIDS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_run_map, container, false);
        if (isMapsEnabled) {
            //get SupportMapFragment (wrapping most of the Map logic)
            SupportMapFragment smf = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapDetailsFragment);
            //calls onMapReady();
            smf.getMapAsync(this);
        }
        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isMapsEnabled) {
            SupportMapFragment map = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.mapDetailsFragment);
            if (map != null) {
                getActivity().getSupportFragmentManager().beginTransaction().remove(map).commit();
            }
            gMap = null;
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        LatLng first = null;
        for (int i = 0; i < runIds.length; i++){
            ArrayList<LatLng> waypoints = getWaypoints(runIds[i]);
            if(i == 0 && waypoints.size() > 0){
                first = waypoints.get(0);
            }
            PolylineOptions options = new PolylineOptions()
                    .width(5)
                    .color(LineColors.getColor(i))
                    .addAll(waypoints);
            gMap.addPolyline(options);
        }
        if (first != null){
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(first, 14.0f);
            gMap.animateCamera(cameraUpdate);
        }
    }

    protected ArrayList<LatLng> getWaypoints (long id){
        ArrayList<Waypoint> waypoints = detailsActivity.getWaypoints(id);
        return toLatLngs(waypoints);
    }
    protected ArrayList<LatLng> toLatLngs(ArrayList<Waypoint> waypoints){
        ArrayList<LatLng> latLngs = new ArrayList<>(waypoints.size());
        for (int i = 0; i < waypoints.size(); i++){
            latLngs.add(waypoints.get(i).toLatLng());
        }
        if (waypoints.size() != latLngs.size()){
            System.out.println("Fatal error! Lost waypoints during conversion to LatLng");
        }
        return latLngs;
    }

}
