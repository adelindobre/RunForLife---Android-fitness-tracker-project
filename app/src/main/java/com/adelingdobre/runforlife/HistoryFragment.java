package com.adelingdobre.runforlife;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;

import com.adelingdobre.runforlife.R;
import runsdb.LocationDBHelper;
import runsdb.LocationDBReader;
import runsdb.Run;


public class HistoryFragment extends Fragment
    implements AdapterView.OnItemClickListener, AbsListView.MultiChoiceModeListener, FragmentCompat.OnRequestPermissionsResultCallback{

    private long selectedDate;
    private Spinner yearSpinner;
    private Spinner monthSpinner;
    private RunsListAdapter mAdapter;
    private ListView mListView;
    private Bundle spinnerSelections;
    private View mSpinnerLayout;
    private boolean selectionMode = false;

    public String usermail;
    public static int count  = 0;

    public HistoryFragment() {
    }

    public static HistoryFragment newInstance(String usermail) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString("mail", usermail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        this.usermail = args.getString("mail");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        mSpinnerLayout = inflater.inflate(R.layout.spinner_header, null);
        mListView = (ListView) view.findViewById(R.id.runsListView);
        mListView.addHeaderView(mSpinnerLayout);
        spinnerSelections = new Bundle();
        //initializing spinners with ArrayAdapters getting values from string arrays
        yearSpinner = (Spinner) mSpinnerLayout.findViewById(R.id.yearSpinner);
        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.years_array, R.layout.spinner_item);
        yearAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);
        yearSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        int year = position + 2015;
                        spinnerSelections.putInt("spinner_selection_year", year);
                        refreshList(getRuns(spinnerSelections));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
        Calendar c = Calendar.getInstance();
        int currentYear = c.get(Calendar.YEAR);
        yearSpinner.setSelection(currentYear - 2015);
        spinnerSelections.putInt("spinner_selection_year", currentYear);
        monthSpinner = (Spinner) mSpinnerLayout.findViewById(R.id.monthSpinner);
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.months_array, R.layout.spinner_item);
        monthAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);
        monthSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        spinnerSelections.putInt("spinner_selection_month", position);
                        refreshList(getRuns(spinnerSelections));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
        int currentMonth = c.get(Calendar.MONTH); //starts with 0
        monthSpinner.setSelection(currentMonth); //also starts with 0
        spinnerSelections.putInt("spinner_selection_month", currentMonth);
        mAdapter = new RunsListAdapter(getActivity(), getRuns(spinnerSelections));
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedStateInstance){
        super.onActivityCreated(savedStateInstance);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putInt("spinner_selection_year", spinnerSelections.getInt("spinner_selection_year"));
        outState.putInt("spinner_selection_month", spinnerSelections.getInt("spinner_selection_month"));
        super.onSaveInstanceState(outState);
    }

    protected void refreshList(ArrayList<Run> runs){
        //resets checked items
        //mListView.setItemChecked(-1, true);
        selectionMode = false;
        mAdapter.clear();
        mAdapter.addAll(runs);
    }

    public void update(){
        refreshList(getRuns(spinnerSelections));
    }

    protected ArrayList<Run> getRuns(final Bundle args){
        int year = args.getInt("spinner_selection_year");
        int month = args.getInt("spinner_selection_month");
        Calendar calendar = Calendar.getInstance();
        if (year != 0) {
            calendar.set(Calendar.YEAR, year);
        }
        if (month != 0) {
            calendar.set(Calendar.MONTH, month);
        }
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));

        long startTimestamp = calendar.getTimeInMillis();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));

        long endTimestamp = calendar.getTimeInMillis();

        LocationDBHelper dbHelp = new LocationDBHelper(getActivity());
        LocationDBReader dbRead = new LocationDBReader(dbHelp);

        return dbRead.getRuns(startTimestamp, endTimestamp, "ASC", usermail);
    }


    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        ArrayList<Long> selectedIds = new ArrayList<>();
        SparseBooleanArray selections = mListView.getCheckedItemPositions();
        for (int i = 0; i <= mAdapter.getCount(); i++){
            if (selections.get(i)){
                Run selectedRun = (Run) mListView.getItemAtPosition(i);
                selectedIds.add(selectedRun.id);
            }
        }

        long[] runIds = new long[selectedIds.size()];
        for (int i = 0; i < selectedIds.size(); i++){
            runIds[i] = selectedIds.get(i);
        }

        Intent intent = new Intent(getActivity(), DetailsActivity.class);
        intent.putExtra("run_ids", runIds);
        startActivity(intent);
        mode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!selectionMode) {
            //Call DetailsTabActivity with runIds = id;
            Intent intent = new Intent(getActivity(), DetailsActivity.class);
            Run selectedRun = (Run) parent.getItemAtPosition(position);
            long[] runIds = {selectedRun.id};
            intent.putExtra("run_ids", runIds);
            startActivity(intent);
        }
    }

    private void toastIt(String message){
        Toast.makeText(getActivity(), message,
                Toast.LENGTH_SHORT).show();
    }
}
