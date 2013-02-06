package com.galeapp.backgrounds.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelperBasic extends SQLiteOpenHelper {

	private static final String TAG = "DatabaseHelperBasic";

	private static final int DATABASE_VERSION = 1;

	protected final SQLiteDatabase db;

	private Context context;

	public static final String DB_NAME = "backgrounds";

	public DatabaseHelperBasic(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
		this.context = context;
		db = getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// favorite表
		db.execSQL("CREATE TABLE [favorite] (_id integer PRIMARY KEY, date DEFAULT CURRENT_TIMESTAMP)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public SQLiteDatabase getDb() {
		return db;
	}

}
