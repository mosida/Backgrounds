package com.galeapp.backgrounds.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.galeapp.backgrounds.R;
import com.umeng.analytics.MobclickAgent;

public class SplashActivity extends Activity {

	public static final String TAG = "SplashActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// 竖屏

		setContentView(R.layout.activity_splash);

		// Counter.asyncActivate(this, 1);

		ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);
		progressBar.setVisibility(View.VISIBLE);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Intent i = new Intent(SplashActivity.this,
						TabMainActivity.class);
				i.setAction(Intent.ACTION_VIEW);
				startActivity(i);
				finish();
			}
		}).start();
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}