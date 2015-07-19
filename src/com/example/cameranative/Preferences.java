package com.example.cameranative;

import java.util.List;
import java.util.Locale;

import com.example.cameranative.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class Preferences extends PreferenceActivity {

	public enum MENU_ORDER {
		CAMERA_LIST, CAMERA_RESOLUTIONS
	};

	private ListPreference mCameraResolutionPreference;

	private PreferenceScreen mPreferencesRoot;

	private OnSharedPreferenceChangeListener mSharedPrefsChangeListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setPreferenceScreen(createPreferenceHierarchy());
	}

	private PreferenceScreen createPreferenceHierarchy() {

		mPreferencesRoot = getPreferenceManager().createPreferenceScreen(this);

		int numberOfCameras = Camera.getNumberOfCameras();
		final String[] cameraNames = new String[numberOfCameras];
		String[] cameraNamesIndices = new String[numberOfCameras];

		mPreferencesRoot.addPreference(createCamerasList(cameraNames,
				cameraNamesIndices));

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		int resIndex = Integer.decode(prefs.getString(
				getResources().getString(R.string.shared_prefs_camera_list),
				"0"));

		mCameraResolutionPreference = createCamerasResolutions(
				cameraNames[resIndex], resIndex);
		mPreferencesRoot.addPreference(mCameraResolutionPreference);

		mSharedPrefsChangeListener = new OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences, String key) {
				if (key.equals(getResources().getString(
						R.string.shared_prefs_camera_list))) {
					if (mCameraResolutionPreference != null) {
						mPreferencesRoot
								.removePreference(mCameraResolutionPreference);
					}
					int resIndex = Integer.decode(sharedPreferences.getString(
							getResources().getString(
									R.string.shared_prefs_camera_list), "0"));
					mCameraResolutionPreference = createCamerasResolutions(
							cameraNames[resIndex], resIndex);
					mPreferencesRoot.addPreference(mCameraResolutionPreference);
				}
			}

		};

		prefs.registerOnSharedPreferenceChangeListener(mSharedPrefsChangeListener);

		return mPreferencesRoot;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		PreferenceManager.getDefaultSharedPreferences(getBaseContext())
				.unregisterOnSharedPreferenceChangeListener(
						mSharedPrefsChangeListener);
	}

	private ListPreference createCamerasList(String[] cameraNames,
			String[] cameraNamesIndices) {

		for (int i = 0; i < cameraNames.length; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
				cameraNames[i] = "Front Camera";
			} else if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
				cameraNames[i] = "Back Camera";
			} else {
				cameraNames[i] = "Camera " + i;
			}
			cameraNamesIndices[i] = Integer.toString(i);
		}

		Resources stringResources = getResources();
		ListPreference cameraListPreference = new ListPreference(this);
		cameraListPreference.setOrder(MENU_ORDER.CAMERA_LIST.ordinal());
		cameraListPreference.setEntries(cameraNames);
		cameraListPreference.setEntryValues(cameraNamesIndices);
		cameraListPreference.setDialogTitle(stringResources
				.getString(R.string.shared_prefs_camera_list_dialog));
		cameraListPreference.setKey(stringResources
				.getString(R.string.shared_prefs_camera_list));
		cameraListPreference.setDefaultValue(cameraNamesIndices[0]);
		cameraListPreference.setTitle(stringResources
				.getString(R.string.shared_prefs_camera_list_title));
		cameraListPreference.setSummary(stringResources
				.getString(R.string.shared_prefs_camera_list_summary));

		return cameraListPreference;
	}

	private ListPreference createCamerasResolutions(String cameraName,
			int cameraIndex) {

		Camera testCamera = Camera.open(cameraIndex);
		List<Camera.Size> sizes = testCamera.getParameters()
				.getSupportedPreviewSizes();
		int numberCameraSizes = sizes.size();
		String[] cameraSizes = new String[numberCameraSizes];
		String[] cameraSizesIndices = new String[numberCameraSizes];
		for (int i = 0; i < numberCameraSizes; i++) {
			Camera.Size currentSize = sizes.get(i);
			String sizeString = String.format(Locale.getDefault(), "%dx%d",
					currentSize.width, currentSize.height);
			cameraSizes[i] = sizeString;
			cameraSizesIndices[i] = Integer.toString(i);
		}
		testCamera.release();

		Resources stringResources = getResources();
		ListPreference cameraResolutionsPreferences = new ListPreference(this);
		cameraResolutionsPreferences.setOrder(MENU_ORDER.CAMERA_RESOLUTIONS
				.ordinal());
		cameraResolutionsPreferences.setEntries(cameraSizes);
		cameraResolutionsPreferences.setEntryValues(cameraSizesIndices);
		cameraResolutionsPreferences
				.setDialogTitle(String.format(
						stringResources
								.getString(R.string.shared_prefs_camera_resolution_dialog),
						cameraName));
		cameraResolutionsPreferences.setKey(String.format(stringResources
				.getString(R.string.shared_prefs_camera_resolution_key),
				cameraIndex));
		cameraResolutionsPreferences.setDefaultValue(cameraSizesIndices[0]);
		cameraResolutionsPreferences.setTitle(cameraName + " resolutions");
		cameraResolutionsPreferences.setSummary(stringResources
				.getString(R.string.shared_prefs_camera_resolution_summary));

		return cameraResolutionsPreferences;
	}
}
