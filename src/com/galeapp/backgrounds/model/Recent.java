package com.galeapp.backgrounds.model;

public class Recent {

	public static final String IMAGE_ID = "id";
	public static final String TAGS = "tags";

	public int imageId;
	public String tags;

	public Recent(int imageId, String tags) {
		this.imageId = imageId;
		this.tags = tags;
	}
}
