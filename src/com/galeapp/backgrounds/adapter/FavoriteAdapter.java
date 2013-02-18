package com.galeapp.backgrounds.adapter;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.galeapp.backgrounds.R;
import com.galeapp.backgrounds.async.AsyncImageLoader;
import com.galeapp.backgrounds.async.AsyncImageLoader.ImageCallback;
import com.galeapp.utils.AnimationManager;

public class FavoriteAdapter extends SimpleCursorAdapter {
	public static final String TAG = "Favorite";

	private LayoutInflater mInflater;
	int layout;
	Context context;
	private AsyncImageLoader imageLoader;
	public Map<Integer, Bitmap> imageMap = new HashMap<Integer, Bitmap>();

	public FavoriteAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		this.layout = layout;
		this.context = context;
		imageLoader = new AsyncImageLoader(imageMap);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Log.i(TAG, "bingView");
		super.bindView(view, context, cursor);
		RelativeLayout rl = null;
		if (view == null) {
			mInflater = LayoutInflater.from(context);
			rl = (RelativeLayout) mInflater.inflate(layout, null);
		} else {
			rl = (RelativeLayout) view;
		}

		int id = cursor.getInt(0);
		String date = cursor.getString(1);

		final ImageView imageView = (ImageView) rl.findViewById(R.id.image);
		ImageView starView = (ImageView) rl.findViewById(R.id.star);
		TextView dateView = (TextView) rl.findViewById(R.id.extral);

		imageView.setScaleType(ImageView.ScaleType.FIT_XY);
		Bitmap cachedImage = imageLoader.loadBitmap(id, new ImageCallback() {
			public void imageLoaded(Bitmap imageBitmap, int imageId) {
				imageView.setImageBitmap(imageBitmap);
				imageView.startAnimation(AnimationManager.getAlphaAnimation());
			}
		});
		if (cachedImage != null) {
			imageView.setImageBitmap(cachedImage);
			imageView.startAnimation(AnimationManager.getAlphaAnimation());
		}
		starView.setVisibility(View.VISIBLE);
		dateView.setVisibility(View.VISIBLE);
		dateView.setText(date);
		rl.setTag(id);
	}

	@Override
	public int getCount() {
		return super.getCount();
	}

}
