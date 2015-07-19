package com.example.cameranative;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.util.Log;

class CameraSurface extends SurfaceView implements SurfaceHolder.Callback {

	Camera mCamera;

	private static final String TAG = "CameraNativeSurface";

	public CameraSurface(Context context, Camera c) {
		super(context);

		mCamera = c;

		SurfaceHolder surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "SurfaceCreated");
		try {
			if (mCamera != null) {
				mCamera.setPreviewDisplay(holder);
			}
		} catch (Exception exception) {
			Log.e(TAG, "Exception caught during setPreviewDisplay: "
					+ exception.getMessage());
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	public void surfaceDestroyed(SurfaceHolder holder) {
	}
}
