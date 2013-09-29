package com.joyplus.sub;

import com.joyplus.common.ResourcesManager;

import android.content.Context;
import android.os.Environment;

public class JoyplusSubConfig {
       
    private static JoyplusSubConfig mConfig;
    public  static JoyplusSubConfig getInstance(){
    	return mConfig;
    }
    public JoyplusSubConfig(Context context){
	   InitResource(context);
	   mConfig = this;
    }     
	   
    private void InitResource(Context context) {
	   // TODO Auto-generated method stub
       ResourcesManager manager = ResourcesManager.newInstance(context);
	   SubEN   = Boolean.parseBoolean(context.getString(manager.getStringID("sub_en_default")));
	   SubPath = Environment.getExternalStorageDirectory()+context.getString(manager.getStringID("sub_dir"));
	   SubMax  = Integer.parseInt(context.getString(manager.getStringID("sub_max_local")));
//	   Log.d("Jas","InitResource() SubEN="+SubEN+" SubPath="+SubPath+" SubMax="+SubMax);
	}
    /*Interface of Sub EN*/
	private  boolean SubEN = false;
    public   void setSubEN(boolean EN){
	   SubEN = EN;
    }
    public   boolean getSubEN(){
	   return SubEN;
    }
    /*Interface of Sub max*/
    private  int SubMax = 0;
    public   int getSubMax(){
    	return SubMax;
    }
	/*Interface of Sub dir*/   
    private  String SubPath = "";
    public   String getSubPath(){
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
