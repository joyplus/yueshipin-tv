package com.joyplus.tv;

import java.util.Map;

import android.content.Context;
import android.os.Handler;

import com.joyplus.tv.httphelper.LoginHttpHelper;

public class LoginManager{
	
	public static final int MEASSGE_LOGIN_USRNAE_EMPTY = 0;
	public static final int MEASSGE_LOGIN_PASSWD_EMPTY = MEASSGE_LOGIN_USRNAE_EMPTY + 1;
	public static final int MEASSGE_LOGIN_NETWORK_ERROR = MEASSGE_LOGIN_PASSWD_EMPTY + 1;
	public static final int MEASSGE_LOGIN_PASSWD_OR_USRNAME_ERROR = MEASSGE_LOGIN_NETWORK_ERROR + 1;
	public static final int MEASSGE_LOGIN_SUCCESS = MEASSGE_LOGIN_PASSWD_OR_USRNAME_ERROR + 1;
	public static final int MEASSGE_DELAY_SHOW_SUCCESS = MEASSGE_LOGIN_SUCCESS + 1;
	
	private String userName;
	private String passWd;
	private Context mContext;
	private Handler mLHanler;
	private Map<String,String> mHeaders;
	
	public LoginManager(Context context,Handler handler){
		mContext = context;
		mLHanler = handler;
	}
	
	public void setNamePasswd(String name,String passwd,Map<String,String> headers){
		userName = name;
		passWd = passwd; 
		mHeaders = headers;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassWd() {
		return passWd;
	}
	
	public void login(){
		if(mLHanler == null) return;
		if(userName == null || "".equals(userName)) 
			mLHanler.sendEmptyMessage(MEASSGE_LOGIN_USRNAE_EMPTY);
		else if(passWd == null || "".equals(passWd))
			mLHanler.sendEmptyMessage(MEASSGE_LOGIN_PASSWD_EMPTY);
		else {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					String url = Constant.BASE_URL + "account/authWebUser?app_key="+ Constant.APPKEY;
					int loginFlag = LoginHttpHelper.getInstance().
							Login(mContext, mHeaders,userName, passWd, url);
					if(loginFlag == LoginHttpHelper.LOGIN_SUCESS){
						mLHanler.sendEmptyMessage(MEASSGE_LOGIN_SUCCESS);
					}else if(loginFlag == LoginHttpHelper.LOGIN_FAIL){
						mLHanler.sendEmptyMessage(MEASSGE_LOGIN_PASSWD_OR_USRNAME_ERROR);
					}else {
						mLHanler.sendEmptyMessage(MEASSGE_LOGIN_NETWORK_ERROR);
					}
				}
			}).start();
		}
	}
}
