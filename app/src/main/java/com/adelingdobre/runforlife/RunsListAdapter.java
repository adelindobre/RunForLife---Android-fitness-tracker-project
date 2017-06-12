package com.adelingdobre.runforlife;

/**
 * Created by AdelinGDobre on 6/7/2017.
 */

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import runsdb.Run;
import utils.ValueFormatter;

public class RunsListAdapter extends ArrayAdapter<Run> {
   ValueFormatter vf;

    public RunsListAdapter(Context context, ArrayList<Run> runs){
        super(context, 0, runs);
        vf = new ValueFormatter(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Run run = getItem(position);

        convertView = LayoutInflater.from(getContext()).inflate(R.layout.runs_item, parent, false);

        TextView timestampView = (TextView) convertView.findViewById(R.id.runItemDateView);
        TextView distanceView = (TextView) convertView.findViewById(R.id.runItemDistanceView);
        TextView durationView = (TextView) convertView.findViewById(R.id.runItemDurationView);

        timestampView.setText(vf.formatDate(run.timestamp));
        distanceView.setText(vf.formatDistance(run.distance));
        durationView.setText(vf.formatTimeInterval(run.timeInterval));
        return convertView;
    }
}
