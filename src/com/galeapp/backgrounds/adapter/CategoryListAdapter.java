package com.galeapp.backgrounds.adapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.galeapp.backgrounds.R;
import com.galeapp.backgrounds.model.Category;

import android.R.integer;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CategoryListAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater myInflater = null;
	private ArrayList<Category> arrayList = null;
	private Map<Integer, View> viewMap = new HashMap<Integer, View>();

	public CategoryListAdapter(Context context, ArrayList<Category> arrayList) {
		this.mContext = context;
		this.arrayList = arrayList;
	}

	@Override
	public int getCount() {
		return arrayList.size();
	}

	@Override
	public Category getItem(int position) {
		return arrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = this.viewMap.get(position);
		if (rowView == null) {
			myInflater = LayoutInflater.from(mContext);
			rowView = myInflater.inflate(R.layout.list_category, null);
			Category category = arrayList.get(position);
			ImageView imageView = (ImageView) rowView.findViewById(R.id.image);
			TextView nameView = (TextView) rowView.findViewById(R.id.name);
			TextView countView = (TextView) rowView.findViewById(R.id.count);
			int drawableId = getDrawableId(category.categoryId);
			imageView.setImageResource(drawableId);

			nameView.setText(category.name);
			countView.setText("(" + category.count + ")");
			viewMap.put(position, rowView);
		}
		return rowView;
	}

	public int getDrawableId(int categoryId) {
		Field f;
		int drawableId = R.drawable.cl_default;
		try {
			f = (Field) R.drawable.class.getDeclaredField("cl_" + categoryId);
			drawableId = f.getInt(R.drawable.class);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return drawableId;
	}

}
