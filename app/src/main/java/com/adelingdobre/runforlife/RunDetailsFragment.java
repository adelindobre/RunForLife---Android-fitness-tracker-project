package com.adelingdobre.runforlife;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import runsdb.Run;
import utils.LineColors;
import utils.ValueFormatter;

public class RunDetailsFragment extends Fragment {
    private static final String ARG_RUNIDS = "run_ids";

    private long[] runIds;
    private ArrayList<Run> runs;
    private DetailsActivity detailsActivity;
    private Context context;
    private ValueFormatter vf;

    public static RunDetailsFragment newInstance(long[] runIds) {
        RunDetailsFragment fragment = new RunDetailsFragment();
        Bundle args = new Bundle();
        args.putLongArray(ARG_RUNIDS, runIds);
        fragment.setArguments(args);
        return fragment;
    }

    public RunDetailsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            runIds = getArguments().getLongArray(ARG_RUNIDS);
        }
        vf = new ValueFormatter(getActivity().getApplicationContext());
        detailsActivity = (DetailsActivity) getActivity();
        runs = getRuns();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_run_details, container, false);
        context = getActivity();

        //Get all Layouts
        LinearLayout distanceLayout = (LinearLayout) view.findViewById(R.id.distanceLayout);
        LinearLayout timeIntervalLayout = (LinearLayout) view.findViewById(R.id.intervalLayout);
        LinearLayout maxVelocityLayout = (LinearLayout) view.findViewById(R.id.maxVelocityLayout);
        LinearLayout avgVelocityLayout = (LinearLayout) view.findViewById(R.id.avgVelocityLayout);
        LinearLayout ascentLayout = (LinearLayout) view.findViewById(R.id.ascIntervalLayout);
        LinearLayout descentLayout = (LinearLayout) view.findViewById(R.id.descIntervalLayout);
        LinearLayout breakTimeLayout = (LinearLayout) view.findViewById(R.id.totalBreakLayout);
        LinearLayout totalCaloriesLayout = (LinearLayout) view.findViewById(R.id.totalCaloriesLayout);
        LinearLayout hourCaloriesLayout = (LinearLayout) view.findViewById(R.id.hourCaloriesLayout);

        //Generate all the value layouts
        LinearLayout distanceValueLayout = new LinearLayout(context);
        distanceValueLayout.setOrientation(LinearLayout.VERTICAL);
        distanceLayout.addView(distanceValueLayout);

        LinearLayout timeIntervalValueLayout = new LinearLayout(context);
        timeIntervalValueLayout.setOrientation(LinearLayout.VERTICAL);
        timeIntervalLayout.addView(timeIntervalValueLayout);

        LinearLayout maxVelocityValueLayout = new LinearLayout(context);
        maxVelocityValueLayout.setOrientation(LinearLayout.VERTICAL);
        maxVelocityLayout.addView(maxVelocityValueLayout);

        LinearLayout avgVelocityValueLayout = new LinearLayout(context);
        avgVelocityValueLayout.setOrientation(LinearLayout.VERTICAL);
        avgVelocityLayout.addView(avgVelocityValueLayout);

        LinearLayout ascentValueLayout = new LinearLayout(context);
        ascentValueLayout.setOrientation(LinearLayout.VERTICAL);
        ascentLayout.addView(ascentValueLayout);

        LinearLayout descentValueLayout = new LinearLayout(context);
        descentValueLayout.setOrientation(LinearLayout.VERTICAL);
        descentLayout.addView(descentValueLayout);

        LinearLayout breakTimeValueLayout = new LinearLayout(context);
        breakTimeValueLayout.setOrientation(LinearLayout.VERTICAL);
        breakTimeLayout.addView(breakTimeValueLayout);

        LinearLayout totalCaloriesValueLayout = new LinearLayout(context);
        totalCaloriesValueLayout.setOrientation(LinearLayout.VERTICAL);
        totalCaloriesLayout.addView(totalCaloriesValueLayout);

        LinearLayout hourCaloriesValueLayout = new LinearLayout(context);
        hourCaloriesValueLayout.setOrientation(LinearLayout.VERTICAL);
        hourCaloriesLayout.addView(hourCaloriesValueLayout);

        //populate Layouts with TextViews and values
        for (int i = 0; i < runs.size(); i++){
            Run run = runs.get(i);

            int k = 8;
            //set up all TextViews
            TextView distanceView = new TextView(context);
            distanceView.setText(vf.formatDistance(run.distance));
            setUpTextView(distanceView, k);

            TextView timeIntervalView = new TextView(context);
            timeIntervalView.setText(vf.formatTimeInterval(run.timeInterval));
            setUpTextView(timeIntervalView, k);

            TextView maxVelocityView = new TextView(context);
            maxVelocityView.setText(vf.formatVelocity(run.maxVelocity));
            setUpTextView(maxVelocityView, k);

            TextView avgVelocityView = new TextView(context);
            avgVelocityView.setText(vf.formatVelocity(run.medVelocity));
            setUpTextView(avgVelocityView, k);

            TextView ascentView = new TextView(context);
            ascentView.setText(vf.formatDistance(run.ascendInterval));
            setUpTextView(ascentView, k);

            TextView descentView = new TextView(context);
            descentView.setText(vf.formatDistance(run.descendInterval));
            setUpTextView(descentView, k);

            TextView breakView = new TextView(context);
            breakView.setText(vf.formatTimeInterval(run.breakTime));
            setUpTextView(breakView, k);

            TextView totalCalories = new TextView(context);
            totalCalories.setText(vf.formatCalories(run.calories));
            setUpTextView(totalCalories, k);

            double time = run.timeInterval / (double)1000 / (double)60 / (double)60;
            double caloriesPerHour = run.calories / time;
            TextView hourCalories = new TextView(context);
            hourCalories.setText(vf.formatCalories2(caloriesPerHour));
            setUpTextView(hourCalories, k);

            //add TextViews to value layouts
            distanceValueLayout.addView(distanceView);
            timeIntervalValueLayout.addView(timeIntervalView);
            maxVelocityValueLayout.addView(maxVelocityView);
            avgVelocityValueLayout.addView(avgVelocityView);
            ascentValueLayout.addView(ascentView);
            descentValueLayout.addView(descentView);
            breakTimeValueLayout.addView(breakView);
            totalCaloriesLayout.addView(totalCalories);
            hourCaloriesLayout.addView(hourCalories);
        }
        return view;
    }

    private void setUpTextView(TextView tv, int index){
        tv.setTextAppearance(context, R.style.TextAppearance_AppCompat_Medium);
        tv.setTextColor(LineColors.getColor(index));
        tv.setGravity(Gravity.RIGHT);
    }

    private ArrayList<Run> getRuns(){
        return detailsActivity.getRuns(runIds);
    }
}
