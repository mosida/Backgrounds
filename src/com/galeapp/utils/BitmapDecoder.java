package com.galeapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapDecoder {

	private static final String TAG = "BitmapDecoder";
	private Context context;

	public BitmapDecoder(Context context) {
		this.context = context;
	}

	public Bitmap getPhotoItem(String filepath, int size) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = size;
		Bitmap bitmap = BitmapFactory.decodeFile(filepath, options);
		// 预先缩放，避免实时缩放，可以提高更新率
		bitmap = Bitmap.createScaledBitmap(bitmap, 240, 240, true);
		return bitmap;
	}

}
