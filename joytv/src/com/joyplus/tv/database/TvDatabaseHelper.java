package com.joyplus.tv.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.joyplus.tv.utils.DataBaseItems;
import com.joyplus.tv.utils.DataBaseItems.SQLite3_DataType;
import com.joyplus.tv.utils.DataBaseItems.UserShouCang;

public class TvDatabaseHelper extends SQLiteOpenHelper implements UserShouCang,SQLite3_DataType{

	public static final String DATABASE_NAME = "joyplus.db";
	public static final int DATABASE_VERSION = 1;
	public static final String ZHUIJU_TABLE_NAME = "zhuiju";

	private SQLiteDatabase sqLiteDatabase;

	public static TvDatabaseHelper newTvDatabaseHelper(Context context) {

		return new TvDatabaseHelper(context);
	}

	public void openDatabaseWithHelper() {

		try {
			if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {

				if (!sqLiteDatabase.isReadOnly()) {

					return;
				}

				sqLiteDatabase.close();
			}
			sqLiteDatabase = getWritableDatabase();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void openDatabaseReadOnly() {
		try {
			if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
				if (sqLiteDatabase.isReadOnly())
					return;

				sqLiteDatabase.close();
			}
			sqLiteDatabase = getReadableDatabase();
		} catch (SQLException e) {
			 e.printStackTrace();
		}
	}
	
    //Database Close
    public void closeDatabase() {
        try {
            if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
            	
            	sqLiteDatabase.close();
            }
        } catch (SQLException e) {
        	e.printStackTrace();
//            return;
        }
    }

	private TvDatabaseHelper(Context context) {
		this(context, DATABASE_NAME, DATABASE_VERSION);
	}

	public TvDatabaseHelper(Context context, String name, int version) {
		this(context, name, null, version);
	}

	public TvDatabaseHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}
	
//	private String sql_table_zhuiju = "create table if not exists "
//			+ ZHUIJU_TABLE_NAME + " ( " + ID
//			+ " integer primary key autoincrement, " 
//			+ USER_ID + TEXT_DOT 
//			+ PRO_ID + TEXT_DOT
//			+ NAME + TEXT_DOT 
//			+ SCORE + TEXT_DOT 
//			+ PRO_TYPE + TEXT_DOT 
//			+ PIC_URL + TEXT_DOT
//			+ DURATION + TEXT_DOT 
//			+ CUR_EPISODE + TEXT_DOT
//			+ MAX_EPISODE + TEXT_DOT 
//			+ STARS + TEXT_DOT 
//			+ DIRECTORS + TEXT_DOT 
//			+ IS_NEW + INTEGER 
//			+ " )";
	
	private String sql_table_zhuiju = "create table if not exists "
			+ ZHUIJU_TABLE_NAME + " ( " + ID
			+ " integer primary key autoincrement, "
			+ PRO_ID + TEXT_DOT
			+ USER_ID + TEXT_DOT 
			+ NAME + TEXT_DOT 
			+ SCORE + TEXT_DOT 
			+ PRO_TYPE + TEXT_DOT 
			+ PIC_URL + TEXT_DOT
			+ DURATION + TEXT_DOT 
			+ CUR_EPISODE + TEXT_DOT
			+ MAX_EPISODE + TEXT_DOT 
			+ STARS + TEXT_DOT 
			+ DIRECTORS + TEXT_DOT 
			+ IS_NEW + INTEGER_DOT
			+ IS_UPDATE + INTEGER 
			+ " )";

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
