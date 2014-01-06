package com.joyplus.tv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.joyplus.tv.utils.DBUtils;
import com.joyplus.tv.utils.UtilTools;
import com.joyplus.utils.Log;
import com.joyplus.utils.PackageUtils;
import com.umeng.analytics.MobclickAgent;


public class PlaySohuVideoActivity extends Activity {
	
	private static final String TAG = "PlaySohuVideoActivity";
	private String cid;
	private String sid;
	private String vid;
	private String prod_id;
	private boolean is_favority;
	private String sub_name;
	private int play_backtime;
	
	private BroadcastReceiver receiver1 = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String packageName = intent.getData().getSchemeSpecificPart();
			if("com.sohutv.tv.joyplus_player".equals(packageName)){
				//startPlayer();
				finish();
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Intent it = getIntent();
		cid = it.getStringExtra("cid");
		vid = it.getStringExtra("vid");
		sid = it.getStringExtra("sid");
		prod_id = it.getStringExtra("prod_id");
		sub_name = it.getStringExtra("prod_sub_name");
		is_favority = it.getBooleanExtra("is_favority", false);
		play_backtime = it.getIntExtra("play_backtime", 0)*1000;
		if(play_backtime<=0){
			play_backtime = (int) getPlayTimeFromLocal();
		}
		Log.d(TAG, "cid = "+ cid+ ";vid = "+ vid+ ";sid = " + sid);
		
		IntentFilter filter1 = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		filter1.addDataScheme("package");
		this.registerReceiver(receiver1, filter1);
		startPlayer();
	}
	
	private void startPlayer(){
		if(isBaiduInstalled()){
			Intent intent = new Intent("com.sohutv.tv.action.joyplus");
			Bundle bundle = new Bundle();
			try{
				JSONObject json = new JSONObject();
				json.put("vid", vid);
				json.put("sid", sid);
				json.put("cid", cid);
				json.put("prod_id", prod_id);
//				json.put("playOrder", 3);	
				json.put("position", getPlayTimeFromLocal());
				json.put("is_favority", is_favority);
//				json.put("vcount", 6);
//				json.put("orderType", 0);
//				json.put("title", "屌丝男士第2季");
//				json.put("passport", "");
//				json.put("isFee", false);
//				json.put("definition", 2);
				bundle.putString("videoInfo", json.toString());
				intent.putExtras(bundle);
				startActivity(intent);
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}else{
			AlertDialog.Builder tDialog = new AlertDialog.Builder(this);
			tDialog.setTitle(R.string.sohu_install_title);
			tDialog.setMessage(R.string.sohu_install_notice);
			tDialog.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							retrieveApkFromAssets(PlaySohuVideoActivity.this, "SohuPlayer_1.0.1.apk");
//								finish();
						}
					});

			tDialog.setNegativeButton(
					"取消",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});

			tDialog.show();
		}
	}

	public boolean isBaiduInstalled(){
		return PackageUtils.isInstalled(this, "com.sohutv.tv.joyplus_player", 1001);
	}
	
	public boolean retrieveApkFromAssets(Context context, String fileName) {
		boolean bRet = false;
		File cacheDir = context.getCacheDir();
		String path = cacheDir.getAbsolutePath() + "/temp.apk";
		File file = new File(path);
		try {
			Log.d(TAG, path);
			InputStream is = context.getAssets().open(fileName);
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);

			byte[] temp = new byte[4096];
			int i = 0;
			while ((i = is.read(temp)) > 0) {
				fos.write(temp, 0, i);
			}
			fos.flush();
			fos.close();
			is.close();

			bRet = true;
			Log.d(TAG, "file move done");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		chmod("777", path);

		// install the apk.
		try{
			Intent intent = new Intent(Intent.ACTION_VIEW);
			//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setDataAndType(Uri.parse("file://" + path),
					"application/vnd.android.package-archive");
			Log.d(TAG, "file://" + path);
			context.startActivity(intent);
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			Toast.makeText(context, R.string.sohu_install_error, Toast.LENGTH_LONG).show();
			((Activity)context).finish();
		}
		return bRet;
	}
	
	/**
	 * 获取权限
	 * 
	 * @param permission
	 *            权限
	 * @param path
	 *            路径
	 */
	public static void chmod(String permission, String path) {
		try {
			String command = "chmod " + permission + " " + path;
			Runtime runtime = Runtime.getRuntime();
			runtime.exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		finish();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(receiver1);
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
	private long getPlayTimeFromLocal(){
		String lastTimeStr = DBUtils.getDuartion4HistoryDB(
				getApplicationContext(),
				UtilTools.getCurrentUserId(getApplicationContext()), prod_id,sub_name);
		Log.i(TAG, "DBUtils.getDuartion4HistoryDB-->lastTimeStr:" + lastTimeStr);

		if (lastTimeStr != null && !lastTimeStr.equals("")) {

			try {
				long tempTime = Integer.valueOf(lastTimeStr);
				Log.i(TAG, "DBUtils.getDuartion4HistoryDB-->time:" + tempTime);
				if (tempTime != 0) {

					return tempTime * 1000;
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return 0;
	}
}
