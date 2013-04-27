package com.joyplus.tv;

import com.androidquery.AQuery;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebView;

public class FAQActivity extends Activity {
	private App app;
	private AQuery aq;
	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_faq);
		app = (App) getApplication();
		aq = new AQuery(this);
		webView = (WebView) findViewById(R.id.webView);
//		webView.setBackgroundColor(0);
//		webView.getBackground().setAlpha(0);
		webView.getSettings().setJavaScriptEnabled(false);
		webView.getSettings().setEnableSmoothTransition(true);
		webView.setBackgroundColor(Color.TRANSPARENT);
//		webView.
		webView.loadUrl("http://www.joyplus.tv/faq-tv");
//		webView.loadUrl("http://apitest.yue001.com/joyplus-service/index.php/tv_net_top?app_key=ijoyplus_android_0001bj&page_num=1&page_size=1000");
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(app.getUserInfo()!=null){
			aq.id(R.id.iv_head_user_icon).image(
					app.getUserInfo().getUserAvatarUrl(), false, true, 0,
					R.drawable.avatar_defult);
			aq.id(R.id.tv_head_user_name).text(app.getUserInfo().getUserName());
		}
	}
	
}