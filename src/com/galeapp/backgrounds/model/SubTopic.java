package com.galeapp.backgrounds.model;

public class SubTopic {
	public static final String SUB_ID = "subId";
	public static final String SUB_COUNT = "subCount";
	public static final String SUB_NAME = "subName";
	public static final String SUB_SCORE = "subScore";
	public static final String SUB_BUY = "subBuy";
	public static final String TOPIC_NAME = "topicName";

	public int subTopicId;
	public int subCount;
	public int subScore;
	public String subName;
	public boolean subBuy;
	public String topicName;

	public SubTopic(int subTopicId, int subCount, String subName, int subScore,
			boolean subBuy, String topicName) {
		this.subTopicId = subTopicId;
		this.subCount = subCount;
		this.subName = subName;
		this.subScore = subScore;
		this.subBuy = subBuy;
		this.topicName = topicName;
	}

}
