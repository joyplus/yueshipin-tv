package com.joyplus.sub;

import android.content.Context;
import android.util.Log;

public class JoyplusTempSubUri extends JoyplusSubListInatance {    
    private Context mContext;    
    private class SubModelChanged implements ISubModelChangedObserver{
		@Override
		public void onSubModelChanged(SubModel model, boolean dataChanged) {
			// TODO Auto-generated method stub
			SetInstanceState(mContext,false);
		}
		@Override
		public void onInstance(JoyplusSubInstance subInstance) {
			// TODO Auto-generated method stub
			
		}		 	
    }    
	public JoyplusTempSubUri(Context context){
		mContext     = context;
		Instanceing  = false;
		MAX = 2*JoyplusSubConfig.getInstance().getSubMax();
		registerModelChangedObserver(new SubModelChanged());
	}
}
