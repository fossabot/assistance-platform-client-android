package de.tudarmstadt.informatik.tk.assistance.fragment.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;

import de.tudarmstadt.informatik.tk.assistance.R;
import de.tudarmstadt.informatik.tk.assistance.activity.SettingsActivity;
import de.tudarmstadt.informatik.tk.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbDevice;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.device.DeviceUserDefinedNameRequestDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.ApiProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.DaoProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.api.DeviceApiProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.AppUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.assistance.util.PreferenceUtils;
import rx.Subscriber;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 29.06.2015
 */
public class DeviceSettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = DeviceSettingsFragment.class.getSimpleName();

    private Toolbar mParentToolbar;

    public DeviceSettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_user_device_info);

        mParentToolbar = ((SettingsActivity) getActivity()).getToolBar();
        mParentToolbar.setTitle(R.string.settings_header_user_device_title);

        if (!AppUtils.isDebug(getActivity().getApplicationContext())) {
            Preference sensorsList = findPreference("pref_list_of_sensors");
            sensorsList.setEnabled(false);
        }

        long currentDeviceId = PreferenceUtils.getCurrentDeviceId(getActivity().getApplicationContext());

        DbDevice dbDevice = DaoProvider.getInstance(getActivity().getApplicationContext())
                .getDeviceDao().getById(currentDeviceId);

        if (dbDevice != null) {

            String userDeviceName = dbDevice.getUserDefinedName();

            Preference deviceNameProp = findPreference("pref_device_set_title");

            if (userDeviceName != null) {
                deviceNameProp.setDefaultValue(userDeviceName);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Called when a shared preference is changed, added, or removed. This
     * may be called even if a preference is set to its existing value.
     * <p>
     * <p>This callback will be run on your main thread.
     *
     * @param sharedPreferences The {@link SharedPreferences} that received
     *                          the change.
     * @param key               The key of the preference that was changed, added, or
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        // update user defined device title
        if (key.equalsIgnoreCase("pref_device_set_title")) {

            final String userToken = PreferenceUtils.getUserToken(getActivity().getApplicationContext());
            final long currentDeviceId = PreferenceUtils.getCurrentDeviceId(getActivity().getApplicationContext());
            final String deviceName = sharedPreferences.getString("pref_device_set_title", "");

            DeviceUserDefinedNameRequestDto deviceUserDefinedNameRequest = new DeviceUserDefinedNameRequestDto();

            deviceUserDefinedNameRequest.setDeviceId(currentDeviceId);
            deviceUserDefinedNameRequest.setUserDefinedName(deviceName);

            DeviceApiProvider deviceApi = ApiProvider.getInstance(getContext()).getDeviceApiProvider();

            deviceApi.setUserDefinedName(userToken, deviceUserDefinedNameRequest)
                    .subscribe(new UserDefinedNameSubscriber(currentDeviceId, deviceName));
        }
    }

    /**
     * Updates device's user defined title
     *
     * @param currentDeviceId
     * @param deviceName
     */
    private void updateDevice(long currentDeviceId, String deviceName) {

        Log.d(TAG, "Updating device's user defined name...");

        DbDevice dbDevice = DaoProvider.getInstance(getActivity().getApplicationContext())
                .getDeviceDao().getById(currentDeviceId);

        if (dbDevice == null) {
            Log.d(TAG, "Cannot update device information in db");
            return;
        }

        dbDevice.setUserDefinedName(deviceName);

        DaoProvider.getInstance(getActivity().getApplicationContext())
                .getDeviceDao().update(dbDevice);

        Log.d(TAG, "Successful finished updating device's user defined name!");

    }

    /**
     * User defined device subscriber
     */
    private class UserDefinedNameSubscriber extends Subscriber<Void> {

        private final long currentDeviceId;
        private final String deviceName;

        public UserDefinedNameSubscriber(long currentDeviceId, String deviceName) {
            this.currentDeviceId = currentDeviceId;
            this.deviceName = deviceName;
        }

        @Override
        public void onCompleted() {
            // empty
        }

        @Override
        public void onError(Throwable e) {
            Toaster.showLong(getContext(), R.string.error_service_not_available);
        }

        @Override
        public void onNext(Void aVoid) {
            updateDevice(currentDeviceId, deviceName);
            Toaster.showLong(getContext(), R.string.changes_were_saved);
        }
    }
}