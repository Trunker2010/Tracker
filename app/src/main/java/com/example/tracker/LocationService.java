package com.example.tracker;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class LocationService extends Service {
    private final IBinder binder = new LocationBinder();
    private double distanceInMeters;
    private double speed;
    private double altitude;
    private double maxSpeed;
    private double accuracy;
    private static Location lastLocation = null;
    private LocationListener listener;
    private LocationManager locManager;
    public static final String PERMISSION_STRING
            = android.Manifest.permission.ACCESS_FINE_LOCATION;

    @Override
    public void onCreate() {

        super.onCreate();
        listener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                Log.d("LocationService","onLocationChanged "+ location.getAccuracy());
                if (lastLocation == null) {
                    lastLocation = location;
                }

                distanceInMeters += location.distanceTo(lastLocation);
                lastLocation = location;
                speed = location.getSpeed();
                altitude = location.getAltitude();
                accuracy = location.getAccuracy();
                if (maxSpeed<location.getSpeed()){
                    maxSpeed=location.getSpeed();
                }


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, PERMISSION_STRING) == PackageManager.PERMISSION_GRANTED)
            ;
        {
            String provider = locManager.getBestProvider(new Criteria(), true);
            if (provider != null) {
                locManager.requestLocationUpdates(provider, 1000, 1, listener);

            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locManager != null && listener != null) {
            if (ContextCompat.checkSelfPermission(this, PERMISSION_STRING)
                    == PackageManager.PERMISSION_GRANTED) {
                locManager.removeUpdates(listener);

            }
            locManager = null;
            listener = null;

        }
    }

    public class LocationBinder extends Binder {
        LocationService getLocation() {
            return LocationService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public double getDistance() {
        return this.distanceInMeters / 1000;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public double getAltitude() {//Р’С‹СЃРѕС‚Р°
        return altitude;
    }

    public double getSpeed() {
        return speed / 1000 * 3600; // km/h
    }

    public double getMaxSpeed() {
        if (maxSpeed<speed){
            maxSpeed=speed;
        }
        return maxSpeed/1000*3600;
    }

}
