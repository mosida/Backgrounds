package com.galeapp.backgrounds.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.galeapp.backgrounds.R;
import com.galeapp.backgrounds.receiver.WallpaperReceiver;
import com.galeapp.backgrounds.receiver.WidgetPlayReceiver;
import com.galeapp.backgrounds.receiver.WidgetRefreshReceiver;

public class HugeWidgetProvider extends AppWidgetProvider {

	public static final String TAG = "HugeWidgetProvider";
	public static SharedPreferences settingSP;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Log.i(TAG, "onUpdate");

		final int N = appWidgetIds.length;
		Log.i(TAG, "N:" + N);
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			Log.i(TAG, "i:" + i + " appWidgetId:" + appWidgetId);
			updateAppWidget(context, appWidgetManager, appWidgetId);
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		Log.i(TAG, "onDeleted");
	}

	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "onReceive");
		super.onReceive(context, intent);
	}

	public static void updateAppWidget(Context context,
			AppWidgetManager appWidgetManager, int appWidgetId) {
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.huge_appwidget_provider);
		views.setViewVisibility(R.id.switchBtn, View.VISIBLE);

		Intent intent = new Intent(context, WidgetRefreshReceiver.class);
		PendingIntent changeWallpaperPI = PendingIntent.getBroadcast(context,
				0, intent, 0);
		views.setOnClickPendingIntent(R.id.switchBtn, changeWallpaperPI);

		Intent playIntent = new Intent(context, WidgetPlayReceiver.class);
		settingSP = context.getSharedPreferences("settings",
				Context.MODE_PRIVATE);
		boolean isChanged = settingSP.getBoolean("wallpaperChange", false);
		if (isChanged) {// 开启
			Log.i(TAG, "isChange " + isChanged + " playbtn");
			// views.setImageViewResource(R.id.playBtn, R.drawable.play_btn);
			views.setImageViewBitmap(R.id.playBtn,
					BitmapFactory.decodeResource(context.getResources(),
							R.drawable.play_btn));
		} else {// 关闭
			Log.i(TAG, "isChange " + isChanged + " stopbtn");
			// views.setImageViewResource(R.id.playBtn, R.drawable.stop_btn);
			views.setImageViewBitmap(R.id.playBtn,
					BitmapFactory.decodeResource(context.getResources(),
							R.drawable.stop_btn));
		}
		PendingIntent playWallpaperPI = PendingIntent.getBroadcast(context, 0,
				playIntent, 0);
		views.setViewVisibility(R.id.playBtn, View.VISIBLE);
		views.setOnClickPendingIntent(R.id.playBtn, playWallpaperPI);

		appWidgetManager.updateAppWidget(appWidgetId, views);

	}

	public static void updateAppWidget(Context context) {
		Log.i(TAG, "updateHugeAppWidget");
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		updateAppWidget(context, appWidgetManager, R.id.hugeWidget);

	}
}
