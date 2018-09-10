package com.nucleussoftware.mobile.cameraapi;
//------------------------------------------------------------------------------
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.Build;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.nucleussoftware.mobile.cameraapi.main.MainActivity;
import com.nucleussoftware.mobile.cameraapi.utils.Constants;
import com.nucleussoftware.mobile.cameraapi.utils.Log;
//------------------------------------------------------------------------------
@TargetApi(Build.VERSION_CODES.GINGERBREAD) public class 
	CameraPreview 
extends 
	SurfaceView 
implements 
	SurfaceHolder.Callback {
	//--------------------------------------------------------------------------
    private SurfaceHolder 	mHolder;
    private Camera 			mCamera;
	private Context 		mContext;
    private DrawingView 	mDrawingView;
    private int 			widthOfCapture;
    private int 			heightOfCapture;
    private int 			flashMode;
    private String			mode; 
    private double			lat, longitude;
	private Rect 			mFocussedArea;
    private boolean 		isCaptureButtonPressed;
    //--------------------------------------------------------------------------
    public CameraPreview(Context context,DrawingView drawingView) {
        super(context);
        mContext 		= 	context;
        mDrawingView 	=  	drawingView;
        mHolder 		= 	getHolder();
        mHolder.addCallback(this);
    }
    //--------------------------------------------------------------------------
    public void reTakeImage(){
    	setCameraDisplayOrientation((Activity)mContext, CameraInfo.CAMERA_FACING_BACK, mCamera);
        
        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
        	 Log.dubugger("Error starting camera preview: " + e.getMessage());
        }
    }
    //--------------------------------------------------------------------------
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.dubugger("Error setting camera preview: " + e.getMessage());
        }
    }
    //--------------------------------------------------------------------------
    public void surfaceDestroyed(SurfaceHolder holder) {
    	 try {
			if(mCamera!=null){
				 mCamera.stopPreview();
				 mCamera.setPreviewCallback(null);
			 }
		} catch (Exception e) {
		}
    }
    //--------------------------------------------------------------------------

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        setCameraDisplayOrientation((Activity)mContext, CameraInfo.CAMERA_FACING_BACK, mCamera);
        
        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
        	 Log.dubugger("Error starting camera preview: " + e.getMessage());
        }
    }
    //--------------------------------------------------------------------------
    @SuppressLint("NewApi") @SuppressWarnings("deprecation")
    public static void setCameraDisplayOrientation(Activity activity,int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =	new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int 	rotation 	= 	activity.getWindowManager().getDefaultDisplay().getRotation();
        int 	degrees 	= 	0;
        switch (rotation) {
            case Surface.ROTATION_0: 	
            	degrees = 0; 
            break;
            case Surface.ROTATION_90: 	
            	degrees = 90; 
        	break; 
            case Surface.ROTATION_180: 	
            	degrees = 180; 
        	break;
            case Surface.ROTATION_270: 	
            	degrees = 270; 
        	break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
//        	info.orientation=0;
            result = (info.orientation - degrees+360) % 360;
        }
//        info.orientation=0;
        Camera.Parameters params = camera.getParameters();
        params.setRotation(result); 
        camera.setParameters(params);
        
        camera.setDisplayOrientation(result);
    }
    
    //--------------------------------------------------------------------------
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH) public void doTouchFocus(final Rect tfocusRect, boolean isCapturePressed) {
    	Log.dubugger("TouchFocus");
		try {
			final List<Camera.Area> 	focusList 	= 	new ArrayList<Camera.Area>();
			Camera.Area 				focusArea 	= 	new Camera.Area(tfocusRect, 1000);
			focusList.add(focusArea);
			Camera.Parameters 			para 		= 	mCamera.getParameters();
			para.setFocusAreas(focusList);
			para.setMeteringAreas(focusList);
			mCamera.setParameters(para);

			mFocussedArea			=	tfocusRect;
			isCaptureButtonPressed 	= 	isCapturePressed;
			mCamera.autoFocus(myAutoFocusCallback);
		} catch (Exception e) {
			e.printStackTrace();
			Log.dubugger("Unable to autofocus");
		}
	}
    //--------------------------------------------------------------------------
	/**
	 * AutoFocus callback
	 */
	AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback(){
		@Override
		public void onAutoFocus(boolean arg0, Camera arg1) {
			if (arg0){
				if(isCaptureButtonPressed){
					mCamera.takePicture(null, null, null, postView);
				}
			}
		}
	};
	//--------------------------------------------------------------------------
	@Override
	public boolean onTouchEvent(android.view.MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			  float x = event.getX();
		      float y = event.getY();
		      
		      Rect touchRect = new Rect(
		    		(int)(x - 100), 
		  	        (int)(y - 100), 
		  	        (int)(x + 100), 
		  	        (int)(y + 100));
		      
		      final Rect targetFocusRect = new Rect(
						touchRect.left * 2000/this.getWidth() - 1000,
					    touchRect.top * 2000/this.getHeight() - 1000,
					    touchRect.right * 2000/this.getWidth() - 1000,
					    touchRect.bottom * 2000/this.getHeight() - 1000);
		      
		      doTouchFocus(targetFocusRect,false);
		      mDrawingView.setHaveTouch(true, touchRect);
		      mDrawingView.invalidate();
		    	  
		    	  // Remove the square after some time
		    	  Handler handler = new Handler();
		    	  handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						mDrawingView.setHaveTouch(false, new Rect(0, 0, 0, 0));
						mDrawingView.invalidate();
					}
				}, 1000);
		      
		  }
		return false;
	}
	//--------------------------------------------------------------------------
    public Camera getmCamera() {
		return mCamera;
	}
    //--------------------------------------------------------------------------
	public void setmCamera(Camera mCamera) {
		this.mCamera = mCamera;
		initCameraParams();
	}
	//--------------------------------------------------------------------------
	public void initCameraParams(){
		Camera.Parameters 	l_parameters = mCamera.getParameters();
							l_parameters.setJpegQuality(100);
							l_parameters.setPictureFormat(ImageFormat.JPEG);
							
							
		if(widthOfCapture!=0 && heightOfCapture!=0){
			l_parameters.setPictureSize(widthOfCapture, heightOfCapture);
		}
		List<String> modes = l_parameters.getSupportedSceneModes();
		/*for(int i=0; i<modes.size(); i++){
			System.out.println(modes.get(i));
		}*/
		
		setFlashMode(l_parameters);
		if(mode!=null){
			l_parameters.setSceneMode(mode);
		}
		mCamera.setParameters(l_parameters);
		
	}
	//--------------------------------------------------------------------------
	public void captureImage(){
		if(mFocussedArea!=null){
			 doTouchFocus(mFocussedArea,true);
		 }else{
			 mCamera.takePicture(null, null, null, postView);
		 }
	}
	//--------------------------------------------------------------------------
	private PictureCallback postView = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
        	Log.dubugger("postView postView taken");
        	mCamera.cancelAutoFocus();
            ((MainActivity)mContext).setImageData(BitmapFactory.decodeByteArray(data, 0, data.length));
            ((MainActivity)mContext).setRotateAngle(90);
   			((MainActivity)mContext).setScreenFragemt(1);
        }
    };
    //--------------------------------------------------------------------------	    
	public void setCameraResolution(int p_width, int p_height){
		 
		 Camera.Parameters 	l_params = mCamera.getParameters();
		 					l_params.setPictureSize(p_width, p_height);
		 mCamera.setParameters(l_params);
	 }
	//--------------------------------------------------------------------------
	public int getWidthOfCapture() {
		return widthOfCapture;
	}
	//--------------------------------------------------------------------------
	public void setWidthOfCapture(int widthOfCapture) {
		this.widthOfCapture = widthOfCapture;
	}
	//--------------------------------------------------------------------------
	public int getHeightOfCapture() {
		return heightOfCapture;
	}
	//--------------------------------------------------------------------------
	public void setHeightOfCapture(int heightOfCapture) {
		this.heightOfCapture = heightOfCapture;
	}
	//--------------------------------------------------------------------------
    public int getFlashMode() {
		return flashMode;
	}
    //--------------------------------------------------------------------------
	public void setFlashMode(int flashMode) {
		this.flashMode = flashMode;
	}
	//--------------------------------------------------------------------------
	public String getMode() {
		return mode;
	}
	//--------------------------------------------------------------------------
	public void setMode(String mode) {
		this.mode = mode;
	}
	//--------------------------------------------------------------------------
	public void setFlashMode(Camera.Parameters params){
    	
    	switch(flashMode){
    		case Constants.Settings.DEFAULT_FLASH_MODE :
			break;
    		case Constants.Settings.FLASH_OFF:
    			params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			break;
    		case Constants.Settings.FLASH_ON:
    			params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
			break;
    	}
    	mCamera.setParameters(params);
    }
	//-------------------------------------------------------------------------- 
    public void setLat(double lat2) {
    	this.lat = lat2;
    }
    //--------------------------------------------------------------------------
    public void setLongitude(double longitude) {
    	this.longitude = longitude;
    }
    //--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------