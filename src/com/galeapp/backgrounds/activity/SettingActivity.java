package com.galeapp.backgrounds.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import net.umipay.android.PointsNotifier;
import net.umipay.android.PointsResult;
import net.umipay.android.SDKPointsManager;
import net.umipay.android.UmipaySDKManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class SettingActivity extends PreferenceActivity implements
		PointsNotifier {
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
		// 默认不开通
		SharedPreferences appWall = getSharedPreferences(Constants.APPWALL,
				MODE_PRIVATE);
		appWallSwitch = appWall.getString(Constants.APPWALL7, "off");
		if (appWallSwitch.equals("off")) {
			addPreferencesFromResource(R.xml.setting);
		} else {
			addPreferencesFromResource(R.xml.setting_appwall);
		}
		settingSP = getSharedPreferences("settings", Context.MODE_PRIVATE);

		changeTimes = getResources().getStringArray(R.array.change_time);
		changeTimeValues = getResources().getStringArray(
				R.array.change_time_value);

		// SDKPointsManager.AsyncQueryPoints(this, 0, this, 0);

		Log.i(TAG, "appWallSwitch:" + appWallSwitch);
		if (appWallSwitch.equals("off")) {
			// 不开通的情况下请求在线参数是否已经开通了
			MobclickAgent.updateOnlineConfig(this);
			appWallSwitch = MobclickAgent.getConfigParams(this,
					Constants.APPWALL7);
			Log.i(TAG,
					"appWallSwitch from umeng:"
							+ MobclickAgent.getConfigParams(this,
									Constants.APPWALL7));
			// 如果开通了，修改参数
			if (appWallSwitch.equals("on")) {
				Editor editor = settingSP.edit();
				editor.putString(Constants.APPWALL7, "on");
				editor.commit();
			}
		} else {
			setAppWallPS();
		}

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

		if (!UmipaySDKManager.isUmipayAccountLogined(this)) {

		} else {

			SDKPointsManager.AsyncQueryPoints(this, 0, this, 0);
			if (myScore != null) {
				myScore.setTitle(getString(R.string.current_score) + ":"
						+ appWallScore);
			}
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

	// 积分墙设置布局
	public void setAppWallPS() {
		myScore = (PreferenceScreen) findPreference(getString(R.string.current_score));
		myScore.setTitle(getString(R.string.current_score) + ":" + appWallScore);

		PreferenceScreen getScore = (PreferenceScreen) findPreference(getString(R.string.appwall_get_by_free));
		getScore.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				// AVLSKDHFKSDHFK.showAppOffers(SettingActivity.this);
				UmipaySDKManager.requestPayment(SettingActivity.this, 0);
				return false;
			}
		});

		PreferenceScreen loginScore = (PreferenceScreen) findPreference(getString(R.string.login_score));
		loginScore
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {
						SharedPreferences appWall = getSharedPreferences(
								Constants.APPWALL, MODE_PRIVATE);
						String lastDay = appWall.getString(
								Constants.LAST_LOGIN, "");
						SimpleDateFormat sdf = new SimpleDateFormat("dd");
						String currentDay = sdf.format(new Date());

						if (!lastDay.equals(currentDay)) {// 时间不一样
							// 拿积分
							// AVLSKDHFKSDHFK
							// .spendPoints(SettingActivity.this, -5);
							if (!UmipaySDKManager
									.isUmipayAccountLogined(SettingActivity.this)) {
								AlertDialog umiPayDlg = new AlertDialog.Builder(
										SettingActivity.this)
										.setIcon(
												android.R.drawable.ic_dialog_alert)
										.setTitle(R.string.tips)
										.setMessage("请先登录 米掌柜 ,再领取积分吧！")
										.setPositiveButton(
												R.string.appwall_get_by_free,
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														UmipaySDKManager
																.requestUmipayAccountLogin(SettingActivity.this);
													}
												})
										.setNegativeButton(R.string.cancel,
												null).create();
								umiPayDlg.show();
								return true;
							} else {
								SDKPointsManager.AsyncAwardPoints(
										SettingActivity.this, 0,
										SettingActivity.this, 0, 5);
							}

							// appWallScore = AVLSKDHFKSDHFK
							// .getPoints(SettingActivity.this);
							Editor editor = appWall.edit();
							editor.putString(Constants.LAST_LOGIN, currentDay);
							editor.commit();
							Toast.makeText(SettingActivity.this,
									R.string.thanks_for_the_login,
									Toast.LENGTH_LONG).show();
							MobclickAgent
									.onEvent(SettingActivity.this, "login");
						} else {
							Toast.makeText(SettingActivity.this,
									R.string.please_get_score_tomorrow,
									Toast.LENGTH_LONG).show();
						}
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

	@Override
	public void onPointsResult(int requestCode, PointsResult result) {
		// TODO Auto-generated method stub
		try {

			// 米掌柜SDK支持有米服务器虚拟货币托管服务，有别于有米积分墙的本地积分托管，使用米掌柜SDK的虚拟货币账户将会保存到有米服务器中。
			// 虚拟货币账户标识由[米掌柜账户ID+AppID+VID]组成，因此必须要求用户在已经登录了米掌柜的情况下使用虚拟货币账户的操作。
			// 每个虚拟货币账户都有一个编号: VID (目前只支持一套VID为0的默认虚拟货币账户，后续会开放对多套虚拟货币的支持)。
			// 要启用虚拟货币账户，必须在有米主站开发者控制面板中启用米掌柜业务，并且设置虚拟货币的单位名称及汇率。

			// ---
			// Demo中假设虚拟货币单位为"金币",VID为0

			String demoCurrencyName = "金币";

			StringBuilder msg = new StringBuilder();

			if (result != null) {

				switch (result.getOptionsType()) {// 获取回调的操作值
				case PointsResult.TYPE_OPTION_QUERY_POINTS:// 查询虚拟货币余额
					msg.append("查询").append(demoCurrencyName);
					break;
				case PointsResult.TYPE_OPTION_AWARD_POINTS:// 奖励给用户一定数额的虚拟货币
					msg.append("奖励").append(demoCurrencyName);
					break;
				case PointsResult.TYPE_OPTION_SPEND_POINTS:// 消费一定数额的虚拟货币
					msg.append("消费").append(demoCurrencyName);
					break;
				default:
					break;
				}

				switch (result.getResultCode()) {
				case PointsResult.RESULT_OK:// 操作成功
					msg.append("操作成功,账户余额为:").append(result.getPoints());
					appWallScore = result.getPoints();
					// showPoint(result.getPoints());// 显示虚拟货币余额
					myScore.setTitle(getString(R.string.current_score) + ":"
							+ appWallScore);
					break;
				case PointsResult.RESULT_ERROR_UMIPAY_UNLOGIN:// 当前用户未登录米掌柜，虚拟货币账户将不可用，需要用户进行登录
					msg.append("操作失败，请登录米掌柜");
					UmipaySDKManager
							.requestUmipayAccountLogin(SettingActivity.this);// 需要请求登录米掌柜账户
					break;
				case PointsResult.RESULT_ERROR_NETWORK_UNAVAILABLE:// 当前网络不可用，可提醒用户设置网络
					msg.append("操作失败，请检查网络设置");
					break;
				case PointsResult.RESULT_ERROR_SERVER_EXCEPTION:// 连接虚拟货币服务器失败
					msg.append("操作失败，连接服务器异常");
					break;
				case PointsResult.RESULT_ERROR_ILLEGAL_AMOUNT:// 奖励或消费的虚拟货币值不合法，必须为正整数
					msg.append("操作失败");
					break;
				case PointsResult.RESULT_ERROR_INSUFFICIENT:// 虚拟货币账户余额不足，消费虚拟货币失败
					msg.append("操作失败，账户余额不足");
					break;
				case PointsResult.RESULT_ERROR_VID_INVALID:// 虚拟货币编号无效，目标虚拟货币不存在
					msg.append("操作失败，账户不存在");
					break;
				case PointsResult.RESULT_ERROR_EXCEPTION:// 其他不可预料的异常
					msg.append("操作失败");
					break;
				default:
					break;
				}

				// demo:提示操作结果
				Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

				// demo:在界面上显示错误信息
				if (result.getResultCode() != PointsResult.RESULT_OK) {
					showErrMsg(result, requestCode, msg.toString());
				}
			}

		} catch (Throwable e) {
			// TODO: handle exception
		}
	}

	void showErrMsg(PointsResult result, int requestCode, String msg) {
		// Toast.makeText(this,
		// (String.format("%s\n(本次请求码:%d,虚拟货币编号:%d,错误码:%d)", msg,
		// requestCode, result.getVid(), result.getResultCode())),
		// Toast.LENGTH_LONG).show();
	}
}
