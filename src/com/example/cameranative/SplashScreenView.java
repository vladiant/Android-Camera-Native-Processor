package com.example.cameranative;

import com.example.cameranative.SplashScreen;
import com.example.cameranative.R;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.widget.ImageView;

public class SplashScreenView extends ImageView {
	private SplashScreen mSplashScreen;

	public SplashScreenView(SplashScreen splashScreen) {
		super(splashScreen);
		mSplashScreen = splashScreen;

		setBackgroundDrawable(getResources().getDrawable(
				R.drawable.splashscreen));
	}

	public void onSettingsChanged() {
		postInvalidate();
	}

	private boolean mDisableOptionsPopup = false;

	void setDisableOptionsPopup(boolean disable) {
		boolean wasDisabled = mDisableOptionsPopup;
		mDisableOptionsPopup = disable;
		if (wasDisabled && !mDisableOptionsPopup && mSplashScreen != null)
			mSplashScreen.openOptionsMenu();
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (mSplashScreen != null)
				mSplashScreen.openOptionsMenu();
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (mSplashScreen == null)
			return;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}
}
