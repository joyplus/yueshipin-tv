package com.joyplus.utils;

import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Instrumentation;
import android.content.Context;
import android.text.format.DateFormat;

import com.joyplus.tv.R;
import com.joyplus.tv.ShowDetailHaoimsActivity.RETURN_VIEW_TYPE;
import com.joyplus.tv.Service.Return.ReturnProgramView;
import com.joyplus.tv.Service.Return.ReturnProgramView.Movie;

public class Utils {

	public static void simulateKey(final int KeyCode) {
		new Thread() {
			public void run() {
				try {
					Instrumentation inst = new Instrumentation();
					// inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_UP,KeyCode));
					inst.sendKeyDownUpSync(KeyCode);
				} catch (Exception e) {
					Log.e("Exception when sendKeyDownUpSync", e.toString());
				}
			}
		}.start();
	}

	/** s**/
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
	
	/** mms**/
	public static String formatDuration(long duration) {
		duration = duration / 1000;
		int h = (int) duration / 3600;
		int m = (int) (duration - h * 3600) / 60;
		int s = (int) duration - (h * 3600 + m * 60);
		String durationValue;
		// if (h == 0) {
		// durationValue = String.format("%1$02d:%2$02d", m, s);
		// } else {
		durationValue = String.format("%1$02d:%2$02d:%3$02d", h, m, s);
		// }
		return durationValue;
	}
	

	/**
	 * @param duration
	 * @return 单位为分钟
	 */
	public static String formatMovieDuration(Context context,String duration) {

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
				return context.getString(R.string.utils_minute, duration);
			} else if (strs.length == 2) {
				return context.getString(R.string.utils_minute, strs[0]);
			} else if (strs.length == 3) {
				String hourStr = strs[0];
				String minuteStr = strs[1];
				if (hourStr != null && !hourStr.equals("")) {
					int hour = Integer.valueOf(hourStr);
					if (minuteStr != null && !hourStr.equals("")) {
						int minute = Integer.valueOf(minuteStr);
						if (hour != 0) {
							return context.getString(R.string.utils_minute, (hour * 60 + minute));
						} else {
							if (minute != 0) {
								return context.getString(R.string.utils_minute, minute);
							}
						}
					}
				} else {
					if (minuteStr != null && !hourStr.equals("")) {
						int minute = Integer.valueOf(minuteStr);
						if (minute != 0) {
							return context.getString(R.string.utils_minute, minute);
						}
					}
				}
			}
		}
		return "";
	}
	
	public static long formateTimeLong(String timeStr) {
		if(timeStr != null && !timeStr.equals("")) {
			Log.i("Yzg", "formateTimeLong--->" + timeStr);
//			int index = timeStr.indexOf("分钟");
			Pattern p = Pattern.compile("\\d+");
			Matcher m=p.matcher(timeStr);
			if(m.find()){
				String tempStr = m.group();
				try {
					return Integer.valueOf(tempStr) * 60 * 1000;
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return 0l;
	}
	
	public static String movieOverTime(Context context,String duration) {
		String minuteStr = formatMovieDuration(context,duration);
		long movieTime = formateTimeLong(minuteStr);
		if(movieTime == 0) {
			return "";
		}
		long currentTime = System.currentTimeMillis();
		long overTime = currentTime + movieTime;
//		String dateFormat = DateFormat.format("hh:mm", overTime).toString();//12小时制
		String dateFormat = DateFormat.format("kk:mm", overTime).toString();//24小时制
		return dateFormat;
	}

	public static String formateScore(String score) {
		if (score != null && !score.equals("") && !score.equals("0")
				&& !score.equals("-1")) {
			return score;
		}
		return "";
	}
	
    public static boolean isUTF_8(byte[] file){
        if (file.length>3 &&file[0] == -17 && 
        		file[1] == -69 && file[2] == -65) 
            return true;
        return false;
    }
    
    public static String getCharset(byte[] subTitle,int length){
    	if(subTitle != null){
    		if(subTitle.length < length){
    			length = subTitle.length;
    		}
    		ByteArrayInputStream in = new ByteArrayInputStream(subTitle);
    		CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
//    		detector.add(new ParsingDetector(false));
    		detector.add(JChardetFacade.getInstance());
    		try {
    			Charset charset = detector.detectCodepage(in, length);
    			return charset!= null ? charset.name() : "";
    		} catch (IllegalArgumentException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
		
		return "";
    }
    
	public static int getStandardValue(Context context,int value){
		float standardDp = context.getResources().getDimension(R.dimen.standard_1_dp);
		return standardDp == 0 ? value:(int)(value * standardDp);
	}
	
	/**
	 * 获取权限
	 * 
	 * @param permission
	 *            权限
	 * @param path
	 *            路径
	 */
	public static void chmod(String permission, String path) {
		try {
			String command = "chmod " + permission + " " + path;
			Runtime runtime = Runtime.getRuntime();
			runtime.exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void convertToMovieData(ReturnProgramView data,RETURN_VIEW_TYPE type){
		switch (type) {
		case SHOW:
			converShowToMovieData(data);
			break;
		case TV:
			converTVToMovieData(data);
		default:
			break;
		}
	}
	
	private static void converTVToMovieData(ReturnProgramView data){
		if(data == null || data.tv == null) return;
		data.movie = new Movie();
		data.movie.name 				= data.tv.name;
		data.movie.summary 				= data.tv.summary;
		data.movie.poster				= data.tv.poster;
		data.movie.like_num				= data.tv.like_num;
		data.movie.watch_num			= data.tv.watch_num;
		data.movie.favority_num 		= data.tv.favority_num;
		data.movie.score				= data.tv.score;
		data.movie.ipad_poster 			= data.tv.ipad_poster;
		data.movie.support_num			= data.tv.support_num;
		data.movie.publish_date 		= data.tv.publish_date;
		data.movie.directors			= data.tv.directors;
		data.movie.stars				= data.tv.stars;
		data.movie.id					= data.tv.id;
		data.movie.area					= data.tv.area;
		data.movie.total_comment_number	= data.tv.total_comment_number;
		data.movie.definition			= data.tv.definition;
//		data.movie.duration				= data.tv
		data.movie.fee					= data.tv.fee;
		data.movie.episodes				= data.tv.episodes;
	}
	
	private static void converShowToMovieData(ReturnProgramView data){
		if(data == null || data.show == null) return;
		data.movie = new Movie();
		data.movie.name 				= data.show.name;
		data.movie.summary 				= data.show.summary;
		data.movie.poster				= data.show.poster;
		data.movie.like_num				= data.show.like_num;
		data.movie.watch_num			= data.show.watch_num;
		data.movie.favority_num 		= data.show.favority_num;
		data.movie.score				= data.show.score;
		data.movie.ipad_poster 			= data.show.ipad_poster;
		data.movie.support_num			= data.show.support_num;
		data.movie.publish_date 		= data.show.publish_date;
		data.movie.directors			= data.show.directors;
		data.movie.stars				= data.show.stars;
		data.movie.id					= data.show.id;
		data.movie.area					= data.show.area;
		data.movie.total_comment_number	= data.show.total_comment_number;
		data.movie.definition			= data.show.definition;
//		data.movie.duration				= data.show
		data.movie.fee					= data.show.fee;
		data.movie.episodes				= data.show.episodes;
	}

}
