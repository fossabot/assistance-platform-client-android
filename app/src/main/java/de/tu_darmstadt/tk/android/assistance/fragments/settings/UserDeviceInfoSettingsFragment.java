package de.tu_darmstadt.tk.android.assistance.fragments.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.activities.SettingsActivity;

/**
 * Created by Wladimir Schmidt on 29.06.2015.
 */
public class UserDeviceInfoSettingsFragment extends PreferenceFragment {

    private static final String TAG = UserDeviceInfoSettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_user_device_info);

        ((SettingsActivity) getActivity()).getToolBar().setTitle(R.string.settings_header_user_device_title);
    }
}
