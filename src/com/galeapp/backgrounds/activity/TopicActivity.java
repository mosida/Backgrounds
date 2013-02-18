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
import android.content.SharedPreferences;
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
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.galeapp.backgrounds.Constants;
import com.galeapp.backgrounds.R;
import com.galeapp.backgrounds.adapter.SubTopicImgAdapter;
import com.galeapp.backgrounds.adapter.TopicExpandableListAdapter;
import com.galeapp.backgrounds.model.SubTopic;
import com.galeapp.backgrounds.model.SubTopicImg;
import com.galeapp.backgrounds.model.Topic;
import com.galeapp.utils.FileManager;
import com.galeapp.utils.HttpDownloader;
import com.umeng.analytics.MobclickAgent;

public class TopicActivity extends Activity {

	public static final String TAG = "TopicActivity";

	public static final int ALL_TOPIC_STATE = 0;
	public static final int SUB_TOPIC_STATE = 1;
	public static final int BUY_TOPIC_STATE = 2;
	int tabState = 0;
	int state = 0;
	int topicNum = 0;
	// 连接是否成功
	public static int connectState = 0;

	String topicJsonStr;
	String subTopicJsonStr;

	ProgressBar progressBar;
	TopicExpandableListAdapter topicExpandableListAdapter;
	SubTopicImgAdapter subTopicImgAdapter;
	ExpandableListView expandableListView;
	GridView gridView;

	TextView countView;
	ArrayList<Topic> topics = null;
	ArrayList<SubTopicImg> subTopicImgs = null;

	Topic currentTopic;
	SubTopic currentSubTopic;

	int subTopicId;
	// 购买专题选项卡
//	LinearLayout tabLayout;
//	LinearLayout topicListTV;
//	LinearLayout buyListTV;

	// 未开通
	View topicView;
	View subTopicView;
	// 已开通
//	View buyView;
//	ArrayList<SubTopic> buyArrayList = new ArrayList<SubTopic>();
	ListView buyListView;
//	BuyListAdapter buyListAdapter;

	String subTopicName;

	SharedPreferences sharedPreferences;
	/******** 积分墙 *********/
	private int appWallScore = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);

		topicView = LayoutInflater.from(this).inflate(R.layout.activity_topic,
				null);
//		buyView = LayoutInflater.from(this).inflate(
//				R.layout.activity_topic_buy, null);

		setContentView(topicView);
//			tabLayout = (LinearLayout) topicView.findViewById(R.id.tabLayout);
//			tabLayout.setVisibility(View.VISIBLE);
//			topicListTV = (LinearLayout) topicView
//					.findViewById(R.id.topicListTV);
//			buyListTV = (LinearLayout) topicView.findViewById(R.id.buyListTV);
//			topicListTV.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					setContentView(topicView);
//					tabState = ALL_TOPIC_STATE;
//					if (topicJsonStr == null) {
//						loadJsonData();
//					} else {
//						setupViews();
//					}
//				}
//			});
//			buyListTV.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					tabState = BUY_TOPIC_STATE;
//					setContentView(buyView);
//					setupBuyViews();
//				}
//			});
	

		// SDKPointsManager.AsyncQueryPoints(this, 0, this, 0);
		progressBar = (ProgressBar) topicView.findViewById(R.id.progress);

		loadJsonData();
	}

	public void loadJsonData() {
		progressBar.setVisibility(View.VISIBLE);
		Thread thread = new Thread() {
			@Override
			public void run() {
				if (!HttpDownloader.isConnected(getApplicationContext())) {
					topicHandler.sendEmptyMessage(-1);
				}
				topicJsonStr = getJsonData();
				if (topicJsonStr == null) {
					topicHandler.sendEmptyMessage(-2);
					return;
				}
				analyzeJsonData();
				topicHandler.sendEmptyMessage(1);
			}
		};
		thread.start();
	}

	// 解析json数据
	private void analyzeJsonData() {
		if (topicJsonStr != null && topicJsonStr.length() > 0) {
			try {
				JSONArray jsonArray = new JSONArray(topicJsonStr);
				Log.i(TAG, "jsonArray:" + jsonArray.length());
				topics = new ArrayList<Topic>();
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject topicObject = jsonArray.getJSONObject(i);
					int id = topicObject.getInt(Topic.TOPIC_ID);
					int count = 0;
					String name = topicObject.getString(Topic.NAME);
					// 获取子专题
					JSONArray subArray = topicObject
							.getJSONArray(Topic.SUBTOPICS);
					ArrayList<SubTopic> subTopics = new ArrayList<SubTopic>();
					for (int j = 0; j < subArray.length(); j++) {
						JSONObject subTopicObject = subArray.getJSONObject(j);
						int subId = subTopicObject.getInt(SubTopic.SUB_ID);
						String subName = subTopicObject
								.getString(SubTopic.SUB_NAME);
						int subCount = subTopicObject
								.getInt(SubTopic.SUB_COUNT);
//						int subScore = subTopicObject
//								.getInt(SubTopic.SUB_SCORE);

						SubTopic subTopic = new SubTopic(subId, subCount,
								subName, name);
//						if (subBuy == true) {
//							if (!buyArrayList.contains(subTopic)) {
//								buyArrayList.add(subTopic);
//							}
//						}
						subTopics.add(subTopic);
					}
					count = subTopics.size();
					topicNum += count;

					Topic topic = new Topic(id, count, name, subTopics);
					topics.add(topic);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				FileManager.writeJsonCacheFile(Constants.TOPIC,
						getApplicationContext(), topicJsonStr);
			}
		}
	}

	// 下载或者从缓存读取json数据
	private String getJsonData() {
		// String jsonStr =
		// HttpDownloader.downloadTextFromUrl(Constants.ALL_TOPIC_URL,
		// HttpDownloader.DEFAULT_CHARSET_NAME);
		String jsonStr = HttpDownloader
				.getHttpJsonData(Constants.ALL_TOPIC_URL);
		// 下载json数据
		if (jsonStr == null || jsonStr.length() == 0) {
			try {
				FileInputStream fis = openFileInput(Constants.TOPIC);
				StringBuffer sb = new StringBuffer();
				int c;
				while ((c = fis.read()) != -1) {
					sb.append((char) c);
				}
				fis.close();
				jsonStr = sb.toString();
			} catch (FileNotFoundException e) {
				Log.i(TAG, "FileNotFoundException");
				e.printStackTrace();
			} catch (IOException e) {
				Log.i(TAG, "IOException");
				e.printStackTrace();
			}
		}
		return jsonStr;
	}

	// 加载图片数据
	private void setupViews() {
		countView = (TextView) topicView.findViewById(R.id.count);
		countView.setText("" + topicNum);

		if (topics == null || topics.size() == 0) {
			return;
		}

		expandableListView = (ExpandableListView) topicView
				.findViewById(R.id.myEpListView);
		topicExpandableListAdapter = new TopicExpandableListAdapter(
				getApplicationContext(), topics);
		expandableListView.setAdapter(topicExpandableListAdapter);

		expandableListView.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				subTopicName = topics.get(groupPosition).subTopics
						.get(childPosition).subName;

				// 假如没有积分墙服务
					state = SUB_TOPIC_STATE;
					setBackBtn();

					currentTopic = topics.get(groupPosition);
					currentSubTopic = topics.get(groupPosition).subTopics
							.get(childPosition);

					subTopicView = LayoutInflater.from(TopicActivity.this)
							.inflate(R.layout.activity_sub_topic, null);
					setContentView(subTopicView);
					progressBar = (ProgressBar) subTopicView
							.findViewById(R.id.progress);
					loadSubJsonData();

					return true;

				// 先判断是否已经购买此专题,计算所需积分topicScore
//				final SubTopic subTopic = topics.get(groupPosition).subTopics
//						.get(childPosition);
//				subTopicId = topics.get(groupPosition).subTopics
//						.get(childPosition).subTopicId;
//				final int topicScore = topics.get(groupPosition).subTopics
//						.get(childPosition).subScore;
//				boolean topicBuy = sharedPreferences.getBoolean(
//						Constants.APPWALL_TOPIC + subTopicId, false);


//				state = SUB_TOPIC_STATE;
//				setBackBtn();
//
//				currentTopic = topics.get(groupPosition);
//				currentSubTopic = topics.get(groupPosition).subTopics
//						.get(childPosition);
//
//				subTopicView = LayoutInflater.from(TopicActivity.this).inflate(
//						R.layout.activity_sub_topic, null);
//				setContentView(subTopicView);
//				progressBar = (ProgressBar) subTopicView
//						.findViewById(R.id.progress);
//				loadSubJsonData();
//
//				return true;
			}
		});
	}

	public void loadSubJsonData() {
		progressBar.setVisibility(View.VISIBLE);
		Thread thread = new Thread() {
			@Override
			public void run() {
				subTopicJsonStr = getSubJsonData(currentSubTopic.subTopicId);
				if (subTopicJsonStr == null) {
					subTopicHandler.sendEmptyMessage(-2);
					return;
				}
				analyzeSubJsonData();
				subTopicHandler.sendEmptyMessage(1);
			}
		};
		thread.start();
	}

	// 下载或者从缓存读取json数据(某分类里面的内容)
	private String getSubJsonData(int topId) {
		// String jsonStr =
		// HttpDownloader.downloadTextFromUrl(Constants.TOPIC_URL+topId,
		// HttpDownloader.DEFAULT_CHARSET_NAME);
		String jsonStr = HttpDownloader.getHttpJsonData(Constants.TOPIC_URL
				+ topId);
		return jsonStr;
	}

	// 解析json数据
	private void analyzeSubJsonData() {
		if (subTopicJsonStr != null && subTopicJsonStr.length() > 0) {
			try {
				JSONArray jsonArray = new JSONArray(subTopicJsonStr);
				subTopicImgs = new ArrayList<SubTopicImg>();
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jObject = jsonArray.getJSONObject(i);
					int id = jObject.getInt(SubTopicImg.IMAGE_ID);
					SubTopicImg subTopicImg = new SubTopicImg(id);
					subTopicImgs.add(subTopicImg);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				FileManager.writeJsonCacheFile(Constants.TOPIC + subTopicId,
						getApplicationContext(), subTopicJsonStr);
			}
		}
	}

	// 加载图片数据
	private void setupSubViews() {
		TextView topicName = (TextView) subTopicView
				.findViewById(R.id.topicName);
		topicName.setText(currentTopic.name);
		TextView subTopicName = (TextView) subTopicView
				.findViewById(R.id.subTopicName);
		subTopicName.setText(currentSubTopic.subName);
		TextView countTextView = (TextView) subTopicView
				.findViewById(R.id.count);
		countTextView.setText("" + currentSubTopic.subCount);

		if (subTopicImgs == null || subTopicImgs.size() == 0) {
			return;
		}

		gridView = (GridView) subTopicView.findViewById(R.id.myGrid);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		subTopicImgAdapter = new SubTopicImgAdapter(getApplicationContext(),
				subTopicImgs);
		gridView.setAdapter(subTopicImgAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				int imageId = subTopicImgs.get(position).imageId;
				Intent i = new Intent();
				i.putExtra("imageId", imageId);
				i.setClass(TopicActivity.this, PreviewActivity.class);
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
			delBitmap = subTopicImgAdapter.imageMap.get(del);
			if (delBitmap != null) {
				// 如果非空则表示有缓存的bitmap，需要清理
				Log.v(TAG, "release position:" + del);
				// 从缓存中移除该del->bitmap的映射
				subTopicImgAdapter.imageMap.remove(del);
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
		for (int del = end + 1; del < subTopicImgAdapter.imageMap.size(); del++) {
			delBitmap = subTopicImgAdapter.imageMap.get(del);
			if (delBitmap != null) {
				subTopicImgAdapter.imageMap.remove(del);
				// delBitmap.recycle();
				delBitmap = null;
				Log.v(TAG, "release position:" + del);
			}
		}
	}

	private final Handler topicHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {// 成功获取数据
				progressBar.setVisibility(View.GONE);
				setupViews();
				connectState = Constants.CONNECTED;
			} else if (msg.what == -1) {// 无网络连接
				progressBar.setVisibility(View.GONE);
				Toast.makeText(TopicActivity.this,
						R.string.please_check_network_connection,
						Toast.LENGTH_LONG).show();
				connectState = Constants.UNCONNECT;
			} else if (msg.what == -2) {// 无法连接服务器
				progressBar.setVisibility(View.GONE);
				Toast.makeText(TopicActivity.this,
						R.string.server_cannot_connect, Toast.LENGTH_LONG)
						.show();
				if (topicJsonStr != null) {
					connectState = Constants.LOCALDATA;
				}
			}
		}
	};

	private final Handler subTopicHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {// 成功获取数据
				setupSubViews();
				progressBar.setVisibility(View.GONE);
				connectState = Constants.CONNECTED;
				// 统计哪个分类比较多人浏览
				MobclickAgent.onEvent(TopicActivity.this,
						getString(R.string.topic), subTopicName);
			} else if (msg.what == -1) {// 无网络连接
				progressBar.setVisibility(View.GONE);
				Toast.makeText(TopicActivity.this,
						R.string.please_check_network_connection,
						Toast.LENGTH_LONG).show();
			} else if (msg.what == -2) {// 无法连接服务器
				progressBar.setVisibility(View.GONE);
				Toast.makeText(TopicActivity.this,
						R.string.server_cannot_connect, Toast.LENGTH_LONG)
						.show();
				if (subTopicJsonStr != null) {
					connectState = Constants.LOCALDATA;
				}
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.i(TAG, "onkeydowns");

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (state == ALL_TOPIC_STATE) {
				return false;
			} else {
				Log.i(TAG, "setContentView");
				if (tabState == ALL_TOPIC_STATE) {
					setContentView(topicView);
				} 
//				else {
//					setContentView(buyView);
//				}
				state = ALL_TOPIC_STATE;
				backBtn = (Button) getParent().findViewById(R.id.back);
				backBtn.setVisibility(View.INVISIBLE);
				return true;
			}
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	public static boolean first = true;

	@Override
	public void onResume() {
		if (first == true) {
			first = false;
		} else {
			// 没有连接成功，重新加载
			if (connectState == Constants.UNCONNECT) {
				if (state == SUB_TOPIC_STATE) {
					loadSubJsonData();
				} else {
					loadJsonData();
				}
			}
		}
		super.onResume();
		Log.i(TAG, "onResume");

		MobclickAgent.onResume(this);
		setBackBtn();
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
		if (state == SUB_TOPIC_STATE) {
			backBtn.setVisibility(View.VISIBLE);
			backBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					backBtn.setVisibility(View.INVISIBLE);
					state = ALL_TOPIC_STATE;
					if (tabState == ALL_TOPIC_STATE) {
						setContentView(topicView);
					} 
//					else {
//						setContentView(buyView);
//					}
				}
			});
		} else {
			backBtn.setVisibility(View.INVISIBLE);
		}
	}

	// 设置已购买界面
//	public void setupBuyViews() {
//		tabLayout = (LinearLayout) buyView.findViewById(R.id.tabLayout);
//		tabLayout.setVisibility(View.VISIBLE);
//		topicListTV = (LinearLayout) buyView.findViewById(R.id.topicListTV);
//		buyListTV = (LinearLayout) buyView.findViewById(R.id.buyListTV);
//		countView = (TextView) buyView.findViewById(R.id.count);
//		countView.setText("" + buyArrayList.size());
//
//		topicListTV.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				setContentView(topicView);
//				tabState = ALL_TOPIC_STATE;
//				if (topicJsonStr == null) {
//					loadJsonData();
//				} else {
//					setupViews();
//				}
//			}
//		});
//		buyListTV.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				tabState = BUY_TOPIC_STATE;
//				setContentView(buyView);
//				setupBuyViews();
//			}
//		});
//		buyListView = (ListView) buyView.findViewById(R.id.buyList);
//		buyListAdapter = new BuyListAdapter(this, buyArrayList);
//		buyListView.setAdapter(buyListAdapter);
//		buyListView.setOnItemClickListener(new OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1,
//					int position, long arg3) {
//				state = SUB_TOPIC_STATE;
//				setBackBtn();
//				currentSubTopic = buyArrayList.get(position);
//				currentTopic = new Topic(-1, buyArrayList.size(),
//						currentSubTopic.subName, buyArrayList);
//
//				subTopicView = LayoutInflater.from(TopicActivity.this).inflate(
//						R.layout.activity_sub_topic, null);
//				setContentView(subTopicView);
//				progressBar = (ProgressBar) subTopicView
//						.findViewById(R.id.progress);
//				loadSubJsonData();
//
//			}
//		});
//	}

	
}