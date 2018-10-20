package com.simplecity.amp_library.playback;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;


import com.simplecity.amp_library.interfaces.AccelerometerListener;

import java.util.List;


public class ShakeManager {

    private static Context context = null;
    /**
     * Accuracy configuration
     */
    private static float threshold = 12.25f;
    private static int interval = 4000;

    private static Sensor sensor;
    private static SensorManager sensorManager;

    private static AccelerometerListener listener;

    /**
     * indicates whether or not Accelerometer Sensor is supported
     */
    private static Boolean supported;
    /**
     * indicates whether or not Accelerometer Sensor is running
     */
    private static boolean running = false;

    /**
     * Returns true if the manager is listening to orientation changes
     */
    public static boolean isListening() {
        return running;
    }

    /**
     * Unregisters listeners
     */
    public static void stopListening() {
        running = false;
        try {
            if (sensorManager != null && sensorEventListener != null) {
                sensorManager.unregisterListener(sensorEventListener);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Returns true if at least one Accelerometer sensor is available
     */
    public static boolean isSupported(Context cntxt) {
        context = cntxt;
        if (supported == null) {
            if (context != null) {

                sensorManager = (SensorManager) context.
                        getSystemService(Context.SENSOR_SERVICE);

// Get all sensors in device
                List<Sensor> sensors = sensorManager.getSensorList(
                        Sensor.TYPE_ACCELEROMETER);

                supported = new Boolean(sensors.size() > 0);
            } else {
                supported = Boolean.FALSE;
            }
        }
        return supported;
    }


    public static void configure(int threshold, int interval) {
        ShakeManager.threshold = threshold;
        ShakeManager.interval = interval;
    }


    public static void startListening(AccelerometerListener accelerometerListener) {

        sensorManager = (SensorManager) context.
                getSystemService(Context.SENSOR_SERVICE);

// Take all sensors in device
        List<Sensor> sensors = sensorManager.getSensorList(
                Sensor.TYPE_ACCELEROMETER);

        if (sensors.size() > 0) {

            sensor = sensors.get(0);

// Register Accelerometer Listener
            running = sensorManager.registerListener(
                    sensorEventListener, sensor,
                    SensorManager.SENSOR_DELAY_GAME);

            listener = accelerometerListener;
        }
    }


    public static void startListening(AccelerometerListener accelerometerListener, int threshold, int interval) {
        configure(threshold, interval);
        startListening(accelerometerListener);
    }

    public static SensorEventListener sensorEventListener = new SensorEventListener() {


        private long lastShake = 0;

        private float force=0;

        private float accValues[] = {0, 0, 0};

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {

            long curTime = System.currentTimeMillis();
            if ((curTime - lastShake) > interval) {

                accValues[0] = event.values[0];
                accValues[1] = event.values[1];
                accValues[2] = event.values[2];
                force = Math.abs(accValues[0] + accValues[1] + accValues[2]);
                double acceleration = Math.sqrt(Math.pow(accValues[0], 2) +
                        Math.pow(accValues[1], 2) +
                        Math.pow(accValues[2], 2)) - SensorManager.GRAVITY_EARTH;


                if (acceleration > threshold) {
                    listener.onShake(force);
                    lastShake = curTime;

                }
            }


// trigger change event
            if (listener != null) {
                listener.onAccelerationChanged(accValues[0], accValues[1], accValues[2]);
            }

        }
    };
}