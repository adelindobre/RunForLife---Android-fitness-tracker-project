package com.adelingdobre.runforlife;


import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import java.util.ArrayList;

import runsdb.LocationDBHelper;
import runsdb.LocationDBReader;
import runsdb.Run;
import runsdb.Waypoint;
import runsdb.Segment;

public class DetailsActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private AppBarLayout appBarLayout;

    private LocationDBHelper dbHelp;
    private LocationDBReader dbRead;

    private RunMapFragment mapFragment;
    private RunDetailsFragment detailsFragment;
    protected long[] runIds;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        runIds = getIntent().getLongArrayExtra("run_ids");
        setContentView(R.layout.activity_details);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                turnOffTabLayoutScrolling();
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });
        mViewPager.setOffscreenPageLimit(2);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        dbHelp = new LocationDBHelper(this);
        dbRead = new LocationDBReader(dbHelp);
    }

    protected ArrayList<Waypoint> getWaypoints(long id){
        return dbRead.getWaypoints(id);
    }

    protected ArrayList<Segment> getSegments(long id){
        return dbRead.getSegments(id);
    }

    public ArrayList<Run> getRuns(long[] runIds) {
        ArrayList<Run> runs = new ArrayList<>();
        for (int i = 0; i < runIds.length; i++){
            runs.add(dbRead.getRun(runIds[i]));
        }
        return runs;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    protected void turnOffTabLayoutScrolling() {
        //turn off scrolling
        AppBarLayout.LayoutParams toolbarLayoutParams = (AppBarLayout.LayoutParams) mTabLayout.getLayoutParams();
        toolbarLayoutParams.setScrollFlags(0);
        CoordinatorLayout.LayoutParams appBarLayoutParams = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        appBarLayoutParams.setBehavior(null);
        appBarLayout.setLayoutParams(appBarLayoutParams);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position){
                case 0:
                    if (mapFragment == null) {
                        mapFragment = RunMapFragment.newInstance(runIds);
                    }
                    return mapFragment;

                case 1:
                    if (detailsFragment == null){
                        detailsFragment = RunDetailsFragment.newInstance(runIds);
                    }
                    return detailsFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Map";
                case 1:
                    return "Details";
            }
            return null;
        }
    }
}
