package com.joyplus;

import com.joyplus.mediaplayer.MediaInfo;
import com.joyplus.tv.R;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;



public class JoyplusMediaPlayerMiddleControlMini extends LinearLayout implements JoyplusMediaPlayerInterface{
	
	private Context        mContext;
	private static Handler mHandler;
	//switch layout
	private LinearLayout mSwitch;
	private ImageButton  mSwitch_center;
	private ImageButton  mSwitch_left;
	private ImageButton  mSwitch_top;
	private ImageButton  mSwitch_right;
	private ImageButton  mSwitch_bottom;
	//pause or play layout
	private LinearLayout mPauseplay;
	private ImageButton  mPauseplay_button;
	
	/*Event of this layout*/
	public final static int MSG_KEYDOWN_CENTER    = 1;
	public final static int MSG_KEYDOWN_LEFT      = 2;
	public final static int MSG_KEYDOWN_TOP       = 3;
	public final static int MSG_KEYDOWN_RIGHT     = 4;
	public final static int MSG_KEYDOWN_BOTTOM    = 5;
	public final static int MSG_KEYDOWN_PAUSEPLAY = 6;
	public final static int MSG_REQUESTHIDEVIEW   = 7;
	public final static int MSG_PAUSEPLAY         = 8;
	
	public final static int LAYOUT_PAUSEPLAY    = 1;
	public final static int LAYOUT_SWITCH       = 2;
	public final static int LAYOUT_UNKNOW       = 3;
	private      static int mLayout             = LAYOUT_PAUSEPLAY;
	public static void setLayout(int layout){		
		if(layout == LAYOUT_SWITCH || layout == LAYOUT_PAUSEPLAY){
			mLayout = layout;
		}else{
			mLayout = LAYOUT_UNKNOW;
		}
	}
	public static void setHandler(Handler handler){
		mHandler = handler;
	}
	private class MessageOnClick implements OnClickListener,OnFocusChangeListener {
        private int mWhat;
        public MessageOnClick(int what) {
            mWhat = what;
        }
        public void onClick(View v) {
            Message msg = Message.obtain(mHandler, mWhat);
            msg.sendToTarget();
        }
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
			Message msg = Message.obtain(mHandler, mWhat);
            msg.sendToTarget();
		}
    }
	public JoyplusMediaPlayerMiddleControlMini(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
	}
	public JoyplusMediaPlayerMiddleControlMini(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context; 
		
	}
	public void UpdateShowLayout(){
		if(mLayout       == this.LAYOUT_SWITCH){
			ShowSwitch();
		}else if(mLayout == this.LAYOUT_PAUSEPLAY){
			ShowPlaypause();
		}else{
			Message.obtain(mHandler, MSG_REQUESTHIDEVIEW).sendToTarget();
		}
		mLayout             = LAYOUT_UNKNOW;
	}
	protected void onFinishInflate() {
		super.onFinishInflate();
		mSwitch           = (LinearLayout) findViewById(R.id.joyplusvideoview_mini_switch);
		mPauseplay        = (LinearLayout) findViewById(R.id.joyplusvideoview_mini_pauseplay);
		//UpdateShowLayout();	    
	}
	private void ShowSwitch(){
		mPauseplay.setVisibility(View.GONE);
		mSwitch.setVisibility(View.VISIBLE);
		mSwitch_center = (ImageButton) findViewById(R.id.joyplusvideoview_mini_switch_center);
		mSwitch_left   = (ImageButton) findViewById(R.id.joyplusvideoview_mini_switch_left);
		mSwitch_top    = (ImageButton) findViewById(R.id.joyplusvideoview_mini_switch_top);
		mSwitch_right  = (ImageButton) findViewById(R.id.joyplusvideoview_mini_switch_right);
		mSwitch_bottom = (ImageButton) findViewById(R.id.joyplusvideoview_mini_switch_bottom);
		mSwitch_center.setOnClickListener(new MessageOnClick(MSG_KEYDOWN_CENTER));
		mSwitch_center.setOnFocusChangeListener(new MessageOnClick(MSG_KEYDOWN_CENTER));
		mSwitch_left.setOnClickListener(new MessageOnClick(MSG_KEYDOWN_LEFT));
		mSwitch_left.setOnFocusChangeListener(new MessageOnClick(MSG_KEYDOWN_LEFT));
		mSwitch_top.setOnClickListener(new MessageOnClick(MSG_KEYDOWN_TOP));
		mSwitch_top.setOnFocusChangeListener(new MessageOnClick(MSG_KEYDOWN_TOP));
		mSwitch_right.setOnClickListener(new MessageOnClick(MSG_KEYDOWN_RIGHT));
		mSwitch_right.setOnFocusChangeListener(new MessageOnClick(MSG_KEYDOWN_RIGHT));
		mSwitch_bottom.setOnClickListener(new MessageOnClick(MSG_KEYDOWN_BOTTOM));
		mSwitch_bottom.setOnFocusChangeListener(new MessageOnClick(MSG_KEYDOWN_BOTTOM));
		if(!JoyplusMediaPlayerActivity.mInfo.getHaveNext()){
			mSwitch_right.setFocusable(false);
			mSwitch_right.setEnabled(false);
		}
		if(!JoyplusMediaPlayerActivity.mInfo.getHavePre()){
			mSwitch_left.setFocusable(false);
			mSwitch_left.setEnabled(false);
		}
		if(JoyplusMediaPlayerActivity.mInfo.mCollection != 0){
			mSwitch_bottom.setBackgroundResource(R.drawable.player_btn_unfav);
		}else{
			mSwitch_bottom.setBackgroundResource(R.drawable.player_btn_fav);
		}
		Message.obtain(mHandler, MSG_PAUSEPLAY).sendToTarget();
	}
	private void ShowPlaypause(){
		mPauseplay.setVisibility(View.VISIBLE);
		mSwitch.setVisibility(View.GONE);
		mPauseplay_button = (ImageButton)  findViewById(R.id.joyplusvideoview_mini_pauseplay_button);
		mPauseplay_button.setOnClickListener(new MessageOnClick(MSG_KEYDOWN_PAUSEPLAY));
		Message.obtain(mHandler, MSG_PAUSEPLAY).sendToTarget();
	}
	
	@Override
	public boolean JoyplusdispatchMessage(Message msg) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean JoyplusonKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch(keyCode){
		case KeyEvent.KEYCODE_DPAD_CENTER:
			if(mSwitch.getVisibility() == View.VISIBLE){
				Message.obtain(mHandler, MSG_KEYDOWN_CENTER).sendToTarget();
			}else if(mPauseplay.getVisibility() == View.VISIBLE){
				Message.obtain(mHandler, MSG_KEYDOWN_PAUSEPLAY).sendToTarget();
			}
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			if(mSwitch.getVisibility() == View.VISIBLE){
				Message.obtain(mHandler, MSG_KEYDOWN_BOTTOM).sendToTarget();
			}
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if(mSwitch.getVisibility() == View.VISIBLE){
				Message.obtain(mHandler, MSG_KEYDOWN_LEFT).sendToTarget();
			}
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if(mSwitch.getVisibility() == View.VISIBLE){
				Message.obtain(mHandler, MSG_KEYDOWN_RIGHT).sendToTarget();
			}
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
			if(mSwitch.getVisibility() == View.VISIBLE){
				Message.obtain(mHandler, MSG_KEYDOWN_TOP).sendToTarget();
			}
			break;
		case KeyEvent.KEYCODE_BACK:
			break;
		}
		return true;
	}
	@Override
	public void JoyplussetVisible(boolean visible, int layout) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public int JoyplusgetLayout() {
		// TODO Auto-generated method stub
		return JoyplusMediaPlayerMiddleControl.LAYOUT_MINI;
	}

}
