package com.example.covidapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.io.Serializable;
import java.util.HashMap;

//helper class for database
public class DBHandler extends SQLiteOpenHelper {
    private static final String DB_NAME = "covidDB";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "symReport";
    private static final String REC_ID = "rec_id";
    private static final String RESP_COL = "resp_rate";
    private static final String HEART_COL = "heart_rate";
    private static final String NAUSEA_COL = "nausea";
    private static final String HEADACHE_COL = "headache";
    private static final String DIARRHEA_COL = "diarrhea";
    private static final String SORE_COL = "sore_throat";
    private static final String FEVER_COL = "fever";
    private static final String MACHE_COL = "muscle_ache";
    private static final String STLOSS_COL = "smell_taste_loss";
    private static final String COUGH_COL = "cough";
    private static final String BSHORT_COL = "breath_shortness";
    private static final String TIRED_COL = "tired";
    private static int currId = 1;

    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    //initializes table with column names
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + REC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + RESP_COL + " REAL,"
                + HEART_COL + " REAL,"
                + NAUSEA_COL + " INTEGER,"
                + HEADACHE_COL + " INTEGER,"
                + DIARRHEA_COL + " INTEGER,"
                + SORE_COL + " INTEGER,"
                + FEVER_COL + " INTEGER,"
                + MACHE_COL + " INTEGER,"
                + STLOSS_COL + " INTEGER,"
                + COUGH_COL + " INTEGER,"
                + BSHORT_COL + " INTEGER,"
                + TIRED_COL + " INTEGER)";

        db.execSQL(query);

    }

    //adds resp and heart rate to table
    public void recordRates(double rr, double hr){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(RESP_COL, rr);
        values.put(HEART_COL, hr);

        db.insert(TABLE_NAME, null, values);
        db.close();

    }

    //records symptom values into most recent row
    public void recordSymptoms(int[] symptoms){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        int i = 0;
        values.put(NAUSEA_COL, symptoms[0]);
        values.put(HEADACHE_COL, symptoms[1]);
        values.put(DIARRHEA_COL, symptoms[2]);
        values.put(SORE_COL, symptoms[3]);
        values.put(FEVER_COL, symptoms[4]);
        values.put(MACHE_COL, symptoms[5]);
        values.put(STLOSS_COL, symptoms[6]);
        values.put(COUGH_COL, symptoms[7]);
        values.put(BSHORT_COL, symptoms[8]);
        values.put(TIRED_COL, symptoms[9]);

        db.update(TABLE_NAME, values, REC_ID + " = " + currId, null);
        db.close();
        currId++;
    }

    //onUpgrade function
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
