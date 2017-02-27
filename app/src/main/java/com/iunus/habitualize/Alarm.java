package com.iunus.habitualize;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Vibrator;

/**
 * Created by Iunus on 13/02/2017.
 * Source: http://stackoverflow.com/questions/4459058/alarm-manager-example
 */

public class Alarm extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean soundOn = intent.getBooleanExtra("soundOn", false);
        boolean vibrateOn = intent.getBooleanExtra("vibrateOn", false);

        if (soundOn) {
            MediaPlayer mp = MediaPlayer.create(context, R.raw.ringtone);
            mp.start();
        }

        if (vibrateOn) {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(new long[]{0, 1000, 1000, 1000}, 3);
        }
    }

    void set(Context context, long timeOffset, boolean soundOn, boolean vibrateOn) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Alarm.class);
        i.putExtra("soundOn", soundOn);
        i.putExtra("vibrateOn", vibrateOn);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeOffset, pi);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeOffset, pi);
        }
    }

    void cancel(Context context)
    {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
