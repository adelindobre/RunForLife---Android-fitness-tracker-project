package com.adelingdobre.runforlife;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class RunningFragment extends Fragment {

    private Spinner surfaceSpinner;
    private Spinner treadmillSpinner;
    private EditText distance_info;
    private EditText time_info;
    private Button start;
    private String dist, time;

    public RunningFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_running, container, false);

        surfaceSpinner = (Spinner)view.findViewById(R.id.surfaceSpinner);
        ArrayAdapter<CharSequence> surfaceAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.run_surface, R.layout.spinner_item);
        surfaceAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        surfaceSpinner.setAdapter(surfaceAdapter);

        treadmillSpinner = (Spinner)view.findViewById(R.id.treadmillSpinner);
        ArrayAdapter<CharSequence> treadmillAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.treadmill, R.layout.spinner_item);
        treadmillAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        treadmillSpinner.setAdapter(treadmillAdapter);

        distance_info = (EditText)view.findViewById(R.id.distance_info);
        time_info = (EditText)view.findViewById(R.id.time_info);

        start = (Button)view.findViewById(R.id.run_button);
        start.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                dist = distance_info.getText().toString();
                time = time_info.getText().toString();

                if(dist.compareTo("") == 0 || time.compareTo("") == 0)
                    toastIt("Please complete your activity details!");
                else{
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    Bundle args = new Bundle();
                    args.putString("type", "run");
                    args.putString("total_distance", dist);
                    args.putString("total_time", time);
                    args.putInt("level", surfaceSpinner.getSelectedItemPosition());
                    args.putInt("treadmill", treadmillSpinner.getSelectedItemPosition());
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
