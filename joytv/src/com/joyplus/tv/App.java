package com.joyplus.tv;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;

import android.util.Log;
import android.view.Gravity;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.util.AQUtility;
import com.joyplus.tv.Adapters.CurrentPlayData;
import com.joyplus.tv.Service.Return.ReturnProgramView;
import com.parse.Parse;

@SuppressLint("DefaultLocale")
public class App extends Application {
	private final String TAG = "App";

	private static App instance;
	public String UserID;
	private Map<String, String> headers;
	private CurrentPlayData mCurrentPlayData;
	private ReturnProgramView m_ReturnProgramView = null;

	public ReturnProgramView get_ReturnProgramView() {
		return m_ReturnProgramView;
	}

	public void set_ReturnProgramView(ReturnProgramView m_ReturnProgramView) {
		this.m_ReturnProgramView = m_ReturnProgramView;
	}

	public CurrentPlayData getCurrentPlayData() {
		return mCurrentPlayData;
	}

	public void setCurrentPlayData(CurrentPlayData mCurrentPlayData) {
		this.mCurrentPlayData = mCurrentPlayData;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		File cacheDir = new File(Constant.PATH);
		AQUtility.setCacheDir(cacheDir);
		Parse.initialize(this, Constant.Parse_AppId, Constant.Parse_ClientKey);
		instance = this;
	}

	/**
	 * Called when the overall system is running low on memory
	 */
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		BitmapAjaxCallback.clearCache();
		Log.w(TAG, "System is running low on memory");
	}

	/**
	 * @return the main context of the App
	 */
	public static Context getAppContext() {
		return instance;
	}

	/**
	 * @return the main resources from the App
	 */
	public static Resources getAppResources() {
		return instance.getResources();
	}

	/**
	 * 只是简单文本判断
	 * 
	 * @param Url
	 * @return
	 */
	public boolean IfSupportFormat(String Url) {
		// URLUtil里面可以检测网址格式是否有效

		return URLUtil.isNetworkUrl(Url);

	}

	public boolean isNetworkAvailable() {
		Context context = getApplicationContext();
		ConnectivityManager connect = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connect == null) {
			return false;
		} else// get all network info
		{
			NetworkInfo[] info = connect.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}


	public boolean IfIncludeM3U(String Url) {
		for (int i = 0; i < Constant.video_dont_support_extensions.length; i++) {

			if (Url.trim().toLowerCase()
					.contains(Constant.video_dont_support_extensions[i])) {
				return true;
			}
		}
		return false;

	}

	public void SaveServiceData(String where, String Data) {
		SharedPreferences.Editor sharedatab = getSharedPreferences(
				"ServiceData", 0).edit();
		sharedatab.putString(where, Data);
		sharedatab.commit();
	}

	public void DeleteServiceData(String where) {
		SharedPreferences.Editor sharedatab = getSharedPreferences(
				"ServiceData", 0).edit();
		sharedatab.remove(where);
		sharedatab.commit();
	}

	public String GetServiceData(String where) {
		SharedPreferences sharedata = getSharedPreferences("ServiceData", 0);
		return sharedata.getString(where, null);
	}

	public void SavePlayData(String where, String Data) {
		String m_data = GetPlayData("order");
		String m_rep = where + "|";
		// 重复了就不允许添加，只更新
		if (m_data != null) {
			if (m_data.indexOf(m_rep) != -1)// 重复了,只更新
				m_data = m_data.replace(m_rep, "");
			m_data = m_rep + m_data.trim();// 更新到最前面
		} else
			m_data = m_rep;
		SharedPreferences.Editor sharedatab = getSharedPreferences("PlayData",
				0).edit();
		sharedatab.putString("order", m_data);
		sharedatab.putString(where, Data);
		sharedatab.commit();

	}

	public void DeletePlayData(String where) {
		String m_data = GetPlayData("order");
		String m_rep = where + "|";
		if (m_data != null) {
			m_data = m_data.replace(m_rep, "");
		}
		SharedPreferences.Editor sharedatab = getSharedPreferences("PlayData",
				0).edit();
		sharedatab.putString("order", m_data.trim());
		sharedatab.remove(where);
		sharedatab.commit();
	}

	public String GetPlayData(String where) {
		SharedPreferences sharedata = getSharedPreferences("PlayData", 0);
		return sharedata.getString(where, null);
	}

	public void MyToast(Context context, CharSequence text) {
		Toast m_toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		m_toast.setGravity(Gravity.CENTER, m_toast.getXOffset() / 2,
				m_toast.getYOffset() / 2);
		m_toast.show();
	}

	/**
	 * 检查urlLink文本是否正常
	 * 
	 * @param urlLink
	 * @return
	 */
	private boolean CheckUrl(String srcUrl) {

		// url本身不正常 直接返回
		if (srcUrl == null || srcUrl.length() <= 0) {

			return false;
		} else {

			if (!URLUtil.isValidUrl(srcUrl)) {

				return false;
			}
		}
//		// 模拟火狐ios发用请求 使用userAgent
//		AndroidHttpClient mAndroidHttpClient = AndroidHttpClient
//				.newInstance(Constant.USER_AGENT_IOS);
//
//		HttpParams httpParams = mAndroidHttpClient.getParams();
//		// 连接时间最长5秒，可以更改
//		HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
//
//		try {
//			URL url = new URL(srcUrl);
//			HttpGet mHttpGet = new HttpGet(url.toURI());
//			HttpResponse response = mAndroidHttpClient.execute(mHttpGet);
//
//			// 限定连接时间
//
//			StatusLine statusLine = response.getStatusLine();
//			int status = statusLine.getStatusCode();
//
//			Header headertop = response.getFirstHeader("Content-Type");// 拿到重新定位后的header
//			String type = headertop.getValue().toLowerCase();// 从header重新取出信息
//			Header header_length = response.getFirstHeader("Content-Length");
//			String lengthStr = header_length.getValue();
//			int length = 0;
//			try {
//				length = Integer.parseInt(lengthStr);
//			} finally {
//			}
//			
//			if (BuildConfig.DEBUG)
//				Log.i(TAG, "HTTP STATUS : " + status);
//
//			// 如果资源来源为风行，那就对url进行重定向 如果不是就只是简单判断
//			// 风行资源id 为 1
//			// 如果拿到资源直接返回url 如果没有拿到资源，并且要进行跳转,那就使用递归跳转
//			if(!type.startsWith("text/html") && status >= 200 && status <= 299
//					&& length > 100){
//				// 正确的话直接返回，不进行下面的步骤
//				mAndroidHttpClient.close();
//				list.add(srcUrl);
//			}else if (status > 299 && status < 400) {
//				if (BuildConfig.DEBUG)
//					Log.i(TAG, "NOT OK   start");
//
//				// if(sourceId != null && sourceId.equals(FENGXING)) {
//
//				if (BuildConfig.DEBUG)
//					Log.i(TAG, "NOT OK start");
//				if (status == HttpStatus.SC_MOVED_PERMANENTLY || // 网址被永久移除
//						status == HttpStatus.SC_MOVED_TEMPORARILY || // 网址暂时性移除
//						status == HttpStatus.SC_SEE_OTHER || // 重新定位资源
//						status == HttpStatus.SC_TEMPORARY_REDIRECT) {// 暂时定向
//
//					Header header = response.getFirstHeader("Location");// 拿到重新定位后的header
//					String location = header.getValue();// 从header重新取出信息
//					list.add(location);
//
//					mAndroidHttpClient.close();// 关闭此次连接
//
//					if (BuildConfig.DEBUG)
//						Log.i(TAG, "Location: " + location);
//					// 进行下一次递归
//					simulateFirfoxRequest(userAgent, new String[] { location,
//							FENGXING }, list);
//				} else {
//
//					// 如果地址真的不存在，那就往里面加NULL字符串
//					mAndroidHttpClient.close();
//					list.add(NOT_VALID_LINK);
//				}
//				// } else {
//				//
//				// //如果地址真的不存在，那就往里面加NULL字符串
//				// mAndroidHttpClient.close();
//				// list.add(NOT_VALID_LINK);
//				// }
//			}
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			if (BuildConfig.DEBUG)
//				Log.i(TAG, "NOT OK" + e);
//			// 如果地址真的不存在，那就往里面加NULL字符串
//			mAndroidHttpClient.close();
//			e.printStackTrace();
//		}
		return true;
	}

}
