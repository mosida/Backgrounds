package com.galeapp.backgrounds.async;

import java.io.File;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.galeapp.backgrounds.Constants;
import com.galeapp.utils.FileManager;
import com.galeapp.utils.HttpDownloader;

public class AsyncImageLoader {
	public static final String TAG = "MyAsyncImageLoader";
	private Map<Integer, Bitmap> imageCache;

	public AsyncImageLoader(Map<Integer, Bitmap> imageMap) {
		this.imageCache = imageMap;
	}

	public Bitmap loadBitmap(final int imageId, final int posId,
			final ImageCallback callback) {

		if (imageCache.containsKey(posId)) {
			Bitmap bitmap = imageCache.get(posId);

			if (bitmap != null) {
				if (bitmap.isRecycled()) {
				} else {
					return bitmap;
				}
			}
		}
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				callback.imageLoaded((Bitmap) msg.obj, imageId);
			}
		};
		new Thread() {
			public void run() {
				Bitmap bitmap = null;
				// 从sdcard读取缓存图片，若无则从网络获取图片
				File thumbFile = new File(FileManager.getThumbnailPath()
						+ imageId);
				if (thumbFile.exists()) {
					// Log.i(TAG, "read from sdcard:"+imageId+" id:"+posId);
					bitmap = loadImageFromSDcard(imageId);
				} else {
					// Log.i(TAG, "read from web:"+imageId+" id:"+posId);
					bitmap = loadImageFromUrl(imageId);
				}
				if (bitmap != null) {
					imageCache.put(posId, bitmap);
					handler.sendMessage(handler.obtainMessage(0, bitmap));
				}
			};
		}.start();
		return null;
	}

	protected Bitmap loadImageFromUrl(int imageId) {
		Bitmap bitmap = null;
		try {
			bitmap = HttpDownloader
					.downloadBitmapFromUrl(Constants.THUMBNAIL_URL + imageId);
			// 存储至sdcard
			if (bitmap != null) {
				FileManager.saveThumbnailBitmap(bitmap, imageId);
			} else {

			}
		} catch (Exception e) {
			Log.i(TAG, "loadImageFromUrl exception");
		}
		return bitmap;
	}

	protected Bitmap loadImageFromSDcard(int imageId) {
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeFile(FileManager.getThumbnailPath()
					+ imageId);
		} catch (Exception e) {
			Log.i(TAG, "loadImageFromSDcard exception");
		}
		return bitmap;
	}

	public interface ImageCallback {
		public void imageLoaded(Bitmap imageBitmap, int imageId);
	}

	public Bitmap loadBitmap(final int imageId, final ImageCallback callback) {
		if (imageCache.containsKey(imageId)) {
			Bitmap bitmap = imageCache.get(imageId);
			if (bitmap != null) {
				return bitmap;
			}
		}
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				callback.imageLoaded((Bitmap) msg.obj, imageId);
			}
		};
		new Thread() {
			public void run() {
				Bitmap bitmap = null;
				// 从sdcard读取缓存图片，若无则从网络获取图片
				File thumbFile = new File(FileManager.getThumbnailPath()
						+ imageId);
				if (thumbFile.exists()) {
					Log.i(TAG, "read from sdcard:" + imageId);
					bitmap = loadImageFromSDcard(imageId);
				} else {
					Log.i(TAG, "read from web:" + imageId);
					bitmap = loadImageFromUrl(imageId);
				}
				if (bitmap != null) {
					imageCache.put(imageId, bitmap);
					handler.sendMessage(handler.obtainMessage(0, bitmap));
				}
			};
		}.start();
		return null;
	}

}
