package ba.wurth.mb.Services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import ba.wurth.mb.Classes.GPS.Log;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.GPS.DL_GPS;


public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FASTEST_INTERVAL = 5000; // 5 sec
    private Handler mHandler;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        connect();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            mHandler = new Handler();
        } catch (Exception e) {
            wurthMB.AddError("LocationService onCreate", e.getMessage(), e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {

            if (mGoogleApiClient != null) {
                if (mGoogleApiClient.isConnected()) {
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                    mGoogleApiClient.disconnect();
                }
            }

            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;

        } catch (Exception e) {
            wurthMB.AddError("LocationService onDestroy", e.getMessage(), e);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                if (wurthMB.LOCATION_SERVICE_ENABLED) mHandler.postDelayed(rSync, 100);
            }
        } catch (Exception e) {
            wurthMB.AddError("LocationService onConnected", e.getMessage(), e);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            if (location.hasAccuracy()) wurthMB.currentBestLocation = location;
            else wurthMB.currentBestLocation = null;
        } catch (Exception e) {
            wurthMB.AddError("LocationService onLocationChanged", e.getMessage(), e);
        }
    }

    public void connect() {
        try {

            if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {

                if (mGoogleApiClient == null) {
                    mGoogleApiClient = new GoogleApiClient.Builder(this)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .addApi(LocationServices.API)
                            .build();

                    mLocationRequest = LocationRequest.create()
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(UPDATE_INTERVAL)
                            .setFastestInterval(FASTEST_INTERVAL)
                            .setSmallestDisplacement(20)
                    ;
                }

                if (mGoogleApiClient != null) {
                    if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
                        mHandler.removeCallbacksAndMessages(null);
                        mHandler = null;
                        mHandler = new Handler();
                        mGoogleApiClient.connect();
                    }
                }
            }

            if (mHandler == null) mHandler = new Handler();

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    connect();
                }
            }, 5 * 60 * 1000);

        } catch (Exception e) {
            wurthMB.AddError("LocationService connect", e.getMessage(), e);
        }
    }

    private final Runnable rSync = new Runnable()
    {
        public void run()
        {
            try {

                if (wurthMB.currentBestLocation != null) {

                    Log point = new Log();

                    point.Latitude =  wurthMB.currentBestLocation.getLatitude();
                    point.Longitude = wurthMB.currentBestLocation.getLongitude();
                    point.Speed = wurthMB.currentBestLocation.getSpeed();
                    point.Time = System.currentTimeMillis();

                    if (wurthMB.currentBestLocation.hasAccuracy()) point.Accuracy = wurthMB.currentBestLocation.getAccuracy();
                    if (wurthMB.currentBestLocation.hasAltitude()) point.Altitude = wurthMB.currentBestLocation.getAltitude();
                    if (wurthMB.currentBestLocation.hasBearing()) point.Bearing = wurthMB.currentBestLocation.getBearing();

                    if (!wurthMB.dbHelper.getDB().inTransaction()) DL_GPS.AddOrUpdate(point);
                }
            }
            catch (Exception e) {
                wurthMB.AddError("LocationService", e.getMessage(), e);
            }
            if (wurthMB.LOCATION_SERVICE_ENABLED) mHandler.postDelayed(rSync, wurthMB.LOCATION_SERVICE_INTERVAL);
        }
    };
}
