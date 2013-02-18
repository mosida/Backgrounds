package com.galeapp.backgrounds;

public class Constants {
	// 联网状态
	public static final int UNCONNECT = 0;
	public static final int CONNECTED = 1;
	public static final int LOCALDATA = 2;
	// 设置图片的模式
	public final static int STANDARD_TYPE = 0;
	public final static int FIXED_TYPE = 1;
	public final static int ENTIRE_TYPE = 2;

	// 日，周，总排行，冷门
	public final static int DAY_TYPE = 2;
	public final static int WEEK_TYPE = 1;
	public final static int ALL_TYPE = 0;
	public final static int COLD_TYPE = 3;
	public final static int RAND_TYPE = 4;

	public final static String RECENT = "RECENT";
	public final static String HOTEST = "HOTEST";
	public final static String CATEGORY = "CATEGORY";
	public final static String FAVORITE = "FAVORITE";
	public final static String TOPIC = "TOPIC";

	public final static String GALE_PATH = "/gale/";
	public final static String BACKGROUNDS_PATH = "/gale/backgrounds/";
	public final static String THUMBNAIL_PATH = "/gale/backgrounds/Thumbnail/";
	public final static String PRIVIEW_PATH = "/gale/backgrounds/PRIVIEW";
	public final static String RESULT_PATH = "/gale/backgrounds/RESULT";
	public final static String SAVE_PATH = "/gale/backgrounds/Image/";

	// public final static String RECENT_URL =
	// "http://backgrounds.ogqcorp.com/recent_957/";
	// public final static String HOTEST_URL =
	// "http://backgrounds.ogqcorp.com/ranking_777/";
	// public final static String ALL_CATEGORY_URL =
	// "http://backgrounds.ogqcorp.com/categories/";
	// public final static String CATEGORY_URL =
	// "http://backgrounds.ogqcorp.com/category_957/";
	// public final static String THUMBNAIL_URL =
	// "http://static2.backgrounds.ogqcorp.com/thumbnail2/";
	// public final static String PREVIEW_URL =
	// "http://static.backgrounds.ogqcorp.com/preview/";
	// public final static String INFO_URL =
	// "http://backgrounds.ogqcorp.com/image/info/";
	// public final static String RESULT_URL =
	// "http://static.backgrounds.ogqcorp.com/image/";

	public final static String RECENT_URL = "http://www.galeapp.com:81/piwigo/mobi/recent.php";
	public final static String HOTEST_URL = "http://www.galeapp.com:81/piwigo/mobi/hotest.php?type=";
	public final static String ALL_CATEGORY_URL = "http://www.galeapp.com:81/piwigo/mobi/categories.php";
	public final static String CATEGORY_URL = "http://www.galeapp.com:81/piwigo/mobi/category.php?id=";
	public final static String THUMBNAIL_URL = "http://www.galeapp.com:81/piwigo/mobi/thumbnail.php?id=";
	public final static String PREVIEW_URL = "http://www.galeapp.com:81/piwigo/mobi/preview.php?id=";
	public final static String INFO_URL = "http://www.galeapp.com:81/piwigo/mobi/info.php?id=";
	public final static String RESULT_URL = "http://www.galeapp.com:81/piwigo/mobi/result.php?id=";
	public final static String ALL_TOPIC_URL = "http://www.galeapp.com:81/piwigo/mobi/topics.php";
	public final static String TOPIC_URL = "http://www.galeapp.com:81/piwigo/mobi/topic.php?id=";

	// public final static String TEST_URL =
	// "http://www.galeapp.com:81/piwigo/mobi/test.php?type=";

	/****** 积分墙 *******/
//	public final static String APPWALL = "appWall";
//	public final static String APPWALL7 = "appWall4";
//	public final static String APPWALL_TOPIC = "topic_";
	// 被爆菊
//	public final static String YOUMI_ID = "38285e040fc5d559";
//	public final static String YOUMI_SEC = "dd2c7438e22a4eae";

	public final static String LAST_LOGIN = "lastLoginTime";
//	public final static int DOWNLOADE_PIC_SCORE = 5;

	// 图库
	// 文件名
	public final static String TIMEPIC = "TIMEPIC";
	// 文件名前缀
	public final static String PIC_NAME = "PIC_";
}
