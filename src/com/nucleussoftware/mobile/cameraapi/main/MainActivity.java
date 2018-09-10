package com.nucleussoftware.mobile.cameraapi.main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.nucleussoftware.mobile.cameraapi.CameraPreview;
import com.nucleussoftware.mobile.cameraapi.DrawingView;
import com.nucleussoftware.mobile.cameraapi.R;
import com.nucleussoftware.mobile.cameraapi.utils.Constants;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("NewApi")
public class MainActivity
    extends Activity
    implements PreviewFragment.DateExchange,
               CropFragment.DateExchange,
               AdjustBrightnessFragment.DateExchange{
	//--------------------------------------------------------------------------
	private Camera        mCamera;
	private CameraPreview mPreview;
	private DrawingView   mDrawingView;
	private ImageView     mCapture;
	private Bitmap        ImageData;
	private View          surfaceLayout;
	private FrameLayout   preview;
	private Spinner       cameraSizes;
	private Spinner       cameraMode;
	private ImageView     flashView;
	private double        lat,longitude;
	private int           reqHeight,reqWidth;
	private float         mBrightness;
	private Uri           imagePath;
	private ProgressBar   progressBar;
	private boolean       isCamOpened;
	private Bundle        bundle;
	private int			  rotateAngle;
	//--------------------------------------------------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_main_cam);
		
		bundle          = getIntent().getBundleExtra("bundle");
		lat             = bundle.getDouble("lat");
		longitude       = bundle.getDouble("long");
		mBrightness     = bundle.getFloat("bright");
		reqHeight       = bundle.getInt("reqHeight");
		reqWidth        = bundle.getInt("reqWidth");
		imagePath       = bundle.getParcelable(MediaStore.EXTRA_OUTPUT);
		surfaceLayout   = findViewById(R.id.surfacevc);
		cameraSizes     = (Spinner) findViewById(R.id.resolution_spinner);
		cameraMode      = (Spinner) findViewById(R.id.modes_spinner);
		flashView       = (ImageView) findViewById(R.id.flashView);
		progressBar     = (ProgressBar) findViewById(R.id.progressBar1);
		mCamera         = getCameraInstance();
		//----------------------------------------------------------------------
		isCamOpened = true;
		if(mCamera != null){
			mCapture = (ImageView) findViewById(R.id.capture);
			
			// Setting supported sizes
			final List<String> sizes = getSupportedPictureSize(mCamera.getParameters());
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
			                                                            this,
			                                                            R.layout.spinner_resolution_itme,
			                                                            sizes);
			dataAdapter.setDropDownViewResource(R.layout.spinner_resolution_dropdown_itme);
			cameraSizes.setAdapter(dataAdapter);

			// Setting different screen modes
			final List<String> sceneModes = getSupportedPictureModes(mCamera.getParameters());
			if(sceneModes != null && sceneModes.size() > 0){
				ArrayAdapter<String> dataAdapterForModes = new ArrayAdapter<String>(
				                                                                    this,
				                                                                    R.layout.spinner_resolution_itme,
				                                                                    sceneModes);
				dataAdapterForModes.setDropDownViewResource(R.layout.spinner_resolution_dropdown_itme);
				cameraMode.setAdapter(dataAdapterForModes);
			}
			else{
				cameraMode.setVisibility(View.GONE);
			}

			// Create our Preview view and set it as the content of our
			// activity.
			mDrawingView = (DrawingView) findViewById(R.id.drawing_surface);
			mPreview = new CameraPreview(this, mDrawingView);
			mPreview.setLat(lat);
			mPreview.setLongitude(longitude);
			checkFlash();
			mPreview.setmCamera(mCamera);
			preview = (FrameLayout) findViewById(R.id.camera_surface_frame);
			preview.addView(mPreview);
			mCapture.setOnClickListener(captureImageButtonClickListener);
			flashView.setOnClickListener(flashClickListener);

			if(reqHeight > 0){
				setUpDefaultResolution(reqWidth, reqHeight,
				                       mCamera.getParameters());
			}

			cameraSizes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					String size[] = sizes.get(arg2).split("x");
					mPreview.setWidthOfCapture(Integer.parseInt(size[0]));
					mPreview.setHeightOfCapture(Integer.parseInt(size[1]));
					Camera.Parameters params = mCamera.getParameters();
					params.setPictureSize(Integer.parseInt(size[0]),Integer.parseInt(size[1]));
					mCamera.setParameters(params);
				}
				//--------------------------------------------------------------
				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
				//--------------------------------------------------------------
			});

			cameraMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
					mPreview.setMode(sceneModes.get(arg2));
					Camera.Parameters params = mCamera.getParameters();
					params.setSceneMode(sceneModes.get(arg2));
					mCamera.setParameters(params);
				}
				//--------------------------------------------------------------
				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
				//--------------------------------------------------------------
			});
		}
		else{
		}
	}
	//--------------------------------------------------------------------------
	private OnClickListener flashClickListener = new OnClickListener(){
           @Override
           public void onClick(View v) {
               if(hasFlash()){
                   if(mPreview.getFlashMode() == Constants.Settings.FLASH_ON){
                       mPreview.setFlashMode(Constants.Settings.FLASH_OFF);
                       flashView.setImageResource(R.drawable.ic_flash_off);
                   }
                   else{
                       flashView.setImageResource(R.drawable.ic_flash_on);
                       mPreview.setFlashMode(Constants.Settings.FLASH_ON);
                   }
                   mPreview.initCameraParams();
               }

           }
       };
    //--------------------------------------------------------------------------
	public void checkFlash() {
		if(hasFlash()){
			flashView.setImageResource(R.drawable.ic_flash_off);
			mPreview.setFlashMode(Constants.Settings.FLASH_OFF);
		}
	}
	//--------------------------------------------------------------------------
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	public void setScreenFragemt(int screenName) {

		switch (screenName) {
			case Constants.ScreenFragmentId.PREVIEW :
				getFragmentManager().beginTransaction()
				                    .replace(R.id.containervc,PreviewFragment.getInstance(),"preview")
				                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				                    .commit();
				unBlockCamera();
				surfaceLayout.setVisibility(View.INVISIBLE);
				break;

			case Constants.ScreenFragmentId.CAPTURE :
				surfaceLayout.setVisibility(View.VISIBLE);
				blockCamera();
				mPreview.reTakeImage();
				getFragmentManager().beginTransaction()
				                    .remove(getFragmentManager().findFragmentById(R.id.containervc))
				                    .commit();
				break;

			case Constants.ScreenFragmentId.CROP :
				getFragmentManager().beginTransaction()
				                    .replace(R.id.containervc,CropFragment.getInstance(), "crop")
				                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				                    .commit();
				break;

			case Constants.ScreenFragmentId.CONTRAST :
				AdjustBrightnessFragment fragment = AdjustBrightnessFragment.getInstance();
				Bundle bundle = fragment.getArguments();
				bundle.putBoolean("isContrast", true);
				getFragmentManager().beginTransaction()
				                    .replace(R.id.containervc, fragment,"adjust")
				                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				                    .commit();
				break;

			case Constants.ScreenFragmentId.ADJUST :
				getFragmentManager().beginTransaction()
				                    .replace(R.id.containervc,AdjustBrightnessFragment.getInstance(),"adjust")
				                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				                    .commit();
				break;

			default :
				break;
		}

	}
	//--------------------------------------------------------------------------
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Fragment fragment = getFragmentManager().findFragmentById(R.id.containervc);
		
		if(fragment!=null){
			if(fragment instanceof PreviewFragment){
				setScreenFragemt(Constants.ScreenFragmentId.CAPTURE);
			}else{
				setScreenFragemt(Constants.ScreenFragmentId.PREVIEW);
			}
			
			return false;
		}else{
			finish();
			return true;
		}
	    
	    
	}
	//--------------------------------------------------------------------------
	public void unBlockCamera() {
		if(mCamera != null){
			mCamera.stopPreview();
			mCamera.release();
			//isCamOpened = false;
			mCamera = null;
		}
	}
	//--------------------------------------------------------------------------
	public void blockCamera() {
		try{
			mCamera = getCameraInstance();
			mPreview.setmCamera(mCamera);
			//isCamOpened = true;
		}catch (Exception e){
		}
	}
	//--------------------------------------------------------------------------
	public void captureChanges() {
		mCapture.setOnClickListener(captureImageButtonClickListener);
	}
	//--------------------------------------------------------------------------
	private OnClickListener captureImageButtonClickListener   = new OnClickListener(){

          @Override
          public void onClick(View v) {
              mPreview.captureImage();
          }
      };
    //--------------------------------------------------------------------------
	private OnClickListener reCaptureImageButtonClickListener = new OnClickListener(){

          @Override
          public void onClick(View v) {
              try{
                  mCamera.reconnect();
                  mPreview.setmCamera(mCamera);
                  mPreview.reTakeImage();
              }catch (IOException e){
                  // TODO
				  // Auto-generated
				  // catch
				  // block
                  e.printStackTrace();
              }
          }
      };
    //--------------------------------------------------------------------------
	@Override
	protected void onResume() {
		super.onResume();
		if( !isCamOpened){
			//mCamera = getCameraInstance();
			//mPreview.setmCamera(mCamera);
			blockCamera();
			mPreview.reTakeImage();
			//isCamOpened = true;
		}

	}
	//--------------------------------------------------------------------------
	public static Camera getCameraInstance() {
		Camera c = null;
		try{
			c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK); // get Cam Instance
		}catch (Exception e){
		}
		return c; // returns null if camera is unavailable
	}
	//--------------------------------------------------------------------------
	private boolean checkCameraHardware(Context context) {
		if(context.getPackageManager()
		          .hasSystemFeature(PackageManager.FEATURE_CAMERA)){
			// this device has a camera
			return true;
		}
		else{
			// no camera on this device
			return false;
		}
	}
	//--------------------------------------------------------------------------
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if(mCamera != null){
			mCamera.release();
			isCamOpened = false;
			mCamera = null;
		}
	}
	//--------------------------------------------------------------------------
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mCamera != null){
			mCamera.release();
			isCamOpened = false;
			mCamera = null;
		}
	}
	//--------------------------------------------------------------------------
	public Bitmap getImageData() {
		return ImageData;
	}
	//--------------------------------------------------------------------------
	public void setImageData(Bitmap imageData) {
		
		ImageData = imageData;
	}
	//--------------------------------------------------------------------------
	private Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

	InputStream input = getContentResolver().openInputStream(selectedImage);
	ExifInterface ei;
	if (Build.VERSION.SDK_INT > 23)
	    ei = new ExifInterface(input);
	else
	    ei = new ExifInterface(selectedImage.getPath());

	    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

	    switch (orientation) {
	        case ExifInterface.ORIENTATION_ROTATE_90:
	            return rotateImage(img, 90);
	        case ExifInterface.ORIENTATION_ROTATE_180:
	            return rotateImage(img, 180);
	        case ExifInterface.ORIENTATION_ROTATE_270:
	            return rotateImage(img, 270);
	        default:
	            return img;
	    }
	}
	//--------------------------------------------------------------------------
	private static Bitmap rotateImage(Bitmap img, int degree) {
	    Matrix matrix = new Matrix();
	    matrix.postRotate(degree);
	    Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
	    img.recycle();
	    return rotatedImg;
	}
	//--------------------------------------------------------------------------
	private List<String> getSupportedPictureModes(Camera.Parameters parameters) {

		List<String> sizeArray = new ArrayList<String>();
		sizeArray = parameters.getSupportedSceneModes();
		return sizeArray;
	}
	//--------------------------------------------------------------------------
	private List<String> getSupportedPictureSize(Camera.Parameters parameters) {
		Camera.Size result = null;
		ArrayList<String> sizeArray = new ArrayList<String>();

		for (Camera.Size size : parameters.getSupportedPictureSizes()){
			String sizeLocal = size.width + "x" + size.height;
			sizeArray.add(sizeLocal);
		}

		return sizeArray;
	}
	//--------------------------------------------------------------------------
	public boolean hasFlash() {
		if(mCamera == null){
			return false;
		}
		Camera.Parameters parameters = mCamera.getParameters();

		if(parameters.getFlashMode() == null){
			return false;
		}
		List<String> supportedFlashModes = parameters.getSupportedFlashModes();
		if(supportedFlashModes == null || supportedFlashModes.isEmpty() ||
		   supportedFlashModes.size() == 1 &&
		   supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)){
			return false;
		}
		return true;
	}
	//--------------------------------------------------------------------------
	public void customFinish() {
		SaveImageTask imageTask = new SaveImageTask();
		imageTask.execute();
	}
	//--------------------------------------------------------------------------
	private class SaveImageTask
	    extends AsyncTask<Uri, Void, Boolean>{
		//----------------------------------------------------------------------
		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}
		//----------------------------------------------------------------------
		@Override
		protected Boolean doInBackground(Uri... params) {

			String path = imagePath.getPath();
			File file = new File(path);

			// Writing image to file
			try{
				FileOutputStream stream = new FileOutputStream(file);
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				ImageData.compress(Bitmap.CompressFormat.JPEG, 100, byteStream);
				byte[] bytes = null;
				bytes = byteStream.toByteArray();
				stream.write(bytes);
				return true;
			}catch (Exception e){
				return false;
			}

		}
		//----------------------------------------------------------------------
		@Override
		protected void onPostExecute(Boolean result) {
			progressBar.setVisibility(View.INVISIBLE);
			if(result)
				setResult(RESULT_OK);
			else
				setResult(RESULT_CANCELED);
			finish();
		}

	}
	//--------------------------------------------------------------------------
	private void setUpDefaultResolution(int width, int height,
	                                    Camera.Parameters parameters) {

		Camera.Size result = null;
		List<Size> sizeArray = null;
		int small = -1;
		int large = -1;
		int equal = -1;

		int i = 0;
		for (Camera.Size size : parameters.getSupportedPictureSizes()){

			int curHeight;

			if(size.height > size.width){
				curHeight = size.height;
			}
			else{
				curHeight = size.width;
			}

			if(curHeight < height &&
			   ((small == -1 && large == -1 ) || (small == -1 && large != -1 ) || (large == -1 && small != -1 ) )){
				small = i;
			}
			else if(curHeight > height &&
			        ((small == -1 && large == -1 ) ||
			         (small == -1 && large != -1 ) || (large == -1 && small != -1 ) )){
				large = i;
			}
			else if(curHeight == height){
				equal = i;
			}
			i++;
		}
		sizeArray = parameters.getSupportedPictureSizes();
		if(sizeArray != null){
			if(equal != -1){
				cameraSizes.setSelection(equal);
			}
			else{
				if(small == -1){
					cameraSizes.setSelection(large);
				}
				else if(large == -1){
					cameraSizes.setSelection(small);
				}
				else{
					if((width - sizeArray.get(small).width < sizeArray.get(large).width -
					                                         width )){
						cameraSizes.setSelection(small);
					}
					else{
						cameraSizes.setSelection(large);
					}
				}

			}
		}

	}
	//--------------------------------------------------------------------------
    public int getRotateAngle() {
    	return rotateAngle;
    }
    //--------------------------------------------------------------------------
    public void setRotateAngle(int rotateAngle) {
    	this.rotateAngle = rotateAngle;
    }
	//--------------------------------------------------------------------------
}
