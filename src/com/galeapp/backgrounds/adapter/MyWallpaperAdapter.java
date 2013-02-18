package com.galeapp.backgrounds.adapter;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.galeapp.backgrounds.R;
import com.galeapp.backgrounds.async.AsyncLocalImageLoader;
import com.galeapp.backgrounds.async.AsyncLocalImageLoader.ImageCallback;
import com.galeapp.utils.AnimationManager;
import com.galeapp.utils.FileManager;

public class MyWallpaperAdapter extends BaseAdapter {
	public static final String TAG = "MyWallpaperAdapter";

	private LayoutInflater mInflater;
	int layout;
	Context context;
	public String[] picsPath;
	private AsyncLocalImageLoader imageLoader;
	public Map<String, Bitmap> imageMap = new HashMap<String, Bitmap>();

	public MyWallpaperAdapter(Context context, String[] picsPath) {
		super();
		this.context = context;
		this.picsPath = picsPath;
		imageLoader = new AsyncLocalImageLoader(imageMap);
	}

	public int getCount() {
		return picsPath.length;
	}

	public Object getItem(int position) {
		return picsPath[position];
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;

		if (convertView == null) {
			viewHolder = new ViewHolder();
			mInflater = LayoutInflater.from(context);
			convertView = mInflater.inflate(R.layout.list_item, null);
			viewHolder.imageIV = (ImageView) convertView
					.findViewById(R.id.image);
			viewHolder.imageIV.setScaleType(ImageView.ScaleType.FIT_XY);
			viewHolder.sizeTV = (TextView) convertView
					.findViewById(R.id.extral);
			viewHolder.imageName = picsPath[position];
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
			viewHolder.imageIV.setImageBitmap(null);
			viewHolder.imageName = picsPath[position];
		}
		Bitmap cachedImage = null;
		long size = FileManager.getFileSize(FileManager
				.getSaveFile(picsPath[position]));
		final String sizeStr = size / 1024 + "K";

		if (imageMap.get(picsPath[position]) != null) {
			if (!imageMap.get(picsPath[position]).isRecycled()) {
				cachedImage = imageMap.get(picsPath[position]);
			}
		}
		if (cachedImage != null && !cachedImage.isRecycled()) {
			viewHolder.imageIV.setImageBitmap(cachedImage);
			viewHolder.imageIV.startAnimation(AnimationManager
					.getAlphaAnimation());
			viewHolder.sizeTV.setVisibility(View.VISIBLE);
			viewHolder.sizeTV.setText(sizeStr);
		} else {
			cachedImage = imageLoader.loadBitmap(picsPath[position],
					new ImageCallback() {
						public void imageLoaded(Bitmap imageBitmap,
								String fileName) {
							if (imageBitmap != null
									&& !imageBitmap.isRecycled()) {
								if (viewHolder.imageName.equals(fileName)) {
									viewHolder.imageIV
											.setImageBitmap(imageBitmap);
									viewHolder.imageIV
											.startAnimation(AnimationManager
													.getAlphaAnimation());
									viewHolder.sizeTV
											.setVisibility(View.VISIBLE);
									viewHolder.sizeTV.setText(sizeStr);
								}
							}
						}
					});
		}

		return convertView;
	}

	public class ViewHolder {
		private String imageName;
		private ImageView favIV;
		private ImageView imageIV;
		private TextView sizeTV;
	}

}
