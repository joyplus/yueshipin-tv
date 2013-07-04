package com.joyplus.tv;

import com.joyplus.adkey.Ad;
import com.joyplus.adkey.AdListener;
import com.joyplus.adkey.AdManager;
import com.joyplus.adkey.widget.Log;
import com.joyplus.tv.ui.WaitingDialog;
import com.joyplus.tv.utils.UtilTools;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

public class Main extends Activity implements AdListener{
	
	private static final String TAG = "AD_LOGO";
	
	private static final int DIALOG_NETWORK_ERROR = 1;
	
	private AdManager mManager;
	private String publisherId = "53f2f418bfc3759e34e4294ae7b4ebb3";//要显示广告的publisherId
	private boolean cacheMode = true;//该广告加载时是否用本地缓存
	private RelativeLayout starting;
	
	private App app;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 不显示标题
		setContentView(R.layout.logo);// 显示welcom.xml
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN); // 设置全屏显示
		
		app = (App) getApplicationContext();
		
		if (!app.isNetworkAvailable()) {// 如果没有网络，弹出提示dialog
			
			showDialog(DIALOG_NETWORK_ERROR);
			
			return;
		}
		
		MobclickAgent.onError(this);
		
		if(Constant.isJoyPlus) {//如果过是JoyPlus自身应用
			
			UtilTools.setIsJoyPlusApp(getApplicationContext(), true);
		} else {
			
			UtilTools.setIsJoyPlusApp(getApplicationContext(), false);
		}
		
		MobclickAgent.updateOnlineConfig(this);
		
//		String onLineIsShowAd = MobclickAgent.getConfigParams(this, "TV_SHOW_AD");
		String onLineIsShowAd = true + "";//测试数据
		Log.i(TAG, "onLineIsShowAd--->" + onLineIsShowAd);
		if(onLineIsShowAd != null && onLineIsShowAd.equals("true")) {
			
			UtilTools.setIsShowAd(getApplicationContext(), true);
			
			
			mManager = new AdManager(this,publisherId,cacheMode);
			mManager.setListener(this);
			mManager.requestAd();
			starting = (RelativeLayout)findViewById(R.id.starting);
		} else {
			
			UtilTools.setIsShowAd(getApplicationContext(), false);
			//如果不显示广告,直接跳过这个界面
			final Intent intent = new Intent(Main.this, Main1.class);// AndroidMainScreen为主界面
			startActivity(intent);
			Main.this.finish();
		}
		
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch (id) {
		case DIALOG_NETWORK_ERROR:
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
					.setTitle(getString(R.string.toast_no_network))
					.setPositiveButton(getString(R.string.toast_setting_wifi),
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									// wifi设置
									try {
										startActivity(new Intent(
												Settings.ACTION_SETTINGS));
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										
										app.MyToast(getApplicationContext(), "自行进入系统网络设置界面");
									}

								}
							})
					.setNegativeButton(getString(R.string.toast_no_exit),
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									// 退出应用
									finish();
								}
							});
			AlertDialog dialog = builder.show();
			dialog.setCancelable(false);
			Button btn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			btn.setFocusable(true);
			btn.setSelected(true);
			btn.requestFocus();
			return null;
		default:
			return super.onCreateDialog(id);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		
		MobclickAgent.onResume(this);
		
		if(mManager!=null){
			if(!mManager.isCacheLoaded()){
				starting.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mManager!=null)
			mManager.release();
	}

	@Override
	public void adClicked()
	{
		// TODO Auto-generated method stub
//		Toast.makeText(Logo.this, "广告点击事件", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void adClosed(Ad ad, boolean completed)
	{
		// TODO Auto-generated method stub
//		Toast.makeText(Logo.this, "关闭了", Toast.LENGTH_SHORT).show();
		final Intent intent = new Intent(Main.this, Main1.class);// AndroidMainScreen为主界面
		startActivity(intent);
		Main.this.finish();
	}

	@Override
	public void adLoadSucceeded(Ad ad)
	{
		// TODO Auto-generated method stub
		if (mManager != null && mManager.isAdLoaded())
			mManager.showAd();
	}

	@Override
	public void adShown(Ad ad, boolean succeeded)
	{
		// TODO Auto-generated method stub
//		Toast.makeText(Logo.this, "广告显示事件", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void noAdFound()
	{
		// TODO Auto-generated method stub
//		Toast.makeText(Logo.this, "No ad found!", Toast.LENGTH_LONG).show();
		
		Intent intent = new Intent(Main.this, Main1.class);// AndroidMainScreen为主界面
		startActivity(intent);
		finish();
	}
	
	// 返回键
		@Override
		public boolean dispatchKeyEvent(KeyEvent event) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				if (event.getAction() == KeyEvent.ACTION_DOWN
						&& event.getRepeatCount() == 0) {
					if(mManager!=null)
						mManager.release();
				}
			}
			return super.dispatchKeyEvent(event);
		}
}