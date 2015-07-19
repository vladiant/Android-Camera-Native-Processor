package com.example.cameranative;

import com.example.cameranative.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;

public class SplashScreen extends Activity {
	private WindowManager mWindowManager;
	private SplashScreenView mHomeView;
	public static Display sDisplay;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		sDisplay = mWindowManager.getDefaultDisplay();

		mHomeView = new SplashScreenView(this);
		setContentView(mHomeView);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.splashmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.main_start:
			Intent startActivity = new Intent(getBaseContext(),
					CameraSample.class);
			startActivity(startActivity);

			return true;

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
}