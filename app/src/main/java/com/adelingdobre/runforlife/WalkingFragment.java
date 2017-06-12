package com.adelingdobre.runforlife;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class WalkingFragment extends Fragment {

    private Spinner spinner;
    private Button start;
    private EditText distance_info;
    private EditText time_info;
    private String dist, time;

    public WalkingFragment() {
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
        View view = inflater.inflate(R.layout.fragment_walking, container, false);
        spinner = (Spinner)view.findViewById(R.id.typeSpinner);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.level_surface, R.layout.spinner_item);
        typeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(typeAdapter);

        distance_info = (EditText)view.findViewById(R.id.distance_info);
        time_info = (EditText)view.findViewById(R.id.time_info);

        start = (Button)view.findViewById(R.id.walk_button);
        start.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {

                dist = distance_info.getText().toString();
                time = time_info.getText().toString();

                if(dist.compareTo("") == 0 ||
                        time.compareTo("") == 0)
                    toastIt("Please complete your activity details!");
                else{
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    Bundle args = new Bundle();
                    args.putString("type", "walk");
                    args.putString("total_distance", dist);
                    args.putString("total_time", time);
                    args.putInt("level", spinner.getSelectedItemPosition());
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
