package com.joyplus.sub;
import java.util.List;
import android.content.Context;
import android.util.Log;

public class JoyplusSubServer{
	//private static final String  TAG = "JoyplusSubServer";
	private JoyplusUsableSubUri  mUsableSubUri;
	private JoyplusLocalSubUri   mLocalSubUri;
	private JoyplusTempSubUri    mTempSubUri;	
	private JoyplusSubConfig     mSubConfig;
    private Context mContext;
    
    public JoyplusSubServer(Context context){
    	mContext   = context;
    	mSubConfig     = new JoyplusSubConfig(mContext);
    	mUsableSubUri  = new JoyplusUsableSubUri(mContext);
    	mLocalSubUri   = new JoyplusLocalSubUri(mContext);    	
    	mLocalSubUri.registerModelChangedObserver(mUsableSubUri);
    	mTempSubUri    = new JoyplusTempSubUri(mContext);
    	mTempSubUri.registerModelChangedObserver(mLocalSubUri);    	
    }
    public void registerListener(JoyplusSubListener listener){
    	mUsableSubUri.registerListener(listener);
	}
	public void setSubUri(List<SubURI> subUri,boolean clear){
		 if(subUri==null || subUri.size()<=0)return;
		 if(clear)clearSub();
		 for(SubURI sub : subUri){
			 mTempSubUri.add(sub);
		 }	 
	}	
	
	public List<SubURI> getSubList(){
		return mUsableSubUri;
	}
	public boolean clearSub() {
		// TODO Auto-generated method stub
		mTempSubUri.clear();
		mLocalSubUri.clear();
		mUsableSubUri.clear();
		return true;
	}
    public boolean CheckSubAviable(){
    	return mUsableSubUri.CheckSubAviable();
    }
    
    public JoyplusSub getJoyplusSub() throws Exception{
    	if(mUsableSubUri.getSub() != null)return mUsableSubUri.getSub();
    	throw new Exception("JoyplusSub is null");
    }
    
    public void SwitchSub(int index){
    	if(getSubList() == null || index>=getSubList().size()) return ;
    	mUsableSubUri.SwitchSub(index);
    }
    public boolean IsSubEnable(){
    	return mSubConfig.getSubEN();
    }
    public void setSubEnable(boolean EN){
    	mSubConfig.setSubEN(EN);
    }
    public int getCurrentSubIndex(){
    	if(mUsableSubUri.getSub() == null || !IsSubEnable())return -1;
    	return mUsableSubUri.indexOf(mUsableSubUri.getSub().getUri());
    }
    
	public Element getElement(long time) {
		// TODO Auto-generated method stub
		JoyplusSub mSub = mUsableSubUri.getSub();
		if(mSub == null || !IsSubEnable()) return null;
		int start = 0;
		int end   = mSub.elements.size()-1;
		if(end<start || end==0)return null;
		if(time>mSub.elements.get(end).getStartTime().getTime())return null;
		while(start < end){			
			if(mSub.elements.get(getMiddle(start,end)).getStartTime().getTime()>time){
				end   = getMiddle(start,end);
			}else if(mSub.elements.get(getMiddle(start,end)).getStartTime().getTime()<time){
				start = getMiddle(start,end);
			}else if(mSub.elements.get(getMiddle(start,end)).getStartTime().getTime()==time){
				return mSub.elements.get(getMiddle(start,end));
			}
			if(start >end )return null;
			if(start == end ){
				if( mSub.elements.get(getMiddle(start,end)).getStartTime().getTime()<time 
						&& (getMiddle(start,end)+1)<mSub.elements.size()){	
					 return mSub.elements.get(getMiddle(start,end)+1);
				}else
					 return mSub.elements.get(getMiddle(start,end)); 
			}else if((end - start)==1){
				if(mSub.elements.get(end).getStartTime().getTime()<time){
					 if(end>=(mSub.elements.size()-1))end=mSub.elements.size()-2;
					 return mSub.elements.get(end+1);
				}else if(mSub.elements.get(start).getStartTime().getTime()<time)
					 return mSub.elements.get(end);
				else return mSub.elements.get(start);
			}
		}
		return null;
	}
	private int getMiddle(int index,int MAX ,int MIN){
		if(index%2 != 0){
			index++;
		}
		if(index/2>=MAX)return MAX;
		if(index/2<=MIN)return MIN;
		return index/2;
	}
	private int getMiddle(int Start , int End ){
		return getMiddle(Start+End,End,Start);
	}
}