package com.joyplus.tv;

import java.util.HashMap;
import java.util.Map;
import java.util.zip.Inflater;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.joyplus.tv.utils.UtilTools;
import com.joyplus.utils.Log;
import com.umeng.analytics.MobclickAgent;

public class FAQActivity extends Activity {
	
	private static final String TAG = "FAQActivity";
	
	private App app;
	private AQuery aq;
	private LinearLayout layout_content;
//	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_faq);
		app = (App) getApplication();
		aq = new AQuery(this);
		
		ImageView iv = (ImageView) findViewById(R.id.iv_head_logo);
		
		UtilTools.setLogoPic(getApplicationContext(), aq, iv);
		layout_content = (LinearLayout) findViewById(R.id.layout_content);
//		webView = (WebView) findViewById(R.id.webView);
////		webView.setBackgroundColor(0);
////		webView.getBackground().setAlpha(0);
//		webView.getSettings().setJavaScriptEnabled(false);
//		webView.getSettings().setEnableSmoothTransition(true);
//		webView.setBackgroundColor(Color.TRANSPARENT);
//		webView.setWebViewClient(new WebViewClient()
//		   {
//		          @Override
//		          public boolean shouldOverrideUrlLoading(WebView view, String url)
//		          {
//		 
//		            view.loadUrl(url); // 在当前的webview中跳转到新的url
//		 
//		            return true;
//		          }
//		    });
////		webView.
//		webView.loadUrl("http://www.joyplus.tv/faq-tv?"+System.currentTimeMillis());
		
		getFAQData();
//		webView.loadUrl("http://apitest.yue001.com/joyplus-service/index.php/tv_net_top?app_key=ijoyplus_android_0001bj&page_num=1&page_size=1000");
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		MobclickAgent.onResume(this);
		
		if(app.getUserInfo()!=null){
			aq.id(R.id.iv_head_user_icon).image(
					app.getUserInfo().getUserAvatarUrl(), false, true, 0,
					R.drawable.avatar_defult);
			if(VIPLoginActivity.isLogin(this))
				aq.id(R.id.tv_head_user_name).text(UtilTools.getVIP_NickName(this));
			else
				aq.id(R.id.tv_head_user_name).text(app.getUserInfo().getUserName());
		}
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		MobclickAgent.onPause(this);
	}
	
	
	public void getFAQData() {
		String url = Constant.PARSE_URL_BASE_URL + "questions/yueshipin";
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
		cb.url(url).type(JSONObject.class).weakHandler(this, "initFAQ");
		Map<String, String> header = new HashMap<String, String>();
		header.put("app_key", "ijoyplus_android_0001bj");
		cb.SetHeader(header);
		aq.ajax(cb);
	}
	
	public void initFAQ(String url, JSONObject json, AjaxStatus status) {
		if (status.getCode() == AjaxStatus.NETWORK_ERROR) {
			// aq.id(R.id.ProgressText).invisible();
			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			handleHelperDateError();
			return;
		}else{
			try{
				Log.d(TAG, "qustion---->" + json.toString());
				JSONArray array = json.getJSONArray("results");
				for(int i=0; i<array.length(); i++){
					JSONObject obj_item = array.getJSONObject(i);
					View v = LayoutInflater.from(this).inflate(R.layout.item_faq_list,null);
					TextView qustion = (TextView) v.findViewById(R.id.text_qustion);
					TextView answer = (TextView) v.findViewById(R.id.text_answer);
					qustion.setText(obj_item.getString("question"));
					answer.setText("\t\t"+obj_item.getString("answer"));
					layout_content.addView(v);
				}
			}catch (Exception e) {
				// TODO: handle exception
				handleHelperDateError();
				e.printStackTrace();
			}
			
		}
	}
	
	private void handleHelperDateError(){
		//获取帮助信息失败或者获取数据有误
		TextView tv = new TextView(this);
		tv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		tv.setGravity(Gravity.CENTER);
		tv.setText("获取帮助界面失败");
		layout_content.addView(tv);
	}
}
