package com.galeapp.backgrounds.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.galeapp.backgrounds.R;
import com.galeapp.backgrounds.adapter.FavoriteAdapter;
import com.galeapp.backgrounds.db.DatabaseHelper;
import com.umeng.analytics.MobclickAgent;

public class FavoriteActivity extends Activity {

	public static final String TAG = "FavoriteActivity";
	public DatabaseHelper dbHelper;
	FavoriteAdapter favoriteAdapter;
	GridView gridView;
	TextView countView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);

		setContentView(R.layout.activity_favority);
		gridView = (GridView) findViewById(R.id.myGrid);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		countView = (TextView) findViewById(R.id.count);
		dbHelper = DatabaseHelper.initOrSingleton(this);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Cursor cursor = dbHelper.favoriteDao.getFavoriteCursor();
		favoriteAdapter = new FavoriteAdapter(this, R.layout.list_item, cursor,
				dbHelper.favoriteDao.COLS_STRING, new int[] { R.id.image,
						R.id.entire });
		gridView.setAdapter(favoriteAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				int imageId = Integer.valueOf(arg1.getTag().toString());
				Intent i = new Intent();
				i.putExtra("imageId", imageId);
				i.setClass(FavoriteActivity.this, PreviewActivity.class);
				startActivity(i);
			}
		});
		gridView.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
			}
		});

		gridView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {
				// Log.i(TAG, "onScrollStateChanged is ...."+arg1);
			}

			@Override
			public void onScroll(AbsListView arg0, int pos, int arg2, int arg3) {
				// Log.i(TAG,
				// "onScrollStateChanged is ....pos:"+pos+" arg2:"+arg2+" arg3:"+arg3);
				releaseBitmap(pos);
			}
		});
		countView.setText("" + favoriteAdapter.getCount());

		getParent().findViewById(R.id.back).setVisibility(View.INVISIBLE);

		MobclickAgent.onResume(this);

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
			delBitmap = favoriteAdapter.imageMap.get(del);
			if (delBitmap != null) {
				// 如果非空则表示有缓存的bitmap，需要清理
				// Log.v(TAG, "release position:"+del);
				// 从缓存中移除该del->bitmap的映射
				favoriteAdapter.imageMap.remove(del);
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
		// 释放之外的bitmap资源
		Bitmap delBitmap;
		for (int del = end + 1; del < favoriteAdapter.imageMap.size(); del++) {
			delBitmap = favoriteAdapter.imageMap.get(del);
			if (delBitmap != null) {
				favoriteAdapter.imageMap.remove(del);
				// delBitmap.recycle();
				delBitmap = null;
				Log.v(TAG, "release position:" + del);
			}
		}
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

}