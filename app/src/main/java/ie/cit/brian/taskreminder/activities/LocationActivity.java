package ie.cit.brian.taskreminder.activities;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import ie.cit.brian.taskreminder.CustomAdapter;
import ie.cit.brian.taskreminder.R;





/**
 * Created by briancoveney on 11/25/15.
 */
public class LocationActivity extends BaseActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static Bundle bundle = new Bundle();
    private Switch locationSwitch;
    private static String TAG = "ie.cit.brian.taskreminder";
    private final Calendar cal = Calendar.getInstance();
    private GoogleApiClient mGoogleApiClient;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private final LocationRequest mLocationRequestHighAccuracy = LocationRequest
            .create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(60000)
            .setFastestInterval(100);

    private final LocationRequest mLocationRequestBalancedPowerAccuracy = LocationRequest
            .create().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
            .setInterval(60000)
            .setFastestInterval(100);

    protected Location mCurrentLocation;
    protected LocationRequest mLocationRequest;
    protected TextView mLatitudeText,mLongitudeText,mTimeText;
    protected Boolean mRequestingLocationUpdates;
    protected String mLastUpdateTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* We will not use setContentView in this activty
           Rather than we will use layout inflater to add view in FrameLayout of our base activity layout*/
        getLayoutInflater().inflate(R.layout.activity_location, frameLayout);

        buildGoogleApiClient();
        createLocationRequest();


        mLatitudeText = (TextView)findViewById(R.id.mLatitudeText);
        mLongitudeText = (TextView)findViewById(R.id.mLongitudeText);
        mTimeText = (TextView) findViewById(R.id.mLastUpdateTimeTextView);
        locationSwitch = (Switch) findViewById(R.id.switch_location);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                // deals with the app crashing due to "GoogleApiClient is not Connected yet"
                try {
                    togglePeriodicLocUpdates();
                }catch (IllegalStateException i){
                    Log.i(TAG, "togglePeriodicLocUpdates GoogleApiClient is not Connected yet = " + i.getMessage());
                }
            }
        });
    }



    private void togglePeriodicLocUpdates() {

        try {
            if (!mRequestingLocationUpdates) {
                locationSwitch.setText(R.string.stop_loc_updates);
                mRequestingLocationUpdates = true;
                startLocationUpdates();
            } else {
                locationSwitch.setText(R.string.start_loc_updates);
                mRequestingLocationUpdates = false;
                stopLocationUpdates();
            }
        }catch (SecurityException se){
            se.getMessage();
        }

    }


    protected synchronized void buildGoogleApiClient()
    {
        //create an instance of GoogleApiClient
        GoogleApiClient.Builder builder =
                new GoogleApiClient.Builder(this);

        //Add the location api
        builder.addApi(LocationServices.API);

        //tell it what to call back to when it has connected to the Google Service
        builder.addConnectionCallbacks(this);
        // connection error checking
        builder.addOnConnectionFailedListener(this);

        mGoogleApiClient = builder.build();

        if(mGoogleApiClient != null){
            isConnected();
        }
    }


    // SettingsApi
    // This API makes it easy for an app to ensure that the device's system settings are
    // properly configured for the app's location needs.
    public void isConnected() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequestHighAccuracy)
                .addLocationRequest(mLocationRequestBalancedPowerAccuracy);


        final PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());


        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates locationSettingsStates
                        = result.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    LocationActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        createLocationRequest();//FINALLY YOUR OWN METHOD TO GET YOUR USER LOCATION HERE
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to

                        break;
                    default:
                        break;
                }
                break;
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected to the GoogleAPIClient");

        try {
            if (mCurrentLocation == null) {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                updateUI();
            }

            if(mRequestingLocationUpdates) {
                startLocationUpdates();
            }

        }catch (SecurityException se){
            se.printStackTrace();
        }


        // prevents null pointer exception
        if(mCurrentLocation != null) {
            // save Lat Long for use in Maps Activity
            Double mLat = mCurrentLocation.getLatitude();
            Double mLong = mCurrentLocation.getLongitude();
            SharedPreferences sharedPref = getSharedPreferences("your_prefs", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong("latitude_key", Double.doubleToLongBits((mLat)));
            editor.putLong("longitude_key", Double.doubleToLongBits((mLong)));
            editor.apply();
        }

    }


    protected void createLocationRequest()
    {
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(50000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }

    protected void startLocationUpdates()
    {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }catch (SecurityException se){
            se.printStackTrace();
        }
    }

    protected void stopLocationUpdates()
    {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }catch (SecurityException se){
            se.printStackTrace();
        }
    }


    // define the LocationListener's interface method to display the current Lat, Long and Timestamp
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }

    private void updateUI()
    {

        if(mCurrentLocation != null) {
            mLatitudeText.setText(String.valueOf(mCurrentLocation.getLatitude()));
            mLongitudeText.setText(String.valueOf(mCurrentLocation.getLongitude()));
            mLongitudeText.setVisibility(View.VISIBLE);
            mTimeText.setText(mLastUpdateTime);

        }else {
            mLatitudeText.setText(R.string.connection_failed);
            mLongitudeText.setVisibility(View.INVISIBLE);
        }

    }



    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection FAILED: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }



    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }



    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }

        bundle.putBoolean("SwitchState", locationSwitch.isChecked());
    }


    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        try {
            if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        }catch (IllegalStateException i){
            Log.i(TAG, "onResume GoogleApiClient is not Connected yet = " + i.getMessage());
        }
        locationSwitch.setChecked(bundle.getBoolean("SwitchState", false));
    }




    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);

        savedInstanceState.putBoolean("SwitchButtonState", locationSwitch.isChecked());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        locationSwitch.setChecked(savedInstanceState.getBoolean("SwitchButtonState", false));

    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();
        }
    }

}






















