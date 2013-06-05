package com.joyplus.tv;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.Instrumentation;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joyplus.tv.Service.Return.ReturnTVBangDanList;
import com.joyplus.tv.Service.Return.ReturnTops;
import com.joyplus.tv.Service.Return.ReturnUserFavorities;
import com.joyplus.tv.Service.Return.ReturnUserPlayHistories;
import com.joyplus.tv.database.TvDatabaseHelper;
import com.joyplus.tv.entity.HotItemInfo;
import com.joyplus.tv.entity.MovieItemData;
import com.joyplus.tv.entity.ReturnFilterMovieSearch;
import com.joyplus.tv.utils.BangDanKey;
import com.joyplus.tv.utils.DataBaseItems;
import com.joyplus.tv.utils.DataBaseItems.UserHistory;
import com.joyplus.tv.utils.DataBaseItems.UserShouCang;
import com.joyplus.tv.utils.JieMianConstant;
import com.joyplus.tv.utils.Log;

public class StatisticsUtils implements JieMianConstant, BangDanKey {

	private static final String TAG = "StatisticsUtils";

	/**
	 * 用来统计用户点击播放视屏后正常跳转的次数 有可能跳转到播放器，也有可能跳转到浏览器
	 * 
	 * 数据从服务器上获取
	 * 
	 * @param aq
	 * @param prod_id
	 * @param prod_name
	 * @param prod_subname
	 * @param pro_type
	 */
	public static void StatisticsClicksShow(AQuery aq, App app, String prod_id,
			String prod_name, String prod_subname, int pro_type) {

		String url = Constant.BASE_URL + "program/recordPlay";

		Map<String, Object> params = new HashMap<String, Object>();
		// params.put("client","android");
		// params.put("version", "0.9.9");
		// params.put("app_key", Constant.APPKEY);// required string //
		// 申请应用时分配的AppKey。

		params.put("prod_id", prod_id);// required string // 视频id

		params.put("prod_name", prod_name);// required // string 视频名字

		params.put("prod_subname", prod_subname);// required // string 视频的集数
													// 电影的subname为空

		params.put("prod_type", pro_type);// required int 视频类别
											// 1：电影，2：电视剧，3：综艺，4：视频

		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.SetHeader(app.getHeaders());
		cb.params(params).url(url).type(JSONObject.class);

		aq.ajax(cb);
	}

	public static int count = 0;

	public static void simulateKey(final int KeyCode) {

		count++;
		if (count > 2) {
			return;
		}

		new Thread() {

			public void run() {

				try {

					Instrumentation inst = new Instrumentation();
					// inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_UP,
					// KeyCode));

					inst.sendKeyDownUpSync(KeyCode);
					// handler.sendEmptyMessage(0X111);

				} catch (Exception e) {

					Log.e("Exception when sendKeyDownUpSync", e.toString());

				}

			}

		}.start();

	}

	public static String getTopItemURL(String url, String top_id,
			String page_num, String page_size) {

		return url + "?top_id=" + top_id + "&page_num=" + page_num
				+ "&page_size=" + page_size;
	}

	public static String getTopURL(String url, String page_num,
			String page_size, String topic_type) {

		return url + "?page_num=" + page_num + "&page_size=" + page_size
				+ "&topic_type=" + topic_type;
	}

	/**
	 * type required 视频的类别，节目类型，1：电影，2：电视剧，3：综艺节目，131：动漫
	 * 
	 * @param url
	 * @param page_num
	 * @param page_size
	 * @param type
	 * @return
	 */
	public static String getFilterURL(String url, String page_num,
			String page_size, String type) {

		return url + "?page_num=" + page_num + "&page_size=" + page_size
				+ "&type=" + type;
	}

	public static String getSearchURL(String url, String page_num,
			String page_size, String keyword) {

		return url + "?page_num=" + page_num + "&page_size=" + page_size
				+ "&keyword=" + keyword;
	}

	public static String getUserFavURL(String url, String page_num,
			String page_size, String vod_type, String userId) {

		return url + "?page_num=" + page_num + "&page_size=" + page_size
				+ "&vod_type=" + vod_type + "&userid=" + userId;
	}
	
	public static String getYingPinURL(String url,String page_num,
			String page_size,String prod_id) {
		
		return url + "?page_num=" + page_num + "&page_size=" + page_size
				+ "&prod_id=" + prod_id ;
	}

	public static final int CACHE_NUM = 20;
	public static final int FIRST_NUM = 30;

	// TV 全部分类 10
	public static String getTV_Quan10URL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, TV_DIANSHIJU,
				1 + "", CACHE_NUM + "");
	}

	// TV 全部分类
	public static String getTV_QuanAllFirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", TV_TYPE);
	}

	// TV 全部分类
	public static String getTV_QuanAllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", TV_TYPE);
	}

	// TV 大陆剧
	public static String getTV_DalujuFirstURL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_DALU_DIANSHI,
				1 + "", FIRST_NUM + "");
	}

	// TV 大陆剧
	public static String getTV_DalujuCacheURL(int pageNum) {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_QINZI_DONGMAN,
				(pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//获取内地电视剧filter first
	public static String getTV_Daluju_Quan_FirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", TV_TYPE) + AREA 
				+ URLEncoder.encode("内地");
	}
	
	//获取内地电视剧filter cache
	public static String getTV_Daluju_Quan_AllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", TV_TYPE)+ AREA 
				+ URLEncoder.encode("内地");
	}

	// 港剧
	public static String getTV_GangjuFirstURL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_GANGJU_DIANSHI,
				1 + "", FIRST_NUM + "");
	}

	// 港剧
	public static String getTV_GangjuCacheURL(int pageNum) {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_GANGJU_DIANSHI,
				(pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//获取香港电视剧filter first
	public static String getTV_Gangju_Quan_FirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", TV_TYPE) + AREA 
				+ URLEncoder.encode("香港");
	}
	
	//获取香港电视剧filter cache
	public static String getTV_Gangju_Quan_AllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", TV_TYPE)+ AREA 
				+ URLEncoder.encode("香港");
	}

	public static String getTV_TaijuFirstURL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_TAIJU_DIANSHI,
				1 + "", FIRST_NUM + "");
	}

	public static String getTV_TaijuCacheURL(int pageNum) {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_TAIJU_DIANSHI,
				(pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//获取台湾电视剧filter first
	public static String getTV_Taiju_Quan_FirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", TV_TYPE) + AREA 
				+ URLEncoder.encode("台湾");
	}
	
	//获取台湾电视剧filter cache
	public static String getTV_Taiju_Quan_AllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", TV_TYPE)+ AREA 
				+ URLEncoder.encode("台湾");
	}

	public static String getTV_HanjuFirstURL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_HANJU_DIANSHI,
				1 + "", FIRST_NUM + "");
	}

	public static String getTV_HanjuCacheURL(int pageNum) {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_HANJU_DIANSHI,
				(pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//获取韩国电视剧filter first
	public static String getTV_Hanju_Quan_FirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", TV_TYPE) + AREA 
				+ URLEncoder.encode("韩国");
	}
	
	//获取韩国电视剧filter cache
	public static String getTV_Hanju_Quan_AllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", TV_TYPE)+ AREA 
				+ URLEncoder.encode("韩国");
	}

	public static String getTV_MeijuFirstURL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_OUMEI_DIANSHI,
				1 + "", FIRST_NUM + "");
	}

	public static String getTV_MeijuCacheURL(int pageNum) {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_OUMEI_DIANSHI,
				(pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//获取美国电视剧filter first
	public static String getTV_Meiju_Quan_FirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", TV_TYPE) + AREA 
				+ URLEncoder.encode("美国");
	}
	
	//获取美国电视剧filter cache
	public static String getTV_Meiju_Quan_AllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", TV_TYPE)+ AREA 
				+ URLEncoder.encode("美国");
	}

	public static String getTV_RijuFirstURL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_RIJU_DIANSHI,
				1 + "", FIRST_NUM + "");
	}

	public static String getTV_RijuCacheURL(int pageNum) {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_RIJU_DIANSHI,
				(pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//获取日本电视剧filter first
	public static String getTV_Riju_Quan_FirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", TV_TYPE) + AREA 
				+ URLEncoder.encode("日本");
	}
	
	//获取日本电视剧filter cache
	public static String getTV_Riju_Quan_AllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", TV_TYPE)+ AREA 
				+ URLEncoder.encode("日本");
	}

	// Movie

	public static String getMovie_Quan10URL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, TV_DIANYING, 1 + "",
				CACHE_NUM + "");
	}

	public static String getMovie_QuanAllFirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", MOVIE_TYPE);
	}

	public static String getMovie_QuanAllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", MOVIE_TYPE);
	}

	public static String getMovie_DongzuoFirstURL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_DONGZUO_MOVIE,
				1 + "", FIRST_NUM + "");
	}

	public static String getMovie_DongzuoCacheURL(int pageNum) {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_DONGZUO_MOVIE,
				(pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//获取动作电影filter first
	public static String getMovie_Dongzuo_Quan_FirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", MOVIE_TYPE) + SUB_TYPE 
				+ URLEncoder.encode("动作");
	}
	
	//获取动作电影filter cache
	public static String getMovie_Dongzuo_Quan_AllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", MOVIE_TYPE)+ SUB_TYPE 
				+ URLEncoder.encode("动作");
	}

	public static String getMovie_KehuanFirstURL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_KEHUAN_MOVIE,
				1 + "", FIRST_NUM + "");
	}

	public static String getMovie_KehuanCacheURL(int pageNum) {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_KEHUAN_MOVIE,
				(pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//获取科幻电影filter first
	public static String gettMovie_Kehuan_Quan_FirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", MOVIE_TYPE) + SUB_TYPE 
				+ URLEncoder.encode("科幻");
	}
	
	//获取科幻电影filter cache
	public static String gettMovie_Kehuan_Quan_AllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", MOVIE_TYPE)+ SUB_TYPE 
				+ URLEncoder.encode("科幻");
	}

	public static String getMovie_LunliFirstURL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_LUNLI_MOVIE,
				1 + "", FIRST_NUM + "");
	}

	public static String getMovie_LunliCacheURL(int pageNum) {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_LUNLI_MOVIE,
				(pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//获取伦理电影filter first
	public static String gettMovie_Lunli_Quan_FirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", MOVIE_TYPE) + SUB_TYPE 
				+ URLEncoder.encode("伦理");
	}
	
	//获取伦理电影filter cache
	public static String gettMovie_Lunli_Quan_AllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", MOVIE_TYPE)+ SUB_TYPE 
				+ URLEncoder.encode("伦理");
	}

	public static String getMovie_XijuFirstURL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_XIJU_MOVIE,
				1 + "", FIRST_NUM + "");
	}

	public static String getMovie_XijuCacheURL(int pageNum) {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_XIJU_MOVIE,
				(pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//获取喜剧电影filter first
	public static String gettMovie_Xiju_Quan_FirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", MOVIE_TYPE) + SUB_TYPE 
				+ URLEncoder.encode("喜剧");
	}
	
	//获取喜剧电影filter cache
	public static String gettMovie_Xiju_Quan_AllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", MOVIE_TYPE)+ SUB_TYPE 
				+ URLEncoder.encode("喜剧");
	}

	public static String getMovie_AiqingFirstURL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_AIQING_MOVIE,
				1 + "", FIRST_NUM + "");
	}

	public static String getMovie_AiqingCacheURL(int pageNum) {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_AIQING_MOVIE,
				(pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//获取爱情电影filter first
	public static String gettMovie_Aiqing_Quan_FirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", MOVIE_TYPE) + SUB_TYPE 
				+ URLEncoder.encode("爱情");
	}
	
	//获取爱情电影filter cache
	public static String gettMovie_Aiqing_Quan_AllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", MOVIE_TYPE)+ SUB_TYPE 
				+ URLEncoder.encode("爱情");
	}

	public static String getMovie_XuanyiFirstURL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_XUANYI_MOVIE,
				1 + "", FIRST_NUM + "");
	}

	public static String getMovie_XuanyiCacheURL(int pageNum) {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_XUANYI_MOVIE,
				(pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//获取悬疑电影filter first
	public static String gettMovie_Xuanyi_Quan_FirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", MOVIE_TYPE) + SUB_TYPE 
				+ URLEncoder.encode("悬疑");
	}
	
	//获取悬疑电影filter cache
	public static String gettMovie_Xuanyi_Quan_AllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", MOVIE_TYPE)+ SUB_TYPE 
				+ URLEncoder.encode("悬疑");
	}

	public static String getMovie_KongbuFirstURL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_KONGBU_MOVIE,
				1 + "", FIRST_NUM + "");
	}

	public static String getMovie_KongbuCacheURL(int pageNum) {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_KONGBU_MOVIE,
				(pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//获取恐怖电影filter first
	public static String gettMovie_Kongbu_Quan_FirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", MOVIE_TYPE) + SUB_TYPE 
				+ URLEncoder.encode("恐怖");
	}
	
	//获取恐怖电影filter cache
	public static String gettMovie_Kongbu_Quan_AllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", MOVIE_TYPE)+ SUB_TYPE 
				+ URLEncoder.encode("恐怖");
	}

	public static String getMovie_DonghuaFirstURL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_DONGHUA_MOVIE,
				1 + "", FIRST_NUM + "");
	}

	public static String getMovie_DonghuaCacheURL(int pageNum) {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_DONGHUA_MOVIE,
				(pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//获取动画电影filter first
	public static String gettMovie_Donghua_Quan_FirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", MOVIE_TYPE) + SUB_TYPE 
				+ URLEncoder.encode("动画");
	}
	
	//获取动画电影filter cache
	public static String gettMovie_Donghua_Quan_AllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", MOVIE_TYPE)+ SUB_TYPE 
				+ URLEncoder.encode("动画");
	}

	// 动漫
	public static String getDongman_Quan10URL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, TV_DONGMAN, 1 + "",
				CACHE_NUM + "");
	}

	public static String getDongman_QuanAllFirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", DONGMAN_TYPE);
	}

	public static String getDongman_QuanAllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", DONGMAN_TYPE);
	}

	public static String getDongman_QinziFirstURL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_QINZI_DONGMAN,
				1 + "", FIRST_NUM + "");
	}
	

	public static String getDongman_QinziCacheURL(int pageNum) {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_QINZI_DONGMAN,
				(pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//获取亲子动漫filter first
	public static String getDongman_Qinzi_Quan_FirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", DONGMAN_TYPE) + SUB_TYPE 
				+ URLEncoder.encode("亲子");
	}
	
	//获取亲子动漫filter cache
	public static String getDongman_Qinzi_Quan_AllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", DONGMAN_TYPE)+ SUB_TYPE 
				+ URLEncoder.encode("亲子");
	}

	public static String getDongman_RexueFirstURL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_REXUE_DONGMAN,
				1 + "", FIRST_NUM + "");
	}
	

	public static String getDongman_RexueCacheURL(int pageNum) {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_REXUE_DONGMAN,
				(pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//获取热血动漫filter first
	public static String getDongman_Rexue_Quan_FirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", DONGMAN_TYPE) + SUB_TYPE 
				+ URLEncoder.encode("热血");
	}
	
	//获取热血动漫filter cache
	public static String getDongman_Rexue_Quan_AllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", DONGMAN_TYPE)+ SUB_TYPE 
				+ URLEncoder.encode("热血");
	}

	public static String getDongman_HougongFirstURL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,
				REBO_HOUGONG_DONGMAN, 1 + "", FIRST_NUM + "");
	}

	public static String getDongman_HougongCacheURL(int pageNum) {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,
				REBO_HOUGONG_DONGMAN, (pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//获取校园动漫filter first
	public static String getDongman_Hougong_Quan_FirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", DONGMAN_TYPE) + SUB_TYPE 
				+ URLEncoder.encode("校园");
	}
	
	//获取校园动漫filter cache
	public static String getDongman_Hougong_Quan_AllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", DONGMAN_TYPE)+ SUB_TYPE 
				+ URLEncoder.encode("校园");
	}

	public static String getDongman_TuiliFirstURL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_TUILI_DONGMAN,
				1 + "", FIRST_NUM + "");
	}

	public static String getDongman_TuiliCacheURL(int pageNum) {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_TUILI_DONGMAN,
				(pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//获取推理动漫filter first
	public static String getDongman_Tuili_Quan_FirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", DONGMAN_TYPE) + SUB_TYPE 
				+ URLEncoder.encode("推理");
	}
	
	//获取推理动漫filter cache
	public static String getDongman_Tuili_Quan_AllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", DONGMAN_TYPE)+ SUB_TYPE 
				+ URLEncoder.encode("推理");
	}

	public static String getDongman_JizhanFirstURL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_JIZHAN_DONGMAN,
				1 + "", FIRST_NUM + "");
	}

	public static String getDongman_JizhanCacheURL(int pageNum) {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL, REBO_JIZHAN_DONGMAN,
				(pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//获取机战动漫filter first
	public static String getDongman_Jizhan_Quan_FirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", DONGMAN_TYPE) + SUB_TYPE 
				+ URLEncoder.encode("机战");
	}
	
	//获取机战动漫filter cache
	public static String getDongman_Jizhan_Quan_AllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", DONGMAN_TYPE)+ SUB_TYPE 
				+ URLEncoder.encode("机战");
	}

	public static String getDongman_GaoxiaoFirstURL() {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,
				REBO_GAOXIAO_DONGMAN, 1 + "", CACHE_NUM + "");
	}

	public static String getDongman_GaoxiaoCacheURL(int pageNum) {

		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,
				REBO_GAOXIAO_DONGMAN, (pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//获取搞笑动漫filter first
	public static String getDongman_Gaoxiao_Quan_FirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "",
				(FIRST_NUM - 10) + "", DONGMAN_TYPE) + SUB_TYPE 
				+ URLEncoder.encode("搞笑");
	}
	
	//获取搞笑动漫filter cache
	public static String getDongman_Gaoxiao_AllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", DONGMAN_TYPE)+ SUB_TYPE 
				+ URLEncoder.encode("搞笑");
	}

	// 悦单

	public static String getYueDan_DianyingFirstURL() {

		return StatisticsUtils.getTopURL(TOP_URL, 1 + "", FIRST_NUM + "",
				MOVIE_TYPE + "");
	}

	public static String getYueDan_DianyingCacheURL(int pageNum) {

		return StatisticsUtils.getTopURL(TOP_URL, (pageNum + 1) + "", CACHE_NUM
				+ "", MOVIE_TYPE + "");
	}

	public static String getYueDan_DianshiFirstURL() {

		return StatisticsUtils.getTopURL(TOP_URL, 1 + "", FIRST_NUM + "",
				TV_TYPE + "");
	}

	public static String getYueDan_DianshiCacheURL(int pageNum) {

		return StatisticsUtils.getTopURL(TOP_URL, (pageNum + 1) + "", CACHE_NUM
				+ "", TV_TYPE + "");
	}

	public static String getZongyi_QuanAllFirstURL() {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "", (FIRST_NUM)
				+ "", ZONGYI_TYPE);
	}

	public static String getZongyi_QuanAllCacheURL(int pageNum) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", ZONGYI_TYPE);
	}

	// search

	public static String getSearch_FirstURL(String search) {
//		Log.i(TAG, "getSearch_FirstURL-->" + StatisticsUtils.getSearchURL(SEARCH_URL, 1 + "", FIRST_NUM + "",
//				search));

		return StatisticsUtils.getSearchURL(SEARCH_CAPITAL_URL, 1 + "", FIRST_NUM + "",
				search);
	}

	public static String getSearch_CacheURL(int pageNum, String search) {

		return StatisticsUtils.getSearchURL(SEARCH_CAPITAL_URL, (pageNum + 1) + "",
				CACHE_NUM + "", search);
	}
	
	//电影搜索 search
	public static String getSearch_Movie_FirstURL(String search) {
//		Log.i(TAG, "getSearch_FirstURL-->" + StatisticsUtils.getSearchURL(SEARCH_URL, 1 + "", FIRST_NUM + "",
//				search));

		return StatisticsUtils.getSearchURL(SEARCH_CAPITAL_URL, 1 + "", FIRST_NUM + "",
				search) + "&type=" + MOVIE_TYPE;
	}

	public static String getSearch_Movie_CacheURL(int pageNum, String search) {

		return StatisticsUtils.getSearchURL(SEARCH_CAPITAL_URL, (pageNum + 1) + "",
				CACHE_NUM + "", search) + "&type=" + MOVIE_TYPE;
	}
	
	//电视剧搜索 search
	public static String getSearch_TV_FirstURL(String search) {
//		Log.i(TAG, "getSearch_FirstURL-->" + StatisticsUtils.getSearchURL(SEARCH_URL, 1 + "", FIRST_NUM + "",
//				search));

		return StatisticsUtils.getSearchURL(SEARCH_CAPITAL_URL, 1 + "", FIRST_NUM + "",
				search) + "&type=" + TV_TYPE;
	}

	public static String getSearch_TV_CacheURL(int pageNum, String search) {

		return StatisticsUtils.getSearchURL(SEARCH_CAPITAL_URL, (pageNum + 1) + "",
				CACHE_NUM + "", search) + "&type=" + TV_TYPE;
	}
	
	//综艺搜索 search
	public static String getSearch_Zongyi_FirstURL(String search) {
//		Log.i(TAG, "getSearch_FirstURL-->" + StatisticsUtils.getSearchURL(SEARCH_URL, 1 + "", FIRST_NUM + "",
//				search));

		return StatisticsUtils.getSearchURL(SEARCH_CAPITAL_URL, 1 + "", FIRST_NUM + "",
				search) + "&type=" + ZONGYI_TYPE;
	}

	public static String getSearch_Zongyi_CacheURL(int pageNum, String search) {

		return StatisticsUtils.getSearchURL(SEARCH_CAPITAL_URL, (pageNum + 1) + "",
				CACHE_NUM + "", search) + "&type=" + ZONGYI_TYPE;
	}
	
	//动漫搜索 search
	public static String getSearch_Dongman_FirstURL(String search) {
//		Log.i(TAG, "getSearch_FirstURL-->" + StatisticsUtils.getSearchURL(SEARCH_URL, 1 + "", FIRST_NUM + "",
//				search));

		return StatisticsUtils.getSearchURL(SEARCH_CAPITAL_URL, 1 + "", FIRST_NUM + "",
				search) + "&type=" + DONGMAN_TYPE;
	}

	public static String getSearch_Dongman_CacheURL(int pageNum, String search) {

		return StatisticsUtils.getSearchURL(SEARCH_CAPITAL_URL, (pageNum + 1) + "",
				CACHE_NUM + "", search) + "&type=" + DONGMAN_TYPE;
	}

	// 动漫filter
	public static String getFilter_DongmanFirstURL(String filterSource) {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "", FIRST_NUM + "",
				DONGMAN_TYPE) + filterSource;
	}

	public static String getFilter_DongmanCacheURL(int pageNum,
			String filterSource) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", DONGMAN_TYPE) + filterSource;
	}

	// 电影filter
	public static String getFilter_DianyingFirstURL(String filterSource) {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "", FIRST_NUM + "",
				MOVIE_TYPE) + filterSource;
	}

	public static String getFilter_DianyingCacheURL(int pageNum,
			String filterSource) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", MOVIE_TYPE) + filterSource;
	}

	// 电视剧filter
	public static String getFilter_DianshijuFirstURL(String filterSource) {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "", FIRST_NUM + "",
				TV_TYPE) + filterSource;
	}

	public static String getFilter_DianshijuCacheURL(int pageNum,
			String filterSource) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", TV_TYPE) + filterSource;
	}

	// 综艺filter
	public static String getFilter_ZongyiFirstURL(String filterSource) {

		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "", FIRST_NUM + "",
				ZONGYI_TYPE) + filterSource;
	}

	public static String getFilter_ZongyiCacheURL(int pageNum,
			String filterSource) {

		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "",
				CACHE_NUM + "", ZONGYI_TYPE) + filterSource;
	}

	public static final int SHOUCANG_NUM = 100;

	// 收藏 取100条数据 为全部类型
	public static String getShoucangURL(String userId) {

		return getUserFavURL(FAV_URL, 1 + "", SHOUCANG_NUM + "", "", userId);
	}
	
	// 历史 取100条数据 为全部类型
	public static String getHistoryURL(String userId) {

		return getUserFavURL(HISTORY_URL, 1 + "", SHOUCANG_NUM + "", "", userId);
	}
	
	//影评
	public static String getYingPin_1_URL(String prod_id) {
		Log.i(TAG, "getYingPin_1_URL--->" + getYingPinURL(YINGPING_URL, 1+ "", 1 + "", prod_id));
		
		return getYingPinURL(YINGPING_URL, 1+ "", 1 + "", prod_id);
	}
	
	public static final String YEAR = "&year=";
	public static final String AREA = "&area=";
	public static final String SUB_TYPE = "&sub_type=";
	public static final String[] PARAMS_3 = { AREA, SUB_TYPE, YEAR };

	public static String getFileterURL3Param(String[] choices,
			String defaultItemName) {

		if (choices.length < 3 && choices.length != PARAMS_3.length) {

			return null;
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < choices.length; i++) {

			if (!choices[i].equals(defaultItemName)) {
				sb.append(PARAMS_3[i]);
				String encode;
				if (i != 2) {

					encode = URLEncoder.encode(choices[i]);
				} else {
					encode = choices[i];
				}
				sb.append(encode);
			}
		}

		return sb.toString();
	}

	public static String getQuanBuFenLeiName(String[] choices,
			String defaultQuanbufenlei, String defaultItemName) {

		if (choices.length < 3) {

			return defaultQuanbufenlei;
		}

		if (choices[0].equals(defaultItemName)
				&& choices[1].equals(defaultItemName)
				&& choices[2].equals(defaultItemName)) {

			return defaultQuanbufenlei;
		} else {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < choices.length; i++) {

				if (!choices[i].equals(defaultItemName)) {
					sb.append(choices[i] + "/");
				}
			}

			sb.deleteCharAt(sb.length() - 1);
			return sb.toString();
		}

	}

	// public static isChineseString

	public static ScaleAnimation getOutScaleAnimation() {

		ScaleAnimation outScaleAnimation = new ScaleAnimation(
				OUT_ANIMATION_FROM_X, OUT_ANIMATION_TO_X, OUT_ANIMATION_FROM_Y,
				OUT_ANIMATION_TO_Y, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);

		outScaleAnimation.setDuration(80);
		outScaleAnimation.setFillAfter(false);

		return outScaleAnimation;
	}

	public static ScaleAnimation getInScaleAnimation() {

		ScaleAnimation inScaleAnimation = new ScaleAnimation(
				IN_ANIMATION_FROM_X, IN_ANIMATION_TO_X, IN_ANIMATION_FROM_Y,
				IN_ANIMATION_TO_Y, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		inScaleAnimation.setDuration(80);
		inScaleAnimation.setFillAfter(false);

		return inScaleAnimation;
	}

	public static TranslateAnimation getTranslateAnimation(View v) {

		TranslateAnimation translateAnimation = new TranslateAnimation(
				v.getX(), v.getX(), v.getY(), v.getY() - 200);
		translateAnimation.setDuration(150);
		translateAnimation.setFillAfter(false);

		return translateAnimation;
	}

	public static String getUserId(Context c) {
		SharedPreferences sharedata = c.getSharedPreferences("userData", 0);
		return sharedata.getString("userId", null);
//		String macAddress = null;
//		WifiManager wifiMgr = (WifiManager) c
//				.getSystemService(Context.WIFI_SERVICE);
//		WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());
//		if (info != null) {
//			macAddress = info.getMacAddress();
//		}
//		return macAddress;
//		return null;
	}

	public static String MD5(String str) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

		char[] charArray = str.toCharArray();
		byte[] byteArray = new byte[charArray.length];

		for (int i = 0; i < charArray.length; i++) {
			byteArray[i] = (byte) charArray[i];
		}
		byte[] md5Bytes = md5.digest(byteArray);

		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++) {
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16) {
				hexValue.append("0");
			}
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();
	}

	public static String formatDuration(long duration) {
		duration = duration / 1000;
		int h = (int) duration / 3600;
		int m = (int) (duration - h * 3600) / 60;
		int s = (int) duration - (h * 3600 + m * 60);
		String durationValue;
//		if (h == 0) {
//			durationValue = String.format("%1$02d:%2$02d", m, s);
//		} else {
			durationValue = String.format("%1$02d:%2$02d:%3$02d", h, m, s);
//		}
		return durationValue;
	}

	public static String formatDuration1(long duration) {
		int h = (int) duration / 3600;
		int m = (int) (duration - h * 3600) / 60;
		int s = (int) duration - (h * 3600 + m * 60);
		String durationValue;
		if (h == 0) {
			durationValue = String.format("%1$02d:%2$02d", m, s);
		} else {
			durationValue = String.format("%1$d:%2$02d:%3$02d", h, m, s);
		}
		return durationValue;
	}

	public static String formatMovieDuration(String duration) {

		if (duration != null && !duration.equals("")) {

			int indexFenZhong = duration.indexOf("分钟");

			if (indexFenZhong != -1) {

				duration = duration.replaceAll("分钟", "");
			}

			int indexFen = duration.indexOf("分");

			if (indexFen != -1) {

				duration = duration.replaceAll("分", "");
			}

			String[] strs = duration.split("：");

			if (strs.length == 1) {

				strs = duration.split(":");
			}

			if (strs.length == 1) {

				return duration + "分钟";
			} else if (strs.length == 2) {

				return strs[0] + "分钟";
			} else if (strs.length == 3) {

				String hourStr = strs[0];
				String minuteStr = strs[1];

				if (hourStr != null && !hourStr.equals("")) {

					int hour = Integer.valueOf(hourStr);

					if (minuteStr != null && !hourStr.equals("")) {

						int minute = Integer.valueOf(minuteStr);

						if (hour != 0) {

							return (hour * 60 + minute) + "分钟";
						} else {

							if (minute != 0) {

								return minute + "分钟";
							}
						}

					}
				} else {

					if (minuteStr != null && !hourStr.equals("")) {

						int minute = Integer.valueOf(minuteStr);

						if (minute != 0) {

							return minute + "分钟";
						}
					}
				}
			}

		}

		return "";

	}

	public static String formateScore(String score) {

		if (score != null && !score.equals("") && !score.equals("0")
				&& !score.equals("-1")) {

			return score;
		}

		return "";
	}

	public static String formateZongyi(String curEpisode, Context context) {

		if (curEpisode != null && !curEpisode.equals("")
				&& !curEpisode.equals("0")) {

			if (isNumber(curEpisode)) {

				if (curEpisode.length() > 2 && curEpisode.length() <= 9) {

					return context.getString(R.string.zongyi_gengxinzhi)
							+ curEpisode;
				}
			}
		}

		return "";
	}

	public static boolean isNumber(String str) {
		java.util.regex.Pattern pattern = java.util.regex.Pattern
				.compile("[0-9]*");
		java.util.regex.Matcher match = pattern.matcher(str);
		if (match.matches() == false) {
			return false;
		} else {
			return true;
		}
	}

	public static void clearList(List list) {

		if (list != null && !list.isEmpty()) {

			list.clear();
		}
	}

	public static final String EMPTY = "EMPTY";

	public static List<MovieItemData> returnFilterMovieSearch_TVJson(String json)
			throws JsonParseException, JsonMappingException, IOException {

		if (json == null || json.equals("")) {

			return new ArrayList<MovieItemData>();
		}
		ObjectMapper mapper = new ObjectMapper();

		ReturnFilterMovieSearch result = mapper.readValue(json.toString(),
				ReturnFilterMovieSearch.class);

		List<MovieItemData> list = new ArrayList<MovieItemData>();

		for (int i = 0; i < result.results.length; i++) {

			MovieItemData movieItemData = new MovieItemData();
			movieItemData.setMovieName(result.results[i].prod_name);
			String bigPicUrl = result.results[i].big_prod_pic_url;
			if (bigPicUrl == null || bigPicUrl.equals("")
					|| bigPicUrl.equals(EMPTY)) {

				bigPicUrl = result.results[i].prod_pic_url;
			}
			movieItemData.setMoviePicUrl(bigPicUrl);
			movieItemData.setMovieScore(result.results[i].score);
			movieItemData.setMovieID(result.results[i].prod_id);
			movieItemData.setMovieDuration(result.results[i].duration);
			movieItemData.setMovieCurEpisode(result.results[i].cur_episode);
			movieItemData.setMovieMaxEpisode(result.results[i].max_episode);
			movieItemData.setMovieProType(result.results[i].prod_type);

			movieItemData.setStars(result.results[i].star);
			movieItemData.setDirectors(result.results[i].director);
			movieItemData.setSummary(result.results[i].prod_sumary);
			movieItemData.setSupport_num(result.results[i].support_num);
			movieItemData.setFavority_num(result.results[i].favority_num);
			movieItemData.setDefinition(result.results[i].definition);
			list.add(movieItemData);
		}

		return list;

	}

	public static List<MovieItemData> returnTopsJson(String json)
			throws JsonParseException, JsonMappingException, IOException {

		if (json == null || json.equals("")) {

			return new ArrayList<MovieItemData>();
		}
		ObjectMapper mapper = new ObjectMapper();

		ReturnTops result = mapper.readValue(json.toString(), ReturnTops.class);

		List<MovieItemData> list = new ArrayList<MovieItemData>();

		for (int i = 0; i < result.tops.length; i++) {
			MovieItemData movieItemData = new MovieItemData();
			movieItemData.setMovieName(result.tops[i].name);
			movieItemData.setMovieID(result.tops[i].id);
			movieItemData.setMovieProType(result.tops[i].prod_type);
			String bigPicUrl = result.tops[i].big_pic_url;
			if (bigPicUrl == null || bigPicUrl.equals("")
					|| bigPicUrl.equals(EMPTY)) {

				bigPicUrl = result.tops[i].pic_url;
			}
			movieItemData.setMoviePicUrl(bigPicUrl);
			movieItemData.setNum(result.tops[i].num);
			movieItemData.setMovieProType(result.tops[i].prod_type);
			// yuedanInfo.content = result.tops[i].content;
			list.add(movieItemData);

		}

		return list;
	}

	public static List<MovieItemData> returnTVBangDanList_YueDanListJson(
			String json) throws JsonParseException, JsonMappingException,
			IOException {

		if (json == null || json.equals("")) {

			return new ArrayList<MovieItemData>();
		}
		ObjectMapper mapper = new ObjectMapper();

		ReturnTVBangDanList result = mapper.readValue(json,
				ReturnTVBangDanList.class);

		List<MovieItemData> list = new ArrayList<MovieItemData>();

		for (int i = 0; i < result.items.length; i++) {

			MovieItemData movieItemData = new MovieItemData();
			movieItemData.setMovieName(result.items[i].prod_name);
			String bigPicUrl = result.items[i].big_prod_pic_url;
			if (bigPicUrl == null || bigPicUrl.equals("")
					|| bigPicUrl.equals(EMPTY)) {

				bigPicUrl = result.items[i].prod_pic_url;
			}
			movieItemData.setMoviePicUrl(bigPicUrl);
			movieItemData.setMovieScore(result.items[i].score);
			movieItemData.setMovieID(result.items[i].prod_id);
			movieItemData.setMovieCurEpisode(result.items[i].cur_episode);
			movieItemData.setMovieMaxEpisode(result.items[i].max_episode);
			movieItemData.setMovieProType(result.items[i].prod_type);

			movieItemData.setStars(result.items[i].stars);
			movieItemData.setDirectors(result.items[i].directors);
			movieItemData.setSupport_num(result.items[i].support_num);
			movieItemData.setFavority_num(result.items[i].favority_num);
			movieItemData.setMovieDuration(result.items[i].duration);
			movieItemData.setDefinition(result.items[i].definition);
			list.add(movieItemData);
		}

		return list;
	}

	// public static List<MovieItemData> returnUserFavoritiesJson(String json)
	// throws JsonParseException, JsonMappingException, IOException {
	//
	// if(json == null || json.equals("")) {
	//
	// return new ArrayList<MovieItemData>();
	// }
	// ObjectMapper mapper = new ObjectMapper();
	//
	// ReturnUserFavorities result = mapper.readValue(json.toString(),
	// ReturnUserFavorities.class);
	// List<MovieItemData> list = new ArrayList<MovieItemData>();
	// for(int i=0; i<result.favorities.length; i++){
	// MovieItemData movieItemData = new MovieItemData();
	// movieItemData.setMovieID(result.favorities[i].content_id);
	// movieItemData.setMovieName(result.favorities[i].content_name);
	// movieItemData.setMovieProType(result.favorities[i].content_type);
	// String bigPicUrl = result.favorities[i].big_content_pic_url;
	// if(bigPicUrl == null || bigPicUrl.equals("")
	// ||bigPicUrl.equals(EMPTY)) {
	//
	// bigPicUrl = result.favorities[i].content_pic_url;
	// }
	// movieItemData.setMoviePicUrl(bigPicUrl);
	// movieItemData.setMovieScore(result.favorities[i].score);
	// list.add(movieItemData);
	// }
	//
	// return list;
	// }

	public static List<HotItemInfo> returnUserFavoritiesJson(String json)
			throws JsonParseException, JsonMappingException, IOException {

		if (json == null || json.equals("")) {

			return new ArrayList<HotItemInfo>();
		}
		ObjectMapper mapper = new ObjectMapper();

		ReturnUserFavorities result = mapper.readValue(json.toString(),
				ReturnUserFavorities.class);
		List<HotItemInfo> list = new ArrayList<HotItemInfo>();
		for (int i = 0; i < result.favorities.length; i++) {
			HotItemInfo item = new HotItemInfo();
			// item.id = result.favorities[i].id;
			item.prod_id = result.favorities[i].content_id;
			item.prod_name = result.favorities[i].content_name;
			item.prod_type = result.favorities[i].content_type;
			// item.prod_pic_url = result.favorities[i].big_content_pic_url;
			String bigPicUrl = result.favorities[i].big_content_pic_url;
			if (bigPicUrl == null || bigPicUrl.equals("")
					|| bigPicUrl.equals(StatisticsUtils.EMPTY)) {

				bigPicUrl = result.favorities[i].content_pic_url;
			}
			item.prod_pic_url = bigPicUrl;
			item.stars = result.favorities[i].stars;
			item.directors = result.favorities[i].directors;
			item.favority_num = result.favorities[i].favority_num;
			item.support_num = result.favorities[i].support_num;
			item.publish_date = result.favorities[i].publish_date;
			item.score = result.favorities[i].score;
			item.area = result.favorities[i].area;
			item.duration = result.favorities[i].duration;
			item.cur_episode = result.favorities[i].cur_episode;
			item.max_episode = result.favorities[i].max_episode;
			list.add(item);
		}

		return list;
	}
	
	//存储历史数据
	public static List<HotItemInfo> returnUserHistoryJson(String json)
			throws JsonParseException, JsonMappingException, IOException {

		if (json == null || json.equals("")) {

			return new ArrayList<HotItemInfo>();
		}
		ObjectMapper mapper = new ObjectMapper();

		ReturnUserPlayHistories result = mapper.readValue(json.toString(),
				ReturnUserPlayHistories.class);
		List<HotItemInfo> list = new ArrayList<HotItemInfo>();
		for (int i = 0; i < result.histories.length; i++) {
			HotItemInfo item = new HotItemInfo();
			item.id = result.histories[i].id;
			item.prod_id = result.histories[i].prod_id;
			item.prod_name = result.histories[i].prod_name;
			item.prod_type = result.histories[i].prod_type;
			String bigPicUrl = result.histories[i].big_prod_pic_url;
			if(bigPicUrl == null || bigPicUrl.equals("")
					||bigPicUrl.equals(StatisticsUtils.EMPTY)) {
				
				bigPicUrl = result.histories[i].prod_pic_url;
			}
			item.prod_pic_url = bigPicUrl;
			item.stars = result.histories[i].stars;
			item.directors = result.histories[i].directors;
			item.favority_num = result.histories[i].favority_num;
			item.support_num = result.histories[i].support_num;
			item.publish_date = result.histories[i].publish_date;
			item.score = result.histories[i].score;
			item.area = result.histories[i].area;
			item.cur_episode = result.histories[i].cur_episode;
			item.max_episode = result.histories[i].max_episode;
			item.definition = result.histories[i].definition;
			item.prod_summary = result.histories[i].prod_summary;
			item.duration = result.histories[i].duration;
			item.video_url = result.histories[i].video_url;
			item.playback_time = result.histories[i].playback_time;
			item.prod_subname = result.histories[i].prod_subname;
			item.play_type = result.histories[i].play_type;
			list.add(item);
		}

		return list;
	}

	public static String getLastBandNotice(String lastTime) {
		// long last = Long.valueOf(lastTime);
		// long time = System.currentTimeMillis()-last;
		// if(time/(24*60*60*1000)>0){
		// int day = (int) (time/(24*60*60*1000));
		// return day+"天前";
		// }else if(time/(60*60*1000)>0){
		// int hour = (int) (time/(60*60*1000));
		// return hour+"小时前";
		// }else if(time/(60*1000)>0){
		// int minute = (int) (time/(60*1000));
		// return minute+"分钟前";
		// }else if(time/1000>0){
		// int second = (int) (time/1000);
		// return second+"秒前";
		// }else{
		// return "1秒前";
		// }
		String re_StrTime = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日\tHH时mm分");
		// 例如：cc_time=1291778220
		long lcc_time = Long.valueOf(lastTime);
		re_StrTime = sdf.format(new Date(lcc_time));
		return re_StrTime;
	}
	
	/**
	 * 
	 * @param networkList
	 * @param dbList 此list中HotItemInfo数据不全
	 * @return
	 */
	public static List<HotItemInfo> sameList4NetWork(List<HotItemInfo> networkList,List<HotItemInfo> dbList) {
		
		List<HotItemInfo> list = new ArrayList<HotItemInfo>();
		
		for(HotItemInfo netWorkInfo : networkList) {
			
			for(HotItemInfo dbInfo : dbList) {
				
				if(netWorkInfo.prod_id.equals(dbInfo.prod_id)) {
					
					list.add(netWorkInfo);
				}
			}
		}
		
		return list;
	}
	
	/**
	 * 
	 * @param networkList
	 * @param dbList 此list中HotItemInfo数据不全
	 * @return
	 */
	public static List<HotItemInfo> sameList4DB(List<HotItemInfo> networkList,List<HotItemInfo> dbList) {
		
		List<HotItemInfo> list = new ArrayList<HotItemInfo>();
		
		for(HotItemInfo netWorkInfo : networkList) {
			
			for(HotItemInfo dbInfo : dbList) {
				
				if(netWorkInfo.prod_id.equals(dbInfo.prod_id)) {
					
					list.add(dbInfo);
				}
			}
		}
		
		return list;
	}
	
	public static List<HotItemInfo> differentList4NetWork(List<HotItemInfo> networkList,List<HotItemInfo> dbList) {
		
		List<HotItemInfo> sameList = sameList4NetWork(networkList, dbList);
		List<HotItemInfo> list = new ArrayList<HotItemInfo>();
		
		for(HotItemInfo netWorkInfo : networkList) {
			
			boolean isSame = false;
			
			for(HotItemInfo sameInfo : sameList) {
				
				if(netWorkInfo.prod_id.equals(sameInfo.prod_id)) {
					
					isSame = true;
				}
			}
			
			if(!isSame){
				
				list.add(netWorkInfo);
			}
		}
		
		return list;
	}
	
	//一进入到能够收藏的界面list排序顺序 收藏集合，将要填充的集合，文字集合（5个），其他集合
	//判断当前位置是为不可见
	public static boolean isPostionEmpty(int position,int shoucangNum) {
		
		//因为position是从零开始 因此需要加1
		position++;
		
		int chu = shoucangNum/5;
		int quyu = shoucangNum%5;
		
		if(quyu != 0) {
			
			int max = (chu + 1) * 5 ;
			int min = shoucangNum;
			
			if(position <= max && position > min) {
				
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isPostionShowText(int position,int shoucangNum) {
		
		int chu = shoucangNum/5;
		
			
		int max = (chu + 1) * 5 ;
		
		if(position == max) {
			
			return true;
		}
		
		return false;
	}
	
	public static boolean isPositionShowQitaTitle(int position,int shoucangNum) {
		
		int chu = shoucangNum/5;
		
		int max = (chu + 1) * 5;
		
		if(position >= max && position < max + 5) {
			
			return true;
		}
		
		return false;
	}
	
	public static int stepToFirstInThisRow(int position) {
		
		int chu = position/5;
		
		return chu *5;
	}
	
	public static List<MovieItemData> getList4DB(Context context,String userId,String type) {
		
		
		List<MovieItemData> list = new ArrayList<MovieItemData>();
		
		String selection = UserShouCang.USER_ID + " = ? and " + UserShouCang.PRO_TYPE + " = ? and "
				+ UserShouCang.IS_UPDATE + " = ?";//通过用户id，找到相应信息
		String[] selectionArgs = {userId,type,DataBaseItems.NEW + ""};
		
		TvDatabaseHelper helper = TvDatabaseHelper.newTvDatabaseHelper(context);
		SQLiteDatabase database = helper.getWritableDatabase();//获取写db
		
		Cursor cursor = database.query(TvDatabaseHelper.ZHUIJU_TABLE_NAME, null, selection, selectionArgs, null, null, null);
		
		Log.i(TAG, "getList4DB----> userId--->>" + userId + " type--->>" + type 
				+ " count--->" + cursor.getCount());
		if(cursor != null && cursor.getCount() > 0) {//数据库有数据
			
			while(cursor.moveToNext()) {
				
				//总共10组数据
				int indexId = cursor.getColumnIndex(UserShouCang.PRO_ID);
				int indexName = cursor.getColumnIndex(UserShouCang.NAME);
				int indexScore = cursor.getColumnIndex(UserShouCang.SCORE);
				int indexType = cursor.getColumnIndex(UserShouCang.PRO_TYPE);
				int indexPicUrl = cursor.getColumnIndex(UserShouCang.PIC_URL);
				int indexDuration = cursor.getColumnIndex(UserShouCang.DURATION);
				int indexCurEpisode = cursor.getColumnIndex(UserShouCang.CUR_EPISODE);
				int indexMaxEpisode = cursor.getColumnIndex(UserShouCang.MAX_EPISODE);
				int indexStars = cursor.getColumnIndex(UserShouCang.STARS);
				int indexDirectors = cursor.getColumnIndex(UserShouCang.DIRECTORS);
				
				if(indexId != -1) {
					
					String pro_id = cursor.getString(indexId);
					String name = cursor.getString(indexName);
					String score = cursor.getString(indexScore);
					String type2 = cursor.getString(indexType);
					String pic_url = cursor.getString(indexPicUrl);
					String duration = cursor.getString(indexDuration);
					String curEpisode = cursor.getString(indexCurEpisode);
					String maxEpisode = cursor.getString(indexMaxEpisode);
					String stars = cursor.getString(indexStars);
					String directors = cursor.getString(indexDirectors);
					
					MovieItemData item = new MovieItemData();
					
					item.setMovieID(pro_id);
					item.setMovieName(name);
					item.setMovieScore(score);
					item.setMovieProType(type2);
					item.setMoviePicUrl(pic_url);
					item.setMovieDuration(duration);
					item.setMovieCurEpisode(curEpisode);
					item.setMovieMaxEpisode(maxEpisode);
					item.setStars(stars);
					item.setDirectors(directors);
					
					list.add(item);
				}
				
			}
		}
		
		cursor.close();
		helper.closeDatabase();
		
		return list;
	}
	
	//收藏48小时后，取消全部置顶状态
	public static void cancelTopState(Context context,String userId) {
		
		String selection = UserShouCang.USER_ID + "=?";//通过用户id，找到相应信息
		String[] selectionArgs = {userId};
		
		TvDatabaseHelper helper = TvDatabaseHelper.newTvDatabaseHelper(context);
		SQLiteDatabase database = helper.getWritableDatabase();//获取写db
		
		ContentValues tempValues = new ContentValues();
		tempValues.put(UserShouCang.IS_UPDATE, DataBaseItems.OLD);
		
		database.update(TvDatabaseHelper.ZHUIJU_TABLE_NAME, tempValues, selection, selectionArgs);
		
		helper.closeDatabase();
	}
	
	//取消收藏，删除prodId影片数据
	public static void deleteData4ProId(Context context,String userId,String proId) {
		
		TvDatabaseHelper helper = TvDatabaseHelper.newTvDatabaseHelper(context);
		SQLiteDatabase database = helper.getWritableDatabase();//获取写db
		
		String deleteSelection = UserShouCang.PRO_ID  + "=? and " + UserShouCang.USER_ID + "=?";
		String[] deleteselectionArgs = {proId,userId};
		database.delete(TvDatabaseHelper.ZHUIJU_TABLE_NAME, deleteSelection, deleteselectionArgs);
		
		helper.closeDatabase();
		
	}
	
	//HotItemInfo 插入数据,置顶状态不开启 Shoucang
	public static void insertHotItemInfo2DB(Context context,HotItemInfo info,String userId,SQLiteDatabase database) {
		
		ContentValues tempContentValues = new ContentValues();
		tempContentValues.put(UserShouCang.USER_ID, userId);
		tempContentValues.put(UserShouCang.PRO_ID, info.prod_id);
		tempContentValues.put(UserShouCang.NAME, info.prod_name);
		tempContentValues.put(UserShouCang.SCORE, info.score);
		tempContentValues.put(UserShouCang.PRO_TYPE, info.prod_type);
		tempContentValues.put(UserShouCang.PIC_URL, info.prod_pic_url);
		tempContentValues.put(UserShouCang.DURATION, info.duration);
		tempContentValues.put(UserShouCang.CUR_EPISODE, info.cur_episode);
		tempContentValues.put(UserShouCang.MAX_EPISODE, info.max_episode);
		tempContentValues.put(UserShouCang.STARS, info.stars);
		tempContentValues.put(UserShouCang.DIRECTORS, info.directors);
		tempContentValues.put(UserShouCang.IS_NEW, DataBaseItems.NEW);
		tempContentValues.put(UserShouCang.IS_UPDATE, DataBaseItems.OLD);
//		tempContentValues.put(UserShouCang.IS_UPDATE, DataBaseItems.NEW);//测试
		
		database.insert(TvDatabaseHelper.ZHUIJU_TABLE_NAME, null, tempContentValues);
	}
	
	//HotItemInfo 插入数据,置顶状态开启 Shoucang
	public static void updateHotItemInfo2DB(Context context,HotItemInfo info,String userId,SQLiteDatabase database) {
		
		ContentValues tempContentValues = new ContentValues();
		tempContentValues.put(UserShouCang.USER_ID, userId);
		tempContentValues.put(UserShouCang.PRO_ID, info.prod_id);
		tempContentValues.put(UserShouCang.NAME, info.prod_name);
		tempContentValues.put(UserShouCang.SCORE, info.score);
		tempContentValues.put(UserShouCang.PRO_TYPE, info.prod_type);
		tempContentValues.put(UserShouCang.PIC_URL, info.prod_pic_url);
		tempContentValues.put(UserShouCang.DURATION, info.duration);
		tempContentValues.put(UserShouCang.CUR_EPISODE, info.cur_episode);
		tempContentValues.put(UserShouCang.MAX_EPISODE, info.max_episode);
		tempContentValues.put(UserShouCang.STARS, info.stars);
		tempContentValues.put(UserShouCang.DIRECTORS, info.directors);
		tempContentValues.put(UserShouCang.IS_NEW, DataBaseItems.NEW);
//		tempContentValues.put(UserShouCang.IS_UPDATE, DataBaseItems.OLD);
		tempContentValues.put(UserShouCang.IS_UPDATE, DataBaseItems.NEW);//测试
		
		String updateSelection = UserShouCang.PRO_ID  + "=? and " + UserShouCang.USER_ID + "=?";
		String[] updateselectionArgs = {info.prod_id,userId};
		
//		database.insert(TvDatabaseHelper.ZHUIJU_TABLE_NAME, null, tempContentValues);
		int updateInt = database.update(TvDatabaseHelper.ZHUIJU_TABLE_NAME, tempContentValues, updateSelection, updateselectionArgs);
	    Log.i(TAG, "info.prod_id--->" + info.prod_id + " updateInt--->" + updateInt);
	}
	
	//HotItemInfo 插入数据 History
	public static void insertHotItemInfo2DB_History(Context context,HotItemInfo info,String userId,SQLiteDatabase database) {
		
		ContentValues tempContentValues = new ContentValues();
		tempContentValues.put(UserHistory.USER_ID, userId);
		tempContentValues.put(UserHistory.PROD_TYPE, info.prod_type);
		tempContentValues.put(UserHistory.PROD_NAME, info.prod_name);
		tempContentValues.put(UserHistory.PROD_SUBNAME, info.prod_subname);
		tempContentValues.put(UserHistory.PRO_ID, info.prod_id);
//		tempContentValues.put(UserHistory.CREATE_DATE, info.);
		tempContentValues.put(UserHistory.PLAY_TYPE, info.play_type);
		tempContentValues.put(UserHistory.PLAYBACK_TIME, info.playback_time);
		tempContentValues.put(UserHistory.VIDEO_URL, info.video_url);
		tempContentValues.put(UserHistory.DURATION, info.duration);
		tempContentValues.put(UserHistory.BOFANG_ID, info.id);
		tempContentValues.put(UserHistory.PROD_PIC_URL, info.prod_pic_url);
//		tempContentValues.put(UserHistory.BIG_PROD_PIC_URL, info);
		tempContentValues.put(UserHistory.DEFINITION, info.definition);
		tempContentValues.put(UserHistory.STARS, info.stars);
		tempContentValues.put(UserHistory.DIRECTORS, info.directors);
		tempContentValues.put(UserHistory.FAVORITY_NUM, info.favority_num);
		tempContentValues.put(UserHistory.SUPPORT_NUM, info.support_num);
		tempContentValues.put(UserHistory.PUBLISH_DATE, info.publish_date);
		tempContentValues.put(UserHistory.SCORE, info.score);
		tempContentValues.put(UserHistory.AREA, info.area);
		tempContentValues.put(UserHistory.MAX_EPISODE, info.max_episode);
		tempContentValues.put(UserHistory.CUR_EPISODE, info.cur_episode);
		tempContentValues.put(UserHistory.IS_NEW, DataBaseItems.NEW);
		
//		tempContentValues.put(UserShouCang.IS_UPDATE, DataBaseItems.NEW);//测试
		
		database.insert(TvDatabaseHelper.HISTORY_TABLE_NAME, null, tempContentValues);
	}
	
	//当前影片是否是置顶影片，并且返回当前更新集数
	public static String getTopPlayerCurEpisode(Context context, String userId,String proId) {
		
		String selection = UserShouCang.USER_ID + "=? and " + UserShouCang.PRO_ID + 
				"=? and " + UserShouCang.IS_UPDATE + "=?";//通过用户id，找到相应信息
		String[] selectionArgs = {userId,proId,DataBaseItems.NEW + ""};
		
		TvDatabaseHelper helper = TvDatabaseHelper.newTvDatabaseHelper(context);
		SQLiteDatabase database = helper.getWritableDatabase();//获取写db
		
		String[] columns = { UserShouCang.CUR_EPISODE };// 返回当前更新集数
		
		Cursor cursor = database.query(TvDatabaseHelper.ZHUIJU_TABLE_NAME, columns, selection, selectionArgs, null, null, null);
		
		if(cursor != null && cursor.getCount() > 0 ) {
			
			while(cursor.moveToNext()) {
				
				int indexCurEpisode = cursor
						.getColumnIndex(UserShouCang.CUR_EPISODE);
				
				if(indexCurEpisode != -1) {
					
					String curEpisode = cursor.getString(indexCurEpisode);
					return curEpisode;
				}
			}

		}
		
		cursor.close();
		helper.close();
		
		return "";
		
	}
	
	//取消当前影片的置顶状态
	public static void cancelAPlayTopState(Context context,String userId,String pro_id) {
		
		TvDatabaseHelper helper = TvDatabaseHelper.newTvDatabaseHelper(context);
		SQLiteDatabase database = helper.getWritableDatabase();//获取写db
		
		String updateSelection = UserShouCang.PRO_ID  + "=? and " + UserShouCang.USER_ID + "=?";
		String[] updateselectionArgs = {pro_id,userId};
		
		ContentValues tempValues = new ContentValues();
		tempValues.put(UserShouCang.IS_UPDATE, DataBaseItems.OLD);
		database.update(TvDatabaseHelper.ZHUIJU_TABLE_NAME,tempValues, updateSelection, updateselectionArgs);
		
		helper.closeDatabase();
	}
	
	//通过proid获取单条历史记录
	public static HotItemInfo getHotItemInfo4DB_History(Context context,String userId,String prod_id) {
		
		TvDatabaseHelper helper = TvDatabaseHelper.newTvDatabaseHelper(context);
		SQLiteDatabase database = helper.getWritableDatabase();//获取写db
		
		String selection = UserHistory.PRO_ID  + "=? and " + UserShouCang.USER_ID + "=?";
		String[] selectionArgs = {prod_id,userId};
		
		String[] columns = { UserHistory.PROD_TYPE,
				UserHistory.PROD_SUBNAME,UserHistory.PLAYBACK_TIME};// 返回当前类型、看到的集数【电影为Empty】,所看到的的时间
		
		Cursor cursor = database.query(TvDatabaseHelper.HISTORY_TABLE_NAME, columns, selection, selectionArgs, null, null, null);
		
		HotItemInfo info = new HotItemInfo();
		
		if(cursor != null && cursor.getCount() > 0) {
			
			Log.i(TAG, "cursor.getCount()--->" + cursor.getCount());
			
			if(cursor.getCount() >= 1) {
				
				while(cursor.moveToNext()) {
					
					int indexType = cursor.getColumnIndex(UserHistory.PROD_TYPE);
					int indexSubName = cursor.getColumnIndex(UserHistory.PROD_SUBNAME);
					int indexPlayBackTime = cursor.getColumnIndex(UserHistory.PLAYBACK_TIME);
					
					if(indexSubName != -1) {
						
						String type = cursor.getString(indexType);
						String subName = cursor.getString(indexSubName);
						String playBackTime = cursor.getString(indexPlayBackTime);
						
						info.prod_type = type;
						info.prod_subname = subName;
						info.playback_time = playBackTime;
						Log.i(TAG, "sub_name---->" + subName);
					}
				}
			}
		}
		
		cursor.close();
		helper.close();
		
		return info;
	}
	
	public static int getHistoryPlayIndex4DB(Context context,String prod_id,String prod_type) {
		
		HotItemInfo info = StatisticsUtils.getHotItemInfo4DB_History(context,
				StatisticsUtils.getCurrentUserId(context), prod_id);
		
		if(info != null){
			
			String type = info.prod_type;
			Log.i(TAG, "type--->" + type);
			if(type != null && type.equals(prod_type)){
				
				String prod_subName = info.prod_subname;
				Log.i(TAG, "prod_subName--->" + prod_subName);
				if(prod_subName != null && !prod_subName.equals("")
						&& !prod_subName.equals("EMPTY")) {
					
					int currentIndex = -1;
					try {
						currentIndex = Integer.valueOf(prod_subName);
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					return currentIndex;
				}
			}
				
		}
		
		return -1;
		
	}
	
	
	public static int tianchongEmptyItem(int shoucangNum) {
		
		int chu = shoucangNum/5;
		int quyu = shoucangNum%5;
		
		if(quyu != 0) {
			
			int max = (chu + 1) * 5 ;
			int min = shoucangNum;
			
			return max-min + 5;
		}
		
		return 5;
	}
	
	public static int string2Int(String str) {
		
		if(str== null || str.equals("")) {
			
			return 0;
		}
		
		try {
			return Integer.valueOf(str.trim());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
	
//	public static String getTitleName(String str) {
//		
//		
//		int index = str.indexOf("第");
//		
//		return str.substring(start)
//	}
	
	public static final String TV_SETTING_XML = "tv_setting_xml";
	
	public static boolean is48TimeClock(Context context) {
		
		SharedPreferences sp = context.getSharedPreferences(TV_SETTING_XML, Context.MODE_PRIVATE);
		
		return sp.getBoolean("is48TimeClock", false);
	}
	
	public static void set48TimeClock(Context context,boolean is48TimeClock) {
		
		SharedPreferences sp = context.getSharedPreferences(TV_SETTING_XML, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putBoolean("is48TimeClock", is48TimeClock);
		editor.commit();
	}
	
	/**
	 * 闹钟保存的id
	 * @param context
	 * @param userId
	 */
	public static void setCurrentUserId(Context context, String userId) {
		
		SharedPreferences sp = context.getSharedPreferences(TV_SETTING_XML, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString("currentUserId", userId);
		editor.commit();
	}
	
	/**
	 * 闹钟绑定的id
	 * @param context
	 * @return
	 */
	public static String getCurrentUserId(Context context) {
		
		SharedPreferences sp = context.getSharedPreferences(TV_SETTING_XML, Context.MODE_PRIVATE);
		
		return sp.getString("currentUserId", "");
	}
	
	public static void setCancelShoucangProId(Context context,String proId) {
		
		SharedPreferences sp = context.getSharedPreferences(TV_SETTING_XML, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString("cancelShoucangProId", proId);
		editor.commit();
	}
	
	public static String getCancelShoucangProId(Context context) {
		
		SharedPreferences sp = context.getSharedPreferences(TV_SETTING_XML, Context.MODE_PRIVATE);

		return sp.getString("cancelShoucangProId", "");
	}
	
	public static List<MovieItemData> getLists4TwoList(List<MovieItemData> list1,List<MovieItemData> list2) {
		
		List<MovieItemData> list = new ArrayList<MovieItemData>();
		
		if(list1 != null) {
			
			list.addAll(list1);
			
			if(list2 != null) {
				
				Log.i(TAG, "getLists4TwoList--> list1-size:" + list1.size() + 
						" list2-size:" + list2.size());
				
				for(MovieItemData movieItemData:list2) {
					
					list.add(movieItemData);
				}
			}
		}
		
		return list;
	}

}
