package com.streamerhonda.locktimer;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
    private boolean DEBUG = BuildConfig.DEBUG;
    static final private String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn = (Button) findViewById(R.id.ButtonStart);
        btn.setOnClickListener(this);
        btn = (Button) findViewById(R.id.ButtonMin5);
        btn.setOnClickListener(this);
        btn = (Button) findViewById(R.id.ButtonMin10);
        btn.setOnClickListener(this);
        btn = (Button) findViewById(R.id.ButtonMin15);
        btn.setOnClickListener(this);
        btn = (Button) findViewById(R.id.ButtonMin20);
        btn.setOnClickListener(this);
        btn = (Button) findViewById(R.id.ButtonMin25);
        btn.setOnClickListener(this);
        btn = (Button) findViewById(R.id.ButtonMin30);
        btn.setOnClickListener(this);
        btn = (Button) findViewById(R.id.ButtonMin45);
        btn.setOnClickListener(this);
        btn = (Button) findViewById(R.id.ButtonMin60);
        btn.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        EditText et = (EditText) findViewById(R.id.editText1);
        switch (v.getId()) {
            case R.id.ButtonMin5:
                et.setText("5");
                break;
            case R.id.ButtonMin10:
                et.setText("10");
                break;
            case R.id.ButtonMin15:
                et.setText("15");
                break;
            case R.id.ButtonMin20:
                et.setText("20");
                break;
            case R.id.ButtonMin25:
                et.setText("25");
                break;
            case R.id.ButtonMin30:
                et.setText("30");
                break;
            case R.id.ButtonMin45:
                et.setText("45");
                break;
            case R.id.ButtonMin60:
                et.setText("60");
                break;
            case R.id.ButtonStart:
                DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                ComponentName cn = new ComponentName(this, LockTimerReceiver.class);
                if (!dpm.isAdminActive(cn)) {
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn);
                    startActivityForResult(intent, 1);
                    return;
                }
                String s = et.getText().toString();
                int msec = -1;
                try {
                    msec = 60 * 1000 * Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, R.string.alert_invalid_period, Toast.LENGTH_LONG).show();
                    return;
                }
                if ((msec < 60 * 1000) || (msec > 60 * 60 * 1000)) {
                    Toast.makeText(this, R.string.alert_invalid_period, Toast.LENGTH_LONG).show();
                    return;
                }
                Intent i = new Intent(this, LockTimerService.class);
                i.setAction(LockTimerService.ACTION_START);
                i.putExtra(LockTimerService.EXTRA_PERIOD_MSEC, msec);
                startService(i);
                v.setEnabled(false);
                break;
            default:
                break;
        }

    }

    @Override
    protected void onResume() {
        Button btn = (Button) findViewById(R.id.ButtonStart);
        if (LockTimerService.isRunning()) {
            btn.setEnabled(false);
        } else {
            btn.setEnabled(true);
        }
        super.onResume();
    }


}
