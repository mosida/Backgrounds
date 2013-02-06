package com.galeapp.utils;

import android.graphics.Bitmap;

public class LoadedImage {
	Bitmap mBitmap;
	int imageId;

	public LoadedImage(Bitmap bitmap) {
		mBitmap = bitmap;
	}

	public Bitmap getBitmap() {
		return mBitmap;
	}

	public void setImageId(int imageId) {
		this.imageId = imageId;
	}

	public int getImageId() {
		return imageId;
	}
}