package com.nucleussoftware.mobile.cameraapi.main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.nucleussoftware.mobile.cameraapi.R;
import com.nucleussoftware.mobile.cameraapi.utils.Constants;
import com.nucleussoftware.mobile.cameraapi.utils.Log;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2) @SuppressLint("NewApi")
public class AdjustBrightnessFragment 
	extends Fragment{
	
	private              ImageView       imagePreview;
	private              SeekBar         adjustBar,               contrastBar;
	private              Button          buttonDone;
	private              Bitmap          imageBitmap;
	private              Bitmap          destBitmap;
	private              ImageView       resetView,resetContrast;
	boolean              isFirstChange;
	boolean              isBrightChange;
	ChangeBrightnessTask task;
	ChangeContrastTask   contrastTask;
	float                mBrightness;
	int                  mContrast;
	private              Button          greyScaleConvert;
	private              boolean         isContrast;
	private              DateExchange    dataExhangeInterface;
	//--------------------------------------------------------------------------
	public static AdjustBrightnessFragment getInstance() {
		AdjustBrightnessFragment fragment = new AdjustBrightnessFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
	//--------------------------------------------------------------------------
	@TargetApi(Build.VERSION_CODES.HONEYCOMB) @SuppressLint("NewApi") @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		imageBitmap =dataExhangeInterface.getImageData();
		isContrast = getArguments().getBoolean("isContrast");
		mBrightness = getArguments().getFloat("bright");
	}
	//--------------------------------------------------------------------------
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_adjust, null, false);
    }
	//--------------------------------------------------------------------------
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		imagePreview     = (ImageView) view.findViewById(R.id.imagePreview);
		adjustBar        = (SeekBar) view.findViewById(R.id.brightness_bar);
		contrastBar      = (SeekBar) view.findViewById(R.id.contrast_bar);
		buttonDone       = (Button) view.findViewById(R.id.buttonDone);
		resetView        = (ImageView) view.findViewById(R.id.reset);
		greyScaleConvert = (Button) view.findViewById(R.id.greyscale);
		resetContrast    = (ImageView) view.findViewById(R.id.reset_contrast);
	
		if(isContrast){
			view.findViewById(R.id.bar_layout).setVisibility(View.INVISIBLE);
        }else{
        	view.findViewById(R.id.contrast_bar_layout).setVisibility(View.GONE);        	
        }
		imagePreview.setImageBitmap(imageBitmap);
		setUpAdjustBar();
		destBitmap = imageBitmap;
		buttonDone.setOnClickListener(new View.OnClickListener(){
			
			@Override
			public void onClick(View v) {
				dataExhangeInterface.setImageData(destBitmap);
		        ((MainActivity)getActivity()).setScreenFragemt(Constants.ScreenFragmentId.PREVIEW);
			}
		});
		
		greyScaleConvert.setOnClickListener(new View.OnClickListener(){
			
			@Override
			public void onClick(View v) {
				GreyScaleTask greyScaleTask = new GreyScaleTask();
				greyScaleTask.execute();
			}
		});
		
		contrastBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if(contrastTask!=null && contrastTask.getStatus()==Status.RUNNING){
					contrastTask.cancel(true);
					contrastTask=null;
					contrastTask = new ChangeContrastTask();
					contrastTask.execute();
				}else{
					contrastTask = new ChangeContrastTask();
					contrastTask.execute();
				}
				
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mContrast = progress;
			}
		});
		
		adjustBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if(task!=null && task.getStatus()==Status.RUNNING){
					task.cancel(true);
					task=null;
					task = new ChangeBrightnessTask();
					task.execute();
				}else{
					task = new ChangeBrightnessTask();
					task.execute();
				}
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mBrightness = progress;
			}
		});
		
		resetView.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				adjustBar.setProgress(200);
				if(task!=null && task.getStatus()==Status.RUNNING){
					task.cancel(true);
					task = new ChangeBrightnessTask();
					task.execute();
				}else{
					task = new ChangeBrightnessTask();
					task.execute();
				}
			}
		});
		
		resetContrast.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				contrastBar.setProgress(255);
				
				if(contrastTask!=null && contrastTask.getStatus()==Status.RUNNING){
					contrastTask.cancel(true);
					contrastTask = new ChangeContrastTask();
					contrastTask.execute();
				}else{
					contrastTask = new ChangeContrastTask();
					contrastTask.execute();
				}
			}
		});
	}
	//--------------------------------------------------------------------------
	public class GreyScaleTask extends AsyncTask<Integer,Void,Bitmap>{

		@Override
		protected void onPreExecute() {
			destBitmap = Bitmap.createBitmap(imageBitmap.getWidth(), imageBitmap.getHeight(), imageBitmap.getConfig());
		}
		//----------------------------------------------------------------------
        @Override
        protected Bitmap doInBackground(Integer... params) {
        	
        	try{
	            ByteArrayOutputStream stream = new ByteArrayOutputStream();
	            destBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
	            byte[] byteArray = stream.toByteArray();
	            String path = Environment.getExternalStorageDirectory().toString();
	            File photo=new File(path+"/custom.jpg");
	            FileOutputStream out = new FileOutputStream(photo);
	            out.write(byteArray);
	            
            }catch (FileNotFoundException e){
	            e.printStackTrace();
            }catch (IOException e){
	            e.printStackTrace();
            }
            
            
            return null;
        }
        //----------------------------------------------------------------------
         @Override
         protected void onPostExecute(Bitmap result) {
         	Log.dubugger("Starting grey scale conversion  " + System.currentTimeMillis());
        	imagePreview.setImageBitmap(result);
        	Log.dubugger("Finished grey scale conversion  " + System.currentTimeMillis());
         }
        //----------------------------------------------------------------------
	}
	//--------------------------------------------------------------------------
	public class ChangeBrightnessTask extends AsyncTask<Integer,Void,Bitmap>{

		@Override
		protected void onPreExecute() {
			destBitmap = Bitmap.createBitmap(imageBitmap.getWidth(), imageBitmap.getHeight(), imageBitmap.getConfig());
		}
		//----------------------------------------------------------------------
		@Override
		protected Bitmap doInBackground(Integer... params) {
			
			Log.dubugger("Starting image Brightness");
			
			int progressPercent =20;
			int progress = (int)mBrightness;
			progressPercent = Math.abs(200-progress);
			int actualProgress = (int)(((float)progressPercent/200f)*255);
			float actuallProgressPercent = (float)(progressPercent/200f);
			final int width = imageBitmap.getWidth();
			final int height = imageBitmap.getHeight();
			int column[] = new int[width];
			
			for(int i=0; i<height; i++){
				imageBitmap.getPixels(column, 0, width, 0,i, width, 1);
				
				for(int j=0; j<column.length; j++){
					
					int pixelColor= column[j];
					int pixelAlpha = Color.alpha(pixelColor);
					int pixelRed = Color.red(pixelColor);
					int pixelGreen = Color.green(pixelColor);
					int pixelBlue = Color.blue(pixelColor);
					
					if(progress>200){
						pixelRed = (int)(actuallProgressPercent*pixelRed)+pixelRed;
						pixelGreen= (int)(actuallProgressPercent*pixelGreen)+pixelGreen;
						pixelBlue = (int)(actuallProgressPercent*pixelBlue)+pixelBlue;
					}else{
						pixelRed = pixelRed-(int)(actuallProgressPercent*pixelRed);
						pixelGreen= pixelGreen- (int)(actuallProgressPercent*pixelGreen);
						pixelBlue = pixelBlue - (int)(actuallProgressPercent*pixelBlue);
					}
					
					if(pixelRed>255){
						pixelRed=255;
					}else if(pixelRed<0){
						pixelRed=0;
					}
					if(pixelGreen>255){
						pixelGreen=255;
					}else if(pixelGreen<0){
						pixelGreen=0;
					}
					if(pixelBlue>255){
						pixelBlue=255;
					}else if(pixelBlue<0){
						pixelBlue=0;
					}
					int newPixel = Color.argb(pixelAlpha, pixelRed, pixelGreen, pixelBlue);
					column[j] = newPixel;
				}
				destBitmap.setPixels(column, 0, width, 0, i, width, 1);
			}
			return destBitmap;
		}
		//----------------------------------------------------------------------
		@Override
		protected void onPostExecute(Bitmap result) {
			
			Log.dubugger("Ending image Brightness");
			imagePreview.setImageBitmap(result);
		}
		//----------------------------------------------------------------------
	}
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
	public class ChangeContrastTask extends AsyncTask<Integer,Void,Bitmap>{
		
		@Override
		protected void onPreExecute() {
			destBitmap = Bitmap.createBitmap(imageBitmap.getWidth(), imageBitmap.getHeight(), imageBitmap.getConfig());
		}
		//----------------------------------------------------------------------
		@Override
		protected Bitmap doInBackground(Integer... params) {
			float correctionFactor=0f;
			float contrast = mContrast-255;
			correctionFactor = (259f*(contrast+255f))/(255f*(259f-contrast));
			
			final int width = imageBitmap.getWidth();
			final int height = imageBitmap.getHeight();
			int column[] = new int[width];
			
			
			for(int i=0; i<height; i++){
				imageBitmap.getPixels(column, 0, width, 0,i, width, 1);
				
				for(int j=0; j<column.length; j++){
					
					int pixelColor= column[j];
					final int pixelAlpha = Color.alpha(pixelColor);
					int pixelRed = Color.red(pixelColor);
					int pixelGreen = Color.green(pixelColor);
					int pixelBlue = Color.blue(pixelColor);
					
					pixelRed = (int)(correctionFactor*(pixelRed-128) + 128);
					pixelGreen = (int)(correctionFactor*(pixelGreen-128) + 128);
					pixelBlue = (int)(correctionFactor*(pixelBlue-128) + 128);
					
					if(pixelRed>255){
						pixelRed=255;
					}else if(pixelRed<0){
						pixelRed=0;
					}
					if(pixelGreen>255){
						pixelGreen=255;
					}else if(pixelGreen<0){
						pixelGreen=0;
					}
					if(pixelBlue>255){
						pixelBlue=255;
					}else if(pixelBlue<0){
						pixelBlue=0;
					}
					
					int newPixel = Color.argb(pixelAlpha, pixelRed, pixelGreen, pixelBlue);
					column[j] = newPixel;
				}
				destBitmap.setPixels(column, 0, width, 0, i, width, 1);
			}
			return destBitmap;
		}
		//----------------------------------------------------------------------
		@Override
		protected void onPostExecute(Bitmap result) {
			
			Log.dubugger("Ending image Brightness");
			imagePreview.setImageBitmap(result);
		}
		//----------------------------------------------------------------------
	}
	//--------------------------------------------------------------------------
	public void setUpAdjustBar(){
		if(mBrightness==0){
			mBrightness = 255;	
		}
		if(!isContrast){
	        adjustBar.setMax(400);
	        adjustBar.incrementProgressBy(1);
	        adjustBar.setProgress(200);
        }else{
        	contrastBar.setMax(510);
    		contrastBar.incrementProgressBy(1);
    		contrastBar.setProgress(255);	
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
	public interface DateExchange{
		public Bitmap getImageData();
		public void setImageData(Bitmap bitmap);
	}
	//--------------------------------------------------------------------------
}
