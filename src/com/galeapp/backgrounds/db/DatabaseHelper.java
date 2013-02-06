package com.galeapp.backgrounds.db;

import android.content.Context;

public class DatabaseHelper extends DatabaseHelperBasic {

	private static DatabaseHelper singleton;
	public final FavoriteDao favoriteDao;

	public static void init(Context context) {
		synchronized (DatabaseHelper.class) {
			singleton = new DatabaseHelper(context);
		}
	}

	public static DatabaseHelper initOrSingleton(Context context) {
		if (singleton == null) {
			init(context);
		}
		return singleton;
	}

	public static DatabaseHelper singleton() {
		return singleton;
	}

	private DatabaseHelper(Context context) {
		super(context);
		favoriteDao = new FavoriteDao();
		favoriteDao.setDb(db);
	}

	public void beginTransaction() {
		db.beginTransaction();
	}

	public void setTransactionSuccessful() {
		db.setTransactionSuccessful();
	}

	public void endTransaction() {
		db.endTransaction();
	}
}
