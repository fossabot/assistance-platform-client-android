package de.tudarmstadt.informatik.tk.android.assistance.fragment.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;

import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activity.ModuleTypesPermissionActivity;
import de.tudarmstadt.informatik.tk.android.assistance.activity.SettingsActivity;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 29.06.2015
 */
public class AppSettingsFragment extends PreferenceFragment {

    private static final String TAG = AppSettingsFragment.class.getSimpleName();

    private Toolbar mParentToolbar;

    public AppSettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_application);

        mParentToolbar = ((SettingsActivity) getActivity()).getToolBar();
        mParentToolbar.setTitle(R.string.settings_header_application_title);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference.getKey().equals("pref_module_types_permissions")) {

            Log.d(TAG, "User clicked launch module types permission list view");

            Intent intent = new Intent(getActivity(), ModuleTypesPermissionActivity.class);
            getActivity().startActivity(intent);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}