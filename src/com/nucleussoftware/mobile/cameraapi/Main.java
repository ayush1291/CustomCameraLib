package com.nucleussoftware.mobile.cameraapi;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import com.nucleussoftware.mobile.cameraapi.main.MainActivity;


public class Main extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    Intent intent = new Intent(this,MainActivity.class);
	    Bundle bundle = new Bundle();
	    bundle.putString("key", "value");
	    bundle.putInt("reqHeight", 1622);
	    bundle.putInt("reqWidth", 1224);
	    String path = Environment.getExternalStorageDirectory().toString();
	    File photo=new File(path+"/main.jpg");
	    Uri uri = Uri.fromFile(photo);
	    bundle.putParcelable(MediaStore.EXTRA_OUTPUT, uri);
	    intent.putExtra("bundle", bundle);
	    startActivityForResult(intent, 0);
	}

}
