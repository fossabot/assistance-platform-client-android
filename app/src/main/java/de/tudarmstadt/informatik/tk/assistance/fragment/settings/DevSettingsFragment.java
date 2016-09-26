package de.tudarmstadt.informatik.tk.assistance.fragment.settings;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;

import java.io.IOException;
import java.util.Arrays;

import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.assistance.R.string;
import de.tudarmstadt.informatik.tk.assistance.R.xml;
import de.tudarmstadt.informatik.tk.assistance.activity.SettingsActivity;
import de.tudarmstadt.informatik.tk.assistance.event.PermissionGrantedEvent;
import de.tudarmstadt.informatik.tk.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.assistance.sdk.Config;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.AppUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.PermissionUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.StorageUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.assistance.util.PreferenceUtils;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 29.06.2015
 */
public class DevSettingsFragment extends
        PreferenceFragment implements
        OnSharedPreferenceChangeListener {

    private static final String TAG = DevSettingsFragment.class.getSimpleName();

    private EditTextPreference editEndpointUrlPref;

    private Preference exportDbPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(xml.preference_development);

        Toolbar mParentToolbar = ((SettingsActivity) getActivity()).getToolBar();

        if (mParentToolbar != null) {
            mParentToolbar.setTitle(string.settings_header_development_title);
        }

        boolean isUserDeveloper = PreferenceUtils.isUserDeveloper(getActivity());

        com.cgollner.unclouded.preferences.SwitchPreferenceCompat beDevPref = (com.cgollner.unclouded.preferences.SwitchPreferenceCompat) findPreference("pref_be_developer");
        beDevPref.setChecked(isUserDeveloper);

        exportDbPref = findPreference("pref_export_database");

        String customEndpoint = PreferenceUtils.getCustomEndpoint(getActivity());

        editEndpointUrlPref = (EditTextPreference) findPreference("pref_edit_endpoint_url");

        if (!AppUtils.isDebug(getActivity())) {
            editEndpointUrlPref.setEnabled(false);
        }

        if (customEndpoint.isEmpty()) {

            editEndpointUrlPref.setTitle(editEndpointUrlPref.getTitle() + " " + Config.ASSISTANCE_ENDPOINT);
            editEndpointUrlPref.setText(Config.ASSISTANCE_ENDPOINT);

        } else {

            editEndpointUrlPref.setTitle(editEndpointUrlPref.getTitle() + " " + customEndpoint);
            editEndpointUrlPref.setText(customEndpoint);
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if ("pref_be_developer".equals(key)) {

            boolean isDeveloperSwitchEnabled = sharedPreferences.getBoolean("pref_be_developer", false);

            if (isDeveloperSwitchEnabled) {
                Log.d(TAG, "Developer mode is ENABLED.");
            } else {
                Log.d(TAG, "Developer mode is DISABLED.");
            }

            exportDbPref.setEnabled(isDeveloperSwitchEnabled);

            if (AppUtils.isDebug(getActivity())) {
                editEndpointUrlPref.setEnabled(isDeveloperSwitchEnabled);
            }

            PreferenceUtils.setDeveloperStatus(getActivity(), isDeveloperSwitchEnabled);
        }

        if ("pref_edit_endpoint_url".equals(key)) {

            Log.d(TAG, "User clicked custom endpoint url");

            String customEndpoint = sharedPreferences.getString("pref_edit_endpoint_url", "");
            EditTextPreference editEndpointUrlPref = (EditTextPreference) findPreference("pref_edit_endpoint_url");

            if (customEndpoint.isEmpty()) {

                editEndpointUrlPref.setTitle(getString(string.settings_edit_endpoint_url) + ' ' + Config.ASSISTANCE_ENDPOINT);
                editEndpointUrlPref.setText(Config.ASSISTANCE_ENDPOINT);

            } else {

                editEndpointUrlPref.setTitle(getString(string.settings_edit_endpoint_url) + ' ' + customEndpoint);
                editEndpointUrlPref.setText(customEndpoint);
            }

            PreferenceUtils.setCustomEndpoint(getActivity(), customEndpoint);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if ("pref_be_developer".equals(preference.getKey())) {

            Log.d(TAG, "User clicked switch developer ON/OFF");
        }

        if ("pref_export_database".equals(preference.getKey())) {

            Log.d(TAG, "User clicked export database menu");

            checkWriteExternalStoragePermissionGranted();
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        super.onPause();
    }

    /**
     * Checks read contacts permission
     */
    private void checkWriteExternalStoragePermissionGranted() {

        boolean isGranted = PermissionUtils
                .getInstance(getActivity())
                .isGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (isGranted) {

            Log.d(TAG, "WRITE_EXTERNAL_STORAGE permission was granted.");

            exportDatabase();

        } else {

            Log.d(TAG, "WRITE_EXTERNAL_STORAGE permission NOT granted!");

            // check if explanation is needed for this permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                Toaster.showLong(getActivity(), string.permission_is_mandatory);
            }

            if (VERSION.SDK_INT >= VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Config.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    /**
     * Exports DB
     */
    private void exportDatabase() {

        try {
            StorageUtils.exportDatabase(
                    getActivity(),
                    Environment
                            .getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS).getPath() +
                            '/' +
                            Config.DATABASE_NAME);

            Toaster.showLong(getActivity(), string.settings_export_database_successful);

        } catch (IOException e) {
            Log.e(TAG, "Cannot export database to public folder. Error: ", e);
            Toaster.showLong(getActivity(), string.settings_export_database_failed);
        }
    }

    /**
     * On permission granted event
     *
     * @param event
     */
    public void onEvent(PermissionGrantedEvent event) {

        String[] permission = event.getPermissions();

        Log.d(TAG, "Permission granted: " + Arrays.toString(permission));

        if (permission == null || permission.length > 1) {
            return;
        }

        if (permission[0].equals(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            exportDatabase();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case Config.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:

                Log.d(TAG, "Back from PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE request");

                boolean result = PermissionUtils.getInstance(getActivity())
                        .handlePermissionResult(grantResults);

                if (result) {
                    exportDatabase();
                } else {
                    Toaster.showLong(getActivity(),
                            string.permission_is_mandatory);
                }

                break;
            default:
                break;
        }
    }
}