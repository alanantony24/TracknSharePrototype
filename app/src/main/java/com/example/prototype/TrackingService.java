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
    FusedLocationProviderClient client;
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            List<Location> locationList = locationResult.getLocations();
            for(int i = 0; i < locationList.size(); i++){
                Location location = locationList.get(i);
                /*db.addRun(location.getLatitude(),location.getLongitude());*/
                Log.d("LOCATIONSERVICE", location.getLatitude() + ", " + location.getLongitude());
            }
        }
    };

    //Services to run in background to be implemented here....This includes distance, speed, getting location in background.

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            String action = intent.getAction();
            if(action.equals(Constants.ACTION_START_OR_RESUME_SERVICE)){
                running = true;
                getBackgroundLocation();
                startForegroundService();
                Log.e("SERVICE", "STARTED SERVICE");
            }
            else if(action.equals(Constants.ACTION_STOP_SERVICE)){
                running = false;
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

    private PendingIntent getMainActivityPendingIntent(){
        Intent intent = new Intent(TrackingService.this, MainActivity.class);
        intent.setAction(Constants.ACTION_SHOW_TRACKING_FRAGMENT);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        return pendingIntent;
    }

    @SuppressLint("MissingPermission")
    public void getBackgroundLocation(){
        client = new FusedLocationProviderClient(this);
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        client.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    //Create notification to show that the app is running in the background.
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(NotificationManager notificationManager){
        NotificationChannel notificationChannel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, Constants.NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    //This is to start the foregroundService with the notification.
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
}
