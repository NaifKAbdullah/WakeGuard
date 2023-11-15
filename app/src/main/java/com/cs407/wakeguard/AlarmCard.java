package com.cs407.wakeguard;

public class AlarmCard {

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

    private String daysActive; // comma-separated list -- 'Mon', 'Tue', 'Wed' etc.

    private String title;

    private String alarmTone; // Name of the audio file

    private boolean vibrationOn;

    private boolean motionMonitoringOn;

    // If an alarm is active (true) it means it will go off when the time comes
    private boolean isActive;

    // Is the alarm selected for deletion/turning it off?
    private boolean isSelected = false;


    public AlarmCard(String time, String daysActive, String title, String alarmTone, boolean vibrationOn, boolean motionMonitoringOn, boolean isActive) {
        this.time = time;
        this.daysActive = daysActive;
        this.title = title;
        this.alarmTone = alarmTone;
        this.vibrationOn = vibrationOn;
        this.motionMonitoringOn = motionMonitoringOn;
        this.isActive = isActive; // By default when creating an alarm, it should be active.
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

    public String getDaysActive() {
        return daysActive;
    }

    public void setDaysActive(String daysActive) {
        this.daysActive = daysActive;
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
                ", daysActive='" + daysActive + '\'' +
                ", title='" + title + '\'' +
                ", alarmTone='" + alarmTone + '\'' +
                ", vibrationOn=" + vibrationOn +
                ", motionMonitoringOn=" + motionMonitoringOn +
                ", isActive=" + isActive +
                ", isSelected=" + isSelected +
                '}';
    }
}
