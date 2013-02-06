package com.galeapp.backgrounds.db;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class FavoriteDao {

	public static final String TAG = "FavoriteDao";

	public static final String TABLE_NAME = "favorite";

	public static final String ID_COL_NAME = "_id";

	public static final String DATE_COL_NAME = "date";

	public static final String[] COLS_STRING = new String[] { ID_COL_NAME,
			DATE_COL_NAME };

	protected SQLiteDatabase db;

	public void setDb(SQLiteDatabase db) {
		this.db = db;
	}

	public Cursor getFavoriteCursor() {
		return db.query(TABLE_NAME, COLS_STRING, null, null, null, null,
				(" date asc"));
	}

	public boolean isFavorite(int id) {
		Cursor cursor = db.query(TABLE_NAME, COLS_STRING, ID_COL_NAME + "=?",
				new String[] { String.valueOf(id) }, null, null, null);
		if (cursor.moveToFirst() != false) {
			cursor.close();
			return true;
		} else {
			cursor.close();
			return false;
		}
	}

	public long addFavorite(int id) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp t = new Timestamp(System.currentTimeMillis());
		ContentValues c = new ContentValues();
		c.put(ID_COL_NAME, id);
		c.put(DATE_COL_NAME, sdf.format(t));
		return db.insert(TABLE_NAME, null, c);
	}

	public long removeFavorite(int id) {
		return db.delete(TABLE_NAME, ID_COL_NAME + "=?",
				new String[] { String.valueOf(id) });
	}
}
