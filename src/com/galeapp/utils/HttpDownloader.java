package com.galeapp.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.galeapp.backgrounds.Constants;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class HttpDownloader {
	public static final String TAG = "HttpDownloader";

	public static final String DEFAULT_CHARSET_NAME = "GBK";

	public static final int DOWNLOAD_RESULT_CODE_URL_ILLEAGL = -1;

	public static final int DOWNLOAD_RESULT_CODE_FAIL = 0;

	public static final int DOWNLOAD_RESULT_CODE_SUCCESS = 1;

	/**
	 * 默认缓存大小
	 */
	public static final int DEFAULT_DOWNLOAD_BUFFER_SIZE = 64 * 1024;

	public static final int DEFAULT_BUFFER_READER_SIZE = 8 * 1024;

	/**
	 * 由指定url下载文本文件
	 * 
	 * @param urlStr
	 * @param charSet
	 *            页面文本的编码
	 * @return
	 */
	public static String downloadTextFromUrl(String urlStr, String charSet) {
		URL url = null;
		StringBuffer contentSb = new StringBuffer();
		BufferedReader br = null;
		HttpURLConnection conn = null;

		/**
		 * try { InetAddress i = InetAddress.getByName(urlStr); } catch
		 * (UnknownHostException e1) { e1.printStackTrace(); }
		 */

		try {
			url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(3000);
			String contentCharset = conn.getContentEncoding();

			if (contentCharset != null && contentCharset.trim().length() > 0) {
				charSet = contentCharset;
			}
			// 连接成功就读取文本内容
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				InputStream is = conn.getInputStream();
				br = new BufferedReader(new InputStreamReader(is, charSet),
						DEFAULT_BUFFER_READER_SIZE);

				String line = null;
				while ((line = br.readLine()) != null) {
					contentSb.append(line);
				}
			} else {
				Log.e(TAG, "can not connect to link : " + urlStr);
				return null;
			}

		} catch (MalformedURLException e) {
			// URL异常
			Log.e(TAG, "downloadFile error - url illeagal! URL : " + urlStr, e);
			return null;
		} catch (IOException e) {
			// 连接失败
			Log.e(TAG, "downloadFile error - connect failed! URL : " + urlStr,
					e);
			return null;
		} finally {
			try {
				if (br != null) {
					br.close();
				}
				if (conn != null) {
					conn.disconnect();
				}
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return contentSb.toString();
	}

	//
	public static String getHttpJsonData(String url) {
		String jsonStr = "";
		HttpGet httpGet = new HttpGet(url);
		/* 发送请求并等待响应 */
		HttpResponse httpResponse;
		try {
			httpResponse = new DefaultHttpClient().execute(httpGet);
			/* 若状态码为200 ok */
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				jsonStr = EntityUtils.toString(httpResponse.getEntity());
			}
		} catch (ClientProtocolException e1) {
			Log.e(TAG, "ClientProtocolException error - url illeagal! URL : "
					+ url, e1);
			e1.printStackTrace();
		} catch (IOException e1) {
			Log.e(TAG, "ioexception error - url illeagal! URL : " + url, e1);
			e1.printStackTrace();
		}
		return jsonStr;
	}

	/**
	 * 下载文件到指定文件, 使用默认缓存大小
	 * 
	 * @param urlStr
	 * @param outputFile
	 * @return -1代表url不正确, 1代表成功， 0代表下载失败
	 */
	public static int downloadFile(String urlStr, File outputFile) {
		return downloadFile(urlStr, outputFile, DEFAULT_DOWNLOAD_BUFFER_SIZE);
	}

	/**
	 * 下载文件到指定位置， 使用默认缓存大小
	 * 
	 * @param urlStr
	 * @param outputFilePath
	 * @return -1代表url不正确, 1代表成功， 0代表下载失败
	 */
	public static int downloadFile(String urlStr, String outputFilePath) {
		File outputFile = new File(outputFilePath);
		return downloadFile(urlStr, outputFile, DEFAULT_DOWNLOAD_BUFFER_SIZE);
	}

	/**
	 * 下载文件到指定位置
	 * 
	 * @param urlStr
	 * @param outputFilePath
	 * @param bufferSize
	 *            缓存大小
	 * @return -1代表url不正确, 1代表成功， 0代表下载失败
	 */
	public static int downloadFile(String urlStr, String outputFilePath,
			int bufferSize) {
		File outputFile = new File(outputFilePath);
		return downloadFile(urlStr, outputFile, bufferSize);
	}

	/**
	 * 下载文件到指定文件
	 * 
	 * @param urlStr
	 * @param outputFile
	 * @param bufferSize
	 *            缓存大小
	 * @return -1代表url不正确, 1代表成功， 0代表下载失败
	 */
	public static int downloadFile(String urlStr, File outputFile,
			int bufferSize) {
		int result = DOWNLOAD_RESULT_CODE_FAIL;

		if (urlStr != null && !urlStr.trim().equals("")) {
			HttpURLConnection conn = null;

			BufferedInputStream bis = null;
			BufferedOutputStream bos = null;

			/*
			 * try { InetAddress i = InetAddress.getByName(urlStr); } catch
			 * (UnknownHostException e1) { e1.printStackTrace(); }
			 */

			try {
				URL url = new URL(urlStr);
				conn = (HttpURLConnection) url.openConnection();
				// conn.setReadTimeout(2000);
				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					byte[] buf = new byte[bufferSize];
					bis = new BufferedInputStream(conn.getInputStream(),
							bufferSize);
					bos = new BufferedOutputStream(new FileOutputStream(
							outputFile), bufferSize);

					int readLen = -1;
					while ((readLen = bis.read(buf)) != -1) {
						bos.write(buf, 0, readLen);
					}
					// 复制完毕，下载成功
					result = DOWNLOAD_RESULT_CODE_SUCCESS;
				}

			} catch (MalformedURLException e) {
				// URL异常
				Log.e(TAG,
						"downloadFile error - url illeagal! URL : " + urlStr, e);
				result = DOWNLOAD_RESULT_CODE_URL_ILLEAGL;
			} catch (IOException e) {
				// 连接失败
				Log.e(TAG, "downloadFile error - connect failed! URL : "
						+ urlStr, e);
				result = DOWNLOAD_RESULT_CODE_FAIL;
			} finally {
				try {
					if (bos != null) {
						bos.close();
					}
					if (bis != null) {
						bis.close();
					}
					if (conn != null) {
						conn.disconnect();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public static Drawable downloadDrawableFormUrl(String urlStr) {

		Drawable drawable = null;
		if (urlStr != null && !urlStr.trim().equals("")) {
			HttpURLConnection conn = null;

			/*
			 * try { InetAddress i = InetAddress.getByName(urlStr); } catch
			 * (UnknownHostException e1) { e1.printStackTrace(); }
			 */
			try {
				URL url = new URL(urlStr);
				conn = (HttpURLConnection) url.openConnection();
				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					Log.d(TAG, "Start download image form : " + urlStr);

					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inJustDecodeBounds = true;

					Bitmap bitmap = BitmapFactory.decodeStream(conn
							.getInputStream());

					Log.d(TAG, "bitmap w h :" + bitmap.getWidth() + " * "
							+ bitmap.getHeight());

					Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
							bitmap.getHeight(), Config.ARGB_4444);
					Canvas canvas = new Canvas(output);

					Paint iconPaint = new Paint();
					iconPaint.setDither(true);// 防抖动
					iconPaint.setFilterBitmap(true);// 用来对Bitmap进行滤波处理，这样，当你选择Drawable时，会有抗锯齿的效果
					iconPaint.setColor(Color.GRAY);
					iconPaint.setAntiAlias(true);

					canvas.drawARGB(0, 0, 0, 0);

					Rect dst = new Rect(0, 0, bitmap.getWidth(),
							bitmap.getHeight());

					canvas.drawBitmap(bitmap, dst, dst, iconPaint);

					BitmapDrawable bd = new BitmapDrawable(output);
					drawable = bd;

					drawable.setBounds(0, 0, bitmap.getWidth(),
							bitmap.getHeight());

					bitmap.recycle();
					Log.d(TAG, "Finish download image form : " + urlStr);
				}
			} catch (IOException e) {
				Log.e(TAG, "downloadDrawableFormUrl error! URL : " + urlStr, e);
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
			}
		}
		return drawable;
	}

	public static Bitmap downloadBitmapFromUrl(String urlStr) {
		Bitmap bitmap = null;
		if (urlStr != null && !urlStr.trim().equals("")) {
			HttpURLConnection conn = null;
			try {
				URL url = new URL(urlStr);
				conn = (HttpURLConnection) url.openConnection();
				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					Log.d(TAG, "Start download image form : " + urlStr);

					bitmap = BitmapFactory.decodeStream(conn.getInputStream());
					Log.d(TAG, "Finish download image form : " + urlStr);
				}
			} catch (IOException e) {
				Log.e(TAG, "downloadDrawableFormUrl error! URL : " + urlStr, e);
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
			}
		}
		return bitmap;
	}

	public static String downloadImageToTempFile(String url, String path) {
		try {
			InputStream is = (InputStream) new URL(url).getContent();

			// System.out.println(path);
			File f = new File(path);

			f.createNewFile();
			FileOutputStream fos = new FileOutputStream(f);
			try {

				byte[] buf = new byte[8 * 1024];
				int len = 0;
				while ((len = is.read(buf)) != -1)
					fos.write(buf, 0, len);
			} catch (Exception e) {
				Log.e(TAG, "downloadImageToTempFile error!", e);
			}
			return f.getAbsolutePath();
		} catch (Exception e) {
			System.out.println("Exc=" + e);
			return null;
		}
	}

	/**
	 * 检查当前是否联网,需要配置<uses-permission android:name =
	 * "android.permission.ACCESS_NETWORK_STATE"/>权限
	 * 
	 * @param context
	 * @return
	 */
	private static ConnectivityManager connectivity;

	public static boolean isConnected(Context context) {
		boolean result = false;
		if (connectivity == null) {
			connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
		}
		if (connectivity == null) {
			result = false;
		} else {
			NetworkInfo[] infos = connectivity.getAllNetworkInfo();
			if (infos != null) {
				for (int i = 0; i < infos.length; i++) {
					if (infos[i].getState().equals(NetworkInfo.State.CONNECTED)) {
						result = true;
						break;
					}
				}
			}
		}
		return result;
	}

}