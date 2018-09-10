package com.nucleussoftware.mobile.cameraapi.main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.nucleussoftware.mobile.cameraapi.R;
import com.nucleussoftware.mobile.cameraapi.utils.Constants;
import com.nucleussoftware.mobile.cameraapi.utils.Log;


@SuppressLint("NewApi")
public class PreviewFragment
    extends Fragment
    {
	//--------------------------------------------------------------------------
	private Button       cropView,             contrast;
	private Button       adjustView;
	private ImageView    imagePreview;
	private Button       reCapture;
	private Bitmap       imageData;
	private Button       buttonDone;
	private Button       rotate;
	private DateExchange dataExhangeInterface;
	//--------------------------------------------------------------------------
	public static PreviewFragment getInstance() {
		PreviewFragment fragment = new PreviewFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}
	//--------------------------------------------------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		imageData =dataExhangeInterface.getImageData();
	}
	//--------------------------------------------------------------------------
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_preview, null, false);
	}
	//--------------------------------------------------------------------------
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    Log.dubugger("Activity Created  for "+getClass().getName());
	    
	}
	//--------------------------------------------------------------------------
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		cropView     = (Button) view.findViewById(R.id.crop);
		adjustView   = (Button) view.findViewById(R.id.bright);
		imagePreview = (ImageView) view.findViewById(R.id.imagePreview);
		reCapture    = (Button) view.findViewById(R.id.recapture);
		buttonDone   = (Button) view.findViewById(R.id.buttonDone);
		contrast     = (Button) view.findViewById(R.id.contrast);
		rotate		 = (Button) view.findViewById(R.id.rotate);

		// Setting Bitmap
		imagePreview.setImageBitmap(imageData);

		reCapture.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((MainActivity) getActivity() ).setScreenFragemt(Constants.ScreenFragmentId.CAPTURE);
			}
		});

		contrast.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				((MainActivity) getActivity() ).setScreenFragemt(Constants.ScreenFragmentId.CONTRAST);
			}
		});

		buttonDone.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// For debugg
				// SavePhotoTask2 task = new SavePhotoTask2();
				// task.execute(imageData);
				((MainActivity) getActivity() ).customFinish();
			}
		});

		cropView.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((MainActivity) getActivity() ).setScreenFragemt(Constants.ScreenFragmentId.CROP);
			}
		});

		adjustView.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((MainActivity) getActivity() ).setScreenFragemt(Constants.ScreenFragmentId.ADJUST);
			}
		});

		rotate.setOnClickListener(new View.OnClickListener(){
			
			@Override
			public void onClick(View v) {
				
				Matrix matrix = new Matrix();
				matrix.postRotate(dataExhangeInterface.getRotateAngle());
				Bitmap rotated = Bitmap.createBitmap(imageData, 0, 0, imageData.getWidth(), imageData.getHeight(),
				                                     matrix, true);
				
				dataExhangeInterface.setImageData(rotated);
				dataExhangeInterface.setRotateAngle(dataExhangeInterface.getRotateAngle()+90);
				if(dataExhangeInterface.getRotateAngle()>360){
					dataExhangeInterface.setRotateAngle(90);
				}
				imagePreview.setImageBitmap(rotated);
			}
		});
		
		getView().setOnKeyListener(new View.OnKeyListener(){

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if(event.getAction() == KeyEvent.ACTION_UP ||
				   keyCode == KeyEvent.KEYCODE_BACK){
					((MainActivity) getActivity() ).setScreenFragemt(Constants.ScreenFragmentId.CAPTURE);
					return false;
				}
				return false;
			}
		});
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
	/*
	 * This method is intended for debugging purpose only
	 */
	class SavePhotoTask2
	    extends AsyncTask<Bitmap, String, String>{
		//----------------------------------------------------------------------
		@Override
		protected String doInBackground(Bitmap... bmp) {

			String path = Environment.getExternalStorageDirectory().toString();
			File photo = new File(path + "/photoBitmap.jpg");
			File photoJpg = new File(path + "/photoBitmapjpg.jpg");
			byte[] bytes = null;
			byte[] bytesJpg = null;
			FileOutputStream out;
			FileOutputStream outJpg;
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ByteArrayOutputStream byteStreamJpg = new ByteArrayOutputStream();
			try{
				out = new FileOutputStream(photo);
				outJpg = new FileOutputStream(photoJpg);

				ByteArrayOutputStream stream = new ByteArrayOutputStream();

				bmp[0].compress(Bitmap.CompressFormat.PNG, 10, byteStream);
				bmp[0].compress(Bitmap.CompressFormat.JPEG, 95, byteStreamJpg);
				bytes = byteStream.toByteArray();
				bytesJpg = byteStreamJpg.toByteArray();
				out.write(bytes);
				outJpg.write(bytesJpg);

			}catch (FileNotFoundException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (IOException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return (null );
		}
		//----------------------------------------------------------------------
	}
	//--------------------------------------------------------------------------
	public interface DateExchange{
		public Bitmap getImageData();
		public void setImageData(Bitmap bitmap);
	    public int getRotateAngle();	
		public void setRotateAngle(int rotateAngle);
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
}
