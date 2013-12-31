package com.joyplus.tv.utils;

import android.content.Intent;

/**
 * 参数文件，不可修改
 * @author Joyplus
 */
public class ShowDtailConfig {

	/**
	 * Example：
	 * prod_id: "1016663" 
	 * prod_name: "笔仙2" 
	 * prod_type: "1" 
	 * prod_pic_url:"http://img4.douban.com/view/photo/photo/public/p2023557318.jpg"
	 * 
	 * intent.putExtra("ID", IDValue); 
	 * intent.putExtra("prod_url",prod_urlValue); 
	 * intent.putExtra("prod_name", prod_nameValue);
	 * intent.putExtra("stars", starsValue); 
	 * intent.putExtra("directors",directorsValue);
	 * intent.putExtra("summary", summaryValue);
	 * intent.putExtra("support_num", support_numValue);
	 * intent.putExtra("favority_num", favority_numValue);
	 * intent.putExtra("definition", definitionValue); 
	 * intent.putExtra("score",scoreValue);
	 */
	
	// 影片类型PROD_TYPE,必须传
	public static final String PROD_TYPE 		= "prod_type";
	// 影片唯一标是ID，必须传
	public static final String ID 				= "ID";
	// 影片海报图片地址，可不传
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
	private static final String JOYPLUS_INTENT_ACTION 					= "action_com_joyplus_tv_detail";
	//外部需要添加的Category 
	private static final String JOYPLUS_INTENT_CATEGORY 				= Intent.CATEGORY_DEFAULT;
	
	//电影类型
	public static final String MOVIE_TYPE = "1";
	//电视剧类型
	public static final String TVSERIES_TYPE = "2";
	//综艺类型
	public static final String VARIETY_TYPE = "3";
	//动漫类型
	public static final String ANIME_TYPE = "131";
	//记录
	public static final String JILU_TYPE = "5";
	
	public static Intent getIntent() throws Exception{
		Intent intent = new Intent(ShowDtailConfig.JOYPLUS_INTENT_ACTION);
		intent.addCategory(ShowDtailConfig.JOYPLUS_INTENT_CATEGORY);
		return intent;
	}
}
