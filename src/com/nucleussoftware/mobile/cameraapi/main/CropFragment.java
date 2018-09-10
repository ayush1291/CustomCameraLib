package com.nucleussoftware.mobile.cameraapi.main;

import java.io.File;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.nucleussoftware.mobile.cameraapi.R;
import com.nucleussoftware.mobile.cameraapi.cropper.CropImageView;
import com.nucleussoftware.mobile.cameraapi.cropper.callback.CropCallback;
import com.nucleussoftware.mobile.cameraapi.cropper.callback.SaveCallback;
import com.nucleussoftware.mobile.cameraapi.utils.Constants;
import com.nucleussoftware.mobile.cameraapi.utils.Log;

@TargetApi(23)
public class CropFragment
    extends Fragment{

	//--------------------------------------------------------------------------
	private CropImageView mCropView;
	private Button        done;
	private Bitmap        imageData;
	private ProgressBar   progressBar;
	private DateExchange dataExhangeInterface;
	// -------------------------------------------------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		imageData = dataExhangeInterface.getImageData();
	}
	//--------------------------------------------------------------------------
	public static CropFragment getInstance() {
		CropFragment fragment = new CropFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}
	//--------------------------------------------------------------------------
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_crop, null, false);
	}
	//--------------------------------------------------------------------------

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    Log.dubugger("Activity Created  for "+getClass().getName());
	    
	}
	
	
	@SuppressLint("NewApi")
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mCropView   = (CropImageView) view.findViewById(R.id.cropImageView);
		done        = (Button) view.findViewById(R.id.buttonDone);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

		if(mCropView.getImageBitmap() == null){
			mCropView.setImageBitmap(imageData);
			mCropView.setCropMode(CropImageView.CropMode.FREE);
		}

		view.findViewById(R.id.button3_4).setOnClickListener(btnListener);
		view.findViewById(R.id.button4_3).setOnClickListener(btnListener);
		view.findViewById(R.id.button9_16).setOnClickListener(btnListener);
		view.findViewById(R.id.button16_9).setOnClickListener(btnListener);
		view.findViewById(R.id.buttonFree).setOnClickListener(btnListener);

		done.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mCropView.startCrop(createSaveUri(), mCropCallback,mSaveCallback);
				progressBar.setVisibility(View.VISIBLE);
			}
		});

		getView().setOnKeyListener(new View.OnKeyListener(){
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction() == KeyEvent.ACTION_DOWN){
					if(keyCode == KeyEvent.KEYCODE_BACK){
						((MainActivity) getActivity() ).setScreenFragemt(Constants.ScreenFragmentId.PREVIEW);
						return false;
					}
				}
				return false;
			}
		});
	}
	//--------------------------------------------------------------------------
	public Uri createSaveUri() {
		return Uri.fromFile(new File(getActivity().getCacheDir(), "cropped"));
	}
	//--------------------------------------------------------------------------
	private final CropCallback mCropCallback = new CropCallback(){
         @TargetApi(Build.VERSION_CODES.HONEYCOMB)
         @Override
         public void onSuccess(Bitmap cropped) {
             progressBar.setVisibility(View.INVISIBLE);
             dataExhangeInterface.setImageData(cropped);
             ((MainActivity) getActivity() ).setScreenFragemt(Constants.ScreenFragmentId.PREVIEW);
         }
         @Override
         public void onError() {
             progressBar.setVisibility(View.INVISIBLE);
             Log.dubugger("Error in crop");
         }
     };
    //--------------------------------------------------------------------------
	private final SaveCallback mSaveCallback = new SaveCallback(){
         @Override
         public void onSuccess(Uri outputUri) {
         }
         @Override
         public void onError() {
         }
     };
    //--------------------------------------------------------------------------
 	@Override
 	public void onAttach(Context context) {
 		super.onAttach(context);
 	    try{
 	    	Log.dubugger("onAttach for "+getClass().getName());
 	    	dataExhangeInterface = (DateExchange)context;	
 	    }catch(Exception e){
 	    	Log.dubugger("");
 	    }
 	    
 	}
    //--------------------------------------------------------------------------
 	@Override
 	public void onAttach(Activity context) {
 		super.onAttach(context);
 	    try{
 	    	Log.dubugger("onAttach for "+getClass().getName());
 	    	dataExhangeInterface = (DateExchange)context;	
 	    }catch(Exception e){
 	    	Log.dubugger("");
 	    }
 	    
 	}
    //--------------------------------------------------------------------------
	private final View.OnClickListener btnListener   = new View.OnClickListener(){
         @Override
         public void onClick(View v) {
             int myId = v.getId();
             if(myId == R.id.button3_4){
                 mCropView.setCropMode(CropImageView.CropMode.RATIO_3_4);
             }
             else if(myId == R.id.button4_3){
                 mCropView.setCropMode(CropImageView.CropMode.RATIO_4_3);
             }
             else if(myId == R.id.button9_16){
                 mCropView.setCropMode(CropImageView.CropMode.RATIO_9_16);
             }
             else if(myId == R.id.button16_9){
                 mCropView.setCropMode(CropImageView.CropMode.RATIO_16_9);
             }
             else if(myId == R.id.buttonFree){
                 mCropView.setCropMode(CropImageView.CropMode.FREE);
             }
         }
     };
    //--------------------------------------------------------------------------
     public interface DateExchange{
 		public Bitmap getImageData();
 		public void setImageData(Bitmap bitmap);
 	}
 	//--------------------------------------------------------------------------
}
