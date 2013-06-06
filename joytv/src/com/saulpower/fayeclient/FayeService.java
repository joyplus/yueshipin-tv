package com.saulpower.fayeclient;

import java.net.URI;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;

import com.joyplus.tv.App;
import com.joyplus.tv.Constant;
import com.joyplus.tv.StatisticsUtils;
import com.joyplus.tv.Adapters.CurrentPlayData;
import com.joyplus.tv.Video.VideoPlayerActivity;
import com.joyplus.tv.utils.Log;
import com.saulpower.fayeclient.FayeClient.FayeListener;

public class FayeService extends Service implements FayeListener{

	private static final String TAG = "FayeService";
	
	public static final String ACTION_SEND_UNBAND = "chennel_send_unBand_message";
	public static final String ACTION_M_APPEAR = "erweima_appear";
	public static final String ACTION_M_DISAPPEAR = "erweima_disappear";
	public static final String ACTION_RECIVEACTION_BAND = "chennel_receive_message_band";
	public static final String ACTION_RECIVEACTION_UNBAND = "chennel_receive_message_unband";
	
	private String serverUrl;
	private String channel;
//	private String phoneChannel;
//	private SharedPreferences preferences; 
	private FayeClient myClient;
//	private FayeClient phoneClient;
	private BroadcastReceiver receiver;
	private Handler handler = new Handler();
	private boolean isConnected = false;
	private boolean isErweimaAppear = false;
	private App app;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		serverUrl = Constant.FAYESERVERURL;
		String userid = StatisticsUtils.getUserId(this);
		if(userid !=null){
			channel = Constant.FAYECHANNEL_TV_BASE + StatisticsUtils.MD5(userid);
		}else{
			channel = null;
		}
		
//		preferences = getSharedPreferences("userIdDate",0);
		app = (App) getApplication();
		IntentFilter filter = new IntentFilter(ACTION_SEND_UNBAND);
		filter.addAction(ACTION_M_APPEAR);
		filter.addAction(ACTION_M_DISAPPEAR);
		receiver = new BroadcastReceiver(){
			@Override
	        public void onReceive(Context context, Intent intent) {
	                // TODO Auto-generated method stub
				Log.d(TAG, intent.getAction());
	        	if(ACTION_SEND_UNBAND.equals(intent.getAction())){
//	        		String date = intent.getStringExtra("date");
	        		try {
	        			JSONObject unBandObj = new JSONObject();
						unBandObj.put("tv_channel", channel.replace(Constant.FAYECHANNEL_TV_HEAD, ""));
						unBandObj.put("push_type","33");
						unBandObj.put("user_id", app.getUserData("phoneID"));
						myClient.sendMessage(unBandObj);
						myClient.disconnectFromServer();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}else if(ACTION_M_APPEAR.equals(intent.getAction())){
	        		//二维码出现啦
//	        		if(app.getUserData("isBand")!=null&&"1".equals(app.getUserData("isBand"))){
//	        			Log.d(TAG, "band =  true connect------------"+app.getUserData("isBand"));
	        		isErweimaAppear = true;	
	        		myClient.connectToServer(null);
//	        		}
	        	}else if(ACTION_M_DISAPPEAR.equals(intent.getAction())){
	        		//二维码消失啦
	        		isErweimaAppear = false;
	        		if(app.getUserData("isBand")!=null||"0".equals(app.getUserData("isBand"))){
	        			if(isConnected){
	        				myClient.disconnectFromServer();
	        			}
	        		}
	        	}
	                
	        }
		};
		registerReceiver(receiver, filter);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if(channel!=null){
			URI url = URI.create(serverUrl);
			myClient = new FayeClient(handler, url, channel);
			myClient.setFayeListener(this);
			if(app.getUserData("isBand")!=null&&"1".equals(app.getUserData("isBand"))){
				myClient.connectToServer(null);
			}
		}
		
		return super.onStartCommand(intent, START_STICKY, startId); 
	}





	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void connectedToServer() {
		// TODO Auto-generated method stub
		Log.d(TAG, "server connected----->");
		isConnected = true;
//		if(app.getUserData("isBand")==null||"0".equals(app.getUserData("isBand"))){
//			myClient.disconnectFromServer();
//		}
	}

	@Override
	public void disconnectedFromServer() {
		// TODO Auto-generated method stub
		Log.w(TAG, "server disconnected!----->");
		isConnected = false;
		if((app.getUserData("isBand")!=null&&"1".equals(app.getUserData("isBand")))||isErweimaAppear){
			myClient.connectToServer(null);
		}
	}

	@Override
	public void subscribedToChannel(String subscription) {
		// TODO Auto-generated method stub
		Log.d(TAG, "Channel subscribed success!----->" + subscription);
//		if(subscription.startsWith(Constant.FAYECHANNEL_MOBILE_BASE)){
//			JSONObject bandSuccessObj = new JSONObject();
//			try {
//				bandSuccessObj.put("push_type","32");
//				bandSuccessObj.put("result", "ok");
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//			phoneClient.sendMessage(bandSuccessObj);
//		}
//		handler.postDelayed(new Runnable() {
//			
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				JSONObject obj  = new JSONObject();
//				try {
//					obj.put("date", "hello world");
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				myClient.sendMessage(obj);
//			}
//		}, 500);
	}

	@Override
	public void subscriptionFailedWithError(String error) {
		// TODO Auto-generated method stub
		Log.w(TAG, "Channel subscribed FailedWithError!----->" + error);
	}

	@Override
	public void messageReceived(JSONObject json) {
		// TODO Auto-generated method stub
		Log.d(TAG, "Receive message:" + json.toString());
		try {
			int push_type = Integer.valueOf(json.getString("push_type")) ;
			String phoneID = null;
			switch (push_type) {
			case 31://绑定
				phoneID = json.getString("user_id");
				if(app.getUserData("isBand") == null||"0".equals(app.getUserData("isBand"))){
					Log.d(TAG, "phone id = " + phoneID);
					app.SaveUserData("isBand", "1");
					app.SaveUserData("phoneID", phoneID);
					JSONObject bandSuccessObj = new JSONObject();
					bandSuccessObj.put("tv_channel", channel.replace(Constant.FAYECHANNEL_TV_HEAD, ""));
					bandSuccessObj.put("push_type","32");
					bandSuccessObj.put("user_id", phoneID);
					bandSuccessObj.put("result", "success");
					myClient.sendMessage(bandSuccessObj);
				}else{
					
					String lastPhoneId = app.getUserData("phoneID");
					
					app.SaveUserData("isBand", "1");
					app.SaveUserData("phoneID", phoneID);
					if(!lastPhoneId.equals(phoneID)){
						JSONObject unBandObj = new JSONObject();
						unBandObj.put("tv_channel", channel.replace(Constant.FAYECHANNEL_TV_HEAD, ""));
						unBandObj.put("push_type","33");
						unBandObj.put("user_id", lastPhoneId);
						myClient.sendMessage(unBandObj);
					}
					
					JSONObject bandSuccessObj = new JSONObject();
					bandSuccessObj.put("tv_channel", channel.replace(Constant.FAYECHANNEL_TV_HEAD, ""));
					bandSuccessObj.put("push_type","32");
					bandSuccessObj.put("user_id", phoneID);
					bandSuccessObj.put("result", "success");
					myClient.sendMessage(bandSuccessObj);
				}
				app.SaveUserData("lastTime", System.currentTimeMillis()+"");
				Intent bandIntent = new Intent(ACTION_RECIVEACTION_BAND);
				sendBroadcast(bandIntent);
				break;
//			case 32://取消绑定
//				phoneID = json.getString("user_id");
//				if(!preferences.getBoolean("isBand", false)){
//					return ;//tv 端收到解除绑定 但是自己的状态为未绑定时不处理
//				}
//				if(phoneID.equals(preferences.getString("phoneID", "-1"))){
//					edit.putBoolean("isBand", false);
//					edit.putString("phoneID", "-1");
//					edit.commit();
//				}
//				//notify to UI 
//				JSONObject bandCancleObj = new JSONObject();
//				bandCancleObj.put("result", "ok"); 
//				bandCancleObj.put("push_type","33");
//				myClient.sendMessage(bandCancleObj);
//				break;
			case 33://取消绑定
				phoneID = json.getString("user_id");
				if(app.getUserData("isBand") == null||"0".equals(app.getUserData("isBand"))){
					return ;//tv 端收到解除绑定 但是自己的状态为未绑定时不处理
				}else if(phoneID.equals(app.getUserData("phoneID"))){
					app.SaveUserData("isBand", "0");
					//notify to UI 
					Log.d(TAG, "send broadCast");
					Intent unbandIntent = new Intent(ACTION_RECIVEACTION_UNBAND);
					sendBroadcast(unbandIntent);
					if(!isErweimaAppear){
						myClient.disconnectFromServer();
					}
				}
				break;
			case 411:
			case 41://投影视频
				phoneID = app.getUserData("phoneID");
				if(phoneID ==null){
					return;
				}
				if(app.getUserData("isBand") != null&&"1".equals(app.getUserData("isBand"))&&phoneID.equals(json.get("user_id"))){
					CurrentPlayData playDate = new CurrentPlayData();
					Intent intent = new Intent(this,VideoPlayerActivity.class);
//					intent.putExtra("ID", json.getString("prod_id"));
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					playDate.prod_id = json.getString("prod_id");
					playDate.prod_type = Integer.valueOf(json.getString("prod_type"));
					playDate.prod_name = json.getString("prod_name");
					playDate.prod_url = json.getString("prod_url");
					playDate.prod_src = json.getString("prod_src");
					playDate.prod_time = Math.round(Float.valueOf(json.getString("prod_time"))*1000);
					playDate.prod_qua = Integer.valueOf(json.getString("prod_qua"));
					app.setCurrentPlayData(playDate);
					app.set_ReturnProgramView(null);
					startActivity(intent);
					
					
					JSONObject responseObj = new JSONObject();
					responseObj.put("push_type", "42");
					responseObj.put("user_id", phoneID);
					responseObj.put("tv_channel", channel.replace(Constant.FAYECHANNEL_TV_HEAD, ""));
					responseObj.put("prod_id", playDate.prod_id);
					responseObj.put("prod_url", playDate.prod_url);
					myClient.sendMessage(responseObj);
				}
				break;
			case 403://视频推送后，手机发送播放指令消息
				phoneID = app.getUserData("phoneID");
				if(phoneID ==null){
					return;
				}
				if(app.getUserData("isBand") != null&&"1".equals(app.getUserData("isBand"))&&phoneID.equals(json.get("user_id"))){
					Intent intent = new Intent(Constant.VIDEOPLAYERCMD);
					intent.putExtra("cmd", 403);
					intent.putExtra("content", "");
					intent.putExtra("prod_url", json.getString("prod_url"));
					sendBroadcast(intent);
				}
				break;
			case 405://视频推送后，手机发送暂停指令消息
				phoneID = app.getUserData("phoneID");
				if(phoneID ==null){
					return;
				}
				if(app.getUserData("isBand") != null&&"1".equals(app.getUserData("isBand"))&&phoneID.equals(json.get("user_id"))){
					Intent intent = new Intent(Constant.VIDEOPLAYERCMD);
					intent.putExtra("cmd", 405);
					intent.putExtra("content", "");
					intent.putExtra("prod_url", json.getString("prod_url"));
					sendBroadcast(intent);
				}
				break;
			case 407://视频推送后，手机发送快进、快退指令消息
				phoneID = app.getUserData("phoneID");
				if(phoneID ==null){
					return;
				}
				if(app.getUserData("isBand") != null&&"1".equals(app.getUserData("isBand"))&&phoneID.equals(json.get("user_id"))){
					Intent intent = new Intent(Constant.VIDEOPLAYERCMD);
					intent.putExtra("cmd", 407);
//					intent.putExtra("content", json.getString("prod_time"));
					intent.putExtra("content", Math.round(Float.valueOf(json.getString("prod_time"))*1000) + "");
					intent.putExtra("prod_url", json.getString("prod_url"));
					sendBroadcast(intent);
				}
				break;
			case 409://退出指令消息
				phoneID = app.getUserData("phoneID");
				if(phoneID ==null){
					return;
				}
				if(app.getUserData("isBand") != null&&"1".equals(app.getUserData("isBand"))&&phoneID.equals(json.get("user_id"))){
					Intent intent = new Intent(Constant.VIDEOPLAYERCMD);
					intent.putExtra("cmd", 409);
					intent.putExtra("content", "");
					intent.putExtra("prod_url", json.getString("prod_url"));
					sendBroadcast(intent);
				}
				break;
			default:
				break;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
