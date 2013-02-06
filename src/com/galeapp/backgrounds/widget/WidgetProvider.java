package com.galeapp.backgrounds.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.galeapp.backgrounds.R;
import com.galeapp.backgrounds.receiver.WallpaperReceiver;
import com.galeapp.backgrounds.receiver.WidgetRefreshReceiver;

public class WidgetProvider extends AppWidgetProvider {

	public static final String TAG = "WidgetProvider";
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
				R.layout.appwidget_provider);
		views.setViewVisibility(R.id.switchBtn, View.VISIBLE);

		Intent intent = new Intent(context, WidgetRefreshReceiver.class);
		PendingIntent changeWallpaperPI = PendingIntent.getBroadcast(context,
				0, intent, 0);
		views.setOnClickPendingIntent(R.id.switchBtn, changeWallpaperPI);
		appWidgetManager.updateAppWidget(appWidgetId, views);

	}
}
