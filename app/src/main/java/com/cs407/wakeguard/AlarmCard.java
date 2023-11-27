package com.cs407.wakeguard;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AlarmCard {

    // Id # of the alarm. This is the PRIMARY KEY generated by the DB.
    private int id;

    /*
    * Notice we're using a String to represent the time and not Java's Date object. This is because
    * using a String for the time in the alarm card example is a matter of simplicity
    * and convenience for display purposes. When you display a time in a user
    * interface, especially if it's for something like an alarm clock,
    * you often want it formatted in a human-readable way, such as "08:00 AM".
    * A String is straightforward for this because it directly represents
    * the formatted text you want to display. You don't need to perform
    * any additional steps to render it in your view.*/
    private String time;

    private String repeatingDays; // comma-separated list -- 'Mon', 'Tue', 'Wed' etc.

    private String title;

    private String alarmTone; // Name of the audio file

    private boolean vibrationOn;

    private boolean motionMonitoringOn;

    // If an alarm is active (true) it means it will go off when the time comes
    private boolean isActive;

    // Is the alarm selected for deletion/turning it off?
    private boolean isSelected = false;


    /**
     * We use this constructor for when we create new alarms (as opposed to retreiving them from db)
     */
    public AlarmCard(String time, String repeatingDays, String title, String alarmTone, boolean vibrationOn, boolean motionMonitoringOn, boolean isActive) {
        this.time = time;
        this.repeatingDays = repeatingDays;
        this.title = title;
        this.alarmTone = alarmTone;
        this.vibrationOn = vibrationOn;
        this.motionMonitoringOn = motionMonitoringOn;
        this.isActive = isActive; // By default when creating an alarm, it should be active.
    }

    /**
     * Constructor with ID for alarms retrieved from the database
      */
    public AlarmCard(int id, String time, String repeatingDays, String title,
                     String alarmTone, boolean vibrationOn, boolean motionMonitoringOn, boolean isActive) {
        this.id = id;
        this.time = time;
        this.repeatingDays = repeatingDays;
        this.title = title;
        this.alarmTone = alarmTone;
        this.vibrationOn = vibrationOn;
        this.motionMonitoringOn = motionMonitoringOn;
        this.isActive = isActive; // By default when creating an alarm, it should be active.
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRepeatingDays() {
        return repeatingDays;
    }

    public boolean [] getRepeatingDaysBooleanArray(){
        boolean [] returnValue = {false, false, false, false, false, false, false};

        String repeatingDaysList = getRepeatingDays();

        if (repeatingDaysList.contains("Su"))
            returnValue[0] = true;
        if (repeatingDaysList.contains("Mo"))
            returnValue[1] = true;
        if (repeatingDaysList.contains("Tu"))
            returnValue[2] = true;
        if (repeatingDaysList.contains("We"))
            returnValue[3] = true;
        if (repeatingDaysList.contains("Th"))
            returnValue[4] = true;
        if (repeatingDaysList.contains("Fr"))
            returnValue[5] = true;
        if (repeatingDaysList.contains("Sa"))
            returnValue[6] = true;

        return returnValue;

    }

    public void setRepeatingDays(String repeatingDays) {
        this.repeatingDays = repeatingDays;
    }

    public String getAlarmTone() {
        return alarmTone;
    }

    public void setAlarmTone(String alarmTone) {
        this.alarmTone = alarmTone;
    }

    public boolean isVibrationOn() {
        return vibrationOn;
    }

    public void setVibrationOn(boolean vibrationOn) {
        this.vibrationOn = vibrationOn;
    }

    public boolean isMotionMonitoringOn() {
        return motionMonitoringOn;
    }

    public void setMotionMonitoringOn(boolean motionMonitoringOn) {
        this.motionMonitoringOn = motionMonitoringOn;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public String toString() {
        return "AlarmCard{" +
                "time='" + time + '\'' +
                ", daysActive='" + repeatingDays + '\'' +
                ", title='" + title + '\'' +
                ", alarmTone='" + alarmTone + '\'' +
                ", vibrationOn=" + vibrationOn +
                ", motionMonitoringOn=" + motionMonitoringOn +
                ", isActive=" + isActive +
                ", isSelected=" + isSelected +
                '}';
    }

    /**
     * This function is used for displaying the time of alarm cards
     *
     * If we're in 24 Hr mode, then we must return the time in 24 Hrs format, otherwise,
     * use normal format.
     *
     * @return alarm's time in the format HH:mm a,
     */
    public String getFormattedTime(){
        try{
            SimpleDateFormat originalFormat = new SimpleDateFormat("HH:mm");
            Date date = originalFormat.parse(this.time);
            SimpleDateFormat newFormat = new SimpleDateFormat("hh:mm a");
            return newFormat.format(date);
        }catch(Exception e){
            e.printStackTrace();
            Log.i("D", "ERROR OCCURRED WHILE CONVERTING TIME ######");
            return this.time; // Fallback to the original time in case of an error
        }
    }
}
