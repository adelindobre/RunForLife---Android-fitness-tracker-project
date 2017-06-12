package facebook_utils;

/**
 * Created by AdelinGDobre on 5/11/2017.
 */

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import facebook_utils.SharedPreferenceManager;
import com.facebook.FacebookSdk;
import com.facebook.drawee.backends.pipeline.Fresco;


public class AppDelegate extends Application implements  Application.ActivityLifecycleCallbacks {

    @Override
    public void onCreate() {
        super.onCreate();
        instantiateManagers();
    }

    /**
     * Method to instantiate all the managers in this app
     */
    private void instantiateManagers() {
        FacebookSdk.sdkInitialize(this);
        Fresco.initialize(this);
        SharedPreferenceManager.getSharedInstance().initiateSharedPreferences(getApplicationContext());
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
