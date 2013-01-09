package com.android.settings.warped;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.widget.EditText;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.util.Helpers;

public class LockScreenSettings extends SettingsPreferenceFragment {
	
    public static final String TAG = "lockscreen_settings";
	
	private static final String PREF_LOCKSCREEN_ALL_WIDGETS = "lockscreen_all_widgets";
	
	CheckBoxPreference mLockscreenAllWidgets;
	
    String mCustomLabelText = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.lockscreen_settings_title);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.lockscreen_settings);
		
		mLockscreenAllWidgets = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_ALL_WIDGETS);
		mLockscreenAllWidgets.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
																	Settings.System.LOCKSCREEN_ALL_WIDGETS, false));
	}
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
										 final Preference preference) {
        if (preference == mLockscreenAllWidgets) {
            Settings.System.putBoolean(mContext.getContentResolver(),
									   Settings.System.LOCKSCREEN_ALL_WIDGETS,
									   ((CheckBoxPreference) preference).isChecked());
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}