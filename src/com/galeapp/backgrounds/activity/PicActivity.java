package com.galeapp.backgrounds.activity;

import java.io.File;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.galeapp.backgrounds.R;
import com.galeapp.utils.FileManager;
import com.umeng.analytics.MobclickAgent;
import com.umeng.api.sns.UMSNSException;
import com.umeng.api.sns.UMSnsService;

public class PicActivity extends Activity {

	ImageView picImageView;
	String imageName;
	Bitmap resultBitmap;
	HorizontalScrollView hsv;

	LinearLayout btnsLayout;
	ImageView setWallpaper;
	ImageView shareWallpaper;
	ImageView deleteWallpaper;
	// ImageView timeWallpaper;
	// 分享
	ProgressDialog progressDialog;
	Bitmap shareBitmap;
	byte[] shareBytes;

	Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		context = this;
		imageName = this.getIntent().getStringExtra("imageName");

		setContentView(R.layout.activity_pic);

		picImageView = (ImageView) findViewById(R.id.image);
		btnsLayout = (LinearLayout) findViewById(R.id.btnsLayout);
		setWallpaper = (ImageView) findViewById(R.id.setWallpaper);
		shareWallpaper = (ImageView) findViewById(R.id.shareWallpaper);
		deleteWallpaper = (ImageView) findViewById(R.id.deleteWallpaper);
		// timeWallpaper = (ImageView) findViewById(R.id.timeWallpaper);

		if (imageName == null || imageName.equals("")) {
			Toast.makeText(this, R.string.please_try_again_file_not_exist,
					Toast.LENGTH_LONG).show();
			finish();
			return;
		} else {
			File file = new File(FileManager.getSaveFile(imageName));
			if (file.exists()) {
				resultBitmap = BitmapFactory.decodeFile(FileManager
						.getSaveFile(imageName));
			} else {
				Toast.makeText(this, R.string.please_try_again_file_not_exist,
						Toast.LENGTH_LONG).show();
				finish();
				return;
			}
		}
		picImageView.setImageBitmap(resultBitmap);

		picImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (btnsLayout.getVisibility() != View.VISIBLE) {
					btnsLayout.setVisibility(View.VISIBLE);
				} else {
					btnsLayout.setVisibility(View.GONE);
				}
			}
		});

		setWallpaper.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				i.putExtra("imageName", imageName);
				i.setClass(getApplicationContext(), WallpaperActivity.class);
				startActivity(i);
			}
		});
		shareWallpaper.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				progressDialog = new ProgressDialog(PicActivity.this);
				progressDialog
						.setMessage(getString(R.string.please_wait_for_processing));
				progressDialog.show();
				Thread thread = new Thread() {
					@Override
					public void run() {
						shareBitmap = BitmapFactory.decodeFile(FileManager
								.getSaveFile(imageName));
						if (shareBitmap == null) {
							handler.sendEmptyMessage(-1);
							return;
						}
						shareBytes = FileManager.Bitmap2Bytes(shareBitmap);
						if (shareBytes == null || shareBytes.length == 0) {
							handler.sendEmptyMessage(-1);
							return;
						}
						handler.sendEmptyMessage(1);
					}
				};
				thread.start();
			}
		});
		deleteWallpaper.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog delDialog = new AlertDialog.Builder(
						PicActivity.this)
						.setTitle(R.string.delete)
						.setMessage(R.string.delete_picture_for_sure)
						.setPositiveButton(R.string.apply,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										String fileDir = FileManager
												.getSaveFile(imageName);
										if (FileManager.deleteFile(fileDir)) {
											Toast.makeText(
													PicActivity.this,
													R.string.delete_picture_success,
													Toast.LENGTH_SHORT).show();
											finish();
										} else {
											Toast.makeText(
													PicActivity.this,
													R.string.delete_picture_failed,
													Toast.LENGTH_SHORT).show();
										}
									}
								}).setNegativeButton(R.string.cancel, null)
						.create();
				delDialog.show();
			}
		});

		// timeWallpaper.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		//
		// }
		// });
	}

	@Override
	public void onResume() {
		super.onResume();

		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();

		MobclickAgent.onPause(this);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		resultBitmap = null;
	}

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				progressDialog.dismiss();
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("test", "test");
				try {
					UMSnsService.share(context.getApplicationContext(),
							shareBytes, map, null);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			} else if (msg.what == -1) {
				progressDialog.dismiss();
				Toast.makeText(PicActivity.this,
						R.string.please_try_again_occur_wrong,
						Toast.LENGTH_SHORT).show();
			}
		}
	};
}
