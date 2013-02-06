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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.galeapp.backgrounds.Constants;
import com.galeapp.backgrounds.R;
import com.galeapp.backgrounds.adapter.CategoryImgAdapter;
import com.galeapp.backgrounds.adapter.CategoryListAdapter;
import com.galeapp.backgrounds.model.Category;
import com.galeapp.backgrounds.model.CategoryImg;
import com.galeapp.utils.FileManager;
import com.galeapp.utils.HttpDownloader;
import com.umeng.analytics.MobclickAgent;

public class CategoryActivity extends Activity {

	public static final String TAG = "CategoryActivity";

	public static final int ALL_CATEGORY_STATE = 0;
	public static final int SUB_CATEGORY_STATE = 1;
	int state = 0;
	// 连接是否成功
	public static int connectState = 0;

	String categoryJsonStr;
	String subCategoryJsonStr;
	private ListView listView;
	ArrayList<Category> categories = null;
	ArrayList<CategoryImg> categoryImgs = null;
	ProgressBar progressBar;
	CategoryListAdapter imageAdapter;
	CategoryImgAdapter categoryImgAdapter;

	//
	View categroyView;
	View subCategoryView;
	TextView countView;
	//
	int categoryId;
	Category category;
	private GridView gridView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);

		// 判断是否重新下载json数据的机制
		//

		categroyView = LayoutInflater.from(this).inflate(
				R.layout.activity_category, null);

		setContentView(categroyView);
		progressBar = (ProgressBar) categroyView.findViewById(R.id.progress);
		loadJsonData();
	}

	public void loadJsonData() {
		progressBar.setVisibility(View.VISIBLE);

		Thread thread = new Thread() {
			@Override
			public void run() {
				if (!HttpDownloader.isConnected(getApplicationContext())) {
					myHandler.sendEmptyMessage(-1);
				}
				categoryJsonStr = getJsonData();
				if (categoryJsonStr == null) {
					myHandler.sendEmptyMessage(-2);
					return;
				}
				analyzeJsonData();
				myHandler.sendEmptyMessage(1);
			}
		};
		thread.start();
	}

	// 解析json数据
	private void analyzeJsonData() {
		if (categoryJsonStr != null && categoryJsonStr.length() > 0) {
			try {
				JSONArray jsonArray = new JSONArray(categoryJsonStr);
				Log.i(TAG, "jsonArray:" + jsonArray.length());
				categories = new ArrayList<Category>();
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jObject = jsonArray.getJSONObject(i);
					int id = jObject.getInt(Category.CATEGORY_ID);
					int count = jObject.getInt(Category.COUNT);
					String name = jObject.getString(Category.NAME);
					Category category = new Category(id, count, name);
					categories.add(category);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				FileManager.writeJsonCacheFile(Constants.CATEGORY,
						getApplicationContext(), categoryJsonStr);
			}
		}
	}

	// 下载或者从缓存读取json数据
	private String getJsonData() {
		// String jsonStr =
		// HttpDownloader.downloadTextFromUrl(Constants.ALL_CATEGORY_URL,
		// HttpDownloader.DEFAULT_CHARSET_NAME);
		String jsonStr = HttpDownloader
				.getHttpJsonData(Constants.ALL_CATEGORY_URL);
		// 下载json数据
		if (jsonStr == null) {
			try {
				FileInputStream fis = openFileInput(Constants.CATEGORY);
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

	private void setupViews() {
		if (categories == null) {
			return;
		}
		countView = (TextView) categroyView.findViewById(R.id.count);
		countView.setText("" + categories.size());
		listView = (ListView) categroyView.findViewById(R.id.myList);
		imageAdapter = new CategoryListAdapter(getApplicationContext(),
				categories);
		listView.setAdapter(imageAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				categoryId = categories.get(arg2).categoryId;
				category = categories.get(arg2);

				state = SUB_CATEGORY_STATE;
				setBackBtn();

				subCategoryView = LayoutInflater.from(CategoryActivity.this)
						.inflate(R.layout.activity_sub_category, null);
				setContentView(subCategoryView);
				progressBar = (ProgressBar) subCategoryView
						.findViewById(R.id.progress);
				loadSubJsonData();

			}
		});

	}

	public void loadSubJsonData() {
		progressBar.setVisibility(View.VISIBLE);

		Thread thread = new Thread() {
			@Override
			public void run() {
				subCategoryJsonStr = getSubJsonData(categoryId);
				if (subCategoryJsonStr == null) {
					mySubHandler.sendEmptyMessage(-2);
					return;
				}
				analyzeSubJsonData();
				mySubHandler.sendEmptyMessage(1);
			}
		};
		thread.start();
	}

	// 下载或者从缓存读取json数据(某分类里面的内容)
	private String getSubJsonData(int categoryId) {
		// String jsonStr =
		// HttpDownloader.downloadTextFromUrl(Constants.CATEGORY_URL+categoryId,
		// HttpDownloader.DEFAULT_CHARSET_NAME);
		String jsonStr = HttpDownloader.getHttpJsonData(Constants.CATEGORY_URL
				+ categoryId);
		if (jsonStr == null || jsonStr.length() == 0) {
			mySubHandler.sendEmptyMessage(-3);
			try {
				FileInputStream fis = openFileInput(Constants.CATEGORY
						+ categoryId);
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

	// 加载图片数据
	private void setupSubViews() {
		TextView categoryName = (TextView) subCategoryView
				.findViewById(R.id.categoryName);
		categoryName.setText(category.name);
		TextView countTextView = (TextView) subCategoryView
				.findViewById(R.id.count);
		countTextView.setText("" + category.count);

		gridView = (GridView) subCategoryView.findViewById(R.id.myGrid);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		categoryImgAdapter = new CategoryImgAdapter(getApplicationContext(),
				categoryImgs);
		gridView.setAdapter(categoryImgAdapter);

		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				int imageId = categoryImgs.get(position).imageId;
				Intent i = new Intent();
				i.putExtra("imageId", imageId);
				i.setClass(CategoryActivity.this, PreviewActivity.class);
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
			delBitmap = categoryImgAdapter.imageMap.get(del);
			if (delBitmap != null) {
				// 如果非空则表示有缓存的bitmap，需要清理
				Log.v(TAG, "release position:" + del);
				// 从缓存中移除该del->bitmap的映射
				categoryImgAdapter.imageMap.remove(del);
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
		for (int del = end + 1; del < categoryImgAdapter.imageMap.size(); del++) {
			delBitmap = categoryImgAdapter.imageMap.get(del);
			if (delBitmap != null) {
				categoryImgAdapter.imageMap.remove(del);
				delBitmap = null;
				Log.v(TAG, "release position:" + del);
			}
		}
	}

	// 解析json数据
	private void analyzeSubJsonData() {
		if (subCategoryJsonStr != null && subCategoryJsonStr.length() > 0) {
			try {
				JSONArray jsonArray = new JSONArray(subCategoryJsonStr);
				Log.i(TAG, "jsonArray:" + jsonArray.length());
				categoryImgs = new ArrayList<CategoryImg>();
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jObject = jsonArray.getJSONObject(i);
					int id = jObject.getInt(CategoryImg.IMAGE_ID);
					CategoryImg categoryImg = new CategoryImg(id);
					categoryImgs.add(categoryImg);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				FileManager.writeJsonCacheFile(Constants.CATEGORY + categoryId,
						getApplicationContext(), subCategoryJsonStr);
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
				Toast.makeText(CategoryActivity.this,
						R.string.please_check_network_connection,
						Toast.LENGTH_LONG).show();
				connectState = Constants.UNCONNECT;
			} else if (msg.what == -2) {// 无法连接服务器
				progressBar.setVisibility(View.GONE);
				Toast.makeText(CategoryActivity.this,
						R.string.server_cannot_connect, Toast.LENGTH_LONG)
						.show();
				if (categoryJsonStr != null) {
					connectState = Constants.LOCALDATA;
				}
			}
		}
	};

	private final Handler mySubHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {// 成功获取数据
				setupSubViews();
				progressBar.setVisibility(View.GONE);
				connectState = Constants.CONNECTED;
				// 统计哪个分类比较多人浏览
				MobclickAgent.onEvent(CategoryActivity.this,
						getString(R.string.category), category.name);
			} else if (msg.what == -1) {// 无网络连接
				progressBar.setVisibility(View.GONE);
				Toast.makeText(CategoryActivity.this,
						R.string.please_check_network_connection,
						Toast.LENGTH_SHORT).show();
				connectState = Constants.UNCONNECT;
			} else if (msg.what == -2) {// 无法连接服务器
				progressBar.setVisibility(View.GONE);
				Toast.makeText(CategoryActivity.this,
						R.string.server_cannot_connect, Toast.LENGTH_SHORT)
						.show();
				if (subCategoryJsonStr != null) {
					connectState = Constants.LOCALDATA;
				}
			} else if (msg.what == -3) {
				// Toast.makeText(CategoryActivity.this,
				// R.string.server_cannot_connect_and_read_cache,
				// Toast.LENGTH_SHORT).show();
				Log.w(TAG, "can't connect and read cache file");
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.i(TAG, "onkeydowns");

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (state == ALL_CATEGORY_STATE) {
				return false;
			} else {
				Log.i(TAG, "setContentView");
				setContentView(categroyView);
				state = ALL_CATEGORY_STATE;
				backBtn = (Button) getParent().findViewById(R.id.back);
				backBtn.setVisibility(View.INVISIBLE);
				return true;
			}
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	public static boolean first = true;

	public void onResume() {
		setBackBtn();
		if (first == true) {
			first = false;
		} else {
			// 没有连接成功，重新加载
			if (connectState == Constants.UNCONNECT) {
				if (state == SUB_CATEGORY_STATE) {
					loadSubJsonData();
				} else {
					loadJsonData();
				}
			}
		}
		Log.i(TAG, "onResume");
		super.onResume();

		MobclickAgent.onResume(this);
	}

	public void onPause() {
		Log.i(TAG, "onPause");

		super.onPause();
		MobclickAgent.onPause(this);
		setBackBtn();
	}

	Button backBtn;

	public void setBackBtn() {
		backBtn = (Button) getParent().findViewById(R.id.back);
		if (state == SUB_CATEGORY_STATE) {
			backBtn.setVisibility(View.VISIBLE);
			backBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					backBtn.setVisibility(View.INVISIBLE);
					state = ALL_CATEGORY_STATE;
					setContentView(categroyView);
				}
			});
		} else {
			backBtn.setVisibility(View.INVISIBLE);
		}
	}
}