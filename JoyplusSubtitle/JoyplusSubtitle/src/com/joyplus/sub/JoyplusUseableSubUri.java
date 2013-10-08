package com.joyplus.sub;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.joyplus.common.Log;


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
			//if(mSub==null && getNextInstance()!=null){
				CheckInstanceList();
			//}
		}
		@Override
		public void onInstance(SubURI sub, JoyplusSubInstance subInstance) {
			// TODO Auto-generated method stub
			
		}				
	}
	private synchronized void CheckInstanceList(){
		if(!Instanceing && size()>0){
			Message m = new Message();
			m.what = MSG_INSTANCE_SUBURI;
			m.obj  = getNextInstance();
			mHandler.sendMessage(m);
		}
	}
	private synchronized void SwitchSub(SubURI suburi){
		if(Switching || suburi==null)return;
		Message m = new Message();
		m.what = MSG_SWITCH_SUBURI;
		m.obj  = suburi;
		mHandler.sendMessage(m);
	}
	private final static String SubNAME = "Sub";
	private boolean Instanceing = false;
	private boolean Switching   = false;
	public JoyplusSub getSub(){
		return mSub;
	}
	private void setSub(JoyplusSub sub){
		mSub = sub;
		mStateTrack.notifySubChange((sub!=null?true:false));		
	}
	private final static int MSG_INSTANCE_SUBURI = 0;
	private final static int MSG_SWITCH_SUBURI   = 1;
    private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what){
			case MSG_INSTANCE_SUBURI:
				final SubURI sub = (SubURI) msg.obj;
				if(sub != null && !Instanceing){
					new Thread(new Runnable() {						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Log.e("Jas", "JoyplusUseableSubUri:InstanceSub Run--->");
							setInstanceState(true);
							JoyplusSub temp =InstanceSub(sub);
							if(temp != null && mSub == null)setSub(temp);
							setInstanceState(false);
							//if(mSub==null && getNextInstance()!=null){
								CheckInstanceList();
							//}
						}
					}).start();
				}
				break;
			case MSG_SWITCH_SUBURI:
				final SubURI subswitch = (SubURI) msg.obj;
				if(subswitch != null && !Switching && subswitch.Instanced){
					new Thread(new Runnable() {						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Log.e("Jas", "JoyplusUseableSubUri:SwitchSub Run--->");
							setSwitchState(true);
							JoyplusSub subtemp1 =InstanceSub(subswitch);
							if(subtemp1 != null){
								setSub(subtemp1);//switch ok 
							}else if(mSub == null){								
								for(SubURI sub:JoyplusUseableSubUri.this){
									if(!sub.Instanced)continue;
									JoyplusSub subtemp2 =InstanceSub(subswitch);
									if(subtemp2 != null){
										setSub(subtemp2);//switch ok 
										break;
									}
								}
							}
							setSwitchState(false);
						}
					}).start();
				}
			    break;
			}
		}    	
    };
    private void setInstanceState(boolean instanceing){
    	Instanceing = instanceing;
    	mStateTrack.notifySubInstanceState(instanceing);
    }
    private void setSwitchState(boolean switching){
    	Switching   = switching;
    	mStateTrack.notifySubSwitchState(switching);
    }
    public void SwitchSub(int index){
    	if(get(index)==null || index>=size() || Switching ||!get(index).Instanced) return;
    	SwitchSub(get(index));
    }
    private synchronized JoyplusSub InstanceSub(SubURI uri){
    	JoyplusSub  sub = null;
    	JoyplusSubInstance mSubInstance = new JoyplusSubInstance(mContext);
    	if(mSubInstance.InstanceSub(uri) && mSubInstance.IsSubAviable()){
    	//if(mSubInstance.InstanceSub(uri) ){
    	//	if(mSubInstance.IsSubAviable()){    	
	    		sub = mSubInstance.getJoyplusSub();
    		//}
    	} 	
    	if(sub!=null && sub.elements.size()>2){
    		uri.Instanced = true;
    		return sub;
    	}
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
	public void onInstance(SubURI sub, JoyplusSubInstance subInstance) {
		// TODO Auto-generated method stub
		//it was callback from temp uri
	    if(size()>=MAX)return;
	    String filename = SubNAME+size()  + subInstance.getJoyplusSub().mContentType.toExtension();
	    File subFile= SaveFile(subInstance.getSubTitle(),JoyplusSubConfig.getInstance().getSubPath(),filename);
	    if(subFile != null){
	    	 sub.Uri  = subFile.getAbsolutePath();
	    	 //sub.Uri = JoyplusSubConfig.getInstance().getSubPath()+filename+subInstance.getJoyplusSub().mContentType.toExtension();
	    	 sub.SubType = SUBTYPE.LOCAL;
	    	 sub.Instanced = false;//this should be notity useablelist to instance this suburi.
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
        
	    
	    public static File SaveFile(byte[] bfile, String filePath,String fileName) {
	        BufferedOutputStream bos = null;
	        FileOutputStream fos = null;
	        File file = null;
	        try {
	            File dir = new File(filePath);
	            if(!(dir.exists() && dir.isDirectory())){
	                dir.mkdirs();
	            }
	            file = new File(dir,fileName);
	            if(file.isDirectory())return null;
	            if(file.exists())file.delete();
	            fos = new FileOutputStream(file);
	            bos = new BufferedOutputStream(fos);
	            bos.write(bfile);
	            return file;
	        } catch (Exception e) {
	            e.printStackTrace();
	            return null;
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
