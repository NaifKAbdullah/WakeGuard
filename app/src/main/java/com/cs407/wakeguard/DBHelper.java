package com.cs407.wakeguard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "alarms.db";
    private static final int DATABASE_VERSION = 1;

    private static int alarmID = 1;


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        String CREATE_ALARMS_TABLE = "CREATE TABLE IF NOT EXISTS alarms " +
                "(id INTEGER PRIMARY KEY, " +
                //"alarmId INTEGER, " +
                "time TEXT, " +
                "daysActive TEXT, " +
                "title TEXT, " +
                "alarmToneName TEXT, " +
                "isVibrationOn INTEGER, " +
                "isMotionMonitoringOn INTEGER, " +
                "isActive INTEGER)";
        db.execSQL(CREATE_ALARMS_TABLE); // comma-separated list -- 'Mon', 'Tue', 'Wed' etc.
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Since we're not planning on upgrading the database, you can keep this simple.
        // However, it's good practice to handle the upgrade scenario.
    }

    public void addAlarm(AlarmCard alarmCard){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //values.put("alarmId", alarmID++);
        values.put("time", alarmCard.getTime());
        values.put("daysActive", String.join(", ", alarmCard.getDaysActive()));
        values.put("title", alarmCard.getTitle());
        values.put("alarmToneName", alarmCard.getAlarmTone());
        values.put("isVibrationOn", alarmCard.isVibrationOn() ? 1 : 0);
        values.put("isMotionMonitoringOn", alarmCard.isMotionMonitoringOn() ? 1 : 0);
        values.put("isActive", alarmCard.isActive() ? 1 : 0);

        Log.i("D", "INSERTING INTO DB##############");

        db.insert("alarms", null, values);
        Log.i("D", "After inserting ###############");
        printAllAlarms();
        db.close();
    }

    public List<AlarmCard> getAllAlarms() {
        List<AlarmCard> alarmList = new ArrayList<>();
        String selectQuery = "SELECT * FROM alarms";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()){
            int timeIndex = cursor.getColumnIndex("time");
            int daysActiveIndex = cursor.getColumnIndex("daysActive");
            int titleIndex = cursor.getColumnIndex("title");
            int alarmToneNameIndex = cursor.getColumnIndex("alarmToneName");
            int isVibrationOnIndex = cursor.getColumnIndex("isVibrationOn");
            int isMotionMonitoringOnIndex = cursor.getColumnIndex("isMotionMonitoringOn");
            int isActiveIndex = cursor.getColumnIndex("isActive");

            if (timeIndex != -1 && daysActiveIndex != -1 && titleIndex != -1 &&
                    alarmToneNameIndex != -1 && isVibrationOnIndex != -1 &&
                    isMotionMonitoringOnIndex != -1 && isActiveIndex != -1) {
                do {
                    AlarmCard alarmCard = new AlarmCard(
                            cursor.getString(timeIndex),
                            cursor.getString(daysActiveIndex),
                            cursor.getString(titleIndex),
                            cursor.getString(alarmToneNameIndex),
                            cursor.getInt(isVibrationOnIndex) == 1,
                            cursor.getInt(isMotionMonitoringOnIndex) == 1,
                            cursor.getInt(isActiveIndex) == 1
                    );
                    alarmList.add(alarmCard);
                }while (cursor.moveToNext());
            }else{
                Log.i("D", "ERROR: COLUMN WASN'T FOUND ###############");
            }
        }
        cursor.close();
        db.close();
        return alarmList;

    }

    public void printAllAlarms() {
        String selectQuery = "SELECT * FROM alarms";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            int timeIndex = cursor.getColumnIndex("time");
            int daysActiveIndex = cursor.getColumnIndex("daysActive");
            int titleIndex = cursor.getColumnIndex("title");
            int alarmToneNameIndex = cursor.getColumnIndex("alarmToneName");
            int isVibrationOnIndex = cursor.getColumnIndex("isVibrationOn");
            int isMotionMonitoringOnIndex = cursor.getColumnIndex("isMotionMonitoringOn");
            int isActiveIndex = cursor.getColumnIndex("isActive");

            if (timeIndex != -1 && daysActiveIndex != -1 && titleIndex != -1 &&
                    alarmToneNameIndex != -1 && isVibrationOnIndex != -1 &&
                    isMotionMonitoringOnIndex != -1 && isActiveIndex != -1) {
                do {
                    String alarmData = String.format("Time: %s, Days Active: %s, Title: %s, Alarm Tone Name: %s, " +
                                    "Is Vibration On: %s, Is Motion Monitoring On: %s, Is Active: %s",
                            cursor.getString(timeIndex),
                            cursor.getString(daysActiveIndex),
                            cursor.getString(titleIndex),
                            cursor.getString(alarmToneNameIndex),
                            cursor.getInt(isVibrationOnIndex) == 1 ? "Yes" : "No",
                            cursor.getInt(isMotionMonitoringOnIndex) == 1 ? "Yes" : "No",
                            cursor.getInt(isActiveIndex) == 1 ? "Yes" : "No");

                    Log.i("DBHelper", alarmData);
                } while (cursor.moveToNext());
            } else {
                Log.i("DBHelper", "ERROR: COLUMN WASN'T FOUND");
            }
        } else {
            Log.i("DBHelper", "No alarms found in the database.");
        }
        cursor.close();
        db.close();
    }


}
