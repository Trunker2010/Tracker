package com.example.tracker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    double speed = 0.0;
    double altitude = 0.0;
    double maxSpd = 0.0;
    double accuracy = 0.0;
    double distance = 0.0;
    private int seconds = 0;
    boolean isRun;
    static Handler timerHandler = new Handler();
    static Handler locationParamsHandler = new Handler();
    Runnable locationParamsRunnable;
    Runnable timerRunnable;
    private LocationService locationService;
    private boolean bound = false;
    private final int PERMISSION_REQUEST_CODE = 699;
    private final int NOTIFICATION_ID = 424;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("isRun")) {
                permission();
                ShowLocationParams();
                runTimer();
                seconds = savedInstanceState.getInt("seconds");
            }
        }
    }

    private void runTimer() {                                                               //РўР°Р№РјРµСЂ

        timerRunnable = new Runnable() {
            final TextView timerTxtView = findViewById(R.id.durationTxtView);

            @Override
            public void run() {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;
                String time = String.format(Locale.getDefault(),
                        "%d:%02d:%02d", hours, minutes, secs);
                timerTxtView.setText(time);

                if (isRun) {
                    seconds++;
                }
                Log.d("runTimer", "runTimer");
                timerHandler.postDelayed(this, 1000);
            }
        };
        {
            timerHandler.post(timerRunnable);
        }

    }


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            if (locationService == null) {
                LocationService.LocationBinder locationBinder = (LocationService.LocationBinder) binder;
                locationService = locationBinder.getLocation();
            }
            bound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, LocationService.class);
                    bindService(intent, connection, Context.BIND_AUTO_CREATE);
                } else {

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this)// РЅСѓР¶РµРЅ РєР°РЅР°Р»
                            .setSmallIcon(android.R.drawable.ic_menu_compass)
                            .setContentTitle(getResources().getString(R.string.app_name))
                            .setContentText(getResources().getString(R.string.permission_denied))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setVibrate(new long[]{1000, 1000})
                            .setAutoCancel(true);
                    Intent actionIntent = new Intent(this, MainActivity.class);
                    PendingIntent actionPendingIntent = PendingIntent.getActivity(
                            this,
                            0,
                            actionIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(actionPendingIntent);

                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(NOTIFICATION_ID, builder.build());
                }
            }
        }
    }


    public void ShowLocationParams() {

        findViewById(R.id.btnStart).setEnabled(false);
        isRun = true;
        final TextView spdTxtView = findViewById(R.id.val_spd_TextView);
        final TextView altitudeTxtView = findViewById(R.id.Altitude_TextView);
        final TextView maxSpdTxtView = findViewById(R.id.MaxSpd_TextView);
        final TextView accuracyTxtView = findViewById(R.id.accuracyTextView);
        final TextView distanceTxtView = findViewById(R.id.distanceTxtView);
        locationParamsHandler = new Handler();

        locationParamsRunnable = new Runnable() {


            @Override
            public void run() {
                if (bound && locationService != null) {
                    speed = locationService.getSpeed();
                    altitude = locationService.getAltitude();
                    accuracy = locationService.getAccuracy();
                    distance = locationService.getDistance();
                    maxSpd = locationService.getMaxSpeed();
                }

                accuracyTxtView.setText(String.format(Locale.getDefault(),
                        "%1$,.0f", accuracy));
                distanceTxtView.setText(String.format(Locale.getDefault(),
                        "%1$,.2f", distance));
                spdTxtView.setText(String.format(Locale.getDefault(),
                        "%1$,.2f", speed));
                maxSpdTxtView.setText(String.format(Locale.getDefault(),
                        "%1$,.2f", maxSpd));
                altitudeTxtView.setText(String.format(Locale.getDefault(),
                        "%1$,.0f", altitude));

                Log.d("ShowLocationParams", "ShowLocationParams");

                locationParamsHandler.postDelayed(this, 1050);
            }
        };

        locationParamsHandler.post(locationParamsRunnable);
    }


    public void onClickStart(View view) {
        permission();
        runTimer();
        ShowLocationParams();
    }

    public void onClickStop(View view) {
        if (bound) {
            unbindService(connection);
            bound = false;
            isRun = false;
            findViewById(R.id.btnStart).setEnabled(true);

            timerHandler.removeCallbacksAndMessages(null);
            locationParamsHandler.removeCallbacksAndMessages(null);
        }
    }

    private void permission() {
        if (ContextCompat.checkSelfPermission(this, LocationService.PERMISSION_STRING)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{LocationService.PERMISSION_STRING},
                    PERMISSION_REQUEST_CODE);
        } else {
            Intent intent = new Intent(this, LocationService.class);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isRun", isRun);
        outState.putInt("seconds", seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRun) {
            timerHandler.removeCallbacksAndMessages(null);
            locationParamsHandler.removeCallbacksAndMessages(null);

        }
    }
}
