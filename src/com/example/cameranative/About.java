package com.example.cameranative;

import com.example.cameranative.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class About extends PreferenceActivity {

	protected static final String TAG = "AboutCameraNative";

	static {
		System.loadLibrary("cameranativesample");
	};

	private PreferenceScreen aboutScreen;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.about);
		if (aboutScreen == null) {
			String softwareVersion = getVersionData();

			aboutScreen = getPreferenceScreen();

			PreferenceScreen versionNumberScreen = getPreferenceManager()
					.createPreferenceScreen(this);
			versionNumberScreen
					.setTitle(getString(R.string.versionNumber_text));
			versionNumberScreen.setSummary(softwareVersion);
			aboutScreen.addPreference(versionNumberScreen);
		}
		setPreferenceScreen(aboutScreen);
	}

	// Native Function Declarations
	public native String getVersionData();
}
