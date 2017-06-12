package com.adelingdobre.runforlife;

import android.Manifest;

import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;

import android.net.Uri;

import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;

import android.os.Bundle;
import android.os.IBinder;

import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.TextView;
import android.widget.Toast;


import android.app.NotificationManager;
import android.app.PendingIntent;

import com.adelingdobre.runforlife.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.maps.model.LatLng;

import service_utils.LocationService;

import java.util.ArrayList;
import java.util.List;

import facebook_utils.SharedPreferenceManager;
import facebook_utils.UserModel;
import utils.UsersDB;

//-----------------------------------------------

public class MainActivity extends AppCompatActivity
        implements LocationService.OnDataChangedListener, StartFragment.OnButtonPressed{

    NavigationView navigationView;
    DrawerLayout drawerLayout;
    SimpleDraweeView simpleDraweeView;
    TextView nameTextView;
    TextView emailTextView;
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;

    //-----------------------------------------------
    private final int ACCESS_LOCATION_FINE_REQUEST = 1;
    LocationService locServ;
    boolean mBound = false;
    public static final String ACTION_STOP = "com.adelingdobre.runforlife.stop_location_service";
    private BroadcastReceiver receiver;
    private Bundle currentRun;
    private StartFragment startFragment;
    //-----------------------------------------------
    private UserModel userModel;
    public static UsersDB usersDB;

    @Override
    public void onRegisteredRun() {
        FragmentPagerAdapter adapter = (FragmentPagerAdapter)viewPager.getAdapter();
        HistoryFragment historyFragment = (HistoryFragment)adapter.getItem(1);
        historyFragment.update();
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usersDB = new UsersDB(this);
        userModel = getUserModelFromIntent();

        Log.d("user", userModel.userName + " " + userModel.userEmail + " " + userModel.profilePic + "\n"
                + userModel.password + " " + userModel.gender);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        drawerLayout = (DrawerLayout) findViewById(R.id.main_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.icon_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        viewPager  = (ViewPager) findViewById(R.id.id_viewpager);
        viewPager.setOffscreenPageLimit(3);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.id_tablayout);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setupWithViewPager(viewPager);

        if(userModel!= null)
            setDataOnNavigationView(userModel);

        //-----------------------------------------------
        FragmentPagerAdapter adapter = (FragmentPagerAdapter)viewPager.getAdapter();
        startFragment = (StartFragment) adapter.getItem(0);

        setUpBroadcastReceiver();
        //-----------------------------------------------
    }

    //-----------------------------------------------
    private void setUpBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_STOP);
        receiver = new StopBroadcastReceiver();
        registerReceiver(receiver, intentFilter);
    }

    protected class StopBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case ACTION_STOP:
                    ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                            .cancelAll();
                    MainActivity.this.stopLocationService();
                    break;
            }
        }
    }
    @Override
    protected void onPause(){
        super.onPause();
        if (mBound){
            issueNotification();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (mBound){
            locServ.stopForeground(true);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        startFragment.getInitialData(intent.getExtras());
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        removeReceiver();
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBundle("run", currentRun);
        //getSupportFragmentManager().putFragment(outState, "ovFragment", startFragment);
    }

    public boolean startLocationService(StartFragment overviewFragment){
        if (!mBound) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(this, service_utils.LocationService.class);
                boolean successful = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                System.out.println(successful);
                return successful;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        ACCESS_LOCATION_FINE_REQUEST);
                return false;
            }
        } else {
            return false;
        }
    }

    public void stopLocationService(){
        try {
            unbindService(mConnection);
        } catch (RuntimeException ex){
            System.out.println(ex);
        }
        startFragment.reset();
        locServ = null;
        mBound = false;
        currentRun = null;
    }

    @Override
    public void onDataChanged(Bundle run) {
        currentRun = run;
        LatLng position = run.getParcelable("position");
        startFragment.update(run, position);
    }

    public boolean isLocationServiceBound(){
        return mBound;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocationBinder binder = (LocationService.LocationBinder) service;
            MainActivity.this.locServ = binder.getService();
            MainActivity.this.startFragment.unblockButton();
            boolean successful = MainActivity.this.locServ.initialize(userModel.userEmail);
            if (successful){
                MainActivity.this.locServ.addOnDataChangedListener(MainActivity.this);
                MainActivity.this.mBound = true;
                System.out.println("onServiceConnected was called");
            } else {
                Toast toast = Toast.makeText(MainActivity.this, getString(R.string.activate_gps), Toast.LENGTH_LONG);
                toast.show();
                stopLocationService();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            MainActivity.this.mBound = false;
            System.out.println("Service successfully disconnected!");
        }
    };

    private void removeReceiver() {
        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException ex){
            System.out.println(ex);
        }
    }

    private void issueNotification(){
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
        //TODO: add proper icons
        //nBuilder.setSmallIcon(R.drawable.ic_stat_name2);
        nBuilder.setContentTitle(getString(R.string.app_name));
        nBuilder.setContentText(getString(R.string.running));
        Intent overviewIntent = this.getIntent();
        overviewIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingOverviewIntent = PendingIntent.getActivity(getApplicationContext(), 0, overviewIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        nBuilder.setContentIntent(pendingOverviewIntent);

        int icon = R.drawable.ic_stop_icon;
        String actionTitle = getString(R.string.stop_recording);
        Intent stopIntent = new Intent();
        stopIntent.setAction(ACTION_STOP);
        PendingIntent pendingStopIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, stopIntent, 0);
        NotificationCompat.Action action = new NotificationCompat.Action(icon, actionTitle, pendingStopIntent);
        nBuilder.addAction(action);
        nBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        //Disable dismissability
        nBuilder.setOngoing(true);
        locServ.startForeground(1, nBuilder.build());
    }
    //-----------------------------------------------

    private void setupViewPager(ViewPager viewPager){
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(StartFragment.newInstance(userModel.userEmail), "Start");
        adapter.addFragment(HistoryFragment.newInstance(userModel.userEmail), "History");
        adapter.addFragment(ProfileFragment.newInstance(userModel), "Profile");
        viewPager.setAdapter(adapter);
    }
    private void setDataOnNavigationView(UserModel userModel){
        if(navigationView != null){
            setupDrawerContent(userModel);
        }

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        switch (menuItem.getItemId()) {
                            case R.id.nav_sign_out:
                                drawerLayout.closeDrawers();
                                SharedPreferenceManager.getSharedInstance().clearAllPreferences();
                                startLoginActivity();
                                return true;
                            default:
                                return true;
                        }
                    }
                });
    }

    private void setupDrawerContent(UserModel userModel) {
        View headerView = navigationView.getHeaderView(0);
        simpleDraweeView = (SimpleDraweeView) headerView.findViewById(R.id.user_imageview);
        if(userModel.profilePic != null)
            simpleDraweeView.setImageURI(Uri.parse(userModel.profilePic));
        else
            simpleDraweeView.setImageResource(R.drawable.user);

        nameTextView = (TextView) headerView.findViewById(R.id.name_textview);
        nameTextView.setText(userModel.userName);

        emailTextView = (TextView) headerView.findViewById(R.id.email_textview);
        emailTextView.setText(userModel.userEmail);
    }

    private UserModel getUserModelFromIntent()
    {
        Intent intent = getIntent();
        return intent.getParcelableExtra(UserModel.class.getSimpleName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finishAffinity();
    }

    private void startLoginActivity()
    {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finishAffinity();
    }
}
