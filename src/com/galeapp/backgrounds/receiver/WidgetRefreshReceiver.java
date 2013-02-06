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
import com.galeapp.utils.FileManager;
import com.galeapp.utils.ImageFilter;

public class WidgetRefreshReceiver extends BroadcastReceiver {
	public static final String TAG = "WidgetReceiver";
	SharedPreferences settingSP;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e(TAG, "WidgetReceiver");
		settingSP = context.getSharedPreferences("settings",
				Context.MODE_PRIVATE);
		boolean isChanged = settingSP.getBoolean("wallpaperChange", false);

		if (isChanged == false) {
			Toast.makeText(context, "还没启动定时壁纸功能，请在最壁纸设置界面启动", Toast.LENGTH_LONG)
					.show();
			return;
		}
		File picsDir = new File(FileManager.getSavePath());
		String[] picsPath = picsDir.list(new ImageFilter());
		if (picsPath == null || picsPath.length == 0) {
			Toast.makeText(context, "最壁纸图库没有壁纸啦，请用最壁纸先下载壁纸", Toast.LENGTH_LONG)
					.show();
			return;
		}

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
