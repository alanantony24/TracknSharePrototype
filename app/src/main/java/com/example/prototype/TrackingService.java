package com.example.prototype;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TrackingService extends Service{

    //Variables
    boolean running = false;
    private int totalSteps = 0;
    private int previousTotalSteps = 0;
    DBHandler dbHandler = new DBHandler(TrackingService.this);
    FusedLocationProviderClient client;
    //Services to run in background to be implemented here....This includes distance, speed, getting location in background.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            String action = intent.getAction();
            if(action.equals(Constants.ACTION_START_OR_RESUME_SERVICE)){
                running = true;
                startLocationTracking();
                startForegroundService();
                Log.e("SERVICE", "STARTED SERVICE");
            }
            else if(action.equals(Constants.ACTION_STOP_SERVICE)){
                client.removeLocationUpdates(locationCallback);
                stopForeground(true);
                TrackingService.this.stopSelf();
                Log.e("SERVICE", "STOPPED SERVICE");
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    //Timer method
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            List<Location> locationList = locationResult.getLocations();
            for(int i = 0; i < locationList.size(); i++){
                Location location = locationList.get(i);
                dbHandler.addUser(location.getLatitude(),location.getLongitude());
                Log.d("LOCATIONSERVICE", location.getLatitude() + ", " + location.getLongitude());
            }
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(NotificationManager notificationManager){
        NotificationChannel notificationChannel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, Constants.NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(notificationChannel);
    }
    public void startForegroundService(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(TrackingService.this, Constants.NOTIFICATION_CHANNEL_ID)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_disabled)
                    .setContentTitle("Live coordinates of current location")
                    .setContentText("Location is received in the background.")
                    .setContentIntent(getMainActivityPendingIntent());
            startForeground(Constants.NOTIFICATION_ID, builder.build());
        }
    }
    private PendingIntent getMainActivityPendingIntent(){
        Intent intent = new Intent(TrackingService.this, MainActivity.class);
        intent.setAction(Constants.ACTION_SHOW_TRACKING_FRAGMENT);
        PendingIntent pendingIntent = PendingIntent.getActivity(TrackingService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }
    @SuppressLint("MissingPermission")
    public void startLocationTracking(){
        client = new FusedLocationProviderClient(this);
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        client.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        dbHandler.delelteAll();
    }
}
