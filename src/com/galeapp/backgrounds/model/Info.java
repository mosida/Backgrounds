package com.galeapp.backgrounds.model;

public class Info {

	public static final String COUNT = "count";
	public static final String WIDTH = "width";
	public static final String HEIGHT = "height";
	public static final String SIZE = "size";
	public static final String SOURCE = "ownership";

	public int count;
	public int width;
	public int height;
	private int size;
	public String source;

	public Info(int width, int height, int size, int count, String source) {
		this.width = width;
		this.height = height;
		this.size = size;
		this.count = count;
		this.source = source;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getSize() {
		return size + "KB";
	}

	public int getIntSize() {
		return size;
	}

	public String getInfoDescription() {
		return "宽度:" + width + "px 高度:" + height + "px " + " 大小:" + getSize()
				+ " 下载次数:" + count;
	}
}
