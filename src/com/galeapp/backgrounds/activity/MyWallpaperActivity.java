package com.galeapp.backgrounds.activity;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.galeapp.backgrounds.R;
import com.galeapp.backgrounds.adapter.MyWallpaperAdapter;
import com.galeapp.utils.FileManager;
import com.galeapp.utils.ImageFilter;
import com.umeng.analytics.MobclickAgent;

public class MyWallpaperActivity extends Activity {

	public static final String TAG = "MyWallpaper";
	String[] picsPath;
	MyWallpaperAdapter myWallpaperAdapter;
	GridView gridView;
	ProgressBar progressBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_mywallpaper);

		gridView = (GridView) findViewById(R.id.myGrid);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					final int position, long arg3) {
				String picName = myWallpaperAdapter.getItem(position)
						.toString();
				Intent i = new Intent();
				i.putExtra("imageName", picName);
				i.setClass(getApplicationContext(), PicActivity.class);
				startActivity(i);
			}
		});

		progressBar = (ProgressBar) findViewById(R.id.progress);

		File file = new File(FileManager.getSavePath());
		picsPath = file.list(new ImageFilter());
		if (picsPath == null) {
			Toast.makeText(MyWallpaperActivity.this, "您图库是空的，赶紧去下载一些图片回来吧!",
					Toast.LENGTH_LONG).show();
			return;
		}
		myWallpaperAdapter = new MyWallpaperAdapter(this, picsPath);
		gridView.setAdapter(myWallpaperAdapter);
		gridView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {
			}

			@Override
			public void onScroll(AbsListView arg0, int pos, int arg2, int arg3) {
				releaseBitmap(pos);
			}
		});
		gridView.setVisibility(View.VISIBLE);

		MobclickAgent.onError(this);
	}

	// 释放资源
	public void releaseBitmap(int firstVisiblePos) {
		// 在这，我们分别预存储了第一个和最后一个可见位置之外的4个位置的bitmap
		// 即dataCache中始终只缓存了（M＝6＋Gallery当前可见view的个数）M个bitmap
		int start = firstVisiblePos - 6;
		int end = firstVisiblePos + 7;
		// 释放position<start之外的bitmap资源
		Bitmap delBitmap;
		for (int del = 0; del < start; del++) {
			if (del == 0) {
				continue;
			}
			delBitmap = myWallpaperAdapter.imageMap.get(del);

			if (delBitmap != null) {
				// 如果非空则表示有缓存的bitmap，需要清理
				Log.v(TAG, "release position:" + del);
				// 从缓存中移除该del->bitmap的映射
				myWallpaperAdapter.imageMap.remove(del);
				// delBitmap.recycle();
				delBitmap = null;
			}
		}
		freeBitmapFromIndex(end);
	}

	/**
	 * 从某一位置开始释放bitmap资源
	 * 
	 * @param index
	 */
	private void freeBitmapFromIndex(int end) {
		if (myWallpaperAdapter == null || myWallpaperAdapter.imageMap == null) {
			return;
		}
		// 释放之外的bitmap资源
		Bitmap delBitmap;
		for (int del = end + 1; del < myWallpaperAdapter.imageMap.size(); del++) {
			delBitmap = myWallpaperAdapter.imageMap.get(del);

			if (delBitmap != null) {
				myWallpaperAdapter.imageMap.remove(del);
				delBitmap = null;
				Log.v(TAG, "release position:" + del);
			}
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		refreshView();
		MobclickAgent.onResume(this);
	}

	public void refreshView() {
		File file = new File(FileManager.getSavePath());
		picsPath = file.list(new ImageFilter());
		if (picsPath == null) {
			return;
		}
		myWallpaperAdapter.picsPath = picsPath;
		myWallpaperAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();

		MobclickAgent.onPause(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

}
