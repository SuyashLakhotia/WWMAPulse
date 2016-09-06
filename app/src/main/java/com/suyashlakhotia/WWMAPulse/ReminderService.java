package com.suyashlakhotia.WWMAPulse;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReminderService extends WakeReminderIntentService {

    public ReminderService() {
        super("ReminderService");
    }

    @Override
    void doReminderWork(Intent intent) {
        Log.d("ReminderService", "Doing work.");
        Long rowId = intent.getExtras().getLong(RemindersDBAdapter.KEY_ROWID);

        NotificationManager mgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra(RemindersDBAdapter.KEY_ROWID, rowId);

        PendingIntent pi = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

        Context ctx2 = this;
        RemindersDBAdapter objDB = new RemindersDBAdapter(ctx2);
        Notification note;


        note = new Notification(android.R.drawable.stat_sys_warning, objDB.getNotif(RemindersDBAdapter.KEY_ROWID), System.currentTimeMillis());
        note.setLatestEventInfo(this, getString(R.string.notify_new_task_title), objDB.getNotif(RemindersDBAdapter.KEY_ROWID), pi);
        note.defaults |= Notification.DEFAULT_SOUND;
        note.flags |= Notification.FLAG_AUTO_CANCEL;

        int id = (int)((long)rowId);
        mgr.notify(id, note);
    }
}

