package com.joyplus.tv.utils;

public class DataBaseItems {
	
	public static interface SQLite3_DataType {
		
		
		String INTEGER = " INTEGER ";
		String NULL = " NULL ";
		String REAL = " REAL ";
		String TEXT = " TEXT ";
		String BLOB = " BLOB ";
		
		String INTEGER_DOT = " INTEGER, ";
		String NULL_DOT = " NULL, ";
		String REAL_DOT = " REAL, ";
		String TEXT_DOT = " TEXT, ";
		String BLOB_DOT = " BLOB, ";
	}

	public static interface UserShouCang {

		String USER_ID = "user_id";// 用户id

		String ID = "id";// 自增id

		String PRO_ID = "pro_id";// 影片id
		String NAME = "name";// 影片名字
		String SCORE = "score";// 影片评分
		String PRO_TYPE = "pro_type";// 影片类型
		String PIC_URL = "pic_url";// 图片url
		String DURATION = "duration";// 时间
		String CUR_EPISODE = "cur_episode";// 当前集数
		String MAX_EPISODE = "max_episode";// 最大集数
		
		String STARS = "stars";//主演
		String DIRECTORS = "directors";//导演
		// String NUM = "num";
	}
	
	public static interface UserBoFang {
		
		String USER_ID = "user_id";// 用户id

		String ID = "id";// 自增id
	}

}
