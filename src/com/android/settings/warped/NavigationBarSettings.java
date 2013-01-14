package com.android.settings.warped;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.widgets.SeekBarPreference;
import com.android.settings.util.Helpers;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class NavigationBarSettings extends SettingsPreferenceFragment implements
OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
	
    private static final String PREF_NAV_ICON = "navbar_icon_list";
    private static final String PREF_NAV_BAR_COLOR = "interface_navbar_color";
    private static final String PREF_NAV_BAR_COLOR_DEF = "interface_navbar_color_default";
    private static final String PREF_NAV_COLOR = "nav_button_color";
    private static final String PREF_NAV_GLOW_COLOR = "nav_button_glow_color";
    private static final String PREF_GLOW_TIMES = "glow_times";
    private static final String KEY_NAV_BUTTONS_HEIGHT = "nav_buttons_height";
	
    ListPreference mNavigationBarTransparency;
    ListPreference mNavBarIcon;
    ListPreference mGlowTimes;
    ListPreference mNavButtonsHeight;
    ColorPickerPreference mNavigationBarColor;
    ColorPickerPreference mNavigationBarGlowColor;
    SeekBarPreference mButtonAlpha;
    ColorPickerPreference mNavBar;
    Preference mStockColor;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.navbar_settings);
		
        mNavBarIcon = (ListPreference) findPreference(PREF_NAV_ICON);
        mNavBarIcon.setOnPreferenceChangeListener(this);
        mNavBarIcon.setValue((Settings.System.getInt(getActivity()
		.getContentResolver(), Settings.System.NAVBAR_STYLE_ICON,
		 0)) + "");

	final float defaultButtonAlpha = Settings.System.getFloat(getActivity()
		.getContentResolver(), Settings.System.NAVIGATION_BAR_BUTTON_ALPHA, 0.6f);

        mButtonAlpha = (SeekBarPreference) findPreference("button_transparency");
        mButtonAlpha.setInitValue((int) (defaultButtonAlpha * 100));
        mButtonAlpha.setOnPreferenceChangeListener(this);
		
        mNavigationBarColor = (ColorPickerPreference) findPreference(PREF_NAV_COLOR);
        mNavigationBarColor.setOnPreferenceChangeListener(this);
		
        mNavigationBarGlowColor = (ColorPickerPreference) findPreference(PREF_NAV_GLOW_COLOR);
        mNavigationBarGlowColor.setOnPreferenceChangeListener(this);

	mNavBar = (ColorPickerPreference) findPreference(PREF_NAV_BAR_COLOR);
	mNavBar.setOnPreferenceChangeListener(this);
	
        mGlowTimes = (ListPreference) findPreference(PREF_GLOW_TIMES);
        mGlowTimes.setOnPreferenceChangeListener(this);
		
	mStockColor = (Preference) findPreference(PREF_NAV_BAR_COLOR_DEF);
	mStockColor.setOnPreferenceClickListener(this);

	mNavButtonsHeight = (ListPreference) findPreference(KEY_NAV_BUTTONS_HEIGHT);
        mNavButtonsHeight.setOnPreferenceChangeListener(this);
		
        int statusNavButtonsHeight = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
															Settings.System.NAV_BUTTONS_HEIGHT, 48);
        mNavButtonsHeight.setValue(String.valueOf(statusNavButtonsHeight));
        mNavButtonsHeight.setSummary(mNavButtonsHeight.getEntry());
		
		setHasOptionsMenu(true);
       		updateGlowTimesSummary();
	}
	
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.nav_bar, menu);
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reset:
				
                Settings.System.putInt(getActivity().getContentResolver(),
									   Settings.System.NAVIGATION_BAR_TINT, Integer.MIN_VALUE);
                Settings.System.putInt(getActivity().getContentResolver(),
									   Settings.System.NAVIGATION_BAR_GLOW_TINT, Integer.MIN_VALUE);
				return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
	
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean result = false;
	if (preference == mNavBarIcon) {		
            int navbarStyle = Integer.valueOf((String) newValue);
	    int navIndex = mNavBarIcon.findIndexOfValue((String) newValue);
		Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
		Settings.System.NAVBAR_STYLE_ICON, navbarStyle);
		mNavBarIcon.setSummary(mNavBarIcon.getEntries()[navIndex]);
		Helpers.restartSystemUI();
		return true;
	} else if (preference == mNavigationBarColor) {
            String hex = ColorPickerPreference.convertToARGB(
			Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
			Settings.System.NAVIGATION_BAR_TINT, intHex);
            return true;
	} else if (preference == mNavBar) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVBAR_COLOR, intHex);
            return true;
        } else if (preference == mNavigationBarGlowColor) {
            String hex = ColorPickerPreference.convertToARGB(
			Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
			Settings.System.NAVIGATION_BAR_GLOW_TINT, intHex);
            return true;
	} else if (preference == mNavButtonsHeight) {
		int statusNavButtonsHeight = Integer.valueOf((String) newValue);
		int index = mNavButtonsHeight.findIndexOfValue((String) newValue);
			Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),						   
			Settings.System.NAV_BUTTONS_HEIGHT, statusNavButtonsHeight);
			mNavButtonsHeight.setSummary(mNavButtonsHeight.getEntries()[index]);
	    return true;
        } else if (preference == mGlowTimes) {
            // format is (on|off) both in MS
            String value = (String) newValue;
            String[] breakIndex = value.split("\\|");
            int onTime = Integer.valueOf(breakIndex[0]);
            int offTime = Integer.valueOf(breakIndex[1]);
			
            Settings.System.putInt(getActivity().getContentResolver(),
				Settings.System.NAVIGATION_BAR_GLOW_DURATION[0], offTime);
            Settings.System.putInt(getActivity().getContentResolver(),
				Settings.System.NAVIGATION_BAR_GLOW_DURATION[1], onTime);
            updateGlowTimesSummary();
            return true;
        } else if (preference == mButtonAlpha) {
            float val = Float.parseFloat((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
				Settings.System.NAVIGATION_BAR_BUTTON_ALPHA,
				val / 100);
            return true;
        }
		return result;
	}

	@Override
    public boolean onPreferenceClick(Preference pref) {
        // TODO Auto-generated method stub
        if (pref.equals(mStockColor)) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVBAR_COLOR, Settings.System.SYSTEMUI_NAVBAR_COLOR_DEF);
        }
        return false;
    }
	
	private void updateGlowTimesSummary() {
        int resId;
        String combinedTime = Settings.System.getString(getContentResolver(),
		Settings.System.NAVIGATION_BAR_GLOW_DURATION[1]) + "|" +
		Settings.System.getString(getContentResolver(),
		Settings.System.NAVIGATION_BAR_GLOW_DURATION[0]);
		
        String[] glowArray = getResources().getStringArray(R.array.glow_times_values);
		
        if (glowArray[0].equals(combinedTime)) {
            resId = R.string.glow_times_off;
            mGlowTimes.setValueIndex(0);
        } else if (glowArray[1].equals(combinedTime)) {
            resId = R.string.glow_times_superquick;
            mGlowTimes.setValueIndex(1);
        } else if (glowArray[2].equals(combinedTime)) {
            resId = R.string.glow_times_quick;
            mGlowTimes.setValueIndex(2);
        } else {
            resId = R.string.glow_times_normal;
            mGlowTimes.setValueIndex(3);
        }
        mGlowTimes.setSummary(getResources().getString(resId));
    }
}
