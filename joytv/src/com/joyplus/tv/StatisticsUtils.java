package com.joyplus.tv;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.Instrumentation;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
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
import com.joyplus.tv.entity.MovieItemData;
import com.joyplus.tv.entity.ReturnFilterMovieSearch;
import com.joyplus.tv.utils.BangDanKey;
import com.joyplus.tv.utils.JieMianConstant;

public class StatisticsUtils implements JieMianConstant,BangDanKey{

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
		
		count ++;
		if(count > 2) {
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
	
	
	public static String getTopItemURL(String url, String top_id ,String page_num , String page_size ) {
		
		return url + "?top_id=" +top_id + "&page_num=" + page_num + "&page_size=" + page_size;
	}
	
	public static String getTopURL(String url, String page_num , String page_size , String topic_type) {
		
		return url + "?page_num=" + page_num + "&page_size=" + page_size + "&topic_type=" + topic_type;
	}
	
	/**
	 * type required 视频的类别，节目类型，1：电影，2：电视剧，3：综艺节目，131：动漫 
	 * @param url
	 * @param page_num
	 * @param page_size
	 * @param type
	 * @return
	 */
	public static String getFilterURL(String url, String page_num , String page_size , String type) {
		
		return url + "?page_num=" + page_num + "&page_size=" + page_size + "&type=" + type;
	}
	
	public static String getSearchURL(String url, String page_num , String page_size , String keyword ) {
		
		return url + "?page_num=" + page_num + "&page_size=" + page_size + "&keyword=" + keyword;
	}
	
	public static String getUserFavURL(String url, String page_num , String page_size ,String vod_type , String userId ) {
		
		return url + "?page_num=" + page_num + "&page_size=" + page_size  +"&vod_type="+ vod_type+  "&userId=" + userId;
	}
	
	public static final int CACHE_NUM = 20;
	public static final int FIRST_NUM = 30;
	
	//TV 全部分类 10
	public static String getTV_Quan10URL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,TV_DIANSHIJU, 1 + "", CACHE_NUM + "");
	}
	
	//TV 全部分类 
	public static String getTV_QuanAllFirstURL() {
		
		return StatisticsUtils.getFilterURL(FILTER_URL, 1+"", (FIRST_NUM- 10)+"", TV_TYPE);
	}
	
	//TV 全部分类
	public static String getTV_QuanAllCacheURL(int pageNum) {
		
		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1)+"", CACHE_NUM+"", TV_TYPE);
	}
	
	//TV 大陆剧 
	public static String getTV_DalujuFirstURL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_DALU_DIANSHI, 1 + "", FIRST_NUM + "");
	}
	
	//TV 大陆剧 
	public static String getTV_DalujuCacheURL(int pageNum) {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_QINZI_DONGMAN, (pageNum + 1) + "", CACHE_NUM + "");
	}
	
	
	//港剧
	public static String getTV_GangjuFirstURL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_GANGJU_DIANSHI, 1 + "", FIRST_NUM + "");
	}
	
	
	//港剧
	public static String getTV_GangjuCacheURL(int pageNum) {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_GANGJU_DIANSHI, (pageNum + 1) + "", CACHE_NUM + "");
	}
	
	
	public static String getTV_TaijuFirstURL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_TAIJU_DIANSHI, 1 + "", FIRST_NUM + "");
	}
	
	
	public static String getTV_TaijuCacheURL(int pageNum) {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_TAIJU_DIANSHI, (pageNum + 1) + "", CACHE_NUM + "");
	}
	
	public static String getTV_HanjuFirstURL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_HANJU_DIANSHI, 1 + "", FIRST_NUM + "");
	}
	
	
	public static String getTV_HanjuCacheURL(int pageNum) {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_HANJU_DIANSHI, (pageNum + 1) + "", CACHE_NUM + "");
	}
	
	public static String getTV_MeijuFirstURL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_OUMEI_DIANSHI, 1 + "", FIRST_NUM + "");
	}
	
	
	public static String getTV_MeijuCacheURL(int pageNum) {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_OUMEI_DIANSHI, (pageNum + 1) + "", CACHE_NUM + "");
	}
	
	public static String getTV_RijuFirstURL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_RIJU_DIANSHI, 1 + "", FIRST_NUM + "");
	}
	
	
	public static String getTV_RijuCacheURL(int pageNum) {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_RIJU_DIANSHI, (pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//Movie

	public static String getMovie_Quan10URL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,TV_DIANYING, 1 + "", CACHE_NUM + "");
	}
	

	public static String getMovie_QuanAllFirstURL() {
		
		return StatisticsUtils.getFilterURL(FILTER_URL, 1+"", (FIRST_NUM- 10)+"", MOVIE_TYPE);
	}
	

	public static String getMovie_QuanAllCacheURL(int pageNum) {
		
		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1)+"", CACHE_NUM+"", MOVIE_TYPE);
	}
	
	
	public static String getMovie_DongzuoFirstURL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_DONGZUO_MOVIE, 1 + "", FIRST_NUM + "");
	}
	

	public static String getMovie_DongzuoCacheURL(int pageNum) {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_DONGZUO_MOVIE, (pageNum + 1) + "", CACHE_NUM + "");
	}
	
	

	public static String getMovie_KehuanFirstURL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_KEHUAN_MOVIE, 1 + "", FIRST_NUM + "");
	}
	
	

	public static String getMovie_KehuanCacheURL(int pageNum) {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_KEHUAN_MOVIE, (pageNum + 1) + "", CACHE_NUM + "");
	}
	
	
	public static String getMovie_LunliFirstURL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_LUNLI_MOVIE, 1 + "", FIRST_NUM + "");
	}
	
	
	public static String getMovie_LunliCacheURL(int pageNum) {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_LUNLI_MOVIE, (pageNum + 1) + "", CACHE_NUM + "");
	}
	
	public static String getMovie_XijuFirstURL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_XIJU_MOVIE, 1 + "", FIRST_NUM + "");
	}
	
	
	public static String getMovie_XijuCacheURL(int pageNum) {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_XIJU_MOVIE, (pageNum + 1) + "", CACHE_NUM + "");
	}
	
	public static String getMovie_AiqingFirstURL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_AIQING_MOVIE, 1 + "", FIRST_NUM + "");
	}
	
	
	public static String getMovie_AiqingCacheURL(int pageNum) {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_AIQING_MOVIE, (pageNum + 1) + "", CACHE_NUM + "");
	}
	
	public static String getMovie_XuanyiFirstURL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_XUANYI_MOVIE, 1 + "", FIRST_NUM + "");
	}
	
	
	public static String getMovie_XuanyiCacheURL(int pageNum) {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_XUANYI_MOVIE, (pageNum + 1) + "", CACHE_NUM + "");
	}
	
	public static String getMovie_KongbuFirstURL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_KONGBU_MOVIE, 1 + "", FIRST_NUM + "");
	}
	
	
	public static String getMovie_KongbuCacheURL(int pageNum) {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_KONGBU_MOVIE, (pageNum + 1) + "", CACHE_NUM + "");
	}
	
	public static String getMovie_DonghuaFirstURL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_DONGHUA_MOVIE, 1 + "", FIRST_NUM + "");
	}
	
	
	public static String getMovie_DonghuaCacheURL(int pageNum) {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_DONGHUA_MOVIE, (pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//动漫
	public static String getDongman_Quan10URL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,TV_DONGMAN, 1 + "", CACHE_NUM + "");
	}
	
	public static String getDongman_QuanAllFirstURL() {
		
		return StatisticsUtils.getFilterURL(FILTER_URL, 1+"", (FIRST_NUM- 10)+"", DONGMAN_TYPE);
	}
	
	public static String getDongman_QuanAllCacheURL(int pageNum) {
		
		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1)+"", CACHE_NUM+"", DONGMAN_TYPE);
	}
	
	public static String getDongman_QinziFirstURL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_QINZI_DONGMAN, 1 + "", FIRST_NUM + "");
	}
	
	public static String getDongman_QinziCacheURL(int pageNum) {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_QINZI_DONGMAN, (pageNum + 1) + "", CACHE_NUM + "");
	}
	
	
	public static String getDongman_RexueFirstURL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_REXUE_DONGMAN, 1 + "", FIRST_NUM + "");
	}
	
	
	public static String getDongman_RexueCacheURL(int pageNum) {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_REXUE_DONGMAN, (pageNum + 1) + "", CACHE_NUM + "");
	}
	
	
	public static String getDongman_HougongFirstURL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_HOUGONG_DONGMAN, 1 + "", FIRST_NUM + "");
	}
	
	
	public static String getDongman_HougongCacheURL(int pageNum) {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_HOUGONG_DONGMAN, (pageNum + 1) + "", CACHE_NUM + "");
	}
	
	public static String getDongman_TuiliFirstURL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_TUILI_DONGMAN, 1 + "", FIRST_NUM + "");
	}
	
	
	public static String getDongman_TuiliCacheURL(int pageNum) {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_TUILI_DONGMAN, (pageNum + 1) + "", CACHE_NUM + "");
	}
	
	public static String getDongman_JizhanFirstURL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_JIZHAN_DONGMAN, 1 + "", FIRST_NUM + "");
	}
	
	
	public static String getDongman_JizhanCacheURL(int pageNum) {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_JIZHAN_DONGMAN, (pageNum + 1) + "", CACHE_NUM + "");
	}
	
	public static String getDongman_GaoxiaoFirstURL() {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_GAOXIAO_DONGMAN, 1 + "", CACHE_NUM + "");
	}
	
	
	public static String getDongman_GaoxiaoCacheURL(int pageNum) {
		
		return StatisticsUtils.getTopItemURL(TOP_ITEM_URL,REBO_GAOXIAO_DONGMAN, (pageNum + 1) + "", CACHE_NUM + "");
	}
	
	//悦单
	
	public static String getYueDan_DianyingFirstURL() {
		
		return StatisticsUtils.getTopURL(TOP_URL, 1 + "", FIRST_NUM + "",MOVIE_TYPE + "");
	}
	
	
	public static String getYueDan_DianyingCacheURL(int pageNum) {
		
		return StatisticsUtils.getTopURL(TOP_URL, (pageNum + 1) + "", CACHE_NUM + "",MOVIE_TYPE + "");
	}
	
	public static String getYueDan_DianshiFirstURL() {
		
		return StatisticsUtils.getTopURL(TOP_URL, 1 + "", FIRST_NUM + "",TV_TYPE + "");
	}
	
	
	public static String getYueDan_DianshiCacheURL(int pageNum) {
		
		return StatisticsUtils.getTopURL(TOP_URL, (pageNum + 1) + "", CACHE_NUM + "",TV_TYPE + "");
	}
	
	public static String getZongyi_QuanAllFirstURL() {
		
		return StatisticsUtils.getFilterURL(FILTER_URL, 1+"", (FIRST_NUM)+"", ZONGYI_TYPE);
	}
	
	public static String getZongyi_QuanAllCacheURL(int pageNum) {
		
		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1)+"", CACHE_NUM+"", ZONGYI_TYPE);
	}
	
	//search
	
	public static String getSearch_FirstURL(String search) {
		
		return StatisticsUtils.getSearchURL(SEARCH_URL,1 + "", FIRST_NUM + "", search);
	}
	
	public static String getSearch_CacheURL(int pageNum,String search) {
		
		return StatisticsUtils.getSearchURL(SEARCH_URL,(pageNum + 1) + "", CACHE_NUM + "", search);
	}
	
	//动漫filter
	public static String getFilter_DongmanFirstURL(String filterSource) {
		
		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "", FIRST_NUM + "",DONGMAN_TYPE) + filterSource;
	}
	
	public static String getFilter_DongmanCacheURL(int pageNum,String filterSource) {
		
		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "", CACHE_NUM + "",DONGMAN_TYPE) + filterSource;
	}
	
	//电影filter
	public static String getFilter_DianyingFirstURL(String filterSource) {
		
		return StatisticsUtils.getFilterURL(FILTER_URL, 1 + "", FIRST_NUM + "",MOVIE_TYPE) + filterSource;
	}
	
	public static String getFilter_DianyingCacheURL(int pageNum,String filterSource) {
		
		return StatisticsUtils.getFilterURL(FILTER_URL, (pageNum + 1) + "", CACHE_NUM + "",MOVIE_TYPE) + filterSource;
	}
	
	
	public static final String YEAR = "&year=";
	public static final String AREA = "&area=";
	public static final String SUB_TYPE = "&sub_type=";
	public static final String[] PARAMS_3 = {AREA , SUB_TYPE,YEAR };
	
	public static String getFileterURL3Param(String[] choices , String defaultItemName) {
		
		if(choices.length <3 && choices.length != PARAMS_3.length) {
			
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<choices.length;i++) {
			
			if(!choices[i].equals(defaultItemName)){
				sb.append(PARAMS_3[i]);
				String encode;
				if(i!= 2) {
					
					encode = URLEncoder.encode(choices[i]);
				} else {
					encode = choices[i];
				}
				sb.append(encode);
			}
		}
		
		
		return sb.toString();
	}
	
	public static String getQuanBuFenLeiName(String[] choices , String defaultQuanbufenlei,String defaultItemName) {
		
		if(choices.length <3) {
			
			return defaultQuanbufenlei;
		}
		
		if(choices[0].equals(defaultItemName) &&
				choices[1].equals(defaultItemName) &&
				choices[2].equals(defaultItemName)) {
			
			return defaultQuanbufenlei;
		} else {
			StringBuilder sb = new StringBuilder();
			for(int i=0;i<choices.length;i++) {
				
				if(!choices[i].equals(defaultItemName)){
					sb.append(choices[i] + "/");
				}
			}
			
			sb.deleteCharAt(sb.length() - 1);
			return sb.toString();
		}
		
	}
	
	public static ScaleAnimation getOutScaleAnimation() {
		
		ScaleAnimation outScaleAnimation = new ScaleAnimation(OUT_ANIMATION_FROM_X,
				OUT_ANIMATION_TO_X, OUT_ANIMATION_FROM_Y, OUT_ANIMATION_TO_Y, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);

		outScaleAnimation.setDuration(80);
		outScaleAnimation.setFillAfter(false);
		
		return outScaleAnimation;
	}
	
	public static ScaleAnimation getInScaleAnimation() {
		
		ScaleAnimation inScaleAnimation = new ScaleAnimation(
				IN_ANIMATION_FROM_X, IN_ANIMATION_TO_X, IN_ANIMATION_FROM_Y, IN_ANIMATION_TO_Y,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		inScaleAnimation.setDuration(80);
		inScaleAnimation.setFillAfter(false);
		
		return inScaleAnimation;
	}
	
	public static TranslateAnimation getTranslateAnimation(View v) {
		
		
		TranslateAnimation translateAnimation = new TranslateAnimation(v.getX(), v.getX(), v.getY(), v.getY() - 200);
		translateAnimation.setDuration(150);
		translateAnimation.setFillAfter(false);
		
		return translateAnimation;
	}
	
	public static String getMacAdd(Context c){
		String macAddress = null;
		WifiManager wifiMgr = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = (null == wifiMgr ? null : wifiMgr
				.getConnectionInfo());
		if (info != null) {
			macAddress = info.getMacAddress();
		}
		return macAddress;
	}
	
	public static String MD5(String str)  
    {  
        MessageDigest md5 = null;  
        try  
        {  
            md5 = MessageDigest.getInstance("MD5"); 
        }catch(Exception e)  
        {  
            e.printStackTrace();  
            return "";  
        }  
          
        char[] charArray = str.toCharArray();  
        byte[] byteArray = new byte[charArray.length];  
          
        for(int i = 0; i < charArray.length; i++)  
        {  
            byteArray[i] = (byte)charArray[i];  
        }  
        byte[] md5Bytes = md5.digest(byteArray);  
          
        StringBuffer hexValue = new StringBuffer();  
        for( int i = 0; i < md5Bytes.length; i++)  
        {  
            int val = ((int)md5Bytes[i])&0xff;  
            if(val < 16)  
            {  
                hexValue.append("0");  
            }  
            hexValue.append(Integer.toHexString(val));  
        }  
        return hexValue.toString();  
    } 
	public static  String formatDuration(long duration) {
		duration = duration / 1000;
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
	
	public static  String formatDuration1(long duration) {
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
		
		int index = duration.indexOf("：");
		
		if(index == -1) {
			
			return duration + "分钟";
		} else {
			
			String str = duration.substring(0, index);
			return str + "分钟";
		}

	}
	
	public  static void clearList(List list) {

		if (list != null && !list.isEmpty()) {

			list.clear();
		}
	}
	
	public static final String EMPTY ="EMPTY";
	
	public static List<MovieItemData> returnFilterMovieSearch_TVJson(String json) throws JsonParseException, JsonMappingException, IOException {
		
		if(json == null || json.equals("")) {
			
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
			if(bigPicUrl == null || bigPicUrl.equals("")
					||bigPicUrl.equals(EMPTY)) {
				
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
	
	public static List<MovieItemData> returnTopsJson(String json) throws JsonParseException, JsonMappingException, IOException {
		
		if(json == null || json.equals("")) {
			
			return new ArrayList<MovieItemData>();
		}
		ObjectMapper mapper = new ObjectMapper();
		
		ReturnTops result = mapper.readValue(json.toString(),
				ReturnTops.class);
		
		List<MovieItemData> list = new ArrayList<MovieItemData>();
		
		for(int i=0; i<result.tops.length; i++){
			MovieItemData movieItemData = new MovieItemData();
			movieItemData.setMovieName(result.tops[i].name);
			movieItemData.setMovieID(result.tops[i].id);
			movieItemData.setMovieProType(result.tops[i].prod_type);
			String bigPicUrl = result.tops[i].big_pic_url;
			if(bigPicUrl == null || bigPicUrl.equals("")
					||bigPicUrl.equals(EMPTY)) {
				
				bigPicUrl = result.tops[i].pic_url;
			}
			movieItemData.setMoviePicUrl(bigPicUrl);
			movieItemData.setNum(result.tops[i].num);
			movieItemData.setMovieProType(result.tops[i].prod_type);
//			yuedanInfo.content = result.tops[i].content;
			list.add(movieItemData);
			
		}
		
		return list;
	}
	
	public static List<MovieItemData> returnTVBangDanList_YueDanListJson(String json) throws JsonParseException, JsonMappingException, IOException {
		
		if(json == null || json.equals("")) {
			
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
			if(bigPicUrl == null || bigPicUrl.equals("")
					||bigPicUrl.equals(EMPTY)) {
				
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
			
			list.add(movieItemData);
		}
		
		return list;
	}
	
	public static List<MovieItemData> returnUserFavoritiesJson(String json) throws JsonParseException, JsonMappingException, IOException {
		
		if(json == null || json.equals("")) {
			
			return new ArrayList<MovieItemData>();
		}
		ObjectMapper mapper = new ObjectMapper();
		
		ReturnUserFavorities result  = mapper.readValue(json.toString(), ReturnUserFavorities.class);
		List<MovieItemData> list = new ArrayList<MovieItemData>();
		for(int i=0; i<result.favorities.length; i++){
			MovieItemData movieItemData = new MovieItemData();
			movieItemData.setMovieID(result.favorities[i].content_id);
			movieItemData.setMovieName(result.favorities[i].content_name);
			movieItemData.setMovieProType(result.favorities[i].content_type);
			String bigPicUrl = result.favorities[i].big_content_pic_url;
			if(bigPicUrl == null || bigPicUrl.equals("")
					||bigPicUrl.equals(EMPTY)) {
				
				bigPicUrl = result.favorities[i].content_pic_url;
			}
			movieItemData.setMoviePicUrl(bigPicUrl);
			movieItemData.setMovieScore(result.favorities[i].score);
			list.add(movieItemData);
		}
		
		return list;
	}
	
}
