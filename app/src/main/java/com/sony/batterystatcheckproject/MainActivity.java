package com.sony.batterystatcheckproject;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.BatteryManager;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;


public class MainActivity extends ActionBarActivity {

    private TextView batteryChargeStatus;
    private TextView usbChargeStatus;
    private TextView acChargeStatus;
    private TextView batteryPercentageStatus;
    private Button btnbatteryUsage;
    private Button btnOptimize;
    private TextView appsCount;

    boolean isCharging;
    boolean usbCharge;
    boolean acCharge;
    int batteryLevel;
    float batteryLife;

    Intent intentBatteryUsage;
    ResolveInfo resolveInfo;

    PowerConnectionReceiver powerConnectionReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        batteryChargeStatus = (TextView)findViewById(R.id.battery_charge_status);
        usbChargeStatus = (TextView)findViewById(R.id.usb_charge_status);
        acChargeStatus = (TextView)findViewById(R.id.ac_charge_status);
        batteryPercentageStatus = (TextView)findViewById(R.id.battery_percentage_status);
        btnbatteryUsage = (Button) findViewById(R.id.btn_battery_usage);
        btnOptimize = (Button) findViewById(R.id.btn_optimize);
        appsCount = (TextView) findViewById(R.id.background_apps_count);

        batteryIntentGenerator();
        batteryLevelRecvFunc();
        batterySummery();
        optimizeFunc();
        updateUI();
    }

    private void batterySummery() {
        intentBatteryUsage = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
        resolveInfo = getPackageManager().resolveActivity(intentBatteryUsage,0);

        //IntentBatteryUsageActivity batteryUsageActivity = new IntentBatteryUsageActivity();

        if (resolveInfo == null) {
            btnbatteryUsage.setEnabled(false);
        }
        else {
            btnbatteryUsage.setEnabled(true);
        }

        btnbatteryUsage.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intentBatteryUsage);
            }
        });
    }

    private void batteryLevelRecvFunc() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);

        BatteryLevelReceiver batteryLevelReceiver = new BatteryLevelReceiver();
        Intent levelRecv = this.registerReceiver(batteryLevelReceiver,filter);
    }

    private void optimizeFunc() {
        final List<ApplicationInfo> apps;
        PackageManager packageManager = getPackageManager();

        apps = packageManager.getInstalledApplications(0);
        appsCount.setText(apps.size()+"");

        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        btnOptimize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (ApplicationInfo info : apps) {
                    if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 1)
                        continue;
                    if (info.packageName.equals("com.sony.batterystatcheckproject"))
                        continue;
                    activityManager.killBackgroundProcesses(info.packageName);
                }
            }
        });

        appsCount.setText(apps.size()+"");
    }

    private void updateUI() {
        batteryChargeStatus.setText(isCharging+"");
        usbChargeStatus.setText(usbCharge+"");
        acChargeStatus.setText(acCharge+"");
        batteryPercentageStatus.setText(batteryLevel+"");
    }

    private void batteryIntentGenerator() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        //Intent batteryStatus = this.registerReceiver(null,filter);

        powerConnectionReceiver = new PowerConnectionReceiver();
        Intent batteryStatus = this.registerReceiver(powerConnectionReceiver,filter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        batteryLevel = (int) ((level / (float)scale) * 100);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(powerConnectionReceiver);
    }

    private class PowerConnectionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MyTag","BroadcastReceiver");
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL);

            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            usbCharge = (chargePlug == BatteryManager.BATTERY_PLUGGED_USB);
            acCharge = (chargePlug == BatteryManager.BATTERY_PLUGGED_AC);

            //updateUI(intent);
            if (context != null){
                updateUI();
            }
        }
    }

    private class BatteryLevelReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        }
    }

    private class IntentBatteryUsageActivity extends Activity {

        @Override
        public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.battery_usage_layout);

        }
    }
}
