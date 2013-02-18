package com.galeapp.backgrounds.model;

public class SubTopic {
	public static final String SUB_ID = "subId";
	public static final String SUB_COUNT = "subCount";
	public static final String SUB_NAME = "subName";
	public static final String TOPIC_NAME = "topicName";

	public int subTopicId;
	public int subCount;
	public String subName;
	public String topicName;

	public SubTopic(int subTopicId, int subCount, String subName,
			String topicName) {
		this.subTopicId = subTopicId;
		this.subCount = subCount;
		this.subName = subName;
		this.topicName = topicName;
	}

}
