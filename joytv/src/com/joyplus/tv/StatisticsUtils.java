package com.joyplus.tv;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONObject;

import android.app.Instrumentation;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;

public class StatisticsUtils implements JieMianConstant{

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
}
