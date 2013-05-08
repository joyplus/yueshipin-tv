package com.joyplus.tv.database;

import com.joyplus.tv.utils.DataBaseItems;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class TvDatabaseHelper extends SQLiteOpenHelper{
	
	public static final String DATABASE_NAME = "joyplus.db";
	public static final int DATABASE_VERSION = 1;
	public static final String ZHUIJU_TABLE_NAME = "zhuiju";

	
	private String sql_table_zhuiju = "create table if not exists " 
			+ZHUIJU_TABLE_NAME +" ( " 
			+ DataBaseItems.ID +" integer primary key autoincrement, "
			+ DataBaseItems.USER_ID + " varchar, "
			+ DataBaseItems.PRO_ID + " varchar, "
			+ DataBaseItems.NAME + " varchar, "
			+ DataBaseItems.SCORE + " varchar, "
			+ DataBaseItems.PRO_TYPE + " varchar, "
			+ DataBaseItems.PIC_URL + " varchar, "
			+ DataBaseItems.DURATION + " varchar, "
			+ DataBaseItems.CUR_EPISODE + " varchar, "
			+ DataBaseItems.MAX_EPISODE + " varchar, "
			+ " )";
	
	
	public TvDatabaseHelper(Context context) {
		this(context,DATABASE_NAME,DATABASE_VERSION);
	}
	
	
	public TvDatabaseHelper(Context context, String name, int version) {
		this(context,name,null,version);
	}
	
	public TvDatabaseHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}
	


	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

		db.execSQL(sql_table_zhuiju);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
