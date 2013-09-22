package com.joyplus.Sub;

import com.joyplus.tv.R;

import android.content.Context;

public class JoyplusSubConfig {
       
    private Context mContext;
    public JoyplusSubConfig(Context context){
	   mContext = context;
	   InitResource();
    }     
	   
    private void InitResource() {
	   // TODO Auto-generated method stub
	   SubEN   = Boolean.parseBoolean(mContext.getString(R.string.sub_en_default));
	   SubPath = mContext.getString(R.string.sub_dir);
	}
    /*Interface of Sub EN*/
	private boolean SubEN = false;
    public void setSubEN(boolean EN){
	   SubEN = EN;
    }
    public boolean getSubEN(){
	   return SubEN;
    }
	/*Interface of Sub dir*/   
    private String SubPath = "";
    public String getSubPath(){
    	return SubPath;
    }
    /*Interface of Sub mode*/
    public enum SUBMODE{
    	SETONLY   (1),
    	ADDONLY   (2),
    	AETANDADD (3);
    	private int MODE;
    	SUBMODE(int mode){
    		MODE = mode;
    	}
    	public int toInt(){
    		return MODE;
    	}
    	public boolean IsAviliableMode(int mode){
    		return (mode>=1 && mode<=3);
    	}
    }
    private SUBMODE SubMode;
    public SUBMODE getSubMode(){
    	return SubMode;
    }
    
}
