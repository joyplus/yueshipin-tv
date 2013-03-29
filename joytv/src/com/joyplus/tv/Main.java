package com.joyplus.tv;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.joyplus.tv.ui.ClockTextView;
import com.joyplus.tv.ui.MyGallery;
import com.joyplus.tv.ui.MyScrollLayout;
import com.joyplus.tv.ui.MyScrollLayout.OnViewChangeListener;

public class Main extends Activity implements OnItemSelectedListener, OnItemClickListener{
//	private String TAG = "Main";
//	private App app;
//	private AQuery aq;
//	private Map<String, String> headers;
//	/**
//	 * The {@link android.support.v4.view.PagerAdapter} that will provide
//	 * fragments for each of the sections. We use a
//	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
//	 * will keep every loaded fragment in memory. If this becomes too memory
//	 * intensive, it may be best to switch to a
//	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
//	 */
//	SectionsPagerAdapter mSectionsPagerAdapter;
//
//	/**
//	 * The {@link ViewPager} that will host the section contents.
//	 */
//	ViewPager mViewPager;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.main);
//
//		app = (App) getApplicationContext();
//		aq = new AQuery(this);
//		
//		headers = new HashMap<String, String>();
//		headers.put("User-Agent",
//				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
//		PackageInfo pInfo;
//		try {
//			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
//			headers.put("version", pInfo.versionName);
//		} catch (NameNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	
//		headers.put("app_key", Constant.APPKEY);
//		headers.put("client","android");
//		app.setHeaders(headers);
//		
//		// Create the adapter that will return a fragment for each of the three
//		// primary sections of the app.
//		mSectionsPagerAdapter = new SectionsPagerAdapter(
//				getSupportFragmentManager());
//
//		// Set up the ViewPager with the sections adapter.
//		mViewPager = (ViewPager) findViewById(R.id.pager);
//		mViewPager.setAdapter(mSectionsPagerAdapter);
//		
//		if(!Constant.TestEnv)
//			ReadLocalAppKey();
//
//		CheckLogin();
//	}
//	@Override
//	protected void onDestroy() {
//		if (aq != null)
//			aq.dismiss();
//		super.onDestroy();
//	}
//
//	public void ReadLocalAppKey() {
//		// online 获取APPKEY
//		MobclickAgent.updateOnlineConfig(this);
//		String OnLine_Appkey = MobclickAgent.getConfigParams(this, "APPKEY");
//		if (OnLine_Appkey != null && OnLine_Appkey.length() >0) {
//			Constant.APPKEY = OnLine_Appkey;
//			headers.remove("app_key");
//			headers.put("app_key", OnLine_Appkey);
//			app.setHeaders(headers);
//		}
//	}
//	public boolean CheckLogin() {
//		String UserInfo = null;
//		UserInfo = app.GetServiceData("UserInfo");
//		if (UserInfo == null) {
//			// 1. 在客户端生成一个唯一的UUID
//			String macAddress = null;
//			WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//			WifiInfo info = (null == wifiMgr ? null : wifiMgr
//					.getConnectionInfo());
//			if (info != null) {
//				macAddress = info.getMacAddress();
//				// 2. 通过调用 service account/generateUIID把UUID传递到服务器
//				String url = Constant.BASE_URL + "account/generateUIID";
//
//				Map<String, Object> params = new HashMap<String, Object>();
//				params.put("uiid", macAddress);
//				params.put("device_type", "Android");
//
//				AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
//				cb.header("User-Agent",
//						"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
//				cb.header("app_key", Constant.APPKEY);
//
//				cb.params(params).url(url).type(JSONObject.class)
//						.weakHandler(this, "CallServiceResult");
//				aq.ajax(cb);
//			}
//		} else {
//			JSONObject json;
//			try {
//				json = new JSONObject(UserInfo);
//				app.UserID = json.getString("user_id").trim();
//				headers.put("user_id", app.UserID);
//			} catch (JSONException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//
//		}
//		return false;
//	}
//
//	public void CallServiceResult(String url, JSONObject json, AjaxStatus status) {
//
//		if (json != null) {
//			app.SaveServiceData("UserInfo", json.toString());
//			try {
//				app.UserID = json.getString("user_id").trim();
//				headers.put("user_id", app.UserID);
//				app.setHeaders(headers);
//
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		} else {
//			// ajax error, show error code
//			if (status.getCode() == AjaxStatus.NETWORK_ERROR) {
//			app.MyToast(aq.getContext(),
//					getResources().getString(R.string.networknotwork));
//			}
//		}
//	}
//
//	/**
//	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
//	 * one of the sections/tabs/pages.
//	 */
//	public class SectionsPagerAdapter extends FragmentPagerAdapter {
//
//		public SectionsPagerAdapter(FragmentManager fm) {
//			super(fm);
//		}
//
//		@Override
//		public Fragment getItem(int position) {
//			// getItem is called to instantiate the fragment for the given page.
//			// Return a DummySectionFragment (defined as a static inner class
//			// below) with the page number as its lone argument.
//			Fragment fragment = new DummySectionFragment();
//			Bundle args = new Bundle();
//			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
//			fragment.setArguments(args);
//			return fragment;
//		}
//
//		@Override
//		public int getCount() {
//			// Show 3 total pages.
//			return 3;
//		}
//
//		@Override
//		public CharSequence getPageTitle(int position) {
//			Locale l = Locale.getDefault();
//			switch (position) {
//			case 0:
//				return getString(R.string.title_section1).toUpperCase(l);
//			case 1:
//				return getString(R.string.title_section2).toUpperCase(l);
//			case 2:
//				return getString(R.string.title_section3).toUpperCase(l);
//			}
//			return null;
//		}
//	}
//
//	/**
//	 * A dummy fragment representing a section of the app, but that simply
//	 * displays dummy text.
//	 */
//	public static class DummySectionFragment extends Fragment {
//		/**
//		 * The fragment argument representing the section number for this
//		 * fragment.
//		 */
//		public static final String ARG_SECTION_NUMBER = "section_number";
//
//		public DummySectionFragment() {
//		}
//
//		@Override
//		public View onCreateView(LayoutInflater inflater, ViewGroup container,
//				Bundle savedInstanceState) {
//			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
//					container, false);
//			TextView dummyTextView = (TextView) rootView
//					.findViewById(R.id.section_label);
//			dummyTextView.setText(Integer.toString(getArguments().getInt(
//					ARG_SECTION_NUMBER)));
//			return rootView;
//		}
//	}
//	public void OnClickPlay(View v) {
//		CurrentPlayData mCurrentPlayData = new CurrentPlayData();
//		
//		mCurrentPlayData.CurrentCategory = 0;
//		mCurrentPlayData.CurrentSource = 0;
//		mCurrentPlayData.CurrentIndex = 0;
//		mCurrentPlayData.CurrentQuality = 0;
//		mCurrentPlayData.prod_id = "1004616";
//		app.setCurrentPlayData(mCurrentPlayData);
//		Intent intent = new Intent();
//		Bundle bundle = new Bundle();
//		bundle.putString("path", "http://117.27.153.51:80/83B516E8091FC8ADAF9C1BB64758CC84ABE0A231/playlist.m3u8");
//		bundle.putString("title", "生化危机5：惩罚 Resident Evil: Retribution");
//		bundle.putString("prod_id", mCurrentPlayData.prod_id);
//		bundle.putString("prod_type", "1");
//		bundle.putLong("current_time", 0);
//		intent.putExtras(bundle);
//		intent.setClass(this, VideoPlayerActivity.class);
//		try {
//			startActivity(intent);
//		} catch (ActivityNotFoundException ex) {
//			Log.e(TAG, "mp4 fail", ex);
//		}
//	}
	
	
	
	
	private int[] resouces = {
			R.drawable.test1,
			R.drawable.test2,
			R.drawable.test3,
			R.drawable.test4,
			R.drawable.test5,
			R.drawable.test6
		};
	
	private int [] resouces_lib_nomal = {
			R.drawable.movie_normal,
			R.drawable.episode_normal,
			R.drawable.cartoon_normal,
			R.drawable.variety_normal,
			R.drawable.search_normal
		};
	private int [] resouces_lib_active = {
			R.drawable.movie_active,
			R.drawable.episode_active,
			R.drawable.cartoon_active,
			R.drawable.variety_active,
			R.drawable.search_active,
		};
	
	private int [] resouces_my_nomal = {
			R.drawable.follow_normal,
			R.drawable.recent_normal,
			R.drawable.down_normal,
			R.drawable.system_normal
		};
	private int [] resouces_my_active = {
			R.drawable.follow_active,
			R.drawable.recent_active,
			R.drawable.down_active,
			R.drawable.system_active,
		};
	
	private MyGallery gallery1;
	private float density;
	private int displayWith;
	private MyScrollLayout titleGroup;
	private FrameLayout itemFram;
	private ClockTextView clock;
	private ImageView highlightImageView;
	private ImageView playIcon;
//	private int mSelectedIndex;
	private LinearLayout contentLayout;
	private TextView noticeView;
	
	private static final String TAG = "TvDemoActivity";
	private View hotView;
	private View yeuDanView;
	private View kuView;
	private View myView;
	
	private Map<Integer, Integer> indexCaces = new HashMap<Integer, Integer>();
	
	private Animation alpha_appear;
	private Animation alpha_disappear;
	
//	private AnimationSet leftSetAnimationSetStep1;
//	private AnimationSet leftSetAnimationSetStep2;
//	private AnimationSet rightSetAnimationSetStep1;
//	private AnimationSet rightSetAnimationSetStep2;
	private TranslateAnimation leftTranslateAnimationStep1;
	private TranslateAnimation leftTranslateAnimationStep2;
	private TranslateAnimation rightTranslateAnimationStep1;
	private TranslateAnimation rightTranslateAnimationStep2;
	
//	private Handler mHandler = new Handler();
	
    /** Called when the activity is first created. */
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        gallery1 = (MyGallery) findViewById(R.id.gallery);
        contentLayout = (LinearLayout) findViewById(R.id.contentlayout);
        noticeView = (TextView) findViewById(R.id.notice_text);
        titleGroup = (MyScrollLayout) findViewById(R.id.group);
        titleGroup.setFocusable(false);
        titleGroup.setFocusableInTouchMode(false);
        hotView = LayoutInflater.from(Main.this).inflate(R.layout.layout_hot, null);
        yeuDanView = LayoutInflater.from(Main.this).inflate(R.layout.layout_list, null);
        kuView = LayoutInflater.from(Main.this).inflate(R.layout.layout_lib, null);
        myView = LayoutInflater.from(Main.this).inflate(R.layout.layout_my, null);
        titleGroup.SetOnViewChangeListener(new OnViewChangeListener() {
			
			@Override
			public void OnViewChange(int index) {
				// TODO Auto-generated method stub
				gallery1.startAnimation(alpha_appear);
				switch (index) {
				case 1:
					contentLayout.removeAllViews();
					hotView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
					contentLayout.startAnimation(alpha_appear);
					contentLayout.addView(hotView);
					gallery1.setAdapter(new HotAdapter());
					if(indexCaces.get(index) == null){
						gallery1.setSelection(0);
					}else{
						gallery1.setSelection(indexCaces.get(index));
					}
					highlightImageView.setImageResource(resouces[gallery1.getSelectedItemPosition()]);
					playIcon.setVisibility(View.VISIBLE);
					noticeView.setText(gallery1.getSelectedItemPosition()+1 + "/" + resouces.length);
					break;
				case 2:
					contentLayout.removeAllViews();
					yeuDanView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
					contentLayout.startAnimation(alpha_appear);
					contentLayout.addView(yeuDanView);
					gallery1.setAdapter(new LibAdapter());
					if(indexCaces.get(index) == null){
						gallery1.setSelection(0);
					}else{
						gallery1.setSelection(indexCaces.get(index));
					}
					highlightImageView.setImageResource(resouces_lib_active[gallery1.getSelectedItemPosition()]);
					playIcon.setVisibility(View.INVISIBLE);
					noticeView.setText(gallery1.getSelectedItemPosition()+1 + "/" + resouces_lib_active.length);
					break;
				case 3:
					contentLayout.removeAllViews();
					kuView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
					contentLayout.startAnimation(alpha_appear);
					contentLayout.addView(kuView);
					gallery1.setAdapter(new LibAdapter());
					if(indexCaces.get(index) == null){
						gallery1.setSelection(0);
					}else{
						gallery1.setSelection(indexCaces.get(index));
					}
					highlightImageView.setImageResource(resouces_lib_active[gallery1.getSelectedItemPosition()]);
					playIcon.setVisibility(View.INVISIBLE);
					noticeView.setText(gallery1.getSelectedItemPosition()+1 + "/" + resouces_lib_active.length);
					break;
				case 4:
					contentLayout.removeAllViews();
					myView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
					contentLayout.startAnimation(alpha_appear);
					contentLayout.addView(myView);
					gallery1.setAdapter(new MyAdapter());
					if(indexCaces.get(index) == null){
						gallery1.setSelection(0);
					}else{
						gallery1.setSelection(indexCaces.get(index));
					}
					highlightImageView.setImageResource(resouces_my_active[gallery1.getSelectedItemPosition()]);
					playIcon.setVisibility(View.INVISIBLE);
					noticeView.setText(gallery1.getSelectedItemPosition() +1+ "/" + resouces_my_active.length);
					break;
				}
			}
		});
        itemFram = (FrameLayout) findViewById(R.id.itemFram);
        clock = (ClockTextView) findViewById(R.id.clock);
        highlightImageView = (ImageView) findViewById(R.id.highlight_img);
        playIcon = (ImageView) findViewById(R.id.play_icon);
        MarginLayoutParams mlp = (MarginLayoutParams) gallery1.getLayoutParams();
        DisplayMetrics metrics = new DisplayMetrics();
        density = metrics.density;
        Display display = getWindowManager().getDefaultDisplay();
        displayWith = display.getWidth();
//        Toast.makeText(this, "widthPixels = " + display.get, 100).show();
//        Toast.makeText(this, "topMargin = " + mlp.topMargin, 100).show();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mlp.setMargins(-displayWith+displayWith/2, 
                       mlp.topMargin, 
                       mlp.rightMargin, 
                       mlp.bottomMargin
        );
        gallery1.setAdapter(new HotAdapter());
        gallery1.setCallbackDuringFling(false);
        gallery1.setOnItemSelectedListener(this);
        gallery1.setOnItemClickListener(this);
        gallery1.setSelection(1);
        noticeView.setText(gallery1.getSelectedItemPosition() +1 + "/" + resouces.length);
        highlightImageView.setImageResource(resouces[gallery1.getSelectedItemPosition()]);
        MarginLayoutParams mlp2 = (MarginLayoutParams) titleGroup.getLayoutParams();
        mlp2.setMargins(displayWith/6, 
		        		mlp2.topMargin, 
		        		mlp2.rightMargin, 
		        		mlp2.bottomMargin);
        LayoutParams param = itemFram.getLayoutParams();
        param.height = 2*displayWith/9+3;
        param.width = displayWith/6+3;
        alpha_appear = AnimationUtils.loadAnimation(this, R.anim.alpha_appear);
        alpha_disappear = AnimationUtils.loadAnimation(this, R.anim.alpha_disappear);
    }
    
    
    
    class HotAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return resouces.length;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
//			ImageView view = new ImageView(TvDemoActivity.this);
			View view = LayoutInflater.from(Main.this).inflate(R.layout.item_layout_gallery, null);
			view.setPadding(20, 30, 20, 10);
//			view.setScaleType(ScaleType.FIT_XY);
//			Toast.makeText(TvDemoActivity.this, "parent width" + parent.getWidth(), 100).show();
			view.setLayoutParams(new android.widget.Gallery.LayoutParams(displayWith/6,2*displayWith/9));
			
			ImageView img = (ImageView) view.findViewById(R.id.image);
			
			img.setImageResource(resouces[position]);
			return view;
		}
    	
    }



	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1,final int arg2,
			long arg3) {
		indexCaces.put(titleGroup.getSelectedTitleIndex(), arg2);
		// TODO Auto-generated method stub
//		highlightImageView.setImageResource(resouces[arg2]);
//		mSelectedIndex = arg2;
//		mHandler.post(new Runnable() {
//			
//			@Override
//			public void run() {
				// TODO Auto-generated method stub
//				View seletedView1 = gallery1.getChildAt(mSelectedIndex);
//				LinearLayout playLayout1 = (LinearLayout) seletedView1.findViewById(R.id.play_icon_layout);
//				ImageView reflectView1 = (ImageView) seletedView1.findViewById(R.id.reflact_bottom);
//				playLayout1.setVisibility(View.INVISIBLE);
//				reflectView1.setVisibility(View.VISIBLE);
//				seletedView1.setPadding(20, 30, 20, 20);
//				seletedView1.setBackgroundDrawable(null);
//				
//				mSelectedIndex = arg2;
//				
//				View seletedView2 = gallery1.getChildAt(mSelectedIndex);
//				LinearLayout playLayout2 = (LinearLayout) seletedView2.findViewById(R.id.play_icon_layout);
//				ImageView reflectView2= (ImageView) seletedView2.findViewById(R.id.reflact_bottom);
//				playLayout2.setVisibility(View.VISIBLE);
//				reflectView2.setVisibility(View.GONE);
//				seletedView2.setPadding(5, 5, 5, 5);
//				seletedView2.setBackgroundResource(R.drawable.line);
//			}
//		});
	}



	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
//		Toast.makeText(this, "key code = " + keyCode, 100).show();
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_UP:
			titleGroup.selectPreTitle();
			return true;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			titleGroup.selectNextTitle();
			return true;
		
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			final int positon1 = gallery1.getSelectedItemPosition();
			
//			if(leftTranslateAnimationStep1 == null){
//				leftTranslateAnimationStep1  = new TranslateAnimation(itemFram.getLeft()-itemFram.getWidth(), 
//						itemFram.getLeft()+itemFram.getWidth()/-itemFram.getWidth(), 
//						0, 
//						0);
//				leftTranslateAnimationStep1.setDuration(10);
//			}
			if(leftTranslateAnimationStep2 == null){
				leftTranslateAnimationStep2  = new TranslateAnimation(itemFram.getLeft()+itemFram.getWidth()/5-itemFram.getWidth(), 
						itemFram.getLeft()-itemFram.getWidth(), 
						0, 
						0);
				leftTranslateAnimationStep2.setDuration(250);
				leftTranslateAnimationStep2.setInterpolator(new AccelerateInterpolator(0.1f));
			}
//			leftTranslateAnimationStep1.setAnimationListener(new AnimationListener() {
//				
//				@Override
//				public void onAnimationStart(Animation animation) {
//					// TODO Auto-generated method stub
//					
//				}
//				
//				@Override
//				public void onAnimationRepeat(Animation animation) {
//					// TODO Auto-generated method stub
//					
//				}
//				
//				@Override
//				public void onAnimationEnd(Animation animation) {
//					// TODO Auto-generated method stub
//					if(positon1<resouces.length-1){
//						highlightImageView.setImageResource(resouces[positon1+1]);
//						itemFram.startAnimation(leftTranslateAnimationStep2);
//					}
//				}
//			});
			switch (titleGroup.getSelectedTitleIndex()) {
			case 1:
				if(positon1<resouces.length-1){
					highlightImageView.setImageResource(resouces[positon1+1]);
					itemFram.startAnimation(leftTranslateAnimationStep2);
					noticeView.setText(positon1+2 + "/" + resouces.length);
				}
				break;
			case 2:
				if(positon1<resouces_lib_active.length-1){
					highlightImageView.setImageResource(resouces_lib_active[positon1+1]);
					itemFram.startAnimation(leftTranslateAnimationStep2);
					noticeView.setText(positon1+2 + "/" + resouces_lib_active.length);
				}
				break;
			case 3:
				if(positon1<resouces_lib_active.length-1){
					highlightImageView.setImageResource(resouces_lib_active[positon1+1]);
					itemFram.startAnimation(leftTranslateAnimationStep2);
					noticeView.setText(positon1+2 + "/" + resouces_lib_active.length);
				}
				break;
			case 4:
				if(positon1<resouces_my_active.length-1){
					highlightImageView.setImageResource(resouces_my_active[positon1+1]);
					itemFram.startAnimation(leftTranslateAnimationStep2);
					noticeView.setText(positon1+2 + "/" + resouces_my_active.length);
				}
				break;
			
			}
			
			
//			if(leftSetAnimationSetStep1 == null){
//				leftSetAnimationSetStep1 = new AnimationSet(true);
//				ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 
//										(float)(displayWith/6-30)/itemFram.getWidth(), 
//										1.0f, 
//										(float)(2*displayWith/9-60)/itemFram.getHeight(),
//										itemFram.getWidth()/2,
//										itemFram.getHeight()/2);
//				TranslateAnimation translateAnimation = new TranslateAnimation(itemFram.getLeft()+displayWith/6+20-itemFram.getWidth(),
//						itemFram.getLeft()-itemFram.getWidth(), 
//										0, 
//										0);
//				translateAnimation.setAnimationListener(new AnimationListener() {
//					
//					@Override
//					public void onAnimationStart(Animation animation) {
//						// TODO Auto-generated method stub
//						
//					}
//					
//					@Override
//					public void onAnimationRepeat(Animation animation) {
//						// TODO Auto-generated method stub
//						
//					}
//					
//					@Override
//					public void onAnimationEnd(Animation animation) {
//						// TODO Auto-generated method stub
//						
//					}
//				});
//				leftSetAnimationSetStep1.addAnimation(scaleAnimation);
//				leftSetAnimationSetStep1.addAnimation(translateAnimation);
//				leftSetAnimationSetStep1.setDuration(500);
//			}
//			if(leftSetAnimationSetStep2 == null){
//				leftSetAnimationSetStep2 = new AnimationSet(true);
//				ScaleAnimation scaleAnimation = new ScaleAnimation((float)(displayWith/6-30)/itemFram.getWidth(), 
//										1.0f, 
//										(float)(2*displayWith/9-60)/itemFram.getHeight(), 
//										1.0f,
//										itemFram.getWidth()/2,
//										itemFram.getHeight()/2);
//				TranslateAnimation translateAnimation = new TranslateAnimation(itemFram.getLeft()-itemFram.getWidth(),
//										itemFram.getLeft()-displayWith/6+20-itemFram.getWidth(), 
//										0, 
//										0);
//				
//				leftSetAnimationSetStep2.addAnimation(scaleAnimation);
//				leftSetAnimationSetStep2.addAnimation(translateAnimation);
//				leftSetAnimationSetStep2.setDuration(500);
//			}
//			
//			itemFram.startAnimation(leftSetAnimationSetStep1);
//			itemFram.setVisibility(View.INVISIBLE);
//			if(positon1>0){
//				highlightImageView.setImageResource(resouces[positon1-1]);
//			}
//			itemFram.startAnimation(leftSetAnimationSetStep2);
//			itemFram.setVisibility(View.VISIBLE);
			
			return true;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			
			
			final int positon2 = gallery1.getSelectedItemPosition();
//			
//			if(rightTranslateAnimationStep1 == null){
//				rightTranslateAnimationStep1  = new TranslateAnimation(itemFram.getLeft()-itemFram.getWidth(), 
//						itemFram.getLeft()-itemFram.getWidth()/3-itemFram.getWidth(), 
//						0, 
//						0);
//				rightTranslateAnimationStep1.setDuration(250);
//			}
			if(rightTranslateAnimationStep2 == null){
				rightTranslateAnimationStep2  = new TranslateAnimation(itemFram.getLeft()-itemFram.getWidth()/4-itemFram.getWidth(), 
						itemFram.getLeft()-itemFram.getWidth(), 
						0, 
						0);
				rightTranslateAnimationStep2.setDuration(250);
				rightTranslateAnimationStep2.setInterpolator(new AccelerateInterpolator(0.1f));
			}
//			rightTranslateAnimationStep1.setAnimationListener(new AnimationListener() {
//				
//				@Override
//				public void onAnimationStart(Animation animation) {
//					// TODO Auto-generated method stub
//					
//				}
//				
//				@Override
//				public void onAnimationRepeat(Animation animation) {
//					// TODO Auto-generated method stub
//					
//				}
//				
//				@Override
//				public void onAnimationEnd(Animation animation) {
//					// TODO Auto-generated method stub
//					if(positon2>0){
//						highlightImageView.setImageResource(resouces[positon2-1]);
//						itemFram.startAnimation(rightTranslateAnimationStep2);
//					}
//				}
//			});
			
			
		switch (titleGroup.getSelectedTitleIndex()) {
		case 1:
			if(positon2>0){
				highlightImageView.setImageResource(resouces[positon2-1]);
				itemFram.startAnimation(rightTranslateAnimationStep2);
				noticeView.setText(positon2 + "/" + resouces.length);
			}
			break;
		case 2:
			if(positon2>0){
				highlightImageView.setImageResource(resouces_lib_active[positon2-1]);
				itemFram.startAnimation(leftTranslateAnimationStep2);
				noticeView.setText(positon2 + "/" + resouces_lib_active.length);
			}
			break;
		case 3:
			if(positon2>0){
				highlightImageView.setImageResource(resouces_lib_active[positon2-1]);
				itemFram.startAnimation(leftTranslateAnimationStep2);
				noticeView.setText(positon2 + "/" + resouces_lib_active.length);
			}
			break;
		case 4:
			if(positon2>0){
				highlightImageView.setImageResource(resouces_my_active[positon2-1]);
				itemFram.startAnimation(leftTranslateAnimationStep2);
				noticeView.setText(positon2 + "/" + resouces_my_active.length);
			}
			break;
		
		}
			
//			if(rightSetAnimationSetStep1 == null){
//				
//			}
//			if(rightSetAnimationSetStep2 == null){
//				
//			}
//			int positon2 = gallery1.getSelectedItemPosition();
//			if(positon2<resouces.length-1){
//				highlightImageView.setImageResource(resouces[positon2+1]);
//			}
//		KeyEvent.KEYCODE_DPAD_CENTER
			return true;
		}
		return false;
	}
	
	class LibAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return resouces_lib_nomal.length;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if(convertView == null){
				ImageView view = new ImageView(Main.this);
				view.setPadding(20, 30, 20, 20);
				view.setLayoutParams(new android.widget.Gallery.LayoutParams(displayWith/6,2*displayWith/9));
				convertView = view;
			}
			((ImageView)convertView).setImageResource(resouces_lib_nomal[position]);
			return convertView;
		}
    	
    }
	
	class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return resouces_my_nomal.length;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if(convertView == null){
				ImageView view = new ImageView(Main.this);
				view.setPadding(20, 30, 20, 20);
				view.setLayoutParams(new android.widget.Gallery.LayoutParams(displayWith/6,2*displayWith/9));
				convertView = view;
			}
			((ImageView)convertView).setImageResource(resouces_my_nomal[position]);
			return convertView;
		}
    	
    }



	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "item click index = " + titleGroup.getSelectedTitleIndex()+"[" + arg2 + "]", 100).show();
	}
	

}
