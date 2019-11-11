package com.example.managebattery;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.TextView;
import android.widget.Toast;

public class DischargeService extends Service {

    private int mProgressStatus;
    public int ID_NOTIFICATION1 = 1;
    private int chargeLimit;
    private int dechargeLimit;
    private int dureeVibreur;
    private int verif;

    @Override
    public void onCreate() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        //Mentionner l'intent-filter dans le Manifest n'est pas obligatoire
        this.registerReceiver(mDischargeBroadcastReceiver,ifilter);
        super.onCreate();

        setupSharedPreferences();
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        chargeLimit = sharedPreferences.getInt("seekBarPrefCharge",85);
        dechargeLimit = sharedPreferences.getInt("seekBarPrefDischarge",20);
        dureeVibreur = sharedPreferences.getInt("seekBarPref",500);
    }

    //Classe d'un service, à surcharger
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        // START YOUR TASKS
        return super.onStartCommand(intent, flags, startId);
        // return START_STICKY; ???
    }

    //Classe d'un service, à surcharger
    @Override
    public void onDestroy() {
        // STOP YOUR TASKS
        unregisterReceiver(mDischargeBroadcastReceiver);
        super.onDestroy();
    }

    //Classe d'un service, à surcharger
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    private BroadcastReceiver mDischargeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int connect = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

            float batteryPct = 100 * (level / (float) scale);
            mProgressStatus = (int) batteryPct;
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            if (connect == 0) {
                if (verif != mProgressStatus) {
                    if (mProgressStatus == dechargeLimit) {
                        int icon = R.drawable.ic_stat_battery_alertld;

                        Resources res = getResources();
                        String notifdischarge = res.getString(R.string.notif_discharge);
                        String notifdischargetitle = res.getString(R.string.notif_discharge_title);

                        //setupSharedPreferences();

                        //Ce qui suit renvoie vers l'activité "MainActivity" lorsqu'on clique sur la notification
                        Intent notificationIntent = new Intent(context, MainActivity.class);
                        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

                        // La notification est créée
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "ok")
                                .setSmallIcon(icon)
                                .setContentTitle(notifdischargetitle)
                                .setContentText(notifdischarge)
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(notifdischarge))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setContentIntent(contentIntent)
                                .setAutoCancel(true);

                        //notification.setLatestEventInfo(MainActivity.this, "Titre", "Texte", contentIntent);

                        // Récupération du Notification Manager
                        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
                        //NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        manager.notify(ID_NOTIFICATION1, builder.build());

                        if (Build.VERSION.SDK_INT >= 26) {
                            v.vibrate(VibrationEffect.createOneShot(dureeVibreur, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            v.vibrate(dureeVibreur);
                            //Toast.makeText(context, "Niveau batterie = " + mProgressStatus + " et verif = " + verif + " et limite = " + chargeLimit, Toast.LENGTH_LONG).show();
                        }
                        verif = mProgressStatus;
                    }
                }
            } else {
                verif = 0;
                Intent i = new Intent(context, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        }
    };
}
