package com.android.settings.warped;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.content.res.Configuration;
import android.text.Spannable;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.util.Helpers;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class LockScreenSettings extends SettingsPreferenceFragment 
				implements OnPreferenceChangeListener {
	
    public static final String TAG = "lockscreen_settings";
    private static final String PREF_LOCKSCREEN_ALL_WIDGETS = "lockscreen_all_widgets";
    private static final String PREF_LOCKSCREEN_AUTO_ROTATE = "lockscreen_auto_rotate";
    private static final String PREF_LOCKSCREEN_BATTERY = "lockscreen_battery";
    private static final String PREF_LOCKSCREEN_TEXT_COLOR = "lockscreen_text_color";

    CheckBoxPreference mLockscreenAllWidgets;
    CheckBoxPreference mLockscreenBattery;
    ColorPickerPreference mLockscreenTextColor;
    CheckBoxPreference mLockscreenAutoRotate;
	
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

		mLockscreenAutoRotate = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_AUTO_ROTATE);
        mLockscreenAutoRotate.setChecked(Settings.System.getBoolean(mContext
                .getContentResolver(), Settings.System.LOCKSCREEN_AUTO_ROTATE, false));

        mLockscreenBattery = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_BATTERY);
        mLockscreenBattery.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_BATTERY, false));

        mLockscreenTextColor = (ColorPickerPreference) findPreference(PREF_LOCKSCREEN_TEXT_COLOR);
        mLockscreenTextColor.setOnPreferenceChangeListener(this);

	setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
	final Preference preference) {
        if (preference == mLockscreenAllWidgets) {
            Settings.System.putBoolean(mContext.getContentResolver(),
			Settings.System.LOCKSCREEN_ALL_WIDGETS,
			((CheckBoxPreference) preference).isChecked());
            return true;
        } else if (preference == mLockscreenBattery) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_BATTERY,
                    ((CheckBoxPreference)preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mLockscreenAutoRotate) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.LOCKSCREEN_AUTO_ROTATE,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
	}
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean handled = false;
        if (preference == mLockscreenTextColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_TEXT_COLOR, intHex);
            return true;
        }
        return false;
    }
}
