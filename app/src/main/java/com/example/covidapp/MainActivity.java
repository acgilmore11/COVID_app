package com.example.covidapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;

public class MainActivity extends AppCompatActivity{
    static double respRate;
    static double hrRate;
    static final int REQUEST_VIDEO_CAPTURE = 101;
    private Uri fileUri;
    private File mediaFile;
    private boolean rTaken = false;
    private boolean hTaken = false;
    private boolean ratesUploaded = false;
    private DBHandler dbHandler = null;

    //receives resprate and heart rate from sensor reading services
    private BroadcastReceiver  rateReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("rate")) {
                respRate = intent.getDoubleExtra("rate", 0);
                TextView reading = (TextView) findViewById(R.id.rateReading);
                reading.setText("Respiratory Rate: " + respRate);
                rTaken = true;
            } else if (intent.getAction().equals("hrrate")){
                hrRate = intent.getDoubleExtra("hrrate", 0);
                TextView hrreading = (TextView) findViewById(R.id.rateReading);
                hrreading.setText("Heart Rate: " + hrRate);
                onResume();
                hTaken = true;
            }
        }
    };

    //initializes UI components
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeDB();

        Button symptoms = (Button)findViewById(R.id.symptoms);
        Button sensorB = (Button)findViewById(R.id.sensorButton);
        Button hrCamera = (Button)findViewById(R.id.hrButton);
        Button uploadRates = (Button)findViewById(R.id.uploadRates);

        symptoms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ratesUploaded) {
                    Intent int1 = new Intent(getApplicationContext(), SymptomLog.class);
                    startActivity(int1);
                } else {
                    Toast.makeText(getApplicationContext(), "Please upload resp/heart rates first", Toast.LENGTH_LONG).show();
                }
            }
        });

        sensorB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ratesUploaded = false;
                Intent startSensorService = new Intent(MainActivity.this, AccelHandler.class);
                startService(startSensorService);
            }
        });

        hrCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ratesUploaded = false;
                startRecording();
            }
        });

        //uploads rates to database
        uploadRates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rTaken && hTaken){
                    dbHandler.recordRates(respRate, hrRate);
                    ratesUploaded = true;
                    rTaken = false;
                    hTaken = false;
                    respRate = 0.0;
                    hrRate = 0.0;
                    //currId++;
                    TextView reading = (TextView)findViewById(R.id.rateReading);
                    reading.setText("Respiratory rate: " + respRate);
                    TextView hrreading = (TextView)findViewById(R.id.rateReading2);
                    hrreading.setText("Heart rate: " + hrRate);
                    Toast.makeText(getApplicationContext(), "Resp/heart rates uploaded successfully", Toast.LENGTH_LONG).show();
                } else if (!rTaken && hTaken){
                    Toast.makeText(getApplicationContext(), "Error: Resp rate not measured", Toast.LENGTH_LONG).show();
                } else if (rTaken && !hTaken){
                    Toast.makeText(getApplicationContext(), "Error: Heart rate not measured", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error: Heart & Resp. rate not measured", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //accesses camera and begins recording
    public void startRecording(){
        mediaFile = new File(getExternalFilesDir(null).getAbsolutePath() + "/myvideo.mp4");
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,5);
        fileUri =  FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + "." + getLocalClassName() + ".provider", mediaFile);
        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
    }

    //registers main activity to receive data from services
    protected void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(rateReceiver, new IntentFilter("rate"));
        TextView reading = (TextView)findViewById(R.id.rateReading);
        reading.setText("Respiratory rate: " + respRate);

        LocalBroadcastManager.getInstance(this).registerReceiver(rateReceiver, new IntentFilter("hrrate"));
        TextView hrreading = (TextView)findViewById(R.id.rateReading2);
        hrreading.setText("Heart rate: " + hrRate);

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            dbHandler.recordSymptoms(extras.getIntArray("ratings"));
            getIntent().removeExtra("ratings");
            Toast.makeText(getApplicationContext(), "Symptoms uploaded successful", Toast.LENGTH_LONG).show();
        }
    }

    protected void onPause (){
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(rateReceiver);
    }

    //receives video and initializes video analysis
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        showVideo();

        if (requestCode == REQUEST_VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                notifyService();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to record video",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    //calls vidhandler service to begin video analysis
    private void notifyService(){
        Intent startVidService = new Intent(MainActivity.this, VidHandler2.class);
        startVidService.putExtra("vidUri", fileUri);
        startService(startVidService);
    }

    //plays recorded video in VideoView
    private void showVideo() {
        try {
            VideoView mVideoView  = (VideoView)findViewById(R.id.videoView);
            mVideoView.setMediaController(new MediaController(this));
            mVideoView.setVideoURI(fileUri);
            mVideoView.requestFocus();
            mVideoView.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //initializes database to store symptoms
    private void initializeDB(){
        dbHandler = new DBHandler(MainActivity.this);
    }
}