package com.galeapp.backgrounds.async;

import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

import com.galeapp.utils.FileManager;

public class AsyncLocalImageLoader {
	public static final String TAG = "AsyncLocalImageLoader";
	private Map<String, Bitmap> imageCache;

	public AsyncLocalImageLoader(Map<String, Bitmap> imageMap) {
		this.imageCache = imageMap;
	}

	public interface ImageCallback {
		public void imageLoaded(Bitmap imageBitmap, String filePath);
	}

	public Bitmap loadBitmap(final String fileName, final ImageCallback callback) {
		if (imageCache.containsKey(fileName)) {
			Bitmap bitmap = imageCache.get(fileName);
			if (bitmap != null) {
				return bitmap;
			}
		}
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				callback.imageLoaded((Bitmap) msg.obj, fileName);
			}
		};
		new Thread() {
			public void run() {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				// 获取这个图片的宽和高
				Bitmap bitmap = BitmapFactory.decodeFile(
						FileManager.getSaveFile(fileName), options); // 此时返回bm为空
				options.inJustDecodeBounds = false;
				// 计算缩放比
				int be = (int) (options.outHeight / (float) 200);
				if (be <= 0)
					be = 1;
				options.inSampleSize = be;
				bitmap = BitmapFactory.decodeFile(
						FileManager.getSaveFile(fileName), options);
				if (bitmap != null) {
					bitmap = Bitmap.createScaledBitmap(bitmap, 244, 244, false);
					imageCache.put(fileName, bitmap);
					handler.sendMessage(handler.obtainMessage(0, bitmap));
				}
			};
		}.start();
		return null;
	}

}
