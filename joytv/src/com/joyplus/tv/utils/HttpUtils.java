package com.joyplus.tv.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.net.http.AndroidHttpClient;

import com.joyplus.tv.Constant;

public class HttpUtils {
	
	private static final String TAG = "HttpUtils";
	
	public static void urlRedirect(String urlStr,List<String> list) {		
		// 模拟火狐ios发用请求 使用userAgent
		AndroidHttpClient mAndroidHttpClient = AndroidHttpClient
				.newInstance(Constant.USER_AGENT_IOS);
		HttpParams httpParams = mAndroidHttpClient.getParams();
		// 连接时间最长5秒，可以更改
		HttpConnectionParams.setConnectionTimeout(httpParams, 5000 * 1);		
		URL url;
		try {
			url = new URL(urlStr);
			HttpGet mHttpGet = new HttpGet(url.toURI());
			HttpResponse response = mAndroidHttpClient.execute(mHttpGet);
			StatusLine statusLine = response.getStatusLine();			
			int status = statusLine.getStatusCode();
			Log.i(TAG, "HTTP STATUS : " + status);			
			if (status == HttpStatus.SC_OK) {
				Log.i(TAG, "HttpStatus.SC_OK--->" + urlStr);
				// 正确的话直接返回，不进行下面的步骤
				mAndroidHttpClient.close();
				list.add(urlStr);				
				return;//后面不执行
			} else {				
				Log.i(TAG, "NOT HttpStatus.SC_OK--->" + urlStr);				
				if (status == HttpStatus.SC_MOVED_PERMANENTLY || // 网址被永久移除
						status == HttpStatus.SC_MOVED_TEMPORARILY || // 网址暂时性移除
						status == HttpStatus.SC_SEE_OTHER || // 重新定位资源
						status == HttpStatus.SC_TEMPORARY_REDIRECT) {// 暂时定向					
					Header header = response.getFirstHeader("Location");// 拿到重新定位后的header					
					if(header != null) {						
						String location = header.getValue();// 从header重新取出信息
						Log.i(TAG, "Location: " + location);
						if(location != null && !location.equals("")) {							
							urlRedirect(location, list);							
							mAndroidHttpClient.close();// 关闭此次连接
							return;//后面不执行
						}
					}					
					list.add(null);
					mAndroidHttpClient.close();					
					return;
				} else {//地址真的不存在					
					mAndroidHttpClient.close();
					list.add(null);					
					return;//后面不执行
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block 
			e.printStackTrace();
		}	
	}
	
	public static String getRedirectUrl(String currentPlayUrl){
		String urlStr = null;		
		List<String> list = new ArrayList<String>();		
		try {
			HttpUtils.urlRedirect(currentPlayUrl,list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//超时异常
		}
		if(list.size() > 0) {
			 urlStr = list.get(list.size() -1);
		}
		return urlStr;
	}

}
