package com.nucleussoftware.mobile.cameraapi.utils;

public class Log {
	
	final static String TAG = "CustomCameraLib";
	final static boolean logReqd=true;
	
	public static void dubugger(String msg){
		if(logReqd){
			android.util.Log.d(TAG, msg);	
		}
		
	}

}
