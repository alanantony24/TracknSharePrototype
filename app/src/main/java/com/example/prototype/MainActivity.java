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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startBtn = findViewById(R.id.startRun);
        Button stopBtn = findViewById(R.id.stopRun);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE);
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommandToService(Constants.ACTION_STOP_SERVICE);
            }
        });

    }

    public void sendCommandToService(String action){
        Intent intent = new Intent(MainActivity.this, TrackingService.class);
        intent.setAction(action);
        MainActivity.this.startService(intent);
    }

}