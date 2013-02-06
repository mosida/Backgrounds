package com.galeapp.backgrounds.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.galeapp.backgrounds.R;
import com.galeapp.backgrounds.async.AsyncImageLoader;
import com.galeapp.backgrounds.async.AsyncImageLoader.ImageCallback;
import com.galeapp.backgrounds.model.Hotest;
import com.galeapp.utils.AnimationManager;

public class HotestImgAdapter extends BaseAdapter {

	public static final String TAG = "HotestImgAdapter";

	private Context mContext;
	private LayoutInflater myInflater = null;
	private ArrayList<Hotest> hotest;

	private AsyncImageLoader imageLoader;
	// private Map<Integer, View> viewMap = new HashMap<Integer, View>();
	public Map<Integer, Bitmap> imageMap = new HashMap<Integer, Bitmap>();

	public HotestImgAdapter(Context context, ArrayList<Hotest> hotest) {
		super();
		mContext = context;
		this.hotest = hotest;
		imageLoader = new AsyncImageLoader(imageMap);
		Log.i(TAG, "hotests:" + hotest.size());
	}

	public int getCount() {
		return hotest.size();
	}

	public Hotest getItem(int position) {
		return hotest.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		Hotest hotest = (Hotest) getItem(position);

		if (convertView == null) {
			viewHolder = new ViewHolder();
			myInflater = LayoutInflater.from(mContext);
			convertView = myInflater.inflate(R.layout.list_item, null);
			viewHolder.imageIV = (ImageView) convertView
					.findViewById(R.id.image);
			viewHolder.imageIV.setScaleType(ImageView.ScaleType.FIT_CENTER);
			viewHolder.imageId = hotest.imageId;
			viewHolder.numTV = (TextView) convertView.findViewById(R.id.extral);
			viewHolder.numTV.setVisibility(View.VISIBLE);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
			viewHolder.imageIV.setImageBitmap(null);
			viewHolder.imageId = hotest.imageId;
			viewHolder.numTV.setText("" + hotest.count);
		}

		Bitmap cachedImage = null;
		if (imageMap.get(position) != null) {
			if (!imageMap.get(position).isRecycled()) {
				cachedImage = imageMap.get(position);
			}
		}

		if (cachedImage != null && !cachedImage.isRecycled()) {
			viewHolder.imageIV.setImageBitmap(cachedImage);
			viewHolder.imageIV.startAnimation(AnimationManager
					.getAlphaAnimation());
			viewHolder.numTV.setText("" + hotest.count);
		} else {
			cachedImage = imageLoader.loadBitmap(hotest.imageId, position,
					new ImageCallback() {
						public void imageLoaded(Bitmap imageBitmap, int imageId) {
							if (imageBitmap != null
									&& !imageBitmap.isRecycled()) {
								if (viewHolder.imageId == imageId) {
									viewHolder.imageIV
											.setImageBitmap(imageBitmap);
									viewHolder.imageIV
											.startAnimation(AnimationManager
													.getAlphaAnimation());

									Hotest hotest = (Hotest) getItem(position);
									viewHolder.numTV.setText("" + hotest.count);

								}
							}
						}
					});
		}
		return convertView;
	}

	public class ViewHolder {
		private int imageId = 0;
		private ImageView favIV;
		private ImageView imageIV;
		private TextView numTV;
	}

}