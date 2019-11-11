package com.example.managebattery;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private TextView charge = null;
    private int mProgressStatus;
    private int verif;
    private Menu m = null;
    private int chargeLimit;
    private int dechargeLimit;
    private int dureeVibreur;
    public int ID_NOTIFICATION = 0;
    //private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupSharedPreferences();

        //mContext = getApplicationContext();

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        //Mentionner l'intent-filter pour ACTION_BATTERY_CHANGED dans le Manifest n'est pas obligatoire
        this.registerReceiver(mBroadcastReceiver,ifilter);

        IntentFilter ifilterclose = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
        //Mentionner l'intent-filter dans le Manifest n'est pas obligatoire
        this.registerReceiver(mCloseApp,ifilterclose);
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        chargeLimit = sharedPreferences.getInt("seekBarPrefCharge",85);
        dechargeLimit = sharedPreferences.getInt("seekBarPrefDischarge",20);
        dureeVibreur = sharedPreferences.getInt("seekBarPref",500);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        //R.menu.menu est l'id de notre menu
        inflater.inflate(R.menu.menu, menu);
        m = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, AppCompatPreferenceActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("seekBarPrefCharge")) {
            chargeLimit = sharedPreferences.getInt("seekBarPrefCharge", 85);
            //Toast.makeText(MainActivity.this, "limite de charge = " + chargeLimit, Toast.LENGTH_LONG).show();
        }
        if (key.equals("seekBarPrefDischarge")) {
            dechargeLimit = sharedPreferences.getInt("seekBarPrefDischarge", 20);
            //Toast.makeText(MainActivity.this, "limite de décharge = " + dechargeLimit, Toast.LENGTH_LONG).show();
        }
        if (key.equals("seekBarPref")) {
            dureeVibreur = sharedPreferences.getInt("seekBarPref", 500);
            //Toast.makeText(MainActivity.this, "durée de vibration = " + dureeVibreur, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(mCloseApp);
        android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //receiver = new PowerConnectionReceiver();
            //Intent batteryStatus = this.registerReceiver(null, ifilter);

            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            charge = (TextView) findViewById(R.id.charge);

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            float batteryPct = 100 * (level / (float) scale);
            mProgressStatus = (int) batteryPct;
            charge.setText(String.valueOf(mProgressStatus) + "%");

            if (verif != mProgressStatus) {
                if (mProgressStatus >= chargeLimit) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        v.vibrate(VibrationEffect.createOneShot(dureeVibreur, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        v.vibrate(dureeVibreur);
                        //Toast.makeText(MainActivity.this, "Niveau batterie = " + mProgressStatus + " et verif = " + verif + " et limite = " + chargeLimit, Toast.LENGTH_LONG).show();
                    }
                    verif = mProgressStatus;
                }
                if (mProgressStatus == chargeLimit) {
                    int icon = R.drawable.ic_stat_battery_fullld;

                    Resources res = getResources();
                    String notifcharge = res.getString(R.string.notif_charge);
                    String notifchargetitle = res.getString(R.string.notif_charge_title);

                    //setupSharedPreferences();

                    //Ce qui suit renvoie vers l'activité "MainActivity" lorsqu'on clique sur la notification
                    Intent notificationIntent = new Intent(context, MainActivity.class);
                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

                    // La notification est créée
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "ok")
                            .setSmallIcon(icon)
                            .setContentTitle(notifchargetitle)
                            .setContentText(notifcharge)
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(notifcharge))
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(contentIntent)
                            .setAutoCancel(true);

                    //notification.setLatestEventInfo(MainActivity.this, "Titre", "Texte", contentIntent);

                    // Récupération du Notification Manager
                    NotificationManagerCompat manager = NotificationManagerCompat.from(context);
                    //NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    manager.notify(ID_NOTIFICATION, builder.build());
                }
            }
        }
    };

    private BroadcastReceiver mCloseApp = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //verif = 0;
            finishAndRemoveTask();
            //finish();
            //moveTaskToBack(true);
        }
    };
}
