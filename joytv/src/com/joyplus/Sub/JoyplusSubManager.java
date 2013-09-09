package com.joyplus.Sub;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class JoyplusSubManager {

	   private boolean Debug = true;
	   private String  TAG   = "JoyplusSubManager";
	   
	   private Context mContext; 
	   
	   private JoyplusSubServer mSubServer;
	   
	   public JoyplusSubManager(Context context){
		     mContext   = context;
		     mSubServer = new JoyplusSubServer(mContext);
	   }
	   
	   public void setSubUri(SubURI string){
		   if(string == null || !mSubServer.CheckSubAviable())return;
		   List<SubURI> sub = new ArrayList<SubURI>();
		   sub.add(string);
		   setSubUri(sub);
	   }
	   public void setSubUri(List<SubURI> subUri){
		   if(subUri==null || subUri.size()<=0 ||!mSubServer.CheckSubAviable())return;
		   mSubServer.setSubUri(subUri);
	   }
	   public int getCurrentSubIndex(){
		   return mSubServer.getCurrentSubIndex();
	   }
	   public List<SubURI> getSubList(){
		   return mSubServer.getSubList();
	   }
	   public void SwitchSub(int index){
		   if(getSubList().size()<0 || index<0 || index>getSubList().size())return;
		   mSubServer.SwitchSub(index);
	   }
	   public Element getElement(long time){
		   return mSubServer.getElement(time);
	   }
}
