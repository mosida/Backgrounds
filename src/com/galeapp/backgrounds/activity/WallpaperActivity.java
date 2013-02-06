package com.galeapp.backgrounds.activity;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.galeapp.backgrounds.Constants;
import com.galeapp.backgrounds.R;
import com.galeapp.backgrounds.views.ResultView;
import com.galeapp.utils.FileManager;
import com.umeng.analytics.MobclickAgent;

public class WallpaperActivity extends Activity {

	public static final String TAG = "WallpaperActivity";

	private int bitmapType = Constants.ENTIRE_TYPE;

	public int imageId;
	public int deviceW;
	public int deviceH;
	public int pBitmapW;
	public int pBitmapH;

	String imageName;

	Bitmap resultBitmap;
	ResultView resultView;
	ImageView standardIV;
	ImageView fixedIV;
	ImageView entireIV;

	Button applyBtn;
	Button cancelBtn;
	CheckBox checkBox;
	ProgressDialog progressDialog;
	WallpaperManager wallpaperManager;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		deviceW = dm.widthPixels;
		deviceH = dm.heightPixels;

		wallpaperManager = (WallpaperManager) getSystemService(WALLPAPER_SERVICE);
		Log.i(TAG,
				"wallpaperMgr width:"
						+ wallpaperManager.getDesiredMinimumWidth()
						+ " height:"
						+ wallpaperManager.getDesiredMinimumHeight());
		imageId = this.getIntent().getIntExtra("imageId", -1);

		if (imageId == -1) {
			imageName = this.getIntent().getStringExtra("imageName");
			if (imageName == null || imageName.equals("")) {

			} else {
				File file = new File(FileManager.getSaveFile(imageName));
				if (file.exists()) {
					resultBitmap = BitmapFactory.decodeFile(FileManager
							.getSaveFile(imageName));
				} else {
					Toast.makeText(this,
							R.string.please_try_again_file_not_exist,
							Toast.LENGTH_LONG).show();
					finish();
					return;
				}
			}
		} else {
			File file = new File(FileManager.getSaveFile(imageId));
			if (file.exists()) {
				Log.i(TAG, "file 存在");
				resultBitmap = BitmapFactory.decodeFile(FileManager
						.getSaveFile(imageId));
			} else {
				Log.i(TAG, "file 不存在");
				resultBitmap = BitmapFactory.decodeFile(FileManager
						.getResultPath());
			}
		}

		// 如果获取不到图片
		/*** !!!!!!!!!!!!!!!!!!!!!!! ***/
		if (resultBitmap == null) {
			Toast.makeText(this, R.string.please_download_wallpaper_again,
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		pBitmapW = resultBitmap.getWidth();
		pBitmapH = resultBitmap.getHeight();

		setContentView(R.layout.activity_wallpaper);
		setupViews();

		setListeners();
	}

	public void setupViews() {
		applyBtn = (Button) findViewById(R.id.apply);
		cancelBtn = (Button) findViewById(R.id.cancel);
		resultView = (ResultView) findViewById(R.id.image);
		resultView.setImageBitmap(resultBitmap);

		checkBox = (CheckBox) findViewById(R.id.scrollable);
		checkBox.setChecked(true);
		checkBox.setEnabled(true);

		standardIV = (ImageView) findViewById(R.id.standard);
		fixedIV = (ImageView) findViewById(R.id.fixed);
		entireIV = (ImageView) findViewById(R.id.entire);
		entireIV.setImageResource(R.drawable.tb_entire_s);
	}

	public void setListeners() {
		applyBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				progressDialog = new ProgressDialog(WallpaperActivity.this);
				progressDialog
						.setMessage(getString(R.string.please_wait_for_setwallpaper));
				progressDialog.show();

				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						Bitmap newBitmap = null;

						// 根据用户选择的类型制造壁纸
						if (bitmapType == Constants.STANDARD_TYPE) {
							int left = (int) (pBitmapW * resultView
									.getScaleLeft());
							int right = (int) (pBitmapW * resultView
									.getScaleRight());
							int top = (int) (pBitmapH * resultView
									.getScaleTop());
							int bottom = (int) (pBitmapH * resultView
									.getScaleBottom());
							Log.i(TAG, "left:" + left + " right:" + right
									+ " top:" + top + " right:" + right);
							Bitmap tempBitmap = Bitmap.createBitmap(
									resultBitmap, left, top, right - left,
									bottom - top);

							int matrixX = deviceH * tempBitmap.getWidth()
									/ tempBitmap.getHeight();
							newBitmap = Bitmap.createScaledBitmap(tempBitmap,
									matrixX, deviceH, true);
							tempBitmap.recycle();
							wallpaperManager.suggestDesiredDimensions(matrixX,
									deviceH);

						} else if (bitmapType == Constants.FIXED_TYPE) {
							int left = (int) (pBitmapW * resultView
									.getScaleLeft());
							int right = (int) (pBitmapW * resultView
									.getScaleRight());
							int top = (int) (pBitmapH * resultView
									.getScaleTop());
							int bottom = (int) (pBitmapH * resultView
									.getScaleBottom());
							newBitmap = Bitmap.createBitmap(resultBitmap, left,
									top, right - left, bottom - top);
							newBitmap = Bitmap.createScaledBitmap(newBitmap,
									deviceW, deviceH, true);
							wallpaperManager.suggestDesiredDimensions(deviceW,
									deviceH);
						} else if (bitmapType == Constants.ENTIRE_TYPE) {
							// 可滚动的壁纸，缩略成符合
							if (checkBox.isChecked() == true) {
								int newW = deviceH * resultBitmap.getWidth()
										/ resultBitmap.getHeight();
								newBitmap = Bitmap.createScaledBitmap(
										resultBitmap, newW, deviceH, true);
								wallpaperManager.suggestDesiredDimensions(newW,
										deviceH);

							} else {
								// 清空壁纸，黑色背景
								int newH = deviceW * resultBitmap.getHeight()
										/ resultBitmap.getWidth();
								Bitmap tempBitmap = Bitmap.createScaledBitmap(
										resultBitmap, deviceW, newH, true);

								wallpaperManager.suggestDesiredDimensions(
										deviceW, deviceH);

								newBitmap = Bitmap.createBitmap(deviceW,
										deviceH, Config.ARGB_8888);
								Canvas canvas = new Canvas(newBitmap);
								Paint paint = new Paint();
								paint.setColor(Color.BLACK);
								canvas.drawRect(0, 0, deviceW, deviceH, paint);
								canvas.drawBitmap(tempBitmap, 0,
										(deviceH - newH) / 2, null);
							}
						}

						try {
							wallpaperManager.setBitmap(newBitmap);
						} catch (IOException e) {
							myHandler.sendEmptyMessage(-1);
							e.printStackTrace();
							return;
						}
						myHandler.sendEmptyMessage(1);
					}
				});
				thread.start();
			}
		});

		cancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		standardIV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				resetImageView();
				standardIV.setImageResource(R.drawable.tb_standard_s);
				checkBox.setChecked(true);
				checkBox.setEnabled(false);
				bitmapType = Constants.STANDARD_TYPE;
				resultView.reflashResultView(bitmapType);

			}
		});

		fixedIV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				resetImageView();
				fixedIV.setImageResource(R.drawable.tb_fixed_s);
				checkBox.setChecked(false);
				checkBox.setEnabled(false);
				bitmapType = Constants.FIXED_TYPE;
				resultView.reflashResultView(bitmapType);

			}
		});

		entireIV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				resetImageView();
				entireIV.setImageResource(R.drawable.tb_entire_s);
				checkBox.setChecked(true);
				checkBox.setEnabled(true);
				bitmapType = Constants.ENTIRE_TYPE;
				resultView.reflashResultView(bitmapType);
			}
		});

	}

	// 重置imageView
	public void resetImageView() {
		standardIV.setImageResource(R.drawable.tb_standard_n);
		fixedIV.setImageResource(R.drawable.tb_fixed_n);
		entireIV.setImageResource(R.drawable.tb_entire_n);
	}

	// 设置壁纸图片的handler
	private final Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				progressDialog.dismiss();
				Toast.makeText(WallpaperActivity.this,
						R.string.new_wallpaper_seted, Toast.LENGTH_LONG).show();
				finish();
			} else if (msg.what == -1) {
				progressDialog.dismiss();
				Toast.makeText(WallpaperActivity.this,
						R.string.wallpaper_wrong, Toast.LENGTH_LONG).show();
			}
		}
	};

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		if (resultView != null) {
			if (resultView.getDrawingCache() != null) {
				if (!resultView.getDrawingCache().isRecycled()) {
					resultView.getDrawingCache().recycle();
					resultView = null;
				}
			}
		}
		if (resultBitmap != null) {
			if (!resultBitmap.isRecycled()) {
				resultBitmap.recycle();
				resultBitmap = null;
			}
		}
		super.onDestroy();
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