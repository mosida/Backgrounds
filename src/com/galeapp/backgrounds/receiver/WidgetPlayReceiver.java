package com.galeapp.backgrounds.receiver;

import java.io.File;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.Toast;

import com.galeapp.backgrounds.R;
import com.galeapp.backgrounds.activity.SettingActivity;
import com.galeapp.backgrounds.widget.HugeWidgetProvider;
import com.galeapp.utils.FileManager;
import com.galeapp.utils.ImageFilter;

public class WidgetPlayReceiver extends BroadcastReceiver {
	public static final String TAG = "WidgetReceiver";
	SharedPreferences settingSP;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e(TAG, "WidgetReceiver");
		settingSP = context.getSharedPreferences("settings",
				Context.MODE_PRIVATE);
		boolean isChanged = settingSP.getBoolean("wallpaperChange", false);

		if (isChanged) {
			// 已经在播放，关闭播放
			isChanged = false;
			Toast.makeText(context, "关闭定时壁纸功能", Toast.LENGTH_LONG).show();
			Editor editor = settingSP.edit();
			editor.putBoolean("wallpaperChange", false);
			editor.commit();
			HugeWidgetProvider.updateAppWidget(context);
			Intent i = new Intent(context, WallpaperReceiver.class);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
					0, i, 0);
			AlarmManager alarmManager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(pendingIntent);
			return;
		} else {
			isChanged = true;
			Toast.makeText(context, "开启定时壁纸功能壁纸功能", Toast.LENGTH_LONG).show();
		}
		File picsDir = new File(FileManager.getSavePath());
		String[] picsPath = picsDir.list(new ImageFilter());
		if (picsPath == null || picsPath.length == 0) {
			Toast.makeText(context, "最壁纸图库没有壁纸啦，请用最壁纸先下载壁纸", Toast.LENGTH_LONG)
					.show();
			return;
		}
		Editor editor = settingSP.edit();
		editor.putBoolean("wallpaperChange", true);
		editor.commit();
		HugeWidgetProvider.updateAppWidget(context);

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
		wallpaperCalendar.setTimeInMillis(System.currentTimeMillis());
		alarmManager.setRepeating(AlarmManager.RTC,
				wallpaperCalendar.getTimeInMillis(), changeTime, pendingIntent);
		Toast.makeText(context, "开始切换下一张壁纸...", Toast.LENGTH_LONG).show();

	}

}
