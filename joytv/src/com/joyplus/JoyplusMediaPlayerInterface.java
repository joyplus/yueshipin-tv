package com.joyplus;

import com.joyplus.mediaplayer.ViewInterface;

import android.os.Message;
import android.view.KeyEvent;

/*define by Jas@20130813 for control layout in joyplusvideoview.xml*/
public interface JoyplusMediaPlayerInterface {
       
	  boolean JoyplusdispatchMessage(Message msg);
	  
	  boolean JoyplusonKeyDown(int keyCode, KeyEvent event);
	  
	  void    JoyplussetVisible(boolean visible,int layout);
	  
	  int     JoyplusgetLayout();
}
