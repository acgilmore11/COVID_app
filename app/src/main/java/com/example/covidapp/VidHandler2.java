package com.example.covidapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;


public class VidHandler2 extends IntentService {
    private Uri fileUri;
    private String file;
    private double hrrate;
    public VidHandler2() {
        super("VidHandler2");
    }

    public void onCreate() {
        super.onCreate();
    }

    //receives uri of heart rate video
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        fileUri = intent.getParcelableExtra("vidUri");
        processVideo();
    }

    //extracts each frame of video, calculates average red value for block of pixels, stores
    //average red values in arraylist, passes arraylist to hrrecognition function
    public void processVideo(){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        //or try this
        retriever.setDataSource(getApplicationContext(),fileUri);
        ArrayList<Bitmap> frames= new ArrayList<Bitmap>();
        MediaPlayer mp = MediaPlayer.create(getBaseContext(), fileUri);
        int durInMillis = mp.getDuration();
        Log.i("MyApp", "Duration: " + durInMillis);


        for(int i = 33333; i < durInMillis * 1000; i+=33333){
            if (retriever.getFrameAtTime(i, MediaMetadataRetriever.OPTION_CLOSEST) != null)
                frames.add(retriever.getFrameAtTime(i, MediaMetadataRetriever.OPTION_CLOSEST));
            if (frames.size() % 10 == 0){
                Log.i("MyApp", "Current frame: " + frames.size());

            }
        }
        int xStart = frames.get(1).getWidth()/2 - 50;
        int yStart = frames.get(1).getHeight()/2 - 50;

        ArrayList<Float> redValues = new ArrayList<Float>();
        Log.i("MyApp", "numFrames: " + frames.size());

        for (int i = 0; i < frames.size(); i++) {
            int sum = 0;
            for (int x = xStart; x < xStart + 100; x++) {
                for (int y = yStart; y < yStart + 100; y++) {
                    if (frames.get(i) != null) {
                        int c = frames.get(i).getPixel(x, y);
                        sum += Color.red(c);
                    }
                }
            }
            float avgRed = sum / 10000;
            redValues.add(avgRed);

        }

        hrrate = callHRRecognition(redValues);
        sendBroadcast(hrrate);

    }

    //broadcasts hrrate value to main activity
    private void sendBroadcast(double r) {
        Intent intent = new Intent ("hrrate");
        intent.putExtra("hrrate", r);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    //simple algoritm to find beats from raw frame readings
    public double callHRRecognition(ArrayList<Float> v){
        float avg = 0;
        int numBeats = 0;
        float max = 0;
        for (int i = 0;i < v.size(); i++){
            avg = avg + v.get(i);
        }
        avg = avg/v.size();
        boolean left = true;

        int crossings = 0;
        if(v.get(0) >= avg){
            left = true;
            for(int i=0;i<v.size();i++){
                if(left){
                    if(v.get(i) < avg){
                        crossings++;
                        left = false;
                    }
                }else{
                    if(v.get(i) >= avg){
                        crossings++;
                        left = true;
                    }
                }
            }
        }else {
            left = false;
            for (int i = 0; i < v.size(); i++) {
                if (left) {
                    if (v.get(i) < avg) {
                        crossings++;
                        left = false;
                    }
                } else {
                    if (v.get(i) >= avg) {
                        crossings++;
                        left = true;
                    }
                }
            }
        }
        numBeats = crossings/2;
        double rate = (numBeats/5.0) * 60.0;
        return rate;
    }
}