package com.joyplus.tv;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.joyplus.tv.httphelper.LoginHttpHelper;
import com.joyplus.tv.ui.WaitingDialog;
import com.joyplus.tv.utils.UtilTools;

public class VIPLoginActivity extends Activity{
	
	public static final int DIALOG_WAITING = 0;
	
	public static final int RESULTCODE_FOR_SETTING = 500;
	public static final int RESULTCODE_FOR_DETAIL = RESULTCODE_FOR_SETTING + 1;
	
	public static final String START_FROM = "start_from";
	public static final String START_FROM_SETTING = "START_FROM_SETTING";
	public static final String START_FROM_DETAIL = "START_FROM_DETAIL";
	public static final String DATA_CURRENT_INDEX = "DATA_CURRENT_INDEX";
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case LoginManager.MEASSGE_LOGIN_USRNAE_EMPTY:
				app.MyToast(VIPLoginActivity.this, getString(R.string.activity_viplogin_toast_usrname_empty));
				break;
			case LoginManager.MEASSGE_LOGIN_PASSWD_EMPTY:
				app.MyToast(VIPLoginActivity.this, getString(R.string.activity_viplogin_toast_passwd_empty));
				break;
			case LoginManager.MEASSGE_LOGIN_NETWORK_ERROR:
				app.MyToast(VIPLoginActivity.this, getString(R.string.activity_viplogin_toast_network_error));
				break;
			case LoginManager.MEASSGE_LOGIN_PASSWD_OR_USRNAME_ERROR:
				app.MyToast(VIPLoginActivity.this, getString(R.string.activity_viplogin_toast_usrname_passwd_error));
				break;
			case LoginManager.MEASSGE_LOGIN_SUCCESS:
				app.MyToast(VIPLoginActivity.this, getString(R.string.activity_viplogin_toast_login_success));
				sendEmptyMessageDelayed(LoginManager.MEASSGE_DELAY_SHOW_SUCCESS, 500);
				break;
			case LoginManager.MEASSGE_DELAY_SHOW_SUCCESS:
				String startFrom = getIntent().getStringExtra(START_FROM);
				if(START_FROM_SETTING.equals(startFrom)){
					setResult(RESULTCODE_FOR_SETTING);
				}else if(START_FROM_DETAIL.equals(startFrom)){
					setResult(RESULTCODE_FOR_DETAIL,getIntent());
				}
				finish();
				break;
			default:
				break;
			}
			removeDialog(DIALOG_WAITING);
		}
	};
	
	private App app;
	private LoginManager mLoginManager;
	private EditText mUserNameEt,mPasswdEt;
	private Button mLoginBt,mCancelBt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_vip_login_dialog);
		
		app = (App) getApplication();
		mLoginManager = new LoginManager(this,mHandler);
		initActivity();
	}
	
	private void initActivity(){
		initView();
		addListener();
	}
	
	private void initView(){
		mUserNameEt = (EditText) findViewById(R.id.et_vip_login_dialog_usrname);
		mPasswdEt = (EditText) findViewById(R.id.et_vip_login_dialog_pw);
		mLoginBt = (Button) findViewById(R.id.bt_vip_login_dialog_login);
		mCancelBt = (Button) findViewById(R.id.bt_vip_login_dialog_cancel);
	}
	
	private void addListener(){
		mLoginBt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDialog(DIALOG_WAITING);
				mLoginManager.setNamePasswd(mUserNameEt.getText().toString(),
						mPasswdEt.getText().toString(),app.getHeaders());
				mLoginManager.login();
			}
		});
		mCancelBt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch (id) {
		case DIALOG_WAITING:
			WaitingDialog dlg = new WaitingDialog(this);
			dlg.show();
			dlg.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					finish();
				}
			});
			dlg.setDialogWindowStyle();
			return dlg;
		default:
			break;
		}
		return super.onCreateDialog(id);
	}
	
	public static boolean isLogin(Context context){
		
		if(!"".equals(UtilTools.getVIP_ID(context))
				&& !"".equals(UtilTools.getVIP_NickName(context))
				&& !"".equals(UtilTools.getVIPPasswd(context))
				&& !"".equals(UtilTools.getVIPUserName(context))){
			return true;
		}
		return false;
	}
	
	public static boolean isUserCounterExist(Context context){
		if(!"".equals(UtilTools.getVIPPasswd(context))
				&& !"".equals(UtilTools.getVIPUserName(context))){
			return true;
		}
		return false;
	}
}
