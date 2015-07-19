package com.example.cameranative;

import java.util.List;
import java.util.Locale;

import com.example.cameranative.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.ToggleButton;

public class CameraSample extends Activity {
	private static final String TAG = "CameraSample";

	private Camera mCamera;

	private static int[] mPrefOptsCameraResolutions;

	private GLSurfaceView mGLSurface = null;

	private CameraSurface mCamSurface = null;

	private ViewGroup mRootViewGroup = null;

	private CameraSampleRenderer mRenderer;

	private TextView[] mTextDisplay = new TextView[2];

	private ToggleButton mToggleButtonStartStop;

	private boolean mIsPreviewRunning = false;

	private byte[] mPreviewBuffer = null;

	private final int mTextBackgroundColor = 0xFF728FCE;

	private final int mTextColor = 0xFFFFFFFF;

	private float mNativeProcessTime = 10;

	private int mPreviewWidth;

	private int mPreviewHeight;

	private Handler mProfileHandler = new Handler();

	private long mProfilePeriod = 300; // milliseconds

	static {
		System.loadLibrary("cameranativesample");
	};

	private final Camera.PreviewCallback mCameraCallback = new Camera.PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera c) {

			cameraFrameTick();

			update(data, mPreviewWidth, mPreviewHeight);

			mNativeProcessTime = getNativeProcessTime();

			if (c != null) {
				c.addCallbackBuffer(mPreviewBuffer);
				c.setPreviewCallbackWithBuffer(this);
			}

			requestRender();
		}
	};

	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			float camFPS = getCameraFPS();
			String message;

			mProfileHandler.postDelayed(this, mProfilePeriod);

			message = String.format(Locale.getDefault(), "%.3f CamFPS  %dx%d",
					camFPS, mPreviewWidth, mPreviewHeight);
			mTextDisplay[0].setText(message);

			mTextDisplay[1].setText(String.format(Locale.getDefault(),
					"Native process time: %.2fms", mNativeProcessTime));

			for (int i = 0; i < mTextDisplay.length; i++) {
				if (mTextDisplay[i].getText().length() > 0) {
					mTextDisplay[i].setBackgroundColor(mTextBackgroundColor);
				} else {
					mTextDisplay[i].setBackgroundColor(0x00);
				}
			}

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();

		int screenw;
		int width;

		setContentView(R.layout.main);
		setTitle(getResources().getString(R.string.app_name));
		setTitleColor(0xFFFF5555);

		init();

		screenw = ((WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
				.getWidth();

		mTextDisplay[0] = (TextView) findViewById(R.id.text1);
		mTextDisplay[1] = (TextView) findViewById(R.id.text2);

		mRootViewGroup = (ViewGroup) mTextDisplay[0].getParent();

		width = (screenw * 1 / 2);

		for (int i = 0; i < mTextDisplay.length; i++) {
			mTextDisplay[i].setTextColor(mTextColor);
			mTextDisplay[i].setWidth(width);
			mTextDisplay[i].setText("");
		}
		mTextDisplay[0].setText("");

		updatePreferences();

		mToggleButtonStartStop = (ToggleButton) findViewById(R.id.startStopSwitch);
		mToggleButtonStartStop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (mToggleButtonStartStop.isChecked()) {
					startPreview();
				} else {
					stopPreview();
				}
			}
		});

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		int cameraIndex = Integer.decode(prefs.getString(getResources()
				.getString(R.string.shared_prefs_camera_list), "0"));

		mCamera = Camera.open(cameraIndex);

		mCamSurface = new CameraSurface(this, mCamera);
		mRootViewGroup.addView(mCamSurface);

		mRenderer = new CameraSampleRenderer(this);
		mGLSurface = mRenderer.getGLSurfaceView();
		mGLSurface.setZOrderMediaOverlay(true);
		mGLSurface.setVisibility(View.VISIBLE);
		mGLSurface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		mRootViewGroup.addView(mGLSurface);

		bringControlsToFront();

		startPreview();

		System.gc();
	}

	protected void updatePreferences() {
		// Add here reading of the external parameters.
	}

	protected void bringControlsToFront() {
		for (int i = 0; i < mTextDisplay.length; i++) {
			mTextDisplay[i].bringToFront();
		}

		mToggleButtonStartStop.bringToFront();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopPreview();
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}

		if (mRenderer != null) {
			mRenderer.cleanup();
		}

		if (mRootViewGroup != null) {
			mRootViewGroup.removeAllViews();
		}

		cleanup();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.settings:
			Intent settingsActivity = new Intent(getBaseContext(),
					Preferences.class);
			startActivity(settingsActivity);

			return true;

		case R.id.about:
			Intent aboutActivity = new Intent(getBaseContext(), About.class);
			startActivity(aboutActivity);

			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void setupCamera() {
		Camera.Parameters parameters = mCamera.getParameters();
		List<Camera.Size> sizes = mCamera.getParameters()
				.getSupportedPreviewSizes();

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		Resources stringResources = getResources();

		int cameraIndex = Integer.decode(prefs.getString(
				stringResources.getString(R.string.shared_prefs_camera_list),
				"0"));

		int cameraResolutionsIndex = Integer
				.decode(prefs.getString(
						String.format(
								stringResources
										.getString(R.string.shared_prefs_camera_resolution_key),
								cameraIndex), "0"));
		Camera.Size list = sizes.get(cameraResolutionsIndex);
		mPreviewWidth = list.width;
		mPreviewHeight = list.height;

		parameters.setPreviewFrameRate(30);
		parameters.setPreviewSize(mPreviewWidth, mPreviewHeight);

		setCameraFocusMode(parameters);

		setCameraWhiteBalanceMode(parameters);

		try {
			mCamera.setParameters(parameters);
		} catch (RuntimeException re) {
			re.printStackTrace();
			Log.e(TAG, "Unable to set Camera Parameters");
			Log.i(TAG, "Falling back to setting just the camera preview");
			parameters = mCamera.getParameters();
			parameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
			try {
				mCamera.setParameters(parameters);
			} catch (RuntimeException re2) {
				re2.printStackTrace();
				Log.e(TAG, "Problem with camera configuration, unable to set "
						+ "Camera Parameters. Camera not available.");

			}
		}

		mPreviewWidth = mCamera.getParameters().getPreviewSize().width;
		mPreviewHeight = mCamera.getParameters().getPreviewSize().height;
	}

	private void setCameraFocusMode(Camera.Parameters parameters) {
		// MODE_INFINITY is preferred mode.
		List<String> supportedFocusModes = parameters.getSupportedFocusModes();
		if (supportedFocusModes != null) {
			if (supportedFocusModes
					.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
				Log.v(TAG, "Set focus mode INFINITY");
			} else if (supportedFocusModes
					.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
				Log.v(TAG, "Set focus mode FIXED");
			} else if (supportedFocusModes
					.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
				Log.v(TAG, "Set focus mode AUTO");
			}
		}
	}

	private void setCameraWhiteBalanceMode(Camera.Parameters parameters) {
		// Set White Balance to Auto if supported.
		List<String> supportedWhiteBalance = parameters
				.getSupportedWhiteBalance();
		if (supportedWhiteBalance != null
				&& supportedWhiteBalance
						.contains(Camera.Parameters.WHITE_BALANCE_AUTO)) {
			parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
			Log.v(TAG, "Set white balance AUTO");

			// TODO: Consider selection of white balance in menu.
		}
	}

	private void startPreview() {
		if (mCamera != null) {
			setupCamera();
			setCallback();
			mCamera.startPreview();
			mIsPreviewRunning = true;
		} else {
			mIsPreviewRunning = false;
		}

		mProfileHandler.removeCallbacks(mUpdateTimeTask);
		mProfileHandler.postDelayed(mUpdateTimeTask, mProfilePeriod);
	}

	private void stopPreview() {
		if ((mCamera != null) && (mIsPreviewRunning == true)) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
		}

		mProfileHandler.removeCallbacks(mUpdateTimeTask);

		mIsPreviewRunning = false;
		System.gc();
	}

	public void setCallback() {
		int bufferSize = 0;
		int pformat;
		int bitsPerPixel;

		pformat = mCamera.getParameters().getPreviewFormat();

		PixelFormat info = new PixelFormat();
		PixelFormat.getPixelFormatInfo(pformat, info);
		bitsPerPixel = info.bitsPerPixel;

		bufferSize = mPreviewWidth * mPreviewHeight * bitsPerPixel / 8;

		mPreviewBuffer = null;

		mPreviewBuffer = new byte[bufferSize + 4096];

		mCamera.addCallbackBuffer(mPreviewBuffer);
		mCamera.setPreviewCallbackWithBuffer(mCameraCallback);
	}

	public void requestRender() {
		if (mGLSurface != null) {
			mGLSurface.requestRender();
		}
	}

	/// Native function declarations
	public native float getCameraFPS();

	public native void init();

	private native void cameraFrameTick();

	public native void update(byte[] data, int w, int h);

	private native void cleanup();

	private native float getNativeProcessTime();

}
