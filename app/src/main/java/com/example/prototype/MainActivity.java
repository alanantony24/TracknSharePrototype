package com.example.prototype;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private SensorManager sensorManager = null;
    int seconds = 0;
    int previousTotalSteps = 0;
    int totalSteps = 0;
    int currentSteps;
    boolean running = false;
    int calories = 0;

    //MainActivity contains stepcounter, timer
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadData();
        Button startBtn = findViewById(R.id.startRun);
        Button stopBtn = findViewById(R.id.stopRun);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE);
                running = true;
                runTimer();
                SensorEventListener sensorEventListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        if(running){
                            totalSteps = (int) event.values[0];
                            currentSteps = totalSteps - previousTotalSteps;
                            TextView tv = findViewById(R.id.tv_stepCounter);
                            tv.setText(""+currentSteps + "steps");
                            Log.e("Running", "TRACKING STEPS" + currentSteps);
                        }
                        else{
                            Log.e("Running", "NOT TRACKING STEPS");
                        }
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {

                    }
                };
                if (stepSensor == null) {
                    // This will give a toast message to the user if there is no sensor in the device
                    Toast.makeText(MainActivity.this, "No sensor detected on this device", Toast.LENGTH_SHORT).show();
                } else {
                    // Rate suitable for the user interface
                    sensorManager.registerListener(sensorEventListener, stepSensor, SensorManager.SENSOR_DELAY_UI);
                }
                Toast.makeText(MainActivity.this, "Start Run!", Toast.LENGTH_SHORT).show();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                running = false;
                sendCommandToService(Constants.ACTION_STOP_SERVICE);
                Toast.makeText(MainActivity.this, "Stopped Run!", Toast.LENGTH_SHORT).show();
                TextView caloriesTv = findViewById(R.id.calories);
                getDistanceAndSpeed();
            }
        });
    }
    private void sendCommandToService(String action){
        Intent intent = new Intent(MainActivity.this, TrackingService.class);
        intent.setAction(action);
        MainActivity.this.startService(intent);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == requestCode){
            Log.e("Map can move", "Map can move to location");
            //TODO Make the marker automatically come to the currentlocation once the permission is given to do so...Helppppp..
        }
    }

    public void resetSteps(View view) {
        TextView tv_stepsTaken = findViewById(R.id.tv_stepCounter);
        tv_stepsTaken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Long tap to reset", Toast.LENGTH_SHORT).show();
            }
        });
        tv_stepsTaken.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                previousTotalSteps = totalSteps;
                tv_stepsTaken.setText(""+0 + "steps");
                saveData();
                return true;
            }
        });
    }
    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences("steps", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("key1", previousTotalSteps);
        editor.apply();
    }
    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences("steps", Context.MODE_PRIVATE);
        int savedData = sharedPreferences.getInt("key1", 0);
        previousTotalSteps = savedData;
    }
    private void runTimer(){
        TextView timer = findViewById(R.id.timer);
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;
                String time = String.format(Locale.getDefault(),
                        "%d:%02d:%02d",
                        hours, minutes, secs);
                if(running){
                    seconds++;
                    timer.setText(time);
                    Log.e("STOPWATCH", ""+time);
                }
                else{
                    handler.removeCallbacks(this::run);
                }
                handler.postDelayed(this, 1000);
            }
        });
    }
    public void getDistanceAndSpeed(){
        DBHandler dbHandler = new DBHandler(MainActivity.this);
        ArrayList<LatLng> locations = dbHandler.getAllPoints();
        LatLng latLng1 = locations.get(0);
        LatLng latLng2 = locations.get(locations.size() - 1);

        Location startPoint = new Location("locationA");
        startPoint.setLatitude(latLng1.latitude);
        startPoint.setLongitude(latLng1.longitude);

        Location endPoint = new Location("locationB");
        startPoint.setLatitude(latLng2.latitude);
        startPoint.setLongitude(latLng2.longitude);

        double distanceInMetres = startPoint.distanceTo(endPoint);
        double speedInMs = distanceInMetres/seconds;
        Log.e("DISTANCE", ""+distanceInMetres);
        TextView distance = findViewById(R.id.distance);
        TextView speed = findViewById(R.id.speed);
        TextView caloriesTv = findViewById(R.id.calories);
        calories = (int) (currentSteps * 0.045);
        caloriesTv.setText(""+totalSteps * calories + "kcal");
        distance.setText(String.format("%.2f", distanceInMetres) + "m");
        speed.setText(String.format("%.2f", speedInMs) + "m/s");
    }
}