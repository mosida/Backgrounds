package com.galeapp.backgrounds.activity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.galeapp.backgrounds.Constants;
import com.galeapp.backgrounds.R;
import com.galeapp.backgrounds.adapter.TestImgAdapter;
import com.galeapp.backgrounds.model.Recent;
import com.galeapp.utils.FileManager;
import com.galeapp.utils.HttpDownloader;
import com.umeng.analytics.MobclickAgent;

public class TestActivity extends Activity {
	public static final String TAG = "TestActivity";

	String recentJsonStr;

	ProgressBar progressBar;
	private GridView gridView;
	private TestImgAdapter imageAdapter;
	ArrayList<Recent> recents = null;
	TextView countView;

	// 连接是否成功
	public static int connectState = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);

		// 判断是否重新下载json数据的机制
		//

		setContentView(R.layout.activity_recent);
		progressBar = (ProgressBar) findViewById(R.id.progress);
		loadJsonData();
	}

	// 开始加载数据
	public void loadJsonData() {
		progressBar.setVisibility(View.VISIBLE);
		Thread thread = new Thread() {
			@Override
			public void run() {
				if (!HttpDownloader.isConnected(getApplicationContext())) {
					myHandler.sendEmptyMessage(-1);
				}
				recentJsonStr = getJsonData();
				FileManager.createBackgroundsFile();
				if (recentJsonStr == null) {
					myHandler.sendEmptyMessage(-2);
					return;
				}
				Log.i(TAG, "jsonData:" + recentJsonStr);
				analyzeJsonData();
				myHandler.sendEmptyMessage(1);
			}
		};
		thread.start();
	}

	// 下载或者从缓存读取json数据
	private String getJsonData() {
		String jsonStr = HttpDownloader.downloadTextFromUrl(
				Constants.RECENT_URL, HttpDownloader.DEFAULT_CHARSET_NAME);
		// 下载json数据
		if (jsonStr == null || jsonStr.length() == 0) {
			try {
				FileInputStream fis = openFileInput(Constants.RECENT);
				StringBuffer sb = new StringBuffer();
				int c;
				while ((c = fis.read()) != -1) {
					sb.append((char) c);
				}
				fis.close();
				jsonStr = sb.toString();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return jsonStr;
	}

	// 解析json数据
	private void analyzeJsonData() {
		if (recentJsonStr != null && recentJsonStr.length() > 0) {
			try {
				JSONArray jsonArray = new JSONArray(recentJsonStr);
				Log.i(TAG, "jsonArray:" + jsonArray.length());
				recents = new ArrayList<Recent>();
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jObject = jsonArray.getJSONObject(i);
					int id = jObject.getInt(Recent.IMAGE_ID);
					String tags = jObject.getString(Recent.TAGS);
					Recent recent = new Recent(id, tags);
					recents.add(recent);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				FileManager.writeJsonCacheFile(Constants.RECENT,
						getApplicationContext(), recentJsonStr);
			}
		}
	}

	// 加载图片数据
	private void setupViews() {
		countView = (TextView) findViewById(R.id.count);
		countView.setText("" + recents.size());

		gridView = (GridView) findViewById(R.id.myGrid);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		imageAdapter = new TestImgAdapter(getApplicationContext(), recents);
		gridView.setAdapter(imageAdapter);

		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				int imageId = recents.get(position).imageId;
				Intent i = new Intent();
				i.putExtra("imageId", imageId);
				i.setClass(TestActivity.this, PreviewActivity.class);
				startActivity(i);
			}
		});

		gridView.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				Log.i(TAG, "foucus is ....");
			}
		});

		gridView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {
				Log.i(TAG, "onScrollStateChanged is ...." + arg1);

			}

			@Override
			public void onScroll(AbsListView arg0, int pos, int arg2, int arg3) {
				Log.i(TAG, "onScrollStateChanged is ....pos:" + pos + " arg2:"
						+ arg2 + " arg3:" + arg3);
				releaseBitmap(pos);
			}
		});
	}

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
			delBitmap = imageAdapter.imageMap.get(del);

			if (delBitmap != null) {
				// 如果非空则表示有缓存的bitmap，需要清理
				Log.v(TAG, "release position:" + del);
				// 从缓存中移除该del->bitmap的映射
				imageAdapter.imageMap.remove(del);
				// delBitmap.recycle();
				delBitmap = null;
			}
		}
		freeBitmapFromIndex(end);
	}

	/**
	 * 
	 * 从某一位置开始释放bitmap资源
	 * 
	 * @param index
	 */
	private void freeBitmapFromIndex(int end) {

		// 释放之外的bitmap资源

		Bitmap delBitmap;

		for (int del = end + 1; del < imageAdapter.imageMap.size(); del++) {
			delBitmap = imageAdapter.imageMap.get(del);

			if (delBitmap != null) {
				imageAdapter.imageMap.remove(del);
				// delBitmap.recycle();
				delBitmap = null;
				Log.v(TAG, "release position:" + del);
			}
		}

	}

	private final Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {// 成功获取数据
				progressBar.setVisibility(View.GONE);
				setupViews();
				connectState = Constants.CONNECTED;
			} else if (msg.what == -1) {// 无网络连接
				progressBar.setVisibility(View.GONE);
				Toast.makeText(TestActivity.this,
						R.string.please_check_network_connection,
						Toast.LENGTH_SHORT).show();
				connectState = Constants.UNCONNECT;
			} else if (msg.what == -2) {// 无法连接服务器
				progressBar.setVisibility(View.GONE);
				Toast.makeText(TestActivity.this,
						R.string.server_cannot_connect, Toast.LENGTH_SHORT)
						.show();
				if (recentJsonStr != null) {
					connectState = Constants.LOCALDATA;
				}
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

	public static boolean first = true;

	public void onResume() {
		if (first == true) {
			first = false;
		} else {
			// 没有连接成功，重新加载
			if (connectState == Constants.UNCONNECT) {
				loadJsonData();
			}
		}
		getParent().findViewById(R.id.back).setVisibility(View.INVISIBLE);
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}