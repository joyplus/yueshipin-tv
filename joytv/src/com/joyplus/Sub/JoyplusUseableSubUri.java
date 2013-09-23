package com.joyplus.Sub;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class JoyplusUseableSubUri extends JoyplusURIList implements ISubModelChangedObserver {
    private Context mContext;
	private JoyplusSub mSub = null;
	private JoyplusSubStateTrack mStateTrack;
	public void registerListener(JoyplusSubListener listener){
		mStateTrack.registerListener(listener);
	}
	private class UseableSubUriChangedObserver implements ISubModelChangedObserver{
		@Override
		public void onSubModelChanged(SubModel model, boolean dataChanged) {
			// TODO Auto-generated method stub
			if(mSub == null){
				CheckInstanceList();
			}
		}
		@Override
		public void onInstance(SubURI sub, byte[] subtitle) {
			// TODO Auto-generated method stub
			
		}		
	}
	private synchronized void CheckInstanceList(){
		if(!Instanceing && size()>0){
			Message m = new Message();
			m.what = MSG_INSTANCE_SUBURI;
			m.obj  = get(0);
			mHandler.sendMessage(m);
		}
	}
	private final static String SubNAME = "Sub";
	private boolean Instanceing = false;
	public JoyplusSub getSub(){
		return mSub;
	}
	private void setSub(JoyplusSub sub){
		mStateTrack.notifySubChange((sub!=null?true:false));
		mSub = sub;
	}
	private final static int MSG_INSTANCE_SUBURI = 0;
    private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what){
			case MSG_INSTANCE_SUBURI:
				SubURI sub = (SubURI) msg.obj;
				if(sub != null && !Instanceing){
					JoyplusSub temp =InstanceSub(sub);
					if(temp != null)setSub(temp);
				}
				break;
			}
		}    	
    };
    public void SwitchSub(int index){
    	if(index>=size()||Instanceing) return ;
    	setSub(null);
    	CheckInstanceList();
    }
    private synchronized JoyplusSub InstanceSub(SubURI uri){
    	Instanceing = true;
    	JoyplusSub  sub = null;
    	JoyplusSubInstance mSubInstance = new JoyplusSubInstance(mContext);
    	if(mSubInstance.InstanceSub(uri) && mSubInstance.IsSubAviable()){
    		sub = mSubInstance.getJoyplusSub();
    	} 	
    	Instanceing = false;
    	if(sub!=null && sub.elements.size()>2)
    		return sub;
    	remove(uri);
    	return null;
    }
	public boolean CheckSubAviable(){
    	if(mSub != null && mSub.getElements().size()>2)return true;
    	return false;
    }
	public JoyplusUseableSubUri(Context context){
		mContext       = context;
		MAX            = JoyplusSubConfig.getInstance().getSubMax();
		mStateTrack    = new JoyplusSubStateTrack();
		registerModelChangedObserver(new UseableSubUriChangedObserver());
	}
	
	@Override
	public void onSubModelChanged(SubModel model, boolean dataChanged) {
		// TODO Auto-generated method stub
		//it was callback from temp uri
	}
	@Override
	public void onInstance(SubURI sub, byte[] subtitle) {
		// TODO Auto-generated method stub
		//it was callback from temp uri
	    if(size()>=MAX)return;
	    String filename = SubNAME+size();
	    if(SaveFile(subtitle,JoyplusSubConfig.getInstance().getSubPath(),filename)){
	    	 sub.Uri = filename;
	    	 sub.SubType = SUBTYPE.LOCAL;
	    	 add(sub);
	    };
	}  
	
	 public static byte[] getBytes(String filePath){
	        byte[] buffer = null;
	        try {
	            File file = new File(filePath);
	            FileInputStream fis = new FileInputStream(file);
	            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
	            byte[] b = new byte[1000];
	            int n;
	            while ((n = fis.read(b)) != -1) {
	                bos.write(b, 0, n);
	            }
	            fis.close();
	            bos.close();
	            buffer = bos.toByteArray();
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return buffer;
	    }
        
	    
	    public static boolean SaveFile(byte[] bfile, String filePath,String fileName) {
	        BufferedOutputStream bos = null;
	        FileOutputStream fos = null;
	        File file = null;
	        try {
	            File dir = new File(filePath);
	            if(!dir.exists()&&dir.isDirectory()){
	                dir.mkdirs();
	            }
	            file = new File(filePath+fileName);
	            if(file.isDirectory())return false;
	            if(file.exists())file.delete();
	            fos = new FileOutputStream(file);
	            bos = new BufferedOutputStream(fos);
	            bos.write(bfile);
	            return true;
	        } catch (Exception e) {
	            e.printStackTrace();
	            return false;
	        } finally {
	            if (bos != null) {
	                try {
	                    bos.close();
	                } catch (IOException e1) {
	                    e1.printStackTrace();
	                }
	            }
	            if (fos != null) {
	                try {
	                    fos.close();
	                } catch (IOException e1) {
	                    e1.printStackTrace();
	                }
	            }
	        }
	    }
	
}
