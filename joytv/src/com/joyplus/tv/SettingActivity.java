package com.joyplus.tv;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.joyplus.tv.ui.UserInfo;
import com.joyplus.tv.utils.UtilTools;
import com.saulpower.fayeclient.FayeService;
import com.umeng.analytics.MobclickAgent;

public class SettingActivity extends Activity implements OnClickListener {
	
	private static final String TAG = "SettingActivity";
	
	private static final int SHOW_DIALOG_UNBAND = 0;
	
	private LinearLayout unbandLayout;
	private TextView aboutLayout,declarationLayout,faqLayout;
	private TextView versionNameTv;
	private App app;
	private AQuery aq;
	private BroadcastReceiver receiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(Main1.ACTION_USERUPDATE.equals(intent.getAction())){
				updateUser();
			}
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		unbandLayout = (LinearLayout) findViewById(R.id.bandLayout);
		aboutLayout = (TextView) findViewById(R.id.about_layout);
		declarationLayout = (TextView) findViewById(R.id.declaration_layout);
		faqLayout = (TextView) findViewById(R.id.faq_layout);
		versionNameTv = (TextView) findViewById(R.id.tv_version_name);
		app = (App) getApplication();
		aq = new AQuery(this);
		
		ImageView iv = (ImageView) findViewById(R.id.iv_head_logo);
		
		UtilTools.setLogoPic(getApplicationContext(), aq, iv);
		
		if(Constant.isJoyPlus) {
			
			findViewById(R.id.ll_setting_erwei).setVisibility(View.VISIBLE);
			findViewById(R.id.iv_about).setVisibility(View.VISIBLE);
			findViewById(R.id.about_layout).setVisibility(View.VISIBLE);
			findViewById(R.id.tv_filling).setVisibility(View.GONE);
			findViewById(R.id.iv_filling).setVisibility(View.GONE);
		}
		 
		PackageInfo pinfo;
		try {
			pinfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
			versionNameTv.setText(pinfo.versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		unbandLayout.setOnClickListener(this);
		aboutLayout.setOnClickListener(this);
		declarationLayout.setOnClickListener(this);
		faqLayout.setOnClickListener(this);
		IntentFilter filter = new IntentFilter(Main1.ACTION_USERUPDATE);
		registerReceiver(receiver, filter);
	}
	@Override
	public void onClick(View v) {
		Log.i(TAG, "onClick(View v)--->");
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
		case R.id.faq_layout:
			Intent intentFaq = new Intent(this,FAQActivity.class);
			startActivity(intentFaq);
			break;
		case R.id.bandLayout:
			if(!app.getUserInfo().getUserId().equals(app.getUserData("userId"))){

//				unbandUserId();
				showDialog(SHOW_DIALOG_UNBAND);
			}
			break;

		default:
			break;
		}
	}
	
	
	private void unbandUserId() {
		
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
	
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch (id) {
		case SHOW_DIALOG_UNBAND:
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.dialog_unband).
			setNegativeButton(R.string.dialog_unband_yes, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					unbandUserId();
				}
			}).setPositiveButton(R.string.dialog_unband_no, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			}).setCancelable(false);
			
			AlertDialog dialog = builder.show();
			Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
			button.setSelected(true);
			button.requestFocus();
			
			return null;

		default:
			break;
		}
		
		
		return super.onCreateDialog(id);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		MobclickAgent.onResume(this);
		
		updateUser();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		MobclickAgent.onPause(this);
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
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(aq!=null){
			aq.dismiss();
		}
		unregisterReceiver(receiver);
		super.onDestroy();
	}
}
