package com.joyplus.utils;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.storage.StorageManager;

public class PackageUtils {
	
	public static final String TAG = "PackageUtils";
	
	
	public static ApplicationInfo getApplicationInfo(Context c, String apkPath){
		PackageManager pm = c.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
		if(info != null){ 
			 ApplicationInfo appInfo = info.applicationInfo;
			 return appInfo;
		}else{
			return null;
		}
	}
	
	public static PackageInfo getAppPackageInfo(Context c, String apkPath){
		PackageManager pm = c.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
		return info;
	}
	
	public static String getAppPackageName(Context c, String apkPath){
		PackageManager pm = c.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
		if(info != null){ 
			 return info.packageName;
		}else{
			return null;
		}
	}
	
	public static String getAppVersionName(Context c, String apkPath){
		PackageManager pm = c.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
		if(info != null){ 
			 return info.versionName;
		}else{
			return null;
		}
		
	}
	
	public static int getAppVersionCode(Context c, String apkPath){
		PackageManager pm = c.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
		if(info != null){ 
			 return info.versionCode;
		}else{
			return 0;
		}
		
	}
	
	public static Drawable getAppIcon(Context c, ApplicationInfo appInfo){
		PackageManager pm = c.getPackageManager();
		return pm.getApplicationIcon(appInfo);
	}
	
	public static String getAppName(Context c, ApplicationInfo appInfo){
		PackageManager pm = c.getPackageManager();
		return pm.getApplicationLabel(appInfo).toString();  
	}
	
	public static boolean isInstalled(Context c, String packageName,int versionCode) {
        List<PackageInfo> pakageinfos = c.getPackageManager().getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (PackageInfo pi : pakageinfos) {
            String pi_packageName = pi.packageName;
//            int pi_versionCode = pi.versionCode;
            //如果这个包名在系统已经安装过的应用中存在
            if(packageName.endsWith(pi_packageName)&&versionCode==pi.versionCode){
            	return true;
//               if(versionCode==pi_versionCode){
//                   Log.i("test","已经安装，不用更新，可以卸载该应�?);
//                   return INSTALLED;
//               }else if(versionCode>pi_versionCode){
//                   Log.i("test","已经安装，有更新");  
//                   return INSTALLED_UPDATE;
//               }
            }
        }
        return false;
    }
	
	public static boolean isNeedInstalled(Context c, String packageName,int versionCode) {
        List<PackageInfo> pakageinfos = c.getPackageManager().getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (PackageInfo pi : pakageinfos) {
            String pi_packageName = pi.packageName;
//            int pi_versionCode = pi.versionCode;
            //如果这个包名在系统已经安装过的应用中存在
            if(packageName.equalsIgnoreCase(pi_packageName)&&versionCode<=pi.versionCode){
            	return false;
//               if(versionCode==pi_versionCode){
//                   Log.i("test","已经安装，不用更新，可以卸载该应�?);
//                   return INSTALLED;
//               }else if(versionCode>pi_versionCode){
//                   Log.i("test","已经安装，有更新");  
//                   return INSTALLED_UPDATE;
//               }
            }
        }
        return true;
    }
	public static boolean isInstalled(Context c, String packageName) {
		List<PackageInfo> pakageinfos = c.getPackageManager().getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		for (PackageInfo pi : pakageinfos) {
			String pi_packageName = pi.packageName;
//            int pi_versionCode = pi.versionCode;
			//如果这个包名在系统已经安装过的应用中存在
			if(packageName.equalsIgnoreCase(pi_packageName)){
				return true;
//               if(versionCode==pi_versionCode){
//                   Log.i("test","已经安装，不用更新，可以卸载该应�?);
//                   return INSTALLED;
//               }else if(versionCode>pi_versionCode){
//                   Log.i("test","已经安装，有更新");  
//                   return INSTALLED_UPDATE;
//               }
			}
		}
		return false;
	}
	
	public static String fomartSize(long size){
		if(size>(1024*1024*1024)){
			double f = Double.valueOf(size)/(1024*1024*1024);
			DecimalFormat df=new DecimalFormat("#.##");
			return  df.format(f)+"GB";
		}else if(size>(1024*1024)){
			double f = Double.valueOf(size)/(1024*1024);
			DecimalFormat df=new DecimalFormat("#.##");
			return  df.format(f)+"MB";
		}else{
			double f = size/1024;
			return Math.round(f)+"KB";
		}
		
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static String getVolumePaths(Context c){
		StorageManager mStorageManager = (StorageManager)c.getSystemService(Context.STORAGE_SERVICE);
		try { 
            Method getPaths = mStorageManager.getClass().getMethod("getVolumePaths"); 
            String[] paths = (String[]) getPaths.invoke(mStorageManager);
            String path = "";
            for(int i =0; i <paths.length; i++){
            	path += paths[i];
            	path += "\t";
            	if(mStorageManager.isObbMounted(paths[i])){
            		path += "true";
            	}else{
            		path += "false";
            	}
            	path += "\n";
            }
            return path;
        } catch (Exception e) { 
            e.printStackTrace(); 
            return null;
        } 
	}
	
//	public static long getInstalledApkSize(Context c, String packageName) throws Exception{
//		PackageManager pm = c.getPackageManager();
//		Method getPackageSizeInfo = pm.getClass().getMethod( "getPackageSizeInfo", String.class, IPackageStatsObserver.class);
//		getPackageSizeInfo.invoke(pm, packageName,
//		    new IPackageStatsObserver.Stub() {
//
//				@Override
//				public void onGetStatsCompleted(PackageStats pStats,
//						boolean succeeded) throws RemoteException {
//					// TODO Auto-generated method stub
//					long applactionSize = pStats.codeSize;
//				}
//
//		    });
//	}

}
