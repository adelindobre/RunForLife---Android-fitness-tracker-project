package com.adelingdobre.runforlife;


import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ar.com.daidalos.afiledialog.FileChooserDialog;

import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;
import utils.ValueFormatter;

import org.gavaghan.geodesy.*;


public class CyclingFragment extends Fragment {
    private Spinner spinner;
    private Button start, choose;
    private EditText time_info;
    private EditText upload_file;
    private String time;
    private ValueFormatter vf;
    private String mFile;

    String TAG = "test";
    GeodeticCalculator geoCalc = new GeodeticCalculator();
    Ellipsoid reference = Ellipsoid.WGS84;
    GlobalPosition first;
    GlobalPosition second;

    private View.OnClickListener btnDialogSimpleOpen = new View.OnClickListener() {
        public void onClick(View v) {

            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                // Create the dialog.
                FileChooserDialog dialog = new FileChooserDialog(getActivity());
                dialog.setFilter(".*gpx");
                // Assign listener for the select event.
                dialog.addListener(onFileSelectedListener);

                // Show the dialog.
                dialog.show();

            }else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 278);
            }
        }
    };

    private FileChooserDialog.OnFileSelectedListener onFileSelectedListener = new FileChooserDialog.OnFileSelectedListener() {
        public void onFileSelected(Dialog source, File file) {
            source.hide();
            //Toast toast = Toast.makeText(getActivity(), "File selected: " + file.getName(), Toast.LENGTH_LONG);
            //toast.show();
            upload_file.setText(file.getName());
            time_info.setText("");
            mFile = file.getName();
            try {
                InputStream in = new FileInputStream(file);
                StartFragment.parsedGpx = StartFragment.mParser.parse(in);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            double time = 0;

            if (StartFragment.parsedGpx != null) {
                // log stuff

                ArrayList<TrackPoint> tp = new ArrayList<TrackPoint>();
                List<Track> tracks = StartFragment.parsedGpx.getTracks();
                for (int i = 0; i < tracks.size(); i++) {
                    Track track = tracks.get(i);
                    //Log.d(TAG, "track " + i + ":");
                    List<TrackSegment> segments = track.getTrackSegments();
                    for (int j = 0; j < segments.size(); j++) {
                        TrackSegment segment = segments.get(j);
                        //Log.d(TAG, "  segment " + j + ":");
                        for (TrackPoint trackPoint : segment.getTrackPoints()) {
                            //Log.d(TAG, " point: " + trackPoint.getLatitude() + ", " + trackPoint.getLongitude() +
                            //   ", elevation " + trackPoint.getElevation() + ", time " + trackPoint.getTime());
                            tp.add(trackPoint);
                        }
                    }
                }
                if(tp.get(0).getTime() != null) {
                    time = tp.get(tp.size() - 1).getTime().toDate().getTime() - tp.get(0).getTime().toDate().getTime();
                    time_info.setText(vf.formatTime2(time / (double) 1000 / (double) 60));

                } else {
                    toastIt("No time defined for this route");
                    time_info.setText("");
                    upload_file.setText("");
                }
            } else {
                Log.e(TAG, "Error parsing gpx track!");
            }

        }
        public void onFileSelected(Dialog source, File folder, String name) {
            source.hide();
            Toast toast = Toast.makeText(getActivity(), "File created: " + folder.getName() + "/" + name, Toast.LENGTH_LONG);
            toast.show();
        }
    };


    public CyclingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vf = new ValueFormatter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cycling, container, false);
        spinner = (Spinner)view.findViewById(R.id.typeSpinner);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.cycling, R.layout.spinner_item);
        typeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(typeAdapter);

        time_info = (EditText)view.findViewById(R.id.time_info);
        upload_file = (EditText)view.findViewById(R.id.gpx_info);

        choose = (Button)view.findViewById(R.id.choose_button);
        choose.setOnClickListener(btnDialogSimpleOpen);

        start = (Button)view.findViewById(R.id.ride_button);
        start.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {

                time = time_info.getText().toString();

                if(time.compareTo("") == 0)
                    toastIt("Please complete your activity details!");
                else{
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    Bundle args = new Bundle();
                    args.putString("type", "ride");
                    args.putString("total_time", time);
                    args.putInt("level", spinner.getSelectedItemPosition());
                    if((StartFragment.parsedGpx != null) && (upload_file.getText() != null) && (mFile != null))
                        if(upload_file.getText().toString().compareTo(mFile) == 0) {
                            args.putBoolean("gpx", true);
                        }
                        else {
                            args.putBoolean("gpx", false);
                        }
                    intent.putExtras(args);
                    getActivity().startActivity(intent);
                    getActivity().finish();
                }
            }
        });
        return view;
    }

    private void toastIt(String message){
        Toast.makeText(getActivity(), message,
                Toast.LENGTH_SHORT).show();
    }


}
