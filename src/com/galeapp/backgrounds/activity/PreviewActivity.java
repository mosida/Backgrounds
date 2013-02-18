package com.galeapp.backgrounds.activity;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.galeapp.backgrounds.Constants;
import com.galeapp.backgrounds.R;
import com.galeapp.backgrounds.db.DatabaseHelper;
import com.galeapp.backgrounds.model.Info;
import com.galeapp.backgrounds.receiver.WallpaperReceiver;
import com.galeapp.utils.FileManager;
import com.galeapp.utils.HttpDownloader;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.ShareImage;
import com.umeng.socialize.bean.SocializeConfig;
import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;

public class PreviewActivity extends Activity implements OnTouchListener {

	public static final String TAG = "PreviewActivity";

	private int imageId = 0;
	public Info info;
	public String infoJsonStr;
	public Bitmap previewBitmap;
	Bitmap shareBitmap;
	byte[] shareBytes;
	private boolean isStar = false;
	private boolean isSave = false;
	int[] fileIds;

	Button setWallBtn;
	Button setContactBtn;
	Button setFavBtn;
	Button saveBtn;
//	Button shareBtn;

	Button backBtn;

	LinearLayout tryBtn;
	TextView infoTV;
	ImageView previewIV;
	ImageView startIV;
	ProgressBar progressBar;
	ProgressDialog progressDialog;

	ImageView arrowLeft;
	ImageView arrowRight;

	DatabaseHelper dbHelper;

	SharedPreferences settingSP;
	SharedPreferences sharedPreferences;

	Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		context = this;
		MobclickAgent.onError(this);

		imageId = this.getIntent().getIntExtra("imageId", 0);
		Log.i(TAG, "imageId:" + imageId);
		dbHelper = DatabaseHelper.initOrSingleton(this);
		isStar = dbHelper.favoriteDao.isFavorite(imageId);
		settingSP = getSharedPreferences("settings", Context.MODE_PRIVATE);

		setContentView(R.layout.activity_preview);
		setViews();

		setListeners();

		loadPreviewBitmap();

		// 获取ids
		getIdsFromSdcard();
	}

	public void loadPreviewBitmap() {
		progressBar.setVisibility(View.VISIBLE);
		Thread thread = new Thread() {
			@Override
			public void run() {
				infoJsonStr = getJsonData(imageId);
				analyzeJsonData();
				// 联网下载预览图片，保存至sdcard
				String downString = Constants.PREVIEW_URL + imageId;
				previewBitmap = HttpDownloader
						.downloadBitmapFromUrl(downString);
				if (previewBitmap == null) {
					myHandler.sendEmptyMessage(-1);
					return;
				}
				try {
					FileManager.savePreviewBitmap(previewBitmap);
				} catch (Exception e) {
					e.printStackTrace();
				}
				myHandler.sendEmptyMessage(1);
			}
		};
		thread.start();
	}

	// 解析json数据
	public void analyzeJsonData() {
		if (infoJsonStr != null && infoJsonStr.length() > 0) {
			try {
				JSONObject jsonObject = new JSONObject(infoJsonStr);
				int count = jsonObject.getInt(Info.COUNT);
				int width = jsonObject.getInt(Info.WIDTH);
				int height = jsonObject.getInt(Info.HEIGHT);
				int size = jsonObject.getInt(Info.SIZE);
				String source = jsonObject.getString(Info.SOURCE);

				info = new Info(width, height, size, count, source);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void setViews() {
		infoTV = (TextView) findViewById(R.id.info);
		startIV = (ImageView) findViewById(R.id.star);
		setWallBtn = (Button) findViewById(R.id.setWallpaper);
		setFavBtn = (Button) findViewById(R.id.favorite);
		setContactBtn = (Button) findViewById(R.id.setContact);

		saveBtn = (Button) findViewById(R.id.save);
//		shareBtn = (Button) findViewById(R.id.share);

		backBtn = (Button) findViewById(R.id.back);
		backBtn.setVisibility(View.VISIBLE);

		previewIV = (ImageView) findViewById(R.id.preview);
		previewIV.setImageBitmap(BitmapFactory.decodeFile(FileManager
				.getThumbnailFile(imageId)));

		if (isStar) {
			startIV.setVisibility(View.VISIBLE);
			setFavBtn.setText(R.string.remove_favorite);
		}

		progressBar = (ProgressBar) findViewById(R.id.progress);

		arrowLeft = (ImageView) findViewById(R.id.arrowLeft);
		arrowRight = (ImageView) findViewById(R.id.arrowRight);

		tryBtn = (LinearLayout) findViewById(R.id.tryBtn);
	}

	public void setListeners() {
		// 下载按钮，需要积分
		saveBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 流程：判断是否已经有了->下载
				if (FileManager.isSaveFileExited(imageId)) {
					Toast.makeText(PreviewActivity.this,
							R.string.image_saved_already, Toast.LENGTH_SHORT)
							.show();
				} else {
					// 检查是否有足够容量下载高清图
					if (FileManager.getSdcardAvailableStore() < 1024) {
						Toast toast = Toast.makeText(getApplicationContext(),
								R.string.sdcard_is_not_enough_for_download,
								Toast.LENGTH_LONG);
						toast.show();
						return;
					}

					// 开始下载高清图片
					progressDialog = new ProgressDialog(PreviewActivity.this);
					progressDialog
							.setMessage(getString(R.string.please_wait_for_downloading));
					progressDialog.show();
					Thread t = new Thread() {
						@Override
						public void run() {
							if (HttpDownloader
									.isConnected(getApplicationContext()) != true) {
								dlHandler.sendEmptyMessage(-1);
							} else {
								String downloadStr = Constants.RESULT_URL
										+ imageId;
								HttpDownloader.downloadImageToTempFile(
										downloadStr,
										FileManager.getSaveFile(imageId));
								dlHandler.sendEmptyMessage(2);
							}
						}
					};
					t.start();
				}

			}
		});

		// 分享按钮
//		shareBtn.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// Intent intent = new Intent(Intent.ACTION_SEND);
//				// intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" +
//				// FileManager.getPreviewPath()));
//				// intent.setType("image/jpeg");
//				// startActivity(intent);
//
//				// 开始下载高清图片
//				progressDialog = new ProgressDialog(PreviewActivity.this);
//				progressDialog
//						.setMessage(getString(R.string.please_wait_for_downloading));
//				progressDialog.show();
//
//				Thread t = new Thread() {
//					@Override
//					public void run() {
//						// 检查是否有足够容量下载高清图
//						if (FileManager.getSdcardAvailableStore() < 1024) {
//							dlHandler.sendEmptyMessage(-4);
//							return;
//						}
//						// 判断是否曾经下载过
//						if (FileManager.isSaveFileExited(imageId)) {
//							shareBitmap = BitmapFactory.decodeFile(FileManager
//									.getSaveFile(imageId));
//							if (shareBitmap == null) {
//								FileManager.deleteFile(FileManager
//										.getSaveFile(imageId));
//								dlHandler.sendEmptyMessage(-3);
//								return;
//							}
//							shareBytes = FileManager.Bitmap2Bytes(shareBitmap);
//							dlHandler.sendEmptyMessage(4);
//							return;
//						} else {
//							// 判断是否有下载到result过
//							if (info != null) {
//								File file = new File(FileManager
//										.getResultPath());
//								int size = (int) (file.length() / 1024);
//								if (size == info.getIntSize()) {
//									shareBitmap = BitmapFactory
//											.decodeFile(FileManager
//													.getResultPath());
//									shareBytes = FileManager
//											.Bitmap2Bytes(shareBitmap);
//									dlHandler.sendEmptyMessage(4);
//									return;
//								}
//							}
//							if (HttpDownloader
//									.isConnected(getApplicationContext()) != true) {
//								dlHandler.sendEmptyMessage(-1);
//							} else {
//								HttpDownloader.downloadImageToTempFile(
//										Constants.RESULT_URL + imageId,
//										FileManager.getSaveFile(imageId));
//								shareBitmap = BitmapFactory
//										.decodeFile(FileManager.getResultPath());
//								if (shareBitmap == null) {
//									dlHandler.sendEmptyMessage(-3);
//									return;
//								} else {
//									shareBytes = FileManager
//											.Bitmap2Bytes(shareBitmap);
//									shareBitmap.recycle();
//								}
//								dlHandler.sendEmptyMessage(4);
//							}
//						}
//
//					}
//				};
//				t.start();
//			}
//		});

		// 返回按钮
		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		// 设置联系人按钮
		setContactBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent localIntent = new Intent(
						"android.intent.action.ATTACH_DATA");
				Uri uri = Uri.parse("file://" + FileManager.getPreviewPath());
				localIntent.setDataAndType(uri, "image/jpeg");
				localIntent.putExtra("mimeType", "image/jpeg");
				ActivityInfo activityInfo = getActivityInfo(getPackageManager()
						.queryIntentActivities(localIntent,
								Intent.FLAG_ACTIVITY_NO_ANIMATION));
				if (activityInfo != null) {
					ComponentName componentName = new ComponentName(
							activityInfo.packageName, activityInfo.name);
					Log.i(TAG, "packageName:" + activityInfo.packageName
							+ " name:" + activityInfo.name);
					localIntent.setComponent(componentName);
				}
				// ComponentName componentName = new
				// ComponentName("com.android.contacts",
				// "com.android.contacts.AttachImage");
				// localIntent.setComponent(componentName);
				startActivity(localIntent);

				// 统计设置联系人次数
				MobclickAgent.onEvent(PreviewActivity.this,
						getString(R.string.set_as_contact));
			}
		});

		// 设置壁纸按钮
		setWallBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 判断是否启动了定时壁纸功能
				boolean isChanged = settingSP.getBoolean("wallpaperChange",
						false);
				if (isChanged == true) {
					AlertDialog tipDlg = new AlertDialog.Builder(
							PreviewActivity.this)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setTitle(R.string.tips)
							.setMessage(
									"由于您启动了定时切换壁纸功能，设置此壁纸将会取消定时切换壁纸功能，您确定要这样做吗？")
							.setPositiveButton(R.string.apply,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											// 关闭定时切换壁纸功能
											shutdownChangeAlarm();
											// 下载高清壁纸
											downloadPicForSetWallpaper();
										}
									}).setNegativeButton(R.string.cancel, null)
							.create();
					tipDlg.show();
				} else {
					downloadPicForSetWallpaper();
				}
			}
		});
		// 收藏按钮
		setFavBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isStar) {
					// 移除收藏
					long num = dbHelper.favoriteDao.removeFavorite(imageId);
					if (num != 0) {
						Toast.makeText(PreviewActivity.this,
								R.string.remove_favorite_success,
								Toast.LENGTH_SHORT).show();
						startIV.setVisibility(View.INVISIBLE);
						setFavBtn.setText(R.string.favorite);
						isStar = false;
					}
				} else {
					// 添加收藏
					long ok = dbHelper.favoriteDao.addFavorite(imageId);
					if (ok != -1) {
						Toast.makeText(PreviewActivity.this,
								R.string.add_favorite_success,
								Toast.LENGTH_SHORT).show();
						startIV.setVisibility(View.VISIBLE);
						setFavBtn.setText(R.string.remove_favorite);
						isStar = true;
						// 统计添加收藏次数
						MobclickAgent.onEvent(PreviewActivity.this,
								getString(R.string.favorite));
					}
				}
			}
		});

		previewIV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (infoTV.getVisibility() != View.VISIBLE) {
					infoTV.setVisibility(View.VISIBLE);
					arrowLeft.setVisibility(View.VISIBLE);
					arrowRight.setVisibility(View.VISIBLE);
				} else {
					infoTV.setVisibility(View.GONE);
					arrowLeft.setVisibility(View.INVISIBLE);
					arrowRight.setVisibility(View.INVISIBLE);
				}
			}
		});

		arrowLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				infoTV.setText("");
				getPreImage();
			}
		});

		arrowRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				infoTV.setText("");
				getNextImage();
			}
		});

		tryBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tryBtn.setVisibility(View.GONE);
				loadPreviewBitmap();
			}
		});
	}

	public void shutdownChangeAlarm() {
		Intent intent = new Intent(PreviewActivity.this,
				WallpaperReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				PreviewActivity.this, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
		Toast.makeText(PreviewActivity.this, "定时切换壁纸已取消", Toast.LENGTH_LONG)
				.show();
		Editor editor = settingSP.edit();
		editor.putBoolean("wallpaperChange", false);
		editor.commit();
	}

	public void downloadPicForSetWallpaper() {
		// 开始下载高清图片
		progressDialog = new ProgressDialog(PreviewActivity.this);
		progressDialog
				.setMessage(getString(R.string.please_wait_for_downloading));
		progressDialog.show();
		Thread t = new Thread() {
			@Override
			public void run() {
				// 判断是否有保存过
				if (FileManager.isSaveFileExited(imageId)) {
					dlHandler.sendEmptyMessage(3);
					dlHandler.sendEmptyMessage(1);
					return;
				}
				// 检查是否有足够容量下载高清图
				if (FileManager.getSdcardAvailableStore() < 1024) {
					dlHandler.sendEmptyMessage(-4);
					return;
				}
				if (HttpDownloader.isConnected(getApplicationContext()) != true) {
					dlHandler.sendEmptyMessage(-1);
				} else {
					String downloadStr = Constants.RESULT_URL + imageId;
					File file = new File(FileManager.getResultPath());
					if (file.exists()) {
						int size = (int) (file.length() / 1024);
						if (info == null) {

						} else {
							if (size == info.getIntSize()) {
								dlHandler.sendEmptyMessage(1);
								return;
							}
						}
					}
					HttpDownloader.downloadImageToTempFile(downloadStr,
							FileManager.getResultPath());
					dlHandler.sendEmptyMessage(1);
				}
			}
		};
		t.start();
	}

	// 下载或者从缓存读取json数据
	private String getJsonData(int imageId) {
		return HttpDownloader.downloadTextFromUrl(Constants.INFO_URL + imageId,
				HttpDownloader.DEFAULT_CHARSET_NAME);
	}

	// 下载预览图的handler
	private final Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				progressBar.setVisibility(View.GONE);
				previewIV.setImageBitmap(previewBitmap);
				previewIV.setOnTouchListener(PreviewActivity.this);
				if (info != null) {
					infoTV.setText(info.getInfoDescription());
				}
				infoTV.setVisibility(View.VISIBLE);
				arrowLeft.setVisibility(View.VISIBLE);
				arrowRight.setVisibility(View.VISIBLE);
				tryBtn.setVisibility(View.GONE);
			} else if (msg.what == -1) {// 无网络连接
				progressBar.setVisibility(View.GONE);
				Toast.makeText(PreviewActivity.this,
						R.string.please_try_again_for_preview_pic,
						Toast.LENGTH_SHORT).show();
				tryBtn.setVisibility(View.VISIBLE);
			}
		}
	};

	// 下载高清图片的handler
	private final Handler dlHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				progressDialog.dismiss();
				Intent i = new Intent();
				i.putExtra("imageId", imageId);
				i.setClass(getApplicationContext(), WallpaperActivity.class);
				startActivity(i);
			} else if (msg.what == -1) {
				Toast toast = Toast.makeText(getApplicationContext(),
						R.string.please_check_network_connection,
						Toast.LENGTH_LONG);
				toast.show();
				progressDialog.dismiss();
			} else if (msg.what == 2) {
				Toast toast = Toast.makeText(getApplicationContext(),
						R.string.image_saved_already, Toast.LENGTH_SHORT);
				toast.show();
				progressDialog.dismiss();
				isSave = true;
				// 统计保存文件次数
				MobclickAgent.onEvent(PreviewActivity.this,
						getString(R.string.save));
			} else if (msg.what == 3) {
				progressDialog
						.setMessage(getString(R.string.read_data_from_saved_pic));
			} else if (msg.what == 4) {
//				progressDialog.dismiss();
//				HashMap<String, String> map = new HashMap<String, String>();
//				map.put("test", "test");
//
//				try {
//					UMSnsService.share(context.getApplicationContext(),
//							shareBytes, map, null);
//				} catch (Throwable e) {
//					e.printStackTrace();
//				}
				if(shareBitmap==null){
//					handler.sendEmptyMessage(-1);
					return;
				}
				
				//des 参数对应actionbar 
				UMSocialService controller = UMServiceFactory.getUMSocialService("分享图片", RequestType.SOCIAL);
				//设置默认分享文字
				controller.setShareContent("#最壁纸黑莓版# 每次打开手机看到美美的壁纸，一天的心情都会亮起来，分享一下我喜欢的图片~");

				//设置分享图片(支持4种方式),一个ActionBar最多只能选择一种
				//注意：预设图片构造会对序列化图片对象，不可以马上使用。
				/*//Resource Id
				controller.setShareImage(new ShareImage(mContext, R.drawable.testimg));
				//File
				controller.setShareImage(new ShareImage(new File("mnt/sdcard/2mb.jpg")));
				//Bitmap
				controller.setShareImage(new ShareImage(mContext, bitmap));*/
				//Raw
				controller.setShareImage(new ShareImage(context.getApplicationContext(), shareBitmap));

				//配置分享平台，默认全部
				SocializeConfig socializeConfig = new SocializeConfig();
				socializeConfig.setPlatforms(SHARE_MEDIA.TENCENT,SHARE_MEDIA.SINA,SHARE_MEDIA.RENREN);//设置分享平台
				controller.setConfig(socializeConfig);//该配置只作用于单个ActionBar（相同des参数描述）

				//配置全局Congfig（作用于所有AcitonBar，View级别集成接口）
				//controller.setGlobalConfig(socializeConfig)
				// 统计分享文件次数
				MobclickAgent.onEvent(PreviewActivity.this,
						getString(R.string.share));
			} else if (msg.what == 5) {// 积分墙的下载方式
				progressDialog.dismiss();
				isSave = true;
				// 消费积分
				// AVLSKDHFKSDHFK.spendPoints(PreviewActivity.this,
				// Constants.DOWNLOADE_PIC_SCORE);
				// appWallScore =
				// AVLSKDHFKSDHFK.getPoints(PreviewActivity.this);

				// YoumiPointsManager.queryPoints(PreviewActivity.this);
				Toast toast = Toast.makeText(getApplicationContext(),
						R.string.image_saved_already_for_score,
						Toast.LENGTH_LONG);
				toast.show();
				// 统计
				MobclickAgent.onEvent(PreviewActivity.this,
						getString(R.string.download));
			} else if (msg.what == -3) {
				progressDialog.dismiss();
				Toast toast = Toast.makeText(getApplicationContext(),
						R.string.please_download_pic_again, Toast.LENGTH_SHORT);
				toast.show();
			} else if (msg.what == -4) {
				progressDialog.dismiss();
				Toast toast = Toast.makeText(getApplicationContext(),
						R.string.sdcard_is_not_enough_for_download,
						Toast.LENGTH_LONG);
				toast.show();
			} else if (msg.what == -5) {
				progressDialog.dismiss();
				Toast toast = Toast.makeText(getApplicationContext(),
						R.string.please_download_pic_again_for_score,
						Toast.LENGTH_SHORT);
				toast.show();
			}
		}
	};

	// 获取下一张图片，获取就图片
	public void getNextImage() {
		if (fileIds == null) {
			return;
		}
		for (int i = fileIds.length - 1; i >= 0; i--) {
			if (fileIds[i] == imageId) {
				if (i == 0) {
					return;
				}
				imageId = fileIds[i - 1];
				Log.i(TAG, "imageId:" + imageId);
				previewIV.setImageBitmap(BitmapFactory.decodeFile(FileManager
						.getThumbnailFile(imageId)));
				previewIV.setOnTouchListener(null);
				isStar = dbHelper.favoriteDao.isFavorite(imageId);
				if (isStar) {
					startIV.setVisibility(View.VISIBLE);
					setFavBtn.setText(R.string.remove_favorite);
				} else {
					startIV.setVisibility(View.INVISIBLE);
					setFavBtn.setText(R.string.favorite);
				}
				loadPreviewBitmap();
				break;
			}
		}
	}

	// 获取上一张图片，获取新图片
	public void getPreImage() {
		if (fileIds == null) {
			return;
		}
		for (int i = fileIds.length - 1; i >= 0; i--) {
			if (fileIds[i] == imageId) {
				if (i == fileIds.length - 1) {
					return;
				}
				imageId = fileIds[i + 1];
				Log.i(TAG, "imageId:" + imageId);
				previewIV.setImageBitmap(BitmapFactory.decodeFile(FileManager
						.getThumbnailFile(imageId)));
				previewIV.setOnTouchListener(null);
				isStar = dbHelper.favoriteDao.isFavorite(imageId);
				if (isStar) {
					startIV.setVisibility(View.VISIBLE);
					setFavBtn.setText(R.string.remove_favorite);
				} else {
					startIV.setVisibility(View.INVISIBLE);
					setFavBtn.setText(R.string.favorite);
				}
				loadPreviewBitmap();
				break;
			}
		}
	}

	// 获取预览图
	// public void getPreviewBitmap() {
	// Thread thread = new Thread() {
	// @Override
	// public void run() {
	// infoJsonStr = getJsonData(imageId);
	// analyzeJsonData();
	// //联网下载预览图片，保存至sdcard
	// String downString = Constants.PREVIEW_URL+imageId;
	// previewBitmap = HttpDownloader.downloadBitmapFromUrl(downString);
	// try {
	// FileManager.savePreviewBitmap(previewBitmap);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// myHandler.sendEmptyMessage(1);
	// }
	// };
	// thread.start();
	// }

	public void getIdsFromSdcard() {
		if (fileIds != null) {
			return;
		}
		File file = new File(FileManager.getThumbnailPath());
		String[] fileNames = file.list();
		if (fileNames == null) {
			return;
		}
		fileIds = new int[fileNames.length];
		for (int i = 0; i < fileNames.length; i++) {
			try {
				fileIds[i] = Integer.valueOf(fileNames[i]);
			} catch (Exception e) {
				continue;
			}
		}
		Arrays.sort(fileIds);
	}

	@Override
	public void onResume() {
		super.onResume();
		// appWallScore = AVLSKDHFKSDHFK.getPoints(this);

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
		if (previewIV.getDrawingCache() != null) {
			previewIV.getDrawingCache().recycle();
		}
		if (previewBitmap != null) {
			previewBitmap.recycle();
		}
	}

	int rawX = 0;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int keyCode = event.getAction();

		if (keyCode == MotionEvent.ACTION_DOWN) {
			rawX = (int) event.getRawX();
		} else if (keyCode == MotionEvent.ACTION_MOVE) {
			int moveX = rawX - (int) event.getRawX();
			if (rawX == 0) {
				return false;
			}
			if (moveX >= 120) {// 向左滑动，获取新图片
				getPreImage();
			} else if (moveX <= -120) {// 向右滑动，获取就图片
				getNextImage();
			}
		} else if (keyCode == MotionEvent.ACTION_UP) {
			rawX = 0;
		}
		return false;
	}

	public ActivityInfo getActivityInfo(List list) {
		Iterator iterator = list.iterator();
		ActivityInfo activityInfo = null;
		boolean flag = false;
		while (iterator.hasNext()) {
			activityInfo = ((ResolveInfo) iterator.next()).activityInfo;
			// Log.e(TAG, "activityInfo.packageName:"+activityInfo.packageName);
			if (activityInfo.packageName.equals("com.android.contacts")) {
				flag = true;
				break;
			}
			if (activityInfo.packageName.equals("com.android.htccontacts")) {
				flag = true;
				break;
			}
			if (activityInfo.packageName.equals("com.motorola.blur.contacts")) {
				flag = true;
				break;
			}
		}
		if (flag) {
			return activityInfo;
		} else {
			return null;
		}
	}

}
