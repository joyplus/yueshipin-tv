package com.joyplus.tv;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.joyplus.tv.Adapters.CurrentPlayData;
import com.joyplus.tv.Video.VideoPlayerActivity;
import com.umeng.analytics.MobclickAgent;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Main extends FragmentActivity {
	private String TAG = "Main";
	private App app;
	private AQuery aq;
	private Map<String, String> headers;
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		app = (App) getApplicationContext();
		aq = new AQuery(this);
		
		headers = new HashMap<String, String>();
		headers.put("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
		PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			headers.put("version", pInfo.versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		headers.put("app_key", Constant.APPKEY);
		headers.put("client","android");
		app.setHeaders(headers);
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		if(!Constant.TestEnv)
			ReadLocalAppKey();

		CheckLogin();
	}
	@Override
	protected void onDestroy() {
		if (aq != null)
			aq.dismiss();
		super.onDestroy();
	}

	public void ReadLocalAppKey() {
		// online 获取APPKEY
		MobclickAgent.updateOnlineConfig(this);
		String OnLine_Appkey = MobclickAgent.getConfigParams(this, "APPKEY");
		if (OnLine_Appkey != null && OnLine_Appkey.length() >0) {
			Constant.APPKEY = OnLine_Appkey;
			headers.remove("app_key");
			headers.put("app_key", OnLine_Appkey);
			app.setHeaders(headers);
		}
	}
	public boolean CheckLogin() {
		String UserInfo = null;
		UserInfo = app.GetServiceData("UserInfo");
		if (UserInfo == null) {
			// 1. 在客户端生成一个唯一的UUID
			String macAddress = null;
			WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = (null == wifiMgr ? null : wifiMgr
					.getConnectionInfo());
			if (info != null) {
				macAddress = info.getMacAddress();
				// 2. 通过调用 service account/generateUIID把UUID传递到服务器
				String url = Constant.BASE_URL + "account/generateUIID";

				Map<String, Object> params = new HashMap<String, Object>();
				params.put("uiid", macAddress);
				params.put("device_type", "Android");

				AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
				cb.header("User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
				cb.header("app_key", Constant.APPKEY);

				cb.params(params).url(url).type(JSONObject.class)
						.weakHandler(this, "CallServiceResult");
				aq.ajax(cb);
			}
		} else {
			JSONObject json;
			try {
				json = new JSONObject(UserInfo);
				app.UserID = json.getString("user_id").trim();
				headers.put("user_id", app.UserID);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
		return false;
	}

	public void CallServiceResult(String url, JSONObject json, AjaxStatus status) {

		if (json != null) {
			app.SaveServiceData("UserInfo", json.toString());
			try {
				app.UserID = json.getString("user_id").trim();
				headers.put("user_id", app.UserID);
				app.setHeaders(headers);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			// ajax error, show error code
			if (status.getCode() == AjaxStatus.NETWORK_ERROR) {
			app.MyToast(aq.getContext(),
					getResources().getString(R.string.networknotwork));
			}
		}
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
					container, false);
			TextView dummyTextView = (TextView) rootView
					.findViewById(R.id.section_label);
			dummyTextView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return rootView;
		}
	}
	public void OnClickPlay(View v) {
		CurrentPlayData mCurrentPlayData = new CurrentPlayData();
		
		mCurrentPlayData.CurrentCategory = 0;
		mCurrentPlayData.CurrentSource = 0;
		mCurrentPlayData.CurrentIndex = 0;
		mCurrentPlayData.CurrentQuality = 0;
		mCurrentPlayData.prod_id = "1004616";
		app.setCurrentPlayData(mCurrentPlayData);
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString("path", "http://117.27.153.51:80/83B516E8091FC8ADAF9C1BB64758CC84ABE0A231/playlist.m3u8");
		bundle.putString("title", "生化危机5：惩罚 Resident Evil: Retribution");
		bundle.putString("prod_id", mCurrentPlayData.prod_id);
		bundle.putString("prod_type", "1");
		bundle.putLong("current_time", 0);
		intent.putExtras(bundle);
		intent.setClass(this, VideoPlayerActivity.class);
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException ex) {
			Log.e(TAG, "mp4 fail", ex);
		}
	}

}
