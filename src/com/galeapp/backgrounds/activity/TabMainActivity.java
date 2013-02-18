package com.galeapp.backgrounds.activity;

import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.galeapp.backgrounds.Constants;
import com.galeapp.backgrounds.R;
import com.galeapp.backgrounds.adapter.TabAdapter;
import com.galeapp.utils.FileManager;
import com.galeapp.utils.ShortcutCreator;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.NotificationType;
import com.umeng.fb.UMFeedbackService;

public class TabMainActivity extends ActivityGroup {

	public static final String TAG = "TabMainActivity";

	private GridView navBar;
	private TabAdapter topImgAdapter;
	public LinearLayout container;// 装载sub Activity的容器
	HorizontalScrollView scrollView;
	private static int screenWidth;
	private static int tabMenuInt;

	SharedPreferences settingSP;

	/** 顶部按钮图片 **/
	int[] topbar_image_array = { R.drawable.recent, R.drawable.hotest,
			R.drawable.category, R.drawable.topic, R.drawable.favorite,
			R.drawable.pics, R.drawable.setting, };

	int[] topbar_image_selected_array = { R.drawable.recent_bg,
			R.drawable.hotest_bg, R.drawable.category_bg, R.drawable.topic_bg,
			R.drawable.favorite_bg, R.drawable.pics_bg, R.drawable.setting_bg, };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		// umeng feedback
		MobclickAgent.onError(this);
		UMFeedbackService.enableNewReplyNotification(this,
				NotificationType.AlertDialog);
		//
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenWidth = dm.widthPixels;

		setContentView(R.layout.activity_tabmain);

		scrollView = (HorizontalScrollView) findViewById(R.id.scroll);
		// 建立各种文件夹
		mkDir();
		settingSP = getSharedPreferences("settings", Context.MODE_PRIVATE);
		int updateVersion = settingSP.getInt("updateVersion", 1);
		if (updateVersion != 8) {
			// AlertDialog changlogDlg = new AlertDialog.Builder(this)
			// .setIcon(R.drawable.icon)
			// .setTitle(R.string.changelog_title)
			// .setMessage("Verson 1.6.1103 beta:\r\n" +
			// "------------------\r\n" +
			// "--(修复)网络连接提示错误问题\r\n"+
			// "--(优化)图库界面操作优化\r\n"+
			// "--(优化)桌面小部件UI优化\r\n"
			// )
			// .setPositiveButton(R.string.apply, new
			// DialogInterface.OnClickListener() {
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// Toast.makeText(TabMainActivity.this, "底部导航栏可以左右拖动哦",
			// Toast.LENGTH_LONG).show();
			// }
			// })
			// .create();
			// changlogDlg.show();
			Toast.makeText(TabMainActivity.this, "底部导航栏可以左右拖动哦",
					Toast.LENGTH_LONG).show();

			Editor editor = settingSP.edit();
			editor.putInt("updateVersion", 8);
			editor.commit();
			boolean shortCutCreated = settingSP.getBoolean("scCreated", false);
			if (shortCutCreated == false) {
				ShortcutCreator.createShortcut(TabMainActivity.this);
				editor.putBoolean("scCreated", true);
				editor.commit();
			}
		}

		// 根据屏幕宽度，设置导航栏的宽度
		int tabSize = screenWidth / 4;
		int allTabWidth = tabSize * topbar_image_array.length;
		tabMenuInt = tabSize * (topbar_image_array.length - 4);
		Log.i(TAG, "tabSize:" + tabSize + " allTabWidth:" + allTabWidth
				+ " tabMenuInt:" + tabMenuInt);
		LinearLayout myGridContent = (LinearLayout) findViewById(R.id.myGridContent);
		FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(allTabWidth,
				FrameLayout.LayoutParams.WRAP_CONTENT);
		myGridContent.setLayoutParams(fl);
		//

		navBar = (GridView) findViewById(R.id.navBar);
		navBar.setNumColumns(topbar_image_array.length);// 设置每行列数
		navBar.setSelector(new ColorDrawable(Color.TRANSPARENT));// 选中的时候为透明色
		navBar.setGravity(Gravity.CENTER);// 位置居中
		navBar.setVerticalSpacing(0);// 垂直间隔

		topImgAdapter = new TabAdapter(this, topbar_image_array,
				topbar_image_selected_array);
		navBar.setAdapter(topImgAdapter);// 设置菜单Adapter
		navBar.setOnItemClickListener(new ItemClickEvent());// 项目点击事件
		container = (LinearLayout) findViewById(R.id.Container);
		SwitchActivity(0);// 默认打开第0页
		
	}

	public void mkDir() {
		// gale根目录
		FileManager.makeDir(Constants.GALE_PATH);
		// background目录
		FileManager.makeDir(Constants.BACKGROUNDS_PATH);
		// thumbnail目录
		FileManager.makeDir(Constants.THUMBNAIL_PATH);
		// image目录
		FileManager.makeDir(Constants.SAVE_PATH);
	}

	class ItemClickEvent implements OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			SwitchActivity(arg2);
		}
	}

	/**
	 * 根据ID打开指定的Activity
	 * 
	 * @param id
	 *            GridView选中项的序号
	 */
	void SwitchActivity(int id) {
		topImgAdapter.SetFocus(id);// 选中项获得高亮
		container.removeAllViews();// 必须先清除容器中所有的View
		Intent intent = null;
		Window subActivity = null;
		if (id == 0) {
			intent = new Intent(TabMainActivity.this, RecentActivity.class);
			// Activity 转为 View
			subActivity = getLocalActivityManager().startActivity(
					"RecentActivity", intent);
		} else if (id == 1) {
			intent = new Intent(TabMainActivity.this, HotestActivity.class);
			// Activity 转为 View
			subActivity = getLocalActivityManager().startActivity(
					"HotestActivity", intent);
		} else if (id == 2) {
			intent = new Intent(TabMainActivity.this, CategoryActivity.class);
			// Activity 转为 View
			subActivity = getLocalActivityManager().startActivity(
					"CategoryActivity", intent);
		} else if (id == 3) {
			intent = new Intent(TabMainActivity.this, TopicActivity.class);
			// Activity 转为 View
			subActivity = getLocalActivityManager().startActivity(
					"TopicActivity", intent);
		} else if (id == 4) {
			intent = new Intent(TabMainActivity.this, FavoriteActivity.class);
			// Activity 转为 View
			subActivity = getLocalActivityManager().startActivity(
					"FavoriteActivity", intent);
		} else if (id == 5) {
			intent = new Intent(TabMainActivity.this, MyWallpaperActivity.class);
			// Activity 转为 View
			subActivity = getLocalActivityManager().startActivity(
					"MyWallpaperActivity", intent);
		} else if (id == 6) {
			intent = new Intent(TabMainActivity.this, SettingActivity.class);
			// Activity 转为 View
			subActivity = getLocalActivityManager().startActivity(
					"SettingActivity", intent);
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		// 容器添加View
		container.addView(subActivity.getDecorView(), LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
	}

	public static int navState = 0;
	public final static int FIRST_STATE = 0;
	public final static int SECOND_STATE = 1;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.i(TAG, "onkeydowns");
		boolean used = this.getLocalActivityManager().getCurrentActivity()
				.onKeyDown(keyCode, event);

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!used) {
				AlertDialog exitDialog = new AlertDialog.Builder(
						TabMainActivity.this)
						.setMessage(R.string.sure_exit)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(R.string.exit)
						.setPositiveButton(R.string.apply,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										finish();
									}
								}).setNegativeButton(R.string.cancel, null)
						.create();
				exitDialog.show();
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (navState == FIRST_STATE) {
				scrollView.smoothScrollTo(tabMenuInt, 0);
				navState = SECOND_STATE;
			} else {
				scrollView.smoothScrollTo(0, 0);
				navState = FIRST_STATE;
			}
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

}