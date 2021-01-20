package com.example.practicaminijuego;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowMetrics;
import android.view.WindowManager;

import java.sql.SQLOutput;

public class MainActivity extends AppCompatActivity  {

    SensorManager sensorManager;
    Sensor acelerometerSensor;
    BallView ballView;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Point resolution = new Point();
        getDisplay().getSize(resolution);

        ballView = new BallView(this, resolution);
        setContentView(ballView);

        ballView.setOnClickListener(v -> {
            if (ballView.isBallGon()) {
                ballView.reset();
            }
        });

        // Initialize sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        acelerometerSensor = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(ballView, acelerometerSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        ballView.resumeMusic();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(ballView);
        ballView.pauseMusic();
    }
}