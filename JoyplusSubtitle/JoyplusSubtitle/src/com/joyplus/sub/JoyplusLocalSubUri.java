package com.joyplus.sub;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.content.Context;
import android.util.Log;

public class JoyplusLocalSubUri extends JoyplusSubListInatance implements ISubModelChangedObserver {
    private Context mContext;
	private class UseableSubUriChangedObserver implements ISubModelChangedObserver{
		@Override
		public void onSubModelChanged(SubModel model, boolean dataChanged) {
			// TODO Auto-generated method stub
			Log.d("Sub","Local "+size());
			SetInstanceState(mContext,false);
		}
		@Override
		public void onInstance(JoyplusSubInstance subInstance) {
			// TODO Auto-generated method stub
			
		}			
	}
	private final static String SubNAME = "Sub"; 	
	public JoyplusLocalSubUri(Context context){
		mContext       = context;
		MAX            = JoyplusSubConfig.getInstance().getSubMax();
		registerModelChangedObserver(new UseableSubUriChangedObserver());
	}	
	@Override
	public void onSubModelChanged(SubModel model, boolean dataChanged) {
		// TODO Auto-generated method stub
		//it was callback from temp uri
	}
	@Override
	public void onInstance(JoyplusSubInstance subInstance) {
		// TODO Auto-generated method stub
		//it was callback from temp uri
	    if(size()>=MAX || !subInstance.IsSubAviable())return;
	    String filename = SubNAME+size()  + subInstance.getJoyplusSub().mContentType.toExtension();
	    File subFile= SaveFile(subInstance.getSubTitle(),JoyplusSubConfig.getInstance().getSubPath(),filename);
	    if(subFile != null){
	    	subInstance.getSubURI().Uri       = subFile.getAbsolutePath();subInstance.getSubURI().SubType   = SUBTYPE.LOCAL;
	    	subInstance.getSubURI().Instanced = false;//this should be notity useablelist to instance this suburi.
	    	add(subInstance.getSubURI()); 
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
