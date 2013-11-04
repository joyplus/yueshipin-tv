package com.joyplus.tv.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.net.Uri;

public class ShowDtailConfig {

	/**
	 * intent.putExtra("ID", IDValue);
	 * intent.putExtra("prod_url", prod_urlValue);
	 * intent.putExtra("prod_name", prod_nameValue);
	 * intent.putExtra("stars", starsValue);
	 * intent.putExtra("directors", directorsValue);
	 * intent.putExtra("summary", summaryValue);
	 * intent.putExtra("support_num", support_numValue);
	 * intent.putExtra("favority_num", favority_numValue);
	 * intent.putExtra("definition", definitionValue);
	 * intent.putExtra("score", scoreValue);
	 */
	
	// 影片唯一标是ID，必须传
	public static final String ID 				= "ID";
	// 影片播放地址，可不传
	public static final String PROD_URL 		= "prod_url";
	// 影片播放名字，可不传
	public static final String PROD_NAME 		= "prod_name";
	// 影片主演名字，可不传
	public static final String STARS 			= "stars";
	// 影片导演，可不传
	public static final String DIRECTORS 		= "directors";
	// 影片详细介绍，可不传
	public static final String SUMMARY 		= "summary";
	// 影片喜爱度，可不传
	public static final String SUPPORT_NUM 	= "support_num";
	// 影片收藏数，可不传
	public static final String FAVORITY_NUM 	= "favority_num";
	// 影片影片清晰度，可不传
	public static final String DEFINITION 		= "definition";
	// 影片豆瓣评分，可不传
	public static final String SCORE 			= "score";
	
	//外部需要添加的Action
	public static final String JOYPLUS_INTENT_ACTION 					= Intent.ACTION_VIEW;
	//外部需要添加的Category 
	public static final String JOYPLUS_INTENT_CATEGORY 				= Intent.CATEGORY_DEFAULT;
	//外部进入电影详情
	public static final String JOYPLUS_DATA_MIME_TYPE_MOVIE		= "joyplus_data_mime_detail_movie/*";
	//外部进入电视剧详情
	public static final String JOYPLUS_DATA_MIME_TYPE_TVSERIES 	= "joyplus_data_mime_detail_tvseries/*";
	//外部进入动漫详情
	public static final String JOYPLUS_DATA_MIME_TYPE_ANIME 		= "joyplus_data_mime_detail_anime/*";
	//外部进入电视剧详情
	public static final String JOYPLUS_DATA_MIME_TYPE_VARIETY 		= "joyplus_data_mime_detail_variety/*";
	
	//四种数据类型
	private static final List<String> JOYPLUS_MIME_LISTS					 = new ArrayList<String>();
	
	static{
		JOYPLUS_MIME_LISTS.add(JOYPLUS_DATA_MIME_TYPE_MOVIE);
		JOYPLUS_MIME_LISTS.add(JOYPLUS_DATA_MIME_TYPE_TVSERIES);
		JOYPLUS_MIME_LISTS.add(JOYPLUS_DATA_MIME_TYPE_ANIME);
		JOYPLUS_MIME_LISTS.add(JOYPLUS_DATA_MIME_TYPE_VARIETY);
	}
	
	//获取JoyplusIntent
	public static Intent getIntent4MimeType(String mimeType) throws Exception{
		Intent intent = new Intent(ShowDtailConfig.JOYPLUS_INTENT_ACTION);
		intent.addCategory(ShowDtailConfig.JOYPLUS_INTENT_CATEGORY);
		if(ShowDtailConfig.JOYPLUS_MIME_LISTS.contains(mimeType)){
			intent.setDataAndType(Uri.parse(""), mimeType);
			return intent;
		}else {
			throw new Exception();
		}
	}
}
