package com.joyplus.tv.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joyplus.tv.R;

public class NavigateView extends RelativeLayout implements OnItemSelectedListener {
	private static final String TAG = "NavigateView";
	private View rootView;
	private LinearLayout layout;
	private MyGallery1 gallery1,gallery2,gallery3;
	private LinearLayout lineLayout1;
	private LinearLayout lineLayout2;
	private LinearLayout lineLayout3;
	private LinearLayout highlightLayout;
	private String[] array_diqu;
	private String[] array_leibie;
	private String[] array_niandai;
	private TextView diqu,leibie,niandai;
	private TextView selectedTextView1;
	private TextView selectedTextView2;
	private TextView selectedTextView3;
	
	private int selected_gallery1_last = 0;
	private int selected_gallery2_last = 0;
	private int selected_gallery3_last = 0;
	private OnResultListener resultListener;
	private int selectedIndex = 1;
	private Handler handler = new Handler();
	
	private BroadcastReceiver recvier = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.d(TAG, intent.getAction() + intent.getBooleanExtra("isBack", false));
//			if(pop!=null){
//				pop.dismiss();
//				Toast.makeText(MainActivity.this, "选择结果：地区="+ array_diqu[gallery1.getSelectedItemPosition()] 
//								+ ";\t分类 =" + array_leibie[gallery2.getSelectedItemPosition()] 
//								+ ";\t年份 =" + array_niandai[gallery3.getSelectedItemPosition()], Toast.LENGTH_LONG).show();
//			}
			String action = intent.getAction();
			if("KEY_EVENT_KEYCODE_DPAD_CENTER".equals(action)){
				if(resultListener != null){
					String[] result = new String[3];
					result[0] = array_diqu[gallery1.getSelectedItemPosition()];
					result[1] = array_leibie[gallery2.getSelectedItemPosition()];
					result[2] = array_niandai[gallery3.getSelectedItemPosition()];
					selected_gallery1_last = gallery1.getSelectedItemPosition();
					selected_gallery2_last = gallery2.getSelectedItemPosition();
					selected_gallery3_last = gallery3.getSelectedItemPosition();
					resultListener.onResult(NavigateView.this, false, result);
				}
			}else if("KEY_EVENT_KEYCODE_BACK".equals(action)){
				if(resultListener != null){
					String[] result = new String[3];
					result[0] = array_diqu[gallery1.getSelectedItemPosition()];
					result[1] = array_leibie[gallery2.getSelectedItemPosition()];
					result[2] = array_niandai[gallery3.getSelectedItemPosition()];
					selected_gallery1_last = gallery1.getSelectedItemPosition();
					selected_gallery2_last = gallery2.getSelectedItemPosition();
					selected_gallery3_last = gallery3.getSelectedItemPosition();
					resultListener.onResult(NavigateView.this, true, result);
				}
			}else if("KEY_EVENT_KEYCODE_DPAD_UP".equals(action)){
				Log.d(TAG, "fouces view name " + getFocusedChild().getClass().getName());
//				getFocusedChild().setBackgroundColor(Color.RED);
				changeBackGround(true);
			}else if("KEY_EVENT_KEYCODE_DPAD_DOWN".equals(action)){
//				getFocusedChild().setBackgroundColor(Color.RED);
				changeBackGround(false);
			}
		}
		
	};

	private void changeBackGround(boolean isUp){
		Log.d(TAG, "-----------------");
		MarginLayoutParams mlp = (MarginLayoutParams) highlightLayout.getLayoutParams();
		switch (selectedIndex) {
		case 1:
			if(isUp){
				
			}else{
				gallery2.requestFocus();
				selectedIndex = 2;
				selectedTextView1.setTextColor(getContext().getResources().getColor(R.color.common_title_selected));
				selectedTextView2.setTextColor(Color.WHITE);
				
				mlp.setMargins(mlp.leftMargin, 
						mlp.topMargin,  
						mlp.rightMargin, 
						mlp.bottomMargin-lineLayout1.getHeight());
//				highlightLayout.layout(lineLayout2.getLeft(), lineLayout2.getTop(), lineLayout2.getRight(),lineLayout2.getBottom());
				highlightLayout.requestLayout();
//				lineLayout1.setBackgroundDrawable(null);
//				lineLayout2.setBackgroundResource(R.drawable.menubg);
//				gallery1.setBackgroundDrawable(null);
//				gallery2.setBackgroundColor(Color.RED);
			}
			break;
		case 2:
			if(isUp){
				gallery1.requestFocus();
				selectedIndex = 1;
				selectedTextView2.setTextColor(getContext().getResources().getColor(R.color.common_title_selected));
				selectedTextView1.setTextColor(Color.WHITE);
				mlp.setMargins(mlp.leftMargin, 
						mlp.topMargin, 
						mlp.rightMargin, 
						mlp.bottomMargin+lineLayout1.getHeight());
//				highlightLayout.layout(lineLayout1.getLeft(), lineLayout1.getTop(), lineLayout1.getRight(),lineLayout1.getBottom());
				highlightLayout.requestLayout();
//				lineLayout2.setBackgroundDrawable(null);
//				lineLayout1.setBackgroundResource(R.drawable.menubg);
//				gallery2.setBackgroundDrawable(null);
//				gallery1.setBackgroundColor(Color.RED);
			}else{
				gallery3.requestFocus();
				selectedIndex = 3;
				selectedTextView2.setTextColor(getContext().getResources().getColor(R.color.common_title_selected));
				selectedTextView3.setTextColor(Color.WHITE);
				mlp.setMargins(mlp.leftMargin, 
						mlp.topMargin, 
						mlp.rightMargin, 
						mlp.bottomMargin-lineLayout1.getHeight());
//				highlightLayout.layout(lineLayout3.getLeft(), lineLayout3.getTop(), lineLayout3.getRight(),lineLayout3.getBottom());
				highlightLayout.requestLayout();
//				lineLayout2.setBackgroundDrawable(null);
//				lineLayout3.setBackgroundResource(R.drawable.menubg);
			}
			break;
		case 3:
			if(isUp){
				gallery2.requestFocus();
				selectedIndex = 2;
				selectedTextView3.setTextColor(getContext().getResources().getColor(R.color.common_title_selected));
				selectedTextView2.setTextColor(Color.WHITE);
				mlp.setMargins(mlp.leftMargin, 
						mlp.topMargin, 
						mlp.rightMargin, 
						mlp.bottomMargin+lineLayout1.getHeight());
//				highlightLayout.layout(lineLayout2.getLeft(), lineLayout2.getTop(), lineLayout2.getRight(),lineLayout2.getBottom());
				highlightLayout.requestLayout();
//				lineLayout3.setBackgroundDrawable(null);
//				lineLayout2.setBackgroundResource(R.drawable.menubg);
			}else{
				
			}
			break;
		}
	}
	
	public NavigateView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public void Init(String[] array_diqu,String[] array_leibie, String[] array_niandai, int x, int y, int width, int height, OnResultListener listener){
		
		rootView = LayoutInflater.from(getContext()).inflate(R.layout.navagator_poplayout, null);
		gallery1 = (MyGallery1) rootView.findViewById(R.id.gallery1);
		gallery2 = (MyGallery1) rootView.findViewById(R.id.gallery2);
		gallery3 = (MyGallery1) rootView.findViewById(R.id.gallery3);
		layout = (LinearLayout) rootView.findViewById(R.id.navigate_layout);
		
		lineLayout1 = (LinearLayout) rootView.findViewById(R.id.line_1);
		lineLayout2 = (LinearLayout) rootView.findViewById(R.id.line_2);
		lineLayout3 = (LinearLayout) rootView.findViewById(R.id.line_3);
		highlightLayout = (LinearLayout) rootView.findViewById(R.id.highlight_backgroud);
		
		diqu = (TextView) rootView.findViewById(R.id.diqu);
		leibie = (TextView) rootView.findViewById(R.id.leibie);
		niandai = (TextView) rootView.findViewById(R.id.niandai);
		
		this.array_diqu = array_diqu;
		this.array_leibie = array_leibie;
		this.array_niandai = array_niandai;
		this.resultListener = listener;
		gallery1.setPadding(-(1280-470)+40, 0, 0, 0);
		gallery2.setPadding(-(1280-470)+40, 0, 0, 0);
		gallery3.setPadding(-(1280-470)+40, 0, 0, 0);
		gallery1.setCallbackDuringFling(false);
		gallery2.setCallbackDuringFling(false);
		gallery3.setCallbackDuringFling(false);
		gallery1.setAdapter(new  ArrayAdapter<String>(getContext(), R.layout.item_gallery_text, 
				array_diqu));
		gallery2.setAdapter(new  ArrayAdapter<String>(getContext(), R.layout.item_gallery_text, 
				array_leibie));
		gallery3.setAdapter(new  ArrayAdapter<String>(getContext(), R.layout.item_gallery_text, 
				array_niandai));
		gallery1.setOnItemSelectedListener(this);
		gallery2.setOnItemSelectedListener(this); 
		gallery3.setOnItemSelectedListener(this);
//		gallery1.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, new KeyEvent( KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
//		gallery1.setSelection(0);
		MarginLayoutParams mlp = (MarginLayoutParams) layout.getLayoutParams();
		mlp.setMargins(x, 
		        		y, 
		        		mlp.rightMargin, 
		        		mlp.bottomMargin);
		mlp.width = width;
		mlp.height = height;
		
//		MarginLayoutParams mlp1 = (MarginLayoutParams) gallery1.getLayoutParams();
//		mlp1.setMargins(-(1280-300)+40, 
//				mlp1.topMargin, 
//				mlp1.rightMargin, 
//				mlp1.bottomMargin);
		
//		MarginLayoutParams mlp2 = (MarginLayoutParams) gallery2.getLayoutParams();
//		mlp2.setMargins(-(1280-300)+40, 
//				mlp2.topMargin, 
//				mlp2.rightMargin, 
//				mlp2.bottomMargin);
//		
//		MarginLayoutParams mlp3 = (MarginLayoutParams) gallery3.getLayoutParams();
//		mlp3.setMargins(-(1280-300)+40, 
//				mlp3.topMargin, 
//				mlp3.rightMargin, 
//				mlp3.bottomMargin);
		rootView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		addView(rootView);
		
		IntentFilter filter = new IntentFilter("KEY_EVENT_KEYCODE_DPAD_CENTER");
		filter.addAction("KEY_EVENT_KEYCODE_BACK");
		filter.addAction("KEY_EVENT_KEYCODE_DPAD_UP");
		filter.addAction("KEY_EVENT_KEYCODE_DPAD_DOWN");
		getContext().registerReceiver(recvier, filter);
		gallery1.requestFocus();
		selectedIndex = 1;
//		highlightLayout.layout(lineLayout1.getLeft(), lineLayout1.getTop(), lineLayout1.getRight(),lineLayout1.getBottom());
		
//		MarginLayoutParams mlp4 = (MarginLayoutParams) highlightLayout.getLayoutParams();
//		mlp4.setMargins(lineLayout1.getLeft(), 
//					lineLayout1.getTop(), 
//					mlp4.rightMargin, 
//					mlp4.bottomMargin);
//		
////		highlightLayout.layout(50, 50, 250,250);
//		highlightLayout.requestLayout();	
	}
	
	private int[] getLocationOnScreen(View v){
		int [] location = new int[2];
		v.getLocationOnScreen(location);
		return location;
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.gallery1:
			
			if (selectedTextView1!=null) {
				selectedTextView1.setTextColor(getContext().getResources().getColor(R.color.common_title_unselected));
			}
			selectedTextView1 = (TextView) arg1;
			if(selectedIndex == 1){
				selectedTextView1.setTextColor(Color.WHITE);
			}else{
				selectedTextView1.setTextColor(getContext().getResources().getColor(R.color.common_title_selected));
			}
			if(arg2==0){
				diqu.setVisibility(View.GONE);
			}else{
				if(diqu.getVisibility() == View.GONE){
					diqu.setVisibility(View.VISIBLE);
					AnimationSet animSet = new AnimationSet(false);
					AlphaAnimation alphaAnimation =  new AlphaAnimation(0f, 1.0f);
					TranslateAnimation anim = new TranslateAnimation(diqu.getMeasuredWidth()/2, 0, 0, 0);
					animSet.addAnimation(anim);
					animSet.addAnimation(alphaAnimation);
					animSet.setDuration(100);
					diqu.startAnimation(animSet);
				}
			}
			if(leibie.getVisibility() == View.GONE&&niandai.getVisibility() == View.GONE){
				
				diqu.setText(array_diqu[arg2]);
			}else{
				diqu.setText(array_diqu[arg2] + "/");
			}
			break;
		case R.id.gallery2:
			if (selectedTextView2!=null) {
				selectedTextView2.setTextColor(getContext().getResources().getColor(R.color.common_title_unselected));
			}
			selectedTextView2 = (TextView) arg1;
			if(selectedIndex == 2){
				selectedTextView2.setTextColor(Color.WHITE);
			}else{
				selectedTextView2.setTextColor(getContext().getResources().getColor(R.color.common_title_selected));
			}
			
			if(arg2==0){
				leibie.setVisibility(View.GONE);
				leibie.setText(array_leibie[arg2]);
				if(niandai.getVisibility()== View.GONE){
					diqu.setText(array_diqu[gallery1.getSelectedItemPosition()]);
				}else{
					diqu.setText(array_diqu[gallery1.getSelectedItemPosition()] + "/");
				}
				
			}else{
				if(leibie.getVisibility() == View.GONE){
					leibie.setVisibility(View.VISIBLE);
					AnimationSet animSet = new AnimationSet(false);
					AlphaAnimation alphaAnimation =  new AlphaAnimation(0f, 1.0f);
					TranslateAnimation anim = new TranslateAnimation(leibie.getMeasuredWidth()/2, 0, 0, 0);
					animSet.addAnimation(anim);
					animSet.addAnimation(alphaAnimation);
					animSet.setDuration(100);
					leibie.startAnimation(animSet);
					diqu.setText(array_diqu[gallery1.getSelectedItemPosition()] + "/");
					
				}
				if(niandai.getVisibility() == View.GONE){
					leibie.setText(array_leibie[arg2]);
				}else{
					leibie.setText(array_leibie[arg2] + "/");
				}
			}
			break;
		case R.id.gallery3:
//			niandai.setText(resouces_2[arg2]);
			
			if (selectedTextView3!=null) {
				selectedTextView3.setTextColor(getContext().getResources().getColor(R.color.common_title_unselected));
			}
			selectedTextView3 = (TextView) arg1;
			if(selectedIndex == 3){
				selectedTextView3.setTextColor(Color.WHITE);
			}else{
				selectedTextView3.setTextColor(getContext().getResources().getColor(R.color.common_title_selected));
			}
			
			if(arg2==0){
				niandai.setVisibility(View.GONE);
				leibie.setText(array_leibie[gallery2.getSelectedItemPosition()]);
				if(leibie.getVisibility() == View.GONE){
					diqu.setText(array_diqu[gallery1.getSelectedItemPosition()]);
				}else{
					diqu.setText(array_diqu[gallery1.getSelectedItemPosition()]+"/");
				}
				niandai.setText(array_niandai[arg2]);
			}else{
				if(niandai.getVisibility() == View.GONE){
					
					AnimationSet animSet = new AnimationSet(false);
					AlphaAnimation alphaAnimation =  new AlphaAnimation(0f, 1.0f);
					TranslateAnimation anim = new TranslateAnimation(niandai.getMeasuredWidth()/2, 0, 0, 0);
					animSet.addAnimation(anim);
					animSet.addAnimation(alphaAnimation);
					animSet.setDuration(100);
					niandai.startAnimation(animSet);
					niandai.setVisibility(View.VISIBLE);
				}
				if(leibie.getVisibility() == View.GONE){
					diqu.setText(array_diqu[gallery1.getSelectedItemPosition()] + "/");
				}else{
					leibie.setText(array_leibie[gallery2.getSelectedItemPosition()] + "/");
				}
				
				niandai.setText(array_niandai[arg2]);
			}
			break;
		}
	}
	

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
//		switch (selectedIndex) {
//		case 1:
//			highlightLayout.layout(lineLayout1.getLeft(), lineLayout1.getTop(), lineLayout1.getRight(),lineLayout1.getBottom());
//			break;
//		case 2:
//			highlightLayout.layout(lineLayout2.getLeft(), lineLayout2.getTop(), lineLayout2.getRight(),lineLayout2.getBottom());
//			break;
//		case 3:
//			highlightLayout.layout(lineLayout3.getLeft(), lineLayout3.getTop(), lineLayout3.getRight(),lineLayout3.getBottom());
//			break;
//
//		default:
//			break;
//		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onAttachedToWindow() {
		// TODO Auto-generated method stub
		super.onAttachedToWindow();
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
//				gallery1.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
//				gallery1.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
//				gallery1.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
//				gallery1.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
				gallery1.setSelection(selected_gallery1_last);
				gallery2.setSelection(selected_gallery2_last);
				gallery3.setSelection(selected_gallery3_last);
				gallery1.onFling(null, null, -rootView.getWidth(), 0);
				gallery2.onFling(null, null, -rootView.getWidth(), 0);
				gallery3.onFling(null, null, -rootView.getWidth(), 0);
//				Log.d(gallery1.getSelectedItemPosition(), msg)
			}
		}, 200);
//		handler.postDelayed(new Runnable() {
//			
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
////				gallery1.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
////				gallery1.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
////				gallery1.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
////				gallery1.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
////				gallery1.onFling(null, null, 1, 0);
////				gallery2.onFling(null, null, 1, 0);
////				gallery3.onFling(null, null, 1, 0);
////				gallery1.setSelection(selected_gallery1_last);
////				gallery2.setSelection(selected_gallery2_last);
////				gallery3.setSelection(selected_gallery3_last);
//			}
//		}, 1000);
	}
	
	public interface OnResultListener{
		abstract void onResult(View v, boolean isBack, String[] choice);
	}
}
