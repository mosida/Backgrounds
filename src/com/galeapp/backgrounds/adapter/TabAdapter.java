package com.galeapp.backgrounds.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.galeapp.backgrounds.R;

public class TabAdapter extends BaseAdapter {
	private Context mContext;
	private ImageView[] imgItems;
	private int tab_bg;
	LayoutInflater mInflater;
	private int picSelectIds[];
	private int picIds[];

	public TabAdapter(Context c, int[] picIds, int[] picSelectIds) {
		mContext = c;
		imgItems = new ImageView[picIds.length];
		tab_bg = R.drawable.tab_bg;
		this.picIds = picIds;
		this.picSelectIds = picSelectIds;

		for (int i = 0; i < picIds.length; i++) {
			imgItems[i] = new ImageView(mContext);
			imgItems[i].setLayoutParams(new GridView.LayoutParams(-2, -2));
			imgItems[i].setAdjustViewBounds(true);
			imgItems[i].setScaleType(ImageView.ScaleType.FIT_XY);
			imgItems[i].setImageResource(picIds[i]);
			imgItems[i].setBackgroundResource(tab_bg);
		}
	}

	public int getCount() {
		return imgItems.length;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	/**
	 * 设置选中的效果
	 */
	public void SetFocus(int index) {
		for (int i = 0; i < imgItems.length; i++) {
			if (i != index) {
				imgItems[i].setImageResource(picIds[i]);// 恢复未选中的样式
			}
		}
		imgItems[index].setImageResource(picSelectIds[index]);// 设置选中的样式
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) {
			imageView = imgItems[position];
		} else {
			imageView = (ImageView) convertView;
		}
		return imageView;
	}
}
