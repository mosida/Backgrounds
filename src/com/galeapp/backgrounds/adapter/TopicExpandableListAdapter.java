package com.galeapp.backgrounds.adapter;

import java.util.ArrayList;
import java.util.jar.Attributes.Name;

import android.R.integer;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.galeapp.backgrounds.Constants;
import com.galeapp.backgrounds.R;
import com.galeapp.backgrounds.model.SubTopic;
import com.galeapp.backgrounds.model.Topic;

public class TopicExpandableListAdapter extends BaseExpandableListAdapter {

	public static final String TAG = "TopicExpandableListAdapter";

	private Context context;
	private LayoutInflater layoutInflater;
	private ArrayList<Topic> topics;

	public TopicExpandableListAdapter(Context context, ArrayList<Topic> topics) {
		this.context = context;
		this.layoutInflater = LayoutInflater.from(context);
		this.topics = topics;
	}

	@Override
	public SubTopic getChild(int groupPosition, int childPosition) {
		return topics.get(groupPosition).subTopics.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		RelativeLayout relativeLayout = (RelativeLayout) layoutInflater
				.inflate(R.layout.topic_list_child_item, null);
		TextView topicTV = (TextView) relativeLayout
				.findViewById(R.id.subTopic);
		TextView topicCountTV = (TextView) relativeLayout
				.findViewById(R.id.subTopicCount);
//		TextView topicScoreTV = (TextView) relativeLayout
//				.findViewById(R.id.subTopicScore);

		SubTopic subTopic = topics.get(groupPosition).subTopics
				.get(childPosition);
		int subCount = subTopic.subCount;
		String subName = subTopic.subName;

		topicTV.setText(subName);
		topicCountTV.setText("(" + subCount + ")");

		return relativeLayout;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return topics.get(groupPosition).count;
	}

	@Override
	public Topic getGroup(int groupPosition) {
		return topics.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return topics.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		LinearLayout linearLayout = (LinearLayout) layoutInflater.inflate(
				R.layout.topic_list_group_item, null);

		TextView topicTV = (TextView) linearLayout.findViewById(R.id.topic);
		TextView topicCountTV = (TextView) linearLayout
				.findViewById(R.id.topicCount);
		topicCountTV.setVisibility(View.INVISIBLE);
		ImageView topicImage = (ImageView) linearLayout
				.findViewById(R.id.topicImage);

		Topic topic = topics.get(groupPosition);
		String topicName = topic.name;
		int topicCount = topic.count;
		topicTV.setText(topicName);
		topicTV.setPadding(36, 0, 0, 0);
		topicCountTV.setText("(" + topicCount + ")");

		return linearLayout;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
