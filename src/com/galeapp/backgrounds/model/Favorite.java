package com.galeapp.backgrounds.model;

public class Favorite {
	public static final String IMAGE_ID = "id";
	public static final String DATE = "date";

	public int imageId;
	public String date;

	public Favorite(int imageId, String date) {
		this.imageId = imageId;
		this.date = date;
	}
}