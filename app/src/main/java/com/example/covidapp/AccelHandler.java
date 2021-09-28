package com.example.covidapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class AccelHandler extends Service implements SensorEventListener {
    private SensorManager accelManage;
    private Sensor senseAccel;
    float accelValuesX[] = new float[4500];
    float accelValuesY[] = new float[4500];
    float accelValuesZ[] = new float[4500];
    int index = 0;
    double respRate = 0;
    public AccelHandler() {
    }

    //initializes sensor
    @Override
    public void onCreate(){
        accelManage = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senseAccel = accelManage.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelManage.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //stores accelerometer readings in array, passes to breathrecognition
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        //every new reading, increase index
        index++;
        if (index % 100 == 0) {
            String in = String.valueOf(45 - index/100);
            Toast.makeText(getApplicationContext(), in + " seconds remaining", Toast.LENGTH_LONG).show();
        }
        //store reading data in appropriate array
        accelValuesX[index] = sensorEvent.values[0];
        accelValuesY[index] = sensorEvent.values[1];
        accelValuesZ[index] = sensorEvent.values[2];
        if (index >= 4499){
            accelManage.unregisterListener(this);
            respRate = callBreathRecognition();
            sendBroadcast(respRate);
            stopSelf();
        }
    }

    //broadcasts resp rate to main activity
    private void sendBroadcast(double r) {
        Intent intent = new Intent ("rate");
        intent.putExtra("rate", r);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    //simple algorithm that calculates number of breaths from accelerometer readings
    public double callBreathRecognition(){
        float avgZ = 0;
        int numBreaths = 0;
        //breaths/minute
        float max = 0;
        for (int i = 0;i < 4500; i++){
            avgZ = avgZ + accelValuesZ[i];
        }
        avgZ = avgZ/4500;
        boolean left = true;

        int zeroCrossingZ = 0;
        if(accelValuesZ[0] >= avgZ){
            left = true;
            for(int i=0;i<4500;i+=25){
                if(left){
                    if(accelValuesZ[i] < avgZ){
                        zeroCrossingZ++;
                        left = false;
                    }
                }else{
                    if(accelValuesZ[i] >= avgZ){
                        zeroCrossingZ++;
                        left = true;
                    }
                }
            }
        }else {
            left = false;
            for (int i = 0; i < 4500; i+=25) {
                if (left) {
                    if (accelValuesZ[i] < avgZ) {
                        zeroCrossingZ++;
                        left = false;
                    }
                } else {
                    if (accelValuesZ[i] >= avgZ) {
                        zeroCrossingZ++;
                        left = true;
                    }
                }
            }
        }
        numBreaths = zeroCrossingZ/2;
        double rate = (numBreaths/45.0) * 60.0;
        return rate;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}