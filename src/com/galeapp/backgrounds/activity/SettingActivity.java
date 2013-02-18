package com.galeapp.backgrounds.activity;

import java.io.File;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.galeapp.backgrounds.Constants;
import com.galeapp.backgrounds.R;
import com.galeapp.backgrounds.receiver.WallpaperReceiver;
import com.galeapp.utils.FileManager;
import com.galeapp.utils.ImageFilter;
import com.galeapp.utils.ShortcutCreator;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.UMFeedbackService;

public class SettingActivity extends PreferenceActivity {
	public static final String TAG = "SettingActivity";
	ProgressDialog progressDialog;

	PreferenceScreen myScore;
	String useAgeMessage = "";
	Calendar wallpaperCalendar;
	SharedPreferences settingSP;
	ListPreference changeTimePS;
	boolean isChanged;
	int changeTimePosition = 0;
	String[] changeTimeValues;
	String[] changeTimes;

	CheckBoxPreference wallpaperPS;
	/******** 积分墙 *********/
	// 开关
	String appWallSwitch;
	// 积分
	int appWallScore;
	PreferenceScreen buyScreen;
	/******** 支付宝 *********/
	int chooseItem;
	private ProgressDialog mProgress = null;

	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.setting);
		settingSP = getSharedPreferences("settings", Context.MODE_PRIVATE);

		changeTimes = getResources().getStringArray(R.array.change_time);
		changeTimeValues = getResources().getStringArray(
				R.array.change_time_value);

		setGereralPS();
		setAboutPS();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

	public void onResume() {
		super.onResume();
		if (getParent() == null) {

		} else {
			getParent().findViewById(R.id.back).setVisibility(View.INVISIBLE);
		}


		isChanged = settingSP.getBoolean("wallpaperChange", false);
		wallpaperPS.setChecked(isChanged);
		changeTimePS.setEnabled(isChanged);

		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == -1) {
				progressDialog.dismiss();
				Toast.makeText(SettingActivity.this,
						R.string.clear_cache_failed, Toast.LENGTH_SHORT).show();
			} else if (msg.what == 1) {
				progressDialog.dismiss();
				Toast.makeText(SettingActivity.this,
						R.string.clear_cache_success, Toast.LENGTH_SHORT)
						.show();
			} else if (msg.what == 2) {
				progressDialog.dismiss();
				Dialog usageDlg = new AlertDialog.Builder(SettingActivity.this)
						.setIcon(android.R.drawable.ic_dialog_info)
						.setTitle(R.string.cache_usage)
						.setMessage(useAgeMessage)
						.setPositiveButton(R.string.apply, null).create();
				usageDlg.show();
			}
		}
	};

	// 功能界面布局
	public void setGereralPS() {
		// 定时壁纸
		isChanged = settingSP.getBoolean("wallpaperChange", false);
		wallpaperPS = (CheckBoxPreference) findPreference(getString(R.string.change_wallpaper));
		wallpaperPS
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {
						File file = new File(FileManager.getSavePath());
						if (file == null || !file.exists()) {
							Toast.makeText(SettingActivity.this,
									"sdCard未装载,无法启动定时壁纸功能", Toast.LENGTH_SHORT)
									.show();
							wallpaperPS.setChecked(false);
							return true;
						}
						if (file.list(new ImageFilter()) == null
								|| file.list(new ImageFilter()).length == 0) {
							Toast.makeText(SettingActivity.this,
									"图库中没有图片,请先下载壁纸", Toast.LENGTH_SHORT)
									.show();
							wallpaperPS.setChecked(false);
							return true;
						}

						// 开启状态下点击
						if (isChanged == true) {
							// 关闭
							isChanged = false;
							Intent intent = new Intent(SettingActivity.this,
									WallpaperReceiver.class);
							PendingIntent pendingIntent = PendingIntent
									.getBroadcast(SettingActivity.this, 0,
											intent, 0);
							AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
							alarmManager.cancel(pendingIntent);
							Toast.makeText(SettingActivity.this, "定时切换壁纸已取消",
									Toast.LENGTH_LONG).show();
						} else {
							// 开启
							isChanged = true;
							setChangeAlarm();
							Toast.makeText(
									SettingActivity.this,
									"定时切换壁纸已启动，切换频率:"
											+ changeTimes[changeTimePosition],
									Toast.LENGTH_LONG).show();
						}
						changeTimePS.setEnabled(isChanged);
						Editor editor = settingSP.edit();
						editor.putBoolean("wallpaperChange", isChanged);
						editor.commit();
						return false;
					}
				});
		// 时间频率设置
		changeTimePosition = settingSP.getInt("changeTimePostion", 0);
		changeTimePS = (ListPreference) findPreference(getString(R.string.change_time));
		changeTimePS.setEnabled(isChanged);
		changeTimePS.setDefaultValue(changeTimePosition);
		changeTimePS
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference arg0,
							Object arg1) {
						Log.i(TAG, "onPreferenceChange" + arg1.toString());

						int i = 0;
						for (; i < changeTimeValues.length; i++) {
							if (arg1.toString().equals(changeTimeValues[i])) {
								break;
							}
						}
						changeTimePosition = i;
						Editor editor = settingSP.edit();
						editor.putInt("changeTimePostion", changeTimePosition);
						editor.commit();
						Log.i(TAG, "changeTimePosition:" + changeTimePosition);
						changeTimePS.setDefaultValue(changeTimePosition);
						setChangeAlarm();
						Toast.makeText(
								SettingActivity.this,
								"定时切换壁纸已启动，切换频率:"
										+ changeTimes[changeTimePosition],
								Toast.LENGTH_LONG).show();
						return true;
					}
				});

		PreferenceScreen widgetPS = (PreferenceScreen) findPreference(getString(R.string.widget_change_wallpaper));
		widgetPS.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				AlertDialog widgetDialog = new AlertDialog.Builder(
						SettingActivity.this)
						.setTitle(R.string.widget_setting_detail)
						.setMessage(
								"长按桌面->小工具->选择'最壁纸--切换壁纸器(2x1)'版或者'最壁纸--切换壁纸器(1x1)'版")
						.setPositiveButton(R.string.apply, null).create();
				widgetDialog.show();
				return false;
			}
		});

		PreferenceCategory general = (PreferenceCategory) findPreference(getString(R.string.general));
		// 清空缓存
		PreferenceScreen clearCachePS = (PreferenceScreen) findPreference(getString(R.string.clear_cache));
		clearCachePS
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {
						progressDialog = new ProgressDialog(
								SettingActivity.this);
						progressDialog
								.setMessage(getString(R.string.please_wait_for_clear_cache));
						progressDialog.setCancelable(false);
						progressDialog.show();
						Thread thread = new Thread() {
							@Override
							public void run() {
								boolean flag = FileManager
										.clearDirectory(FileManager
												.getThumbnailPath());
								if (flag == true) {
									myHandler.sendEmptyMessage(1);
								} else {
									myHandler.sendEmptyMessage(-1);
								}
							}
						};
						thread.start();
						MobclickAgent.onEvent(SettingActivity.this,
								"clear_cache");
						return false;
					}
				});
		// 查看缓存占用大小
		Preference checkUsagePf = new Preference(this);
		checkUsagePf.setTitle(R.string.check_cache_usage);
		checkUsagePf.setSummary(R.string.check_cache_usage_of_sdcard);
		checkUsagePf
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {
						progressDialog = new ProgressDialog(
								SettingActivity.this);
						progressDialog
								.setMessage(getString(R.string.please_wait_for_processing));
						progressDialog.setCancelable(true);
						progressDialog.show();
						Thread thread = new Thread() {
							@Override
							public void run() {
								int sdcardSize = 0;
								int cacheUsage = 0;
								try {
									sdcardSize = (int) (FileManager
											.getAvailableStore(FileManager
													.getThumbnailPath()) / 1024) / 1024;
									cacheUsage = (int) (FileManager
											.getDirectorySize(FileManager
													.getThumbnailPath()) / 1024) / 1024;
								} catch (Exception e) {
									Log.i(TAG, "sdCard未装载");
								}
								useAgeMessage = "缓存大小:" + cacheUsage + "M\n\r"
										+ "SdCard剩余空间:" + sdcardSize + "M";
								myHandler.sendEmptyMessage(2);
							}
						};
						thread.start();
						return false;
					}
				});
		general.addPreference(checkUsagePf);
		// 创建快捷方式到桌面
		Preference shortcutPf = new Preference(this);
		shortcutPf.setTitle(R.string.create_shortcut_on_desk);
		shortcutPf
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {
						ShortcutCreator.createShortcut(getApplicationContext());
						return false;
					}
				});
		general.addPreference(shortcutPf);
	}

	// 关于设置布局
	public void setAboutPS() {
		PreferenceScreen feedbackPS = (PreferenceScreen) findPreference(getString(R.string.feedback));
		feedbackPS
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						// 调用反馈提供的接口，进入反馈界面
						UMFeedbackService
								.openUmengFeedbackSDK(SettingActivity.this);
						return false;
					}
				});

		PreferenceScreen galeScreen = (PreferenceScreen) findPreference(getString(R.string.gale));
		galeScreen
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference arg0) {
						Uri uri = Uri.parse("http://www.galeapp.com");
						// 通过Uri获得编辑框里的//地址，加上http://是为了用户输入时可以不要输入
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						// 建立Intent对象，传入uri
						startActivity(intent);
						return false;
					}
				});
	}


	public void setChangeAlarm() {
		Intent intent = new Intent(SettingActivity.this,
				WallpaperReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				SettingActivity.this, 0, intent, 0);

		long changeTime = Integer.valueOf(changeTimeValues[changeTimePosition]) * 60 * 60 * 1000;
		Log.i(TAG, "changeTime:" + changeTime);
		wallpaperCalendar = Calendar.getInstance();
		wallpaperCalendar.setTimeInMillis(System.currentTimeMillis());

		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.RTC,
				wallpaperCalendar.getTimeInMillis(), changeTime, pendingIntent);

	}

}
