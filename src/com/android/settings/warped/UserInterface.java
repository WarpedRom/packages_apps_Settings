package com.android.settings.warped;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.view.IWindowManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.util.Log;
import android.view.WindowManagerGlobal;

import com.android.settings.SettingsPreferenceFragment;
import net.margaritov.preference.colorpicker.ColorPickerPreference;
import com.android.settings.R;
import com.android.settings.util.Helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserInterface extends SettingsPreferenceFragment implements
OnPreferenceChangeListener {
	
    public static final String TAG = "UserInterface";
	
    private static final String PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
	private static final String PREF_USE_ALT_RESOLVER = "use_alt_resolver";
	private static final String PREF_VIBRATE_NOTIF_EXPAND = "vibrate_notif_expand";
	private static final String PREF_VOLUME_ROCKER_WAKE = "volume_rocker_wake";
	private static final String PREF_VOLUME_MUSIC = "volume_music_controls";
	private static final String PREF_ENABLE_VOLUME_OPTIONS = "enable_volume_options";
	private static final String PREF_STATUSBAR_COLOR = "statusbar_background_color";
	private static final String PREF_RECENT_KILL_ALL = "recent_kill_all";
	private static final String PREF_RAM_USAGE_BAR = "ram_usage_bar";
	private static final String KEY_EXPANDED_DESKTOP = "expanded_desktop";
	private static final String KEY_EXPANDED_DESKTOP_NO_NAVBAR = "expanded_desktop_no_navbar";
	
	
	Preference mCustomLabel;
	ColorPickerPreference mBackgroundColor;
	CheckBoxPreference mUseAltResolver;
	CheckBoxPreference mVibrateOnExpand;
	CheckBoxPreference mVolumeRockerWake;
	CheckBoxPreference mVolumeMusic;
	CheckBoxPreference mEnableVolumeOptions;
	CheckBoxPreference mRecentKillAll;
	CheckBoxPreference mRamBar;
	private ListPreference mExpandedDesktopPref;
	
    String mCustomLabelText = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.user_interface_title);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.user_interface);
	PreferenceScreen prefScreen = getPreferenceScreen();

	// Expanded desktop
        mExpandedDesktopPref = (ListPreference) findPreference(KEY_EXPANDED_DESKTOP);
   
        int expandedDesktopValue = Settings.System.getInt(getContentResolver(),
                Settings.System.EXPANDED_DESKTOP_STYLE, 0);

        // Hide no-op "Status bar visible" mode on devices without navbar
        try {
            if (WindowManagerGlobal.getWindowManagerService().hasNavigationBar()) {
                mExpandedDesktopPref.setOnPreferenceChangeListener(this);
                mExpandedDesktopPref.setValue(String.valueOf(expandedDesktopValue));
                updateExpandedDesktop(expandedDesktopValue);
 
            } else {
                prefScreen.removePreference(mExpandedDesktopPref);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error getting navigation bar status");
        }
		
        mCustomLabel = findPreference(PREF_CUSTOM_CARRIER_LABEL);
        updateCustomLabelTextSummary();

	mRecentKillAll = (CheckBoxPreference) findPreference(PREF_RECENT_KILL_ALL);
        mRecentKillAll.setChecked(Settings.System.getBoolean(getActivity ().getContentResolver(),
                Settings.System.RECENT_KILL_ALL_BUTTON, false));
	
	mRamBar = (CheckBoxPreference) findPreference(PREF_RAM_USAGE_BAR);
        mRamBar.setChecked(Settings.System.getBoolean(getActivity ().getContentResolver(),
                Settings.System.RAM_USAGE_BAR, false));
	
	mBackgroundColor = (ColorPickerPreference) findPreference(PREF_STATUSBAR_COLOR);
        mBackgroundColor.setOnPreferenceChangeListener(this);
		
		mUseAltResolver = (CheckBoxPreference) findPreference(PREF_USE_ALT_RESOLVER);
		mUseAltResolver.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
								  Settings.System.ACTIVITY_RESOLVER_USE_ALT, false));
		
		mEnableVolumeOptions = (CheckBoxPreference) findPreference(PREF_ENABLE_VOLUME_OPTIONS);
		mEnableVolumeOptions.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
								   Settings.System.ENABLE_VOLUME_OPTIONS, 0) == 1);
		
		mVibrateOnExpand = (CheckBoxPreference) findPreference(PREF_VIBRATE_NOTIF_EXPAND);
        mVibrateOnExpand.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
								   Settings.System.VIBRATE_NOTIF_EXPAND, true));
		
        mVolumeRockerWake = (CheckBoxPreference) findPreference(PREF_VOLUME_ROCKER_WAKE);
        mVolumeRockerWake.setChecked(Settings.System.getBoolean(mContext
									.getContentResolver(), Settings.System.VOLUME_WAKE_SCREEN, false));

        mVolumeMusic = (CheckBoxPreference) findPreference(PREF_VOLUME_MUSIC);
        mVolumeMusic.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
								   Settings.System.VOLUME_MUSIC_CONTROLS, false));
		
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mBackgroundColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
	    .valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
			
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
	    Settings.System.STATUSBAR_BACKGROUND_COLOR, intHex);
            return true;
	} else if (preference == mExpandedDesktopPref) {
            int expandedDesktopValue = Integer.valueOf((String) newValue);
            updateExpandedDesktop(expandedDesktopValue);
            return true;
        }
	return false;
    }
	
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
	final Preference preference) {
        if (preference == mUseAltResolver) {
			Settings.System.putBoolean(getActivity().getContentResolver(),
				Settings.System.ACTIVITY_RESOLVER_USE_ALT,
			    isCheckBoxPreferenceChecked(preference));
			return true;
		} else if (preference == mVibrateOnExpand) {
            Settings.System.putBoolean(getActivity().getContentResolver(),
		Settings.System.VIBRATE_NOTIF_EXPAND,
		((CheckBoxPreference) preference).isChecked());
            Helpers.restartSystemUI();
            return true;
	} else if (preference == mRamBar) {
            boolean checked = ((CheckBoxPreference)preference).isChecked();
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.RAM_USAGE_BAR, checked ? true : false);
            return true;
        } else if (preference == mVolumeRockerWake) {
            Settings.System.putBoolean(getActivity().getContentResolver(),
		Settings.System.VOLUME_WAKE_SCREEN,
		((CheckBoxPreference) preference).isChecked());
            return true;
		} else if (preference == mVolumeMusic) {
			
            Settings.System.putBoolean(getActivity().getContentResolver(),
		Settings.System.VOLUME_MUSIC_CONTROLS,
		((CheckBoxPreference) preference).isChecked());
            return true;
		} else  if (preference == mEnableVolumeOptions) {
			boolean checked = ((CheckBoxPreference) preference).isChecked();
			Settings.System.putInt(getActivity().getContentResolver(),
			Settings.System.ENABLE_VOLUME_OPTIONS, checked ? 1 : 0);
			return true;

	} else if (preference == mRecentKillAll) {
            boolean checked = ((CheckBoxPreference)preference).isChecked();
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.RECENT_KILL_ALL_BUTTON, checked ? true : false);
            return true;

        } else if (preference == mCustomLabel) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
			
            alert.setTitle(R.string.custom_carrier_label_title);
            alert.setMessage(R.string.custom_carrier_label_explain);
			
            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setText(mCustomLabelText != null ? mCustomLabelText : "");
            alert.setView(input);
			
            alert.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = ((Spannable) input.getText()).toString();
                    Settings.System.putString(getActivity().getContentResolver(),
											  Settings.System.CUSTOM_CARRIER_LABEL, value);
                    updateCustomLabelTextSummary();
                    Intent i = new Intent();
                    i.setAction("com.android.settings.warped.LABEL_CHANGED");
                    mContext.sendBroadcast(i);
                }
            });
            alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });
			
            alert.show();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
	
    private void updateCustomLabelTextSummary() {
        mCustomLabelText = Settings.System.getString(getActivity().getContentResolver(),
													 Settings.System.CUSTOM_CARRIER_LABEL);
        if (mCustomLabelText == null || mCustomLabelText.length() == 0) {
            mCustomLabel.setSummary(R.string.custom_carrier_label_notset);
        } else {
            mCustomLabel.setSummary(mCustomLabelText);
        }
    }
    
    private void updateExpandedDesktop(int value) {
        ContentResolver cr = getContentResolver();
        Resources res = getResources();
        int summary = -1;

        Settings.System.putInt(cr, Settings.System.EXPANDED_DESKTOP_STYLE, value);

        if (value == 0) {
            // Expanded desktop deactivated
            Settings.System.putInt(cr, Settings.System.POWER_MENU_EXPANDED_DESKTOP_ENABLED, 0);
            Settings.System.putInt(cr, Settings.System.EXPANDED_DESKTOP_STATE, 0);
            summary = R.string.expanded_desktop_disabled;
        } else if (value == 1) {
      	    Settings.System.putInt(cr, Settings.System.POWER_MENU_EXPANDED_DESKTOP_ENABLED, 1);
            summary = R.string.expanded_desktop_status_bar;
        } else if (value == 2) {
            Settings.System.putInt(cr, Settings.System.POWER_MENU_EXPANDED_DESKTOP_ENABLED, 1);
            summary = R.string.expanded_desktop_no_status_bar;
        }

        if (mExpandedDesktopPref != null && summary != -1) {
            mExpandedDesktopPref.setSummary(res.getString(summary));
        }
    }
}
