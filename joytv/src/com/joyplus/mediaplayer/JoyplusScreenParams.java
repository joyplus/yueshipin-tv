package com.joyplus.mediaplayer;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;

public class JoyplusScreenParams {
     
     private int   WIDTH  = 0;
     private int   HEIGHT = 0;
     private float Density = (float) 0.0;
     private int   DensityDpi = 0;
     
     public final static int LINEARLAYOUT_PARAMS_16x9     = 0;
     public final static int LINEARLAYOUT_PARAMS_4x3      = 1;
     public final static int LINEARLAYOUT_PARAMS_FULL     = 2;
     public final static int LINEARLAYOUT_PARAMS_ORIGINAL = 3;
     public final static int LINEARLAYOUT_PARAMS_DEFAULT  = 1;
     public JoyplusScreenParams(Activity activity){
    	 DisplayMetrics metric=new DisplayMetrics();
    	 activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
    	 WIDTH      = metric.widthPixels;
    	 HEIGHT     = metric.heightPixels;
    	 Density    = metric.density;
    	 DensityDpi = metric.densityDpi;
    	 Log.d("JoyplusScreenParams"," WIDTH="+WIDTH+" HEIGHT="+HEIGHT);
     }
     public int getWidth(){
    	 return WIDTH;
     }
     public int getHeight(){
    	 return HEIGHT;
     }
     public float getDensity(){
    	 return Density;
     }
     public int getDensityDpi(){
    	 return DensityDpi;
     }
     public int getLinearLayoutParamsType(int type){
    	 if(type>=LINEARLAYOUT_PARAMS_16x9 && type<= LINEARLAYOUT_PARAMS_ORIGINAL)
    		 return type;
    	 else return LINEARLAYOUT_PARAMS_DEFAULT;
     }
     public LinearLayout.LayoutParams getParams(int type){
    	 switch(type){
    	 case LINEARLAYOUT_PARAMS_16x9:
    		 return CreateParams_16x9();
    	 case LINEARLAYOUT_PARAMS_4x3:
    		 return CreateParams_4x3();
    	 case LINEARLAYOUT_PARAMS_FULL:
    		 return CreateParams_FULL();
    	 case LINEARLAYOUT_PARAMS_ORIGINAL:
    		 return CreateParams_ORIGINAL();
    	 default:
    		 return CreateParams_FULL();
    	 }
     }
     private LinearLayout.LayoutParams CreateParams_16x9(){
    	 LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			 WIDTH,getParamsHeight((float)16.000/9), 1);
         params.gravity = Gravity.CENTER;
         return params;
     }
     private LinearLayout.LayoutParams CreateParams_4x3(){
    	 LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			 getParamsWidth((float)4.000/3),HEIGHT, 1);
         params.gravity = Gravity.CENTER;
         return params;
     }
     private LinearLayout.LayoutParams CreateParams_FULL(){
    	 LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			 WIDTH,HEIGHT,1);
         params.gravity = Gravity.CENTER;
         return params;
     } 
     private LinearLayout.LayoutParams CreateParams_ORIGINAL(){
    	 LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			 LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1);
         params.gravity = Gravity.CENTER;
         return params;
     }
     private int getParamsWidth(float ratio){
    	 float width = (float) 0.000; 
    	 if(WIDTH>HEIGHT)
    		 width = HEIGHT;
    	 else
    		 width = WIDTH;
    	 Log.d("JoyplusScreenParams","getParamsWidth("+ratio+") ="+(ratio*width));
    	 return (int) (ratio*width);    	 
     }
     
     private int getParamsHeight(float ratio){
    	 float height = (float) 0.000;
    	 if(WIDTH>HEIGHT)
    		 height = WIDTH;
    	 else
    		 height = HEIGHT;
    	 Log.d("JoyplusScreenParams","getParamsHeight("+ratio+") ="+(1/ratio*height));
    	 return (int)(1/ratio*height);
     }
}
