package com.cs407.wakeguard;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;

/**
 * This activity will be brought to the foreground when the alarm goes off.
 */
public class AlarmAlertActivity extends AppCompatActivity {
    private TextView timeText;
    private ImageView wakeGuardLogo;
    private TextView wakeGuardStatus;
    private TextView alarmTitleNoWakeGuard;
    private TextView alarmTitleWithWakeGuard;
    private Button stopButton;
    private AlarmCard triggeredAlarm;
    // If the user clicked the "Disable Motion Monitoring" Btn, this will be true.
    private boolean showDisableMotionMonitoringButton;
    private DBHelper dbHelper;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    // The value of this variable comes from SharedPreference
    private boolean isMotionMonitoringActive;
    private static double motionThreshold; // The threshold value for the motion sensor to exceed to reset timer B
    /*
     * From experimenting:
     * STATIONARY: 9.9
     * 0: HIGH: 13.5 // Walking
     * 1: MEDIUM: 10.35 // holding the phone while the person is stationary
     * 2: LOW: 9.95 // Turning and tossing in bed
     * */
    private static final double [] THRESHOLD_LEVELS = {13.5, 10.35, 9.95};
    private static long monitoringDuration; // in milliseconds
    private static long timeUntilReactivatingAlarm;
    private long lastMotionTime;
    private SharedPreferences sharedPref;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    private Handler timer_B_Handler = new Handler();

    // Timer B (the [activity monitor duration] (1 mins) minutes timer)
    private Runnable timer_B_Runnable = new Runnable() {
        @Override
        public void run() {
            //sharedPref = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE);
            long currentTime = System.currentTimeMillis();
            showDisableMotionMonitoringButton = sharedPref.getBoolean("showDisableMotionMonitoringButton", false);
            if (showDisableMotionMonitoringButton == false){ // if user stopped the alarm.
                stopMotionMonitoring();
            }else if ((currentTime - lastMotionTime) >= timeUntilReactivatingAlarm){ // Checking if [activity monitor duration] minutes have passed since last motion
                // For now, we'll just do 1 minute
                System.out.printf("User is sleeping after dismissing alarm. No motion for more than %d seconds. Triggering alarm again.\n", timeUntilReactivatingAlarm /1000);
                // Trigger alarm again
                triggerAlarmAgain();
                resetMotionTimers();
            }else {
                // Schedule the next check
                timer_B_Handler.postDelayed(this, timeUntilReactivatingAlarm);
            }
        }
    };

    // Timer A (The 30-minute timer)
    private Handler timer_A_Handler = new Handler();
    private Runnable timer_A_Runnable = new Runnable() {
        @Override
        public void run() {
            // Stop motion monitoring if 30 minutes have passed without it being rest by the resetMotionTimers()
            System.out.printf("%d seconds passed with constant motion, user is awake, stopping motion monitoring.\n", monitoringDuration /1000);
            stopMotionMonitoring();
        }
    };

    private void triggerAlarmAgain(){
        // Checking if we still have the triggered alarm saved first
        if (triggeredAlarm != null){

            // Rescheduling alarm to a past date so that it goes off right away
            scheduleAlarm(triggeredAlarm);
        }else{
            System.out.println("ERROR: Triggered alarm is not saved");
        }
        resetMotionTimers();
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            // Logic to determine if there was significant motion.
            // Reset a timer or a last-motion timestamp here.
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Computing acceleration vector magnitude
            double accelerationMagnitude = Math.sqrt(x * x + y * y + z * z);

            // Determining if the magnitude is big enough to disable the motion monitoring
            if (accelerationMagnitude > motionThreshold){
                // reset timer
                System.out.println("Motion Detected: " + accelerationMagnitude);
                lastMotionTime = System.currentTimeMillis();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Auto-generated method
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_alert);

        // Initializing the sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Set window flags to show the activity over the lock screen and wake up the screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_alarm_alert);

        // Initializing needed variables
        dbHelper = DBHelper.getInstance(this);
        timeText = findViewById(R.id.timeText);
        wakeGuardStatus = findViewById(R.id.wakeGuardStatus);
        alarmTitleNoWakeGuard = findViewById(R.id.alarmTitleNoWakeGuard);
        alarmTitleWithWakeGuard = findViewById(R.id.alarmTitleWithWakeGuard);
        // Makes the WakeGuard logo in this activity rounded (to make it look smoother)
        wakeGuardLogo = findViewById(R.id.wakeGuardLogo);
        wakeGuardLogo.setClipToOutline(true);
        stopButton = findViewById(R.id.stopAlarmButton);

        sharedPref = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE);
        isMotionMonitoringActive = sharedPref.getBoolean("isMotionMonitoringActive", false);
        showDisableMotionMonitoringButton = sharedPref.getBoolean("showDisableMotionMonitoringButton", false);
        motionThreshold = THRESHOLD_LEVELS[sharedPref.getInt("activityThresholdMonitoringLevel", 0)];
        monitoringDuration = sharedPref.getInt("activityMonitoringDuration", 1) * 60 * 1000;
        timeUntilReactivatingAlarm = sharedPref.getInt("timeUntilReactivateAlarm", 1) * 60 * 1000;

        //monitoringDuration =  1 * 60 * 1000;
        //timeUntilReactivatingAlarm = 10 * 1000;

        int alarmId = getIntent().getIntExtra("alarmId", -1);
        if (alarmId != -1){ // Making sure alarmId is valid before fetching from DB
            loadAlarmDetails(alarmId);
        }else{
            System.out.println("ERROR @ AlarmAlertActivity. Alarm not found");
        }

        updateLayoutBasedOnMotionMonitoring();
    }

    private void updateLayoutBasedOnMotionMonitoring() {
        // Get references to views
        ImageView wakeGuardLogo = findViewById(R.id.wakeGuardLogo);
        TextView wakeGuardStatus = findViewById(R.id.wakeGuardStatus);
        TextView alarmTitleWithWakeGuard = findViewById(R.id.alarmTitleWithWakeGuard);
        TextView alarmTitleNoWakeGuard = findViewById(R.id.alarmTitleNoWakeGuard);
        View topSpacer = findViewById(R.id.topSpacer);
        View bottomSpacer = findViewById(R.id.bottomSpacer);

        if (triggeredAlarm != null) {
            boolean isMotionMonitoringEnabled = triggeredAlarm.isMotionMonitoringOn();

            if (isMotionMonitoringEnabled) {
                // Motion monitoring enabled state
                wakeGuardLogo.setVisibility(View.VISIBLE);
                wakeGuardStatus.setVisibility(View.VISIBLE);
                alarmTitleWithWakeGuard.setVisibility(View.VISIBLE);
                alarmTitleNoWakeGuard.setVisibility(View.GONE);
                topSpacer.setVisibility(View.GONE);
                bottomSpacer.setVisibility(View.GONE);
            } else {
                // Motion monitoring disabled state
                wakeGuardLogo.setVisibility(View.GONE);
                wakeGuardStatus.setVisibility(View.GONE);
                alarmTitleWithWakeGuard.setVisibility(View.GONE);
                alarmTitleNoWakeGuard.setVisibility(View.VISIBLE);
                alarmTitleNoWakeGuard.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);
                topSpacer.setVisibility(View.VISIBLE);
                bottomSpacer.setVisibility(View.VISIBLE);
            }
        } else {
            timeText.setText("Low Battery!");
            alarmTitleNoWakeGuard.setText("Battery Is Less Than 10%");
            stopButton.setText("Dismiss");
            wakeGuardLogo.setVisibility(View.GONE);
            wakeGuardStatus.setVisibility(View.GONE);
            alarmTitleWithWakeGuard.setVisibility(View.GONE);
            alarmTitleNoWakeGuard.setVisibility(View.VISIBLE);
            alarmTitleNoWakeGuard.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);
            topSpacer.setVisibility(View.VISIBLE);
            bottomSpacer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister SharedPreferences listener to prevent memory leaks
        if (sharedPref != null && preferenceChangeListener != null) {
            sharedPref.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        }
    }

    public void loadAlarmDetails(int alarmId){
        triggeredAlarm = dbHelper.getAlarmById(alarmId);
        if (triggeredAlarm != null){
            if (triggeredAlarm.getTime().equals("Low Battery!")) {
                timeText.setText(triggeredAlarm.getTime());
                stopButton.setText("Dismiss");
            } else {
                timeText.setText(sharedPref.getBoolean("isMilitaryTimeFormat", false) ? triggeredAlarm.getTime() : triggeredAlarm.get12HrTime());
            }
            alarmTitleWithWakeGuard.setText(triggeredAlarm.getTitle());
            alarmTitleNoWakeGuard.setText(triggeredAlarm.getTitle());
            if (triggeredAlarm.isMotionMonitoringOn()){
                wakeGuardLogo.setVisibility(View.VISIBLE);
                wakeGuardStatus.setVisibility(View.VISIBLE);
                alarmTitleWithWakeGuard.setVisibility(View.VISIBLE);
                alarmTitleNoWakeGuard.setVisibility(View.GONE);
            }else if (!triggeredAlarm.isMotionMonitoringOn()){
                wakeGuardLogo.setVisibility(View.GONE);
                wakeGuardStatus.setVisibility(View.GONE);
                alarmTitleWithWakeGuard.setVisibility(View.GONE);
                alarmTitleNoWakeGuard.setVisibility(View.VISIBLE);
            }else {
                System.out.println("ERROR IN AlarmAlertActivity");
            }
        }
    }

    /**
     * Dismisses the initial alarm screen when the user clicks
     * the "Stop" button.
     */
    public void onStopClick(View view) {
        /*If the alarm is non-repeating, then toggle it off. Otherwise, just return to the dashboard. */
        // Stop the AlarmService
        Intent alarmServiceIntent = new Intent(this, AlarmService.class);
        stopService(alarmServiceIntent);
            boolean isAlarmRepeating = !triggeredAlarm.getRepeatingDays().equals("");
            if (!isAlarmRepeating){ // If alarm is not repeating
                triggeredAlarm.setActive(false);
                dbHelper.toggleAlarm(triggeredAlarm.getId(), false);
            }else { // If alarm is indeed repeating, we're going to remove the request code of the alarm that just went off
                // If alarm is repeating, get the specific request code for this instance and delete it
                /*
                String todayDayString = getTodayDayString();
                String alarmIdentifier = triggeredAlarm.getTitle() + triggeredAlarm.getTime() + triggeredAlarm.getFormattedTime() + todayDayString;
                int requestCode = dbHelper.getRequestCode(alarmIdentifier);

                if (requestCode != -1) {
                    cancelAlarmByRequestCode(requestCode);
                    dbHelper.deleteRequestCodeByRequestCode(requestCode);
                }else{
                    System.out.println("ERROR @ AlarmAlertActivity");
                }
                */
            }

            // Releasing wake lock
            if (AlarmReceiver.wakeLock != null && AlarmReceiver.wakeLock.isHeld()){
                AlarmReceiver.wakeLock.release();
            }

            if (triggeredAlarm.isMotionMonitoringOn() && isMotionMonitoringActive == false){
                startMotionMonitoring();
            }else if (!showDisableMotionMonitoringButton){
                stopMotionMonitoring();
            }

        finish();
    }

    public void cancelAlarmByRequestCode(int reqCode){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent (this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                reqCode, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    private String getTodayDayString() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return getDayString(dayOfWeek); // Use the existing method from DashboardActivity to get the day string
    }

    /**
     * Converts a calendar day to a 2-letter string.
     *
     * @param calendarDay where 0= sunday and 6 = Saturday
     * @return The string representing the day
     */
    private String getDayString(int calendarDay) {
        switch (calendarDay) {
            case Calendar.SUNDAY:
                return "Su";
            case Calendar.MONDAY:
                return "Mo";
            case Calendar.TUESDAY:
                return "Tu";
            case Calendar.WEDNESDAY:
                return "We";
            case Calendar.THURSDAY:
                return "Th";
            case Calendar.FRIDAY:
                return "Fr";
            case Calendar.SATURDAY:
                return "Sa";
            default:
                return "";
        }
    }

    /**
     * Intended to prevent the instantiation of duplicate AlarmAlertActivity
     * @param intent The new intent that was started for the activity.
     */
    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        setIntent(intent);
        int alarmId = intent.getIntExtra("alarmId", -1);
        if (alarmId != -1){
            loadAlarmDetails(alarmId);
        }
    }

    private void startMotionMonitoring() {
        System.out.println("STARTING MOTION MONITORING");
        setIsMotionMonitoringActive(true);
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        lastMotionTime = System.currentTimeMillis(); // Reset last motion time for Timer B
        timer_B_Handler.postDelayed(timer_B_Runnable, timeUntilReactivatingAlarm); // Start Timer B
        timer_A_Handler.postDelayed(timer_A_Runnable, monitoringDuration); // Start Timer A
    }

    private void resetMotionTimers() {
        lastMotionTime = System.currentTimeMillis(); // Reset last motion time for Timer B
        timer_B_Handler.removeCallbacks(timer_B_Runnable);
        timer_A_Handler.removeCallbacks(timer_A_Runnable);
        timer_B_Handler.postDelayed(timer_B_Runnable, timeUntilReactivatingAlarm); // Restart Timer B
        timer_A_Handler.postDelayed(timer_A_Runnable, monitoringDuration); // Restart Timer A
    }

    private void stopMotionMonitoring() {
        System.out.println("Stopping motion detection");
        setIsMotionMonitoringActive(false);
        sensorManager.unregisterListener(sensorEventListener);
        timer_B_Handler.removeCallbacks(timer_B_Runnable); // Stopping periodic check (Timer B)
        timer_A_Handler.removeCallbacks((timer_A_Runnable)); // Stopping Timer A
    }

    private void setIsMotionMonitoringActive(boolean activate){;
        isMotionMonitoringActive = activate;
        showDisableMotionMonitoringButton = activate;
        SharedPreferences sharedPrefs = getSharedPreferences("com.cs407.wakeguard",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean("isMotionMonitoringActive", activate).apply();
        editor.putBoolean("showDisableMotionMonitoringButton", activate).apply();
    }

    //--- Copied from DashboardActivity.java
    private void scheduleAlarm(AlarmCard alarmCard){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // This intent is sent to AlarmReceiver.java with the alarm's ID as an extra
        Intent alarmReceiverIntent = new Intent(this, AlarmReceiver.class);
        alarmReceiverIntent.putExtra("alarmId", alarmCard.getId());
        alarmReceiverIntent.putExtra("vibrationOn", alarmCard.isVibrationOn());
        alarmReceiverIntent.putExtra("alarmToneName", alarmCard.getAlarmTone());

        long immediateAlarmTime = System.currentTimeMillis();

        // Generate a unique request code
        int requestCode = generateRequestCode();
        dbHelper.addRequestCode(alarmCard.getTitle()+alarmCard.getTime()+alarmCard.get12HrTime()+"ALERT", requestCode, "");

        // Use the unique request code for the PendingIntent
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, requestCode,
                alarmReceiverIntent, PendingIntent.FLAG_IMMUTABLE);

        // Set the alarm to go off immediately
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, immediateAlarmTime, alarmPendingIntent);
                } else {
                    // Show a dialog or notification to inform the user they need to grant the permission
                    Intent permissionIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(permissionIntent);
                }
            } else {
                // For older versions, set the alarm without checking the permission
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, immediateAlarmTime, alarmPendingIntent);
            }
        }
    }

    private synchronized int generateRequestCode(){
        int reqCode = DashboardActivity.requestCodeCreator++;
        saveRequestCode(DashboardActivity.requestCodeCreator); // saving it to SharedPreference for persistence.
        return reqCode;
    }

    private void saveRequestCode(int requestCode){
        SharedPreferences sharedPreferences = getSharedPreferences("com.cs407.wakeguard",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("nextRequestCode", DashboardActivity.requestCodeCreator);
        editor.apply();
    }
}