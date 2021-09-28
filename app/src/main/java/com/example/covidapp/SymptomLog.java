package com.example.covidapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class SymptomLog extends AppCompatActivity implements View.OnClickListener {
    private ArrayList<Integer> unClickedStars = new ArrayList<Integer>();
    private ArrayList<Integer> clickedStars = new ArrayList<Integer>();
    private HashMap<String, Integer> symptomRecord;
    private int[] symptomRec = new int[10];
    private String currSym;

    //initializes UI componente
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Spinner symptomSpinner = (Spinner)findViewById(R.id.symptomspinner);

        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(SymptomLog.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.symptoms));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        symptomSpinner.setAdapter(myAdapter);

        symptomSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currSym = (String) adapterView.getItemAtPosition(i);
                restoreRating(view);
                TextView currSymptom = (TextView) findViewById(R.id.currSymptom);
                currSymptom.setText("Severity of symptom: " + currSym);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        symptomRecord = new HashMap<String,Integer>();

        Button upload = (Button) findViewById(R.id.upload);
        upload.setOnClickListener(this);

        Button back = (Button)findViewById(R.id.back);
        back.setOnClickListener(this);

        ImageButton star1 = (ImageButton)findViewById(R.id.unClickedStar1);
        star1.setOnClickListener(this);
        unClickedStars.add(R.id.unClickedStar1);

        ImageButton star2 = (ImageButton)findViewById(R.id.unClickedStar2);
        star2.setOnClickListener(this);
        unClickedStars.add(R.id.unClickedStar2);

        ImageButton star3 = (ImageButton)findViewById(R.id.unClickedStar3);
        star3.setOnClickListener(this);
        unClickedStars.add(R.id.unClickedStar3);

        ImageButton star4 = (ImageButton)findViewById(R.id.unClickedStar4);
        star4.setOnClickListener(this);
        unClickedStars.add(R.id.unClickedStar4);

        ImageButton star5 = (ImageButton)findViewById(R.id.unClickedStar5);
        star5.setOnClickListener(this);
        unClickedStars.add(R.id.unClickedStar5);

        clickedStars.add(R.id.clickedStar1);
        clickedStars.add(R.id.clickedStar2);
        clickedStars.add(R.id.clickedStar3);
        clickedStars.add(R.id.clickedStar4);
        clickedStars.add(R.id.clickedStar5);
    }

    //sets actions for all listeners (stars, buttons, etc)
    @Override
    public void onClick(View v){

        int id = v.getId();
        if (id == R.id.back){
            Intent int2 = new Intent(SymptomLog.this, MainActivity.class);
            startActivity(int2);
            return;
        } else if (id == R.id.upload){
            populateArray();
            Intent int2 = new Intent(SymptomLog.this, MainActivity.class);
            int2.putExtra("ratings", symptomRec);
            symptomRecord = new HashMap<String, Integer>();
            startActivity(int2);
        }
        int index = unClickedStars.indexOf(id);
        if (index < 0){
            index = clickedStars.indexOf(id);
        }
        for (int i = 0; i < unClickedStars.size(); i++){
            if (i <= index){
                findViewById(unClickedStars.get(i)).setVisibility(v.INVISIBLE);
                findViewById(clickedStars.get(i)).setVisibility(v.VISIBLE);
            } else {
                findViewById(unClickedStars.get(i)).setVisibility(v.VISIBLE);
                findViewById(clickedStars.get(i)).setVisibility(v.INVISIBLE);
            }
        }
        symptomRecord.put(currSym, index + 1);
    }

    //restores star rating upon return to previous symptom
    public void restoreRating(View v){
        if (symptomRecord.get(currSym) != null) {
            int index = symptomRecord.get(currSym);
            for (int i = 0; i < unClickedStars.size(); i++) {
                if (i <= index) {
                    findViewById(unClickedStars.get(i)).setVisibility(v.INVISIBLE);
                    findViewById(clickedStars.get(i)).setVisibility(v.VISIBLE);

                } else {
                    findViewById(unClickedStars.get(i)).setVisibility(v.VISIBLE);
                    findViewById(clickedStars.get(i)).setVisibility(v.INVISIBLE);
                }
            }
        } else {
            for (int i = 0; i < unClickedStars.size(); i++){
                findViewById(unClickedStars.get(i)).setVisibility(v.VISIBLE);
                findViewById(clickedStars.get(i)).setVisibility(v.INVISIBLE);
            }
        }
    }

    //fills array with corresponding severity ratings of each symptom
    public void populateArray(){
        String[] symArray = getResources().getStringArray(R.array.symptoms);
        for (int i = 0 ; i < symArray.length; i++){
            if (symptomRecord.get(symArray[i]) != null){
                symptomRec[i] = symptomRecord.get(symArray[i]);
            } else {
                symptomRec[i] = 0;
            }
        }
    }
}