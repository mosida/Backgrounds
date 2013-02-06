package com.galeapp.backgrounds.receiver;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;

import com.galeapp.utils.FileManager;
import com.galeapp.utils.ImageFilter;

public class WallpaperReceiver extends BroadcastReceiver {

	public static final String TAG = "WallpaperReceiver";
	public int deviceW;
	public int deviceH;
	String[] picsPath;
	SharedPreferences settingSP;
	Bitmap resultBitmap;
	WallpaperManager wallpaperManager;
	boolean isChanged = false;

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.e(TAG, "change wallpaperReceiver");
		settingSP = context.getSharedPreferences("settings",
				Context.MODE_PRIVATE);
		isChanged = settingSP.getBoolean("wallpaperChange", false);
		if (!isChanged) {
			return;
		}

		File picsDir = new File(FileManager.getSavePath());
		picsPath = picsDir.list(new ImageFilter());
		if (picsPath == null || picsPath.length == 0) {
			Log.i(TAG, "picsPath is null");
			return;
		}

		String lastFile = settingSP.getString("lastFile", "");
		if (lastFile == null || lastFile.equals("")) {
			lastFile = picsPath[0];
		} else {
			int j = 0;
			for (int i = 0; i < picsPath.length; i++) {
				if (lastFile.equals(picsPath[i])) {
					j = i + 1;
					break;
				}
				;
			}
			if (j == picsPath.length) {
				j = 0;
			}
			lastFile = picsPath[j];
		}
		File file = new File(FileManager.getSaveFile(lastFile));
		if (file.exists()) {
			Log.i(TAG, "file 存在");
			resultBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
		} else {
			Log.i(TAG, "file 不存在");
			return;
		}
		if (resultBitmap == null) {
			Log.i(TAG, "resultBitmap is null");
			return;
		}
		Log.i(TAG, "file is " + file.getAbsolutePath());

		Bitmap newBitmap = null;

		// 清空壁纸，黑色背景
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		deviceW = dm.widthPixels;
		deviceH = dm.heightPixels;
		int newW = deviceH * resultBitmap.getWidth() / resultBitmap.getHeight();
		newBitmap = Bitmap
				.createScaledBitmap(resultBitmap, newW, deviceH, true);
		wallpaperManager = (WallpaperManager) context
				.getSystemService(Context.WALLPAPER_SERVICE);
		wallpaperManager.suggestDesiredDimensions(newW, deviceH);
		try {
			wallpaperManager.setBitmap(newBitmap);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		Editor editor = settingSP.edit();
		editor.putString("lastFile", lastFile);
		editor.putLong("lastSetWallpaperTime", System.currentTimeMillis());
		editor.commit();
		Log.i(TAG, "new Bitmap Seted");

	}

}
