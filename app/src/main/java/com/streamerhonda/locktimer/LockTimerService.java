package com.streamerhonda.locktimer;

import java.io.File;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class LockTimerService extends Service {
    private boolean DEBUG = BuildConfig.DEBUG;
    static final private String TAG = LockTimerService.class.getSimpleName();

    static final public String EXTRA_PERIOD_MSEC = "period_msec";

    // display Heads-up notifications the following periods before locking the screen
    static final private int ALERT1_TIME_LEFT_MSEC = 5 * 60 * 1000;
    static final private int ALERT2_TIME_LEFT_MSEC = 60 * 1000;

    static final public String ACTION_NOTIFY_ALARM = "com.streamerhonda.locktimer.action.notify_alarm";
    static final public String ACTION_LOCK = "com.streamerhonda.locktimer.action.lock";
    static final public String ACTION_START = "com.streamerhonda.locktimer.action.start";
    static final public String ACTION_CANCEL = "com.streamerhonda.locktimer.action.cancel";

    static public int mTimeLeft = -1; // ms
    private Service mService = null;

    static final private int NOTIFICATION_ID_ALARM1 = 101;
    static final private int NOTIFICATION_ID_ALARM2 = 102;

    @Override
    public void onCreate() {
        mService = this;
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        AlarmManager alarm_manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(ACTION_NOTIFY_ALARM);
        PendingIntent pending_intent = PendingIntent.getBroadcast(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarm_manager.cancel(pending_intent);
        pending_intent.cancel();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID_ALARM1);
        notificationManager.cancel(NOTIFICATION_ID_ALARM2);
        this.unregisterReceiver(mScreenOffReceiver);
        mTimeLeft = -1;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        String action = intent.getAction();
        if (action == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        if (action.equals(ACTION_START)) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            try {
                registerReceiver(mScreenOffReceiver, filter);
            } catch (RuntimeException e) {
            }
            Bundle b = intent.getExtras();
            if (b != null) {
                int period = b.getInt(EXTRA_PERIOD_MSEC);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.add(Calendar.MILLISECOND, period);
                Intent intent_activity = new Intent(this, MainActivity.class);
                PendingIntent pendingintent_activity = PendingIntent
                        .getActivity(this, 0, intent_activity, 0);
                Notification notification = new Notification.Builder(this)
                        .setContentTitle(
                                this.getString(R.string.notification_title))
                        .setContentText(
                                this.getString(R.string.notification_text_head)
                                        + calendar.get(Calendar.HOUR_OF_DAY)
                                        + ":" + calendar.get(Calendar.MINUTE))
                        .setContentIntent(pendingintent_activity)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setAutoCancel(true).build();
                startForeground(R.drawable.ic_launcher, notification);
                setAlarm(period);
            }
        } else if (action.equals(ACTION_LOCK)) {
            if (mTimeLeft > 0) {
                setAlarm(0);
            } else if (mTimeLeft == 0) {
                if (DEBUG) {
                    Log.d(TAG, "screen off start");
                }
                DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                dpm.lockNow();
            }
        }

        return START_REDELIVER_INTENT;
    }

    private void setAlarm(int given_time_left) {
        int time_left = mTimeLeft;
        if (given_time_left > 0) {
            time_left = given_time_left;
        }
        if (time_left > ALERT1_TIME_LEFT_MSEC) {
            time_left -= ALERT1_TIME_LEFT_MSEC;
            mTimeLeft = ALERT1_TIME_LEFT_MSEC;
        } else if (time_left > ALERT2_TIME_LEFT_MSEC) {
            if (time_left == ALERT1_TIME_LEFT_MSEC) {
                Notification notification = new Notification.Builder(this)
                        .setCategory(Notification.CATEGORY_ALARM)
                        .setContentTitle(this.getText(R.string.app_name))
                        .setContentText(
                                this.getText(R.string.notification_alarm1_title))
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setAutoCancel(true).setVibrate(new long[]{500})
                        .setPriority(Notification.PRIORITY_HIGH).build();
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager
                        .notify(NOTIFICATION_ID_ALARM1, notification);
            }
            time_left -= ALERT2_TIME_LEFT_MSEC;
            mTimeLeft = ALERT2_TIME_LEFT_MSEC;
        } else if (time_left > 0) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_ID_ALARM1);
            if (time_left == ALERT2_TIME_LEFT_MSEC) {
                Notification notification = new Notification.Builder(this)
                        .setCategory(Notification.CATEGORY_ALARM)
                        .setContentTitle(this.getText(R.string.app_name))
                        .setContentText(
                                this.getText(R.string.notification_alarm2_title))
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setAutoCancel(true).setVibrate(new long[]{500})
                        .setPriority(Notification.PRIORITY_HIGH).build();
                notificationManager
                        .notify(NOTIFICATION_ID_ALARM2, notification);
            }
            mTimeLeft = 0;
        } else {
            mTimeLeft = -1;
            return;
        }
        AlarmManager alarm_manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(ACTION_NOTIFY_ALARM);
        PendingIntent pending_intent = PendingIntent.getBroadcast(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarm_manager.setExact(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + time_left, pending_intent);
        if (DEBUG) {
            Log.d(TAG, "set alarm in " + (time_left / 1000) + " sec.");
        }
    }

    public static boolean isRunning() {
        return mTimeLeft >= 0;
    }

    BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) {
                Log.d(TAG, "screen off -> stop service");
            }
            mService.stopSelf();
        }
    };
}
