package com.nucleussoftware.mobile.cameraapi.cropper.callback;


import android.net.Uri;

public interface SaveCallback extends Callback{
    void onSuccess(Uri outputUri);
    void onError();
}
