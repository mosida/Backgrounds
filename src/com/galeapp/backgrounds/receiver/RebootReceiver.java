package com.galeapp.backgrounds.receiver;

import java.util.Calendar;

import com.galeapp.backgrounds.R;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class RebootReceiver extends BroadcastReceiver {
	public static final String TAG = "RebootReceiver";
	SharedPreferences settingSP;

	@Override
	public void onReceive(Context context, Intent arg1) {
		Log.e(TAG, "RebootReceiver");
		settingSP = context.getSharedPreferences("settings",
				Context.MODE_PRIVATE);
		long lastSetWallpaperTime = settingSP.getLong("lastSetWallpaperTime",
				0L);
		int changeTimePosition = settingSP.getInt("changeTimePostion", 0);
		String[] changeTimeValues = context.getResources().getStringArray(
				R.array.change_time_value);
		long changeTime = Integer.valueOf(changeTimeValues[changeTimePosition]) * 60 * 60 * 1000;

		Intent i = new Intent(context, WallpaperReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i,
				0);
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Calendar wallpaperCalendar = Calendar.getInstance();
		// 下次的时间
		long betweenTime = System.currentTimeMillis() - lastSetWallpaperTime;
		long nextTime = 0L;
		if (betweenTime >= changeTime) {
			nextTime = 0L;
		} else {
			nextTime = changeTime - betweenTime;
		}
		wallpaperCalendar.setTimeInMillis(System.currentTimeMillis());
		alarmManager.setRepeating(AlarmManager.RTC,
				wallpaperCalendar.getTimeInMillis() + nextTime, changeTime,
				pendingIntent);
	}
}
