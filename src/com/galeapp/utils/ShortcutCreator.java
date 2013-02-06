package com.galeapp.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import com.galeapp.backgrounds.R;
import com.galeapp.backgrounds.activity.SplashActivity;

public class ShortcutCreator {

	// 创建快捷方式
	public static void createShortcut(Context context) {
		Intent addIntent = new Intent(
				"com.android.launcher.action.INSTALL_SHORTCUT");
		Parcelable icon = Intent.ShortcutIconResource.fromContext(context,
				R.drawable.icon); // 获取快捷键的图标
		Intent myIntent = new Intent(context, SplashActivity.class);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
				context.getString(R.string.app_name));// 快捷方式的标题
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);// 快捷方式的图标
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, myIntent);// 快捷方式的动作
		context.sendBroadcast(addIntent);// 发送广播
	}
}
