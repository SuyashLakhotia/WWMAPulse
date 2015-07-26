package com.example.WWMAPulse;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.razer.android.nabuopensdk.NabuOpenSDK;
import com.razer.android.nabuopensdk.interfaces.PulseListener;
import com.razer.android.nabuopensdk.models.PulseData;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class PulseReceiver extends Service {
    static NabuOpenSDK nabuSDK = null;

    RemindersDBAdapter reminddb;

    public static final long NOTIFY_INTERVAL = 60 * 1000; // 60 secs

    StringBuilder builder;

    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;

    public void onCreate() {
        nabuSDK = NabuOpenSDK.getInstance(this);

        reminddb = new RemindersDBAdapter(this);

        // cancel if already existed
        if(mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }
        // schedule task
        mTimer.scheduleAtFixedRate(new Pulse(), 0, NOTIFY_INTERVAL);

        super.onCreate();
    }


    class Pulse extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.HOUR_OF_DAY, -1);
                    nabuSDK.getPulseData(PulseReceiver.this, c.getTimeInMillis(), System.currentTimeMillis(), new PulseListener() {

                        @Override
                        public void onReceiveFailed(String arg0) {
                            builder = new StringBuilder();

                            builder.append(arg0);

                            if (builder.length() == 0)
                                builder.append("Receive Failed");

                            Log.e("Pulse Service Data", builder.toString());

                        }

                        @Override
                        public void onReceiveData(PulseData[] arg0) {
                            builder = new StringBuilder();

                            for (PulseData pulse : arg0) {
                                Calendar c = Calendar.getInstance();
                                c.setTimeInMillis(pulse.timeStamp * 1000);
                                builder.append(Integer.toString(c.get(Calendar.DATE)) + Integer.toString(c.get(Calendar.HOUR_OF_DAY)) + Integer.toString(c.get(Calendar.MINUTE)));
                                builder.append("\n");
                                builder.append("-------------------------");
                                builder.append("\n");
                                for (String userID : pulse.userIDs) {
                                    builder.append(userID);
                                    builder.append("\n");
                                }
                                builder.append("-------------------------");
                                builder.append("\n");
                            }

                            if (builder.length() == 0)
                                builder.append("No Nabus Nearby");

                            Log.e("Pulse Service Data", builder.toString());


                            for (PulseData pulse : arg0) {
                                Calendar c = Calendar.getInstance();
                                c.setTimeInMillis(pulse.timeStamp * 1000);

                                if (System.currentTimeMillis() - c.getTimeInMillis() <= 600000) {
                                    for (String userID : pulse.userIDs) {

                                        int[] ROWID = new int[10];

                                        ROWID = reminddb.SearchReminder(userID);

                                        if (ROWID[0] != 0) {
                                            for (int e = 0; e < 10; e++) {
                                                if (ROWID[e] != 0) {
                                                    if (reminddb.getReminded(ROWID[e]) == 0) {
                                                        new ReminderManager(PulseReceiver.this).setReminder((long) ROWID[e], Calendar.getInstance());
                                                        reminddb.setasReminded(ROWID[e]);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    });

                    Log.e("Pulse Service", "Done");
                }

            });
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        super.onDestroy();
    }
}
