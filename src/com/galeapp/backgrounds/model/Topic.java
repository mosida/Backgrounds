package com.galeapp.backgrounds.model;

import java.util.ArrayList;

public class Topic {

	public static final String TOPIC_ID = "id";
	public static final String COUNT = "count";
	public static final String NAME = "name";
	public static final String SUBTOPICS = "subTopics";

	public int topicId;
	public int count;
	public String name;
	public ArrayList<SubTopic> subTopics;

	public Topic(int topicId, int count, String name,
			ArrayList<SubTopic> subTopics) {
		this.topicId = topicId;
		this.count = count;
		this.name = name;
		this.subTopics = subTopics;
	}

}
