package com.streamerhonda.locktimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LockTimerReceiver extends BroadcastReceiver {
    private boolean DEBUG = BuildConfig.DEBUG;
    static final private String TAG = LockTimerReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG) {
            Log.d(TAG, "onReceive() called.");
        }
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        if (action.equals(LockTimerService.ACTION_NOTIFY_ALARM)) {
            if (DEBUG) {
                Log.d(TAG, "alarm received.");
            }
            Intent i = new Intent(context, LockTimerService.class);
            i.setAction(LockTimerService.ACTION_LOCK);
            context.startService(i);
        }
    }

}
