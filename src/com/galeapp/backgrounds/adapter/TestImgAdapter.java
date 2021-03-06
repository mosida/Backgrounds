package com.galeapp.backgrounds.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.galeapp.backgrounds.R;
import com.galeapp.backgrounds.async.AsyncImageLoader;
import com.galeapp.backgrounds.async.AsyncImageLoader.ImageCallback;
import com.galeapp.backgrounds.model.Recent;

public class TestImgAdapter extends BaseAdapter {

	public static final String TAG = "TestImgAdapter";

	private Context mContext;
	private LayoutInflater myInflater = null;
	private ArrayList<Recent> recents;

	private AsyncImageLoader imageLoader;
	public Map<Integer, Bitmap> imageMap = new HashMap<Integer, Bitmap>();

	public TestImgAdapter(Context context, ArrayList<Recent> recents) {
		super();
		mContext = context;
		this.recents = recents;
		imageLoader = new AsyncImageLoader(imageMap);
	}

	public int getCount() {
		return recents.size();
	}

	public Recent getRecent(int i) {
		return recents.get(i);
	}

	public Recent getItem(int position) {
		return recents.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;

		Recent recent = (Recent) getItem(position);
		if (convertView == null) {
			viewHolder = new ViewHolder();
			myInflater = LayoutInflater.from(mContext);
			convertView = myInflater.inflate(R.layout.list_item, null);
			viewHolder.imageIV = (ImageView) convertView
					.findViewById(R.id.image);
			viewHolder.imageIV.setScaleType(ImageView.ScaleType.FIT_XY);
			viewHolder.imageId = recent.imageId;
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
			viewHolder.imageIV.setImageBitmap(null);
			viewHolder.imageId = recent.imageId;
		}

		Bitmap cachedImage = null;
		if (imageMap.get(position) != null) {
			if (!imageMap.get(position).isRecycled()) {
				cachedImage = imageMap.get(position);
			}
		}

		if (cachedImage != null && !cachedImage.isRecycled()) {
			viewHolder.imageIV.setImageBitmap(cachedImage);
		} else {
			cachedImage = imageLoader.loadBitmap(recent.imageId, position,
					new ImageCallback() {
						public void imageLoaded(Bitmap imageBitmap, int imageId) {
							if (imageBitmap != null
									&& !imageBitmap.isRecycled()) {
								if (viewHolder.imageId == imageId) {
									viewHolder.imageIV
											.setImageBitmap(imageBitmap);
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