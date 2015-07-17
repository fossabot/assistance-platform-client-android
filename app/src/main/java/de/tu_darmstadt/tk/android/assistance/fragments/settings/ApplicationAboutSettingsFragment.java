package de.tu_darmstadt.tk.android.assistance.fragments.settings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.activities.SettingsActivity;

/**
 * Created by Wladimir Schmidt on 29.06.2015.
 */
public class ApplicationAboutSettingsFragment extends PreferenceFragment {

    private static final String TAG = ApplicationAboutSettingsFragment.class.getSimpleName();

    private static Preference.OnPreferenceClickListener aboutClickHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_app_about);

        ((SettingsActivity) getActivity()).getToolBar().setTitle(R.string.settings_about_title);

        Preference aboutPref = findPreference("pref_about_app");

        if (aboutClickHandler == null) {
            aboutClickHandler = new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showAboutInformation();
                    return false;
                }
            };
        }

        aboutPref.setOnPreferenceClickListener(aboutClickHandler);
    }

    /**
     * Shows about an app information
     */
    private void showAboutInformation() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("This an about dialog!");
        builder.create().show();
    }
}
