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

public class UserInterface extends SettingsPreferenceFragment {
	
    public static final String TAG = "UserInterface";
	
    private static final String PREF_STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";
    private static final String PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
	private static final String PREF_USE_ALT_RESOLVER = "use_alt_resolver";
	private static final String PREF_VIBRATE_NOTIF_EXPAND = "vibrate_notif_expand";
	private static final String PREF_VOLUME_ROCKER_WAKE = "volume_rocker_wake";
	private static final String PREF_VOLUME_MUSIC = "volume_music_controls";
	private static final String PREF_ENABLE_VOLUME_OPTIONS = "enable_volume_options";
	
	Preference mCustomLabel;
    CheckBoxPreference mStatusBarNotifCount;
	CheckBoxPreference mUseAltResolver;
	CheckBoxPreference mVibrateOnExpand;
	CheckBoxPreference mVolumeRockerWake;
	CheckBoxPreference mVolumeMusic;
	CheckBoxPreference mEnableVolumeOptions;
	
    String mCustomLabelText = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.user_interface_title);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.user_interface);
		
        mStatusBarNotifCount = (CheckBoxPreference) findPreference(PREF_STATUS_BAR_NOTIF_COUNT);
        mStatusBarNotifCount.setChecked(Settings.System.getBoolean(mContext
																   .getContentResolver(), Settings.System.STATUSBAR_NOTIF_COUNT,
																   false));
		
        mCustomLabel = findPreference(PREF_CUSTOM_CARRIER_LABEL);
        updateCustomLabelTextSummary();
		
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
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
										 final Preference preference) {
        if (preference == mStatusBarNotifCount) {
            Settings.System.putBoolean(mContext.getContentResolver(),
									   Settings.System.STATUSBAR_NOTIF_COUNT,
									   ((CheckBoxPreference) preference).isChecked());
            return true;
		} else if (preference == mUseAltResolver) {
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
}