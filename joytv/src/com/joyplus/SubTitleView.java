package com.joyplus;



import com.joyplus.Sub.Element;
import com.joyplus.Sub.JoyplusSubManager;
import com.joyplus.mediaplayer.JoyplusMediaPlayerManager;
import com.joyplus.mediaplayer.MediaInfo;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;

public class SubTitleView extends TextView {
	
	private static final int MESSAGE_SUBTITLE_BEGAIN_SHOW =  0;
	private static final int MESSAGE_SUBTITLE_END_HIDEN = MESSAGE_SUBTITLE_BEGAIN_SHOW + 1;
	
	private static final int SEEKBAR_REFRESH_TIME = 200;//refresh time
	private static final int SUBTITLE_DELAY_TIME_MAX = 1000;
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MESSAGE_SUBTITLE_BEGAIN_SHOW:
				org.blaznyoght.subtitles.model.Element element_show = 
				(org.blaznyoght.subtitles.model.Element) msg.obj;
				if(element_show != null){
					long currentPositionShow = getCurrentTime();
					Element preElement_show = getElement(currentPositionShow);					
					//在字幕的显示时间段内
					if(!element_show.getText().equals(getText())){
						if(element_show.getStartTime().getTime() < currentPositionShow + SEEKBAR_REFRESH_TIME/2
								&& element_show.getStartTime().getTime() > currentPositionShow - SEEKBAR_REFRESH_TIME/2){
							setText(element_show.getText());
							setTag(element_show.getEndTime().getTime());
						}
					}
					if(element_show.getEndTime().getTime() < currentPositionShow){
						setText("");
						setTag(-1L);
						mHandler.removeMessages(MESSAGE_SUBTITLE_END_HIDEN);
						if(preElement_show != null){
							Message messageHiden = mHandler.obtainMessage(MESSAGE_SUBTITLE_END_HIDEN, preElement_show);
							mHandler.sendMessageDelayed(messageHiden, preElement_show.getEndTime().getTime() - currentPositionShow);
						}
					}
					
					long tagEndTime = (Long) getTag();
					if(!element_show.getText().equals(getText()) && tagEndTime != -1
							&& tagEndTime < currentPositionShow){
						setText("");
						setTag(-1L);
					}
					if(preElement_show != null){
						Message messageShow = mHandler.obtainMessage(MESSAGE_SUBTITLE_BEGAIN_SHOW, preElement_show);
						if(preElement_show.getStartTime().getTime() - currentPositionShow > SUBTITLE_DELAY_TIME_MAX){
							mHandler.sendMessageDelayed(messageShow, SUBTITLE_DELAY_TIME_MAX);
						}else {
							mHandler.sendMessageDelayed(messageShow, preElement_show.getStartTime().getTime() - currentPositionShow);
						}
					}
				}
				break;
			case MESSAGE_SUBTITLE_END_HIDEN:
				Element element_end = (Element) msg.obj;
				if(element_end != null){
					long currentPositionShow = getCurrentTime();
					Element preElement_show = getElement(currentPositionShow);
					if(element_end.getEndTime().getTime() > currentPositionShow - SEEKBAR_REFRESH_TIME/2){
						setText("");
						setTag(-1L);
					}
					if(preElement_show != null){
						Message messageHiden = mHandler.obtainMessage(MESSAGE_SUBTITLE_END_HIDEN, preElement_show);
						mHandler.sendMessageDelayed(messageHiden, preElement_show.getEndTime().getTime() - currentPositionShow);
					}
				}
				break;
			default:
				break;
			}
		}
		
	};
	
	//private List<Element> subTitleList = new ArrayList<Element>();
	private JoyplusMediaPlayerActivity   mActivity;
	private void Init(JoyplusMediaPlayerActivity activity){
		if(activity == null)return ;
		mActivity = activity;
	}
	private MediaInfo getMediaInfo(){
		if(mActivity == null || mActivity.getPlayer()==null)return new MediaInfo();
		return  mActivity.getPlayer().getMediaInfo();
	}
	private long getCurrentTime(){
		return getMediaInfo().getCurrentTime();
	}
	private JoyplusSubManager getSubManager(){
		return JoyplusMediaPlayerManager.getInstance().getSubManager();
	}
	private Element getElement(long time){
		return getSubManager().getElement(time);
	}
	
	public SubTitleView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public SubTitleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public SubTitleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	
	
	public void displaySubtitle(){
		
	}
	
	public void hiddenSubtitle(){
		
		mHandler.removeCallbacksAndMessages(null);
	}
	
//	public void 
	
}
