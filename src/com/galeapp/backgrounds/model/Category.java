package com.galeapp.backgrounds.model;

public class Category {

	public static final String CATEGORY_ID = "id";
	public static final String COUNT = "count";
	public static final String NAME = "name";

	public int categoryId;
	public int count;
	public String name;

	public Category(int categoryId, int count, String name) {
		this.categoryId = categoryId;
		this.count = count;
		this.name = name;
	}

}
