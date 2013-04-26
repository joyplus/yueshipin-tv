package com.joyplus.tv;

import com.androidquery.AQuery;
import com.joyplus.tv.ui.UserInfo;
import com.saulpower.fayeclient.FayeService;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingActivity extends Activity implements OnClickListener {
	
	private LinearLayout unbandLayout;
	private TextView aboutLayout,declarationLayout;
	private App app;
	private AQuery aq;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		unbandLayout = (LinearLayout) findViewById(R.id.bandLayout);
		aboutLayout = (TextView) findViewById(R.id.about_layout);
		declarationLayout = (TextView) findViewById(R.id.declaration_layout);
		app = (App) getApplication();
		aq = new AQuery(this);
		unbandLayout.setOnClickListener(this);
		aboutLayout.setOnClickListener(this);
		declarationLayout.setOnClickListener(this);
		Intent service = new Intent(this,FayeService.class);  
        startService(service);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.about_layout:
			Intent intentAbout = new Intent(this,AboutActivity.class);
			startActivity(intentAbout);
			break;
		case R.id.declaration_layout:
			Intent intentDeclaration = new Intent(this,DeclarationActivity.class);
			startActivity(intentDeclaration);
			break;
		case R.id.bandLayout:
			if(!app.getUserInfo().getUserId().equals(app.getUserData("userId"))){
				app.SaveUserData("isBand","0");
				UserInfo currentUserInfo = new UserInfo();
				currentUserInfo.setUserId(app.getUserData("userId"));
				currentUserInfo.setUserName(app.getUserData("userName"));
				currentUserInfo.setUserAvatarUrl(app.getUserData("userAvatarUrl"));
				app.getHeaders().put("user_id", currentUserInfo.getUserId());
				app.setUser(currentUserInfo);
				Intent intent = new Intent(FayeService.ACTION_SEND_UNBAND);
				sendBroadcast(intent);
				updateUser();
			}
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		updateUser();
	}
	
	private void updateUser(){
		if(app.getUserInfo() == null){
			return;
		}
		aq.id(R.id.iv_head_user_icon).image(
				app.getUserInfo().getUserAvatarUrl(), false, true, 0,
				R.drawable.avatar_defult);
		aq.id(R.id.tv_head_user_name).text(app.getUserInfo().getUserName());
		aq.id(R.id.user_avatar).image(
				app.getUserInfo().getUserAvatarUrl(), false, true, 0,
				R.drawable.avatar_defult);
		aq.id(R.id.user_name).text(app.getUserInfo().getUserName());
		if(app.getUserInfo().getUserId().equals(app.getUserData("userId"))){
			aq.id(R.id.user_notice).text("本机用户");
		}else{
			aq.id(R.id.user_notice).text("点击解除绑定");
		}
	}
}
