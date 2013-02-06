package com.galeapp.backgrounds.adapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.galeapp.backgrounds.R;
import com.galeapp.backgrounds.model.Category;
import com.galeapp.backgrounds.model.SubTopic;

import android.R.integer;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BuyListAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater myInflater = null;
	private ArrayList<SubTopic> subTopics;

	public BuyListAdapter(Context context, ArrayList<SubTopic> arrayList) {
		this.mContext = context;
		this.subTopics = arrayList;
	}

	@Override
	public int getCount() {
		return subTopics.size();
	}

	@Override
	public SubTopic getItem(int position) {
		return subTopics.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		myInflater = LayoutInflater.from(mContext);
		convertView = myInflater.inflate(R.layout.topic_list_child_item, null);
		SubTopic subTopic = subTopics.get(position);
		TextView topicTV = (TextView) convertView.findViewById(R.id.subTopic);
		topicTV.setPadding(15, 0, 0, 0);
		TextView topicCountTV = (TextView) convertView
				.findViewById(R.id.subTopicCount);
		TextView topicScoreTV = (TextView) convertView
				.findViewById(R.id.subTopicScore);

		int subCount = subTopic.subCount;
		String subName = subTopic.subName;
		int subScore = subTopic.subScore;

		topicTV.setText(subName);
		topicCountTV.setText("(" + subCount + ")");

		return convertView;
	}
}
