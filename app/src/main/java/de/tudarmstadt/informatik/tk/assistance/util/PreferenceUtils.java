package de.tudarmstadt.informatik.tk.assistance.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Set;

import de.tudarmstadt.informatik.tk.assistance.Constants;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 12.07.2015
 */
public final class PreferenceUtils {

    private PreferenceUtils() {
    }

    /**
     * Puts set of strings to shared preferences
     *
     * @param context
     * @param preferenceName
     * @param preferenceValue
     */
    public static void setPreference(Context context, String preferenceName, Set<String> preferenceValue) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences
                .edit()
                .putStringSet(preferenceName, preferenceValue)
                .apply();
    }

    /**
     * Puts some string value to shared preferences
     *
     * @param context
     * @param preferenceName
     * @param preferenceValue
     */
    public static void setPreference(Context context, String preferenceName, String preferenceValue) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences
                .edit()
                .putString(preferenceName, preferenceValue)
                .apply();
    }

    /**
     * Puts some boolean value to shared preferences
     *
     * @param context
     * @param preferenceName
     * @param preferenceValue
     */
    public static void setPreference(Context context, String preferenceName, boolean preferenceValue) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences
                .edit()
                .putBoolean(preferenceName, preferenceValue)
                .apply();
    }

    /**
     * Puts some long value to shared preferences
     *
     * @param context
     * @param preferenceName
     * @param preferenceValue
     */
    public static void setPreference(Context context, String preferenceName, long preferenceValue) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences
                .edit()
                .putLong(preferenceName, preferenceValue)
                .apply();
    }

    /**
     * Gets set of strings from shared preferences
     *
     * @param context
     * @param preferenceName
     * @param defaultValue
     * @return
     */
    public static Set<String> getPreference(Context context, String preferenceName, Set<String> defaultValue) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getStringSet(preferenceName, defaultValue);
    }

    /**
     * Gets some string value from shared preferences
     *
     * @param context
     * @param preferenceName
     * @param defaultValue
     * @return
     */
    public static String getPreference(Context context, String preferenceName, String defaultValue) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(preferenceName, defaultValue);
    }

    /**
     * Gets some boolean value from shared preferences
     *
     * @param context
     * @param preferenceName
     * @param defaultValue
     * @return
     */
    public static boolean getPreference(Context context, String preferenceName, boolean defaultValue) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(preferenceName, defaultValue);
    }

    /**
     * Gets some long value from shared preferences
     *
     * @param context
     * @param preferenceName
     * @param defaultValue
     * @return
     */
    public static long getPreference(Context context, String preferenceName, long defaultValue) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getLong(preferenceName, defaultValue);
    }

    /**
     * Removes user login data from device
     *
     * @param context
     */
    public static void clearUserCredentials(Context context) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit()
                .remove(Constants.PREF_USER_TOKEN)
                .remove(Constants.PREF_USER_PASSWORD)
                .apply();
    }

    /**
     * Returns current device id saved in SharedPreferences
     *
     * @param context
     * @return
     */
    public static long getCurrentDeviceId(Context context) {
        return getPreference(context, Constants.PREF_DEVICE_ID, -1);
    }

    /**
     * Saves current device id into SharedPreferences
     *
     * @param context
     * @param value
     */
    public static void setCurrentDeviceId(Context context, long value) {
        setPreference(context, Constants.PREF_DEVICE_ID, value);
    }

    /**
     * Returns current server device id saved in SharedPreferences
     *
     * @param context
     * @return
     */
    public static long getServerDeviceId(Context context) {
        return getPreference(context, Constants.PREF_SERVER_DEVICE_ID, -1);
    }

    /**
     * Saves current server device id into SharedPreferences
     *
     * @param context
     * @param value
     */
    public static void setServerDeviceId(Context context, long value) {
        setPreference(context, Constants.PREF_SERVER_DEVICE_ID, value);
    }

    /**
     * Returns user email saved in SharedPreferences
     *
     * @return
     */
    public static String getUserEmail(Context context) {
        return getPreference(context, Constants.PREF_USER_EMAIL, "");
    }

    /**
     * Saves user email to preferences
     *
     * @param context
     * @param value
     */
    public static void setUserEmail(Context context, String value) {
        setPreference(context, Constants.PREF_USER_EMAIL, value);
    }

    /**
     * Returns user password saved in SharedPreferences
     *
     * @return
     */
    public static String getUserPassword(Context context) {
        return getPreference(context, Constants.PREF_USER_PASSWORD, "");
    }

    /**
     * Saves user password to preferences
     *
     * @param context
     * @param value
     */
    public static void setUserPassword(Context context, String value) {
        setPreference(context, Constants.PREF_USER_PASSWORD, value);
    }

    /**
     * Returns user firstname
     *
     * @param context
     * @return
     */
    public static String getUserFirstname(Context context) {
        return getPreference(context, Constants.PREF_USER_FIRSTNAME, "");
    }

    /**
     * Saves user firstname to preferences
     *
     * @param context
     * @param value
     */
    public static void setUserFirstname(Context context, String value) {
        setPreference(context, Constants.PREF_USER_FIRSTNAME, value);
    }

    /**
     * Returns user lastname
     *
     * @param context
     * @return
     */
    public static String getUserLastname(Context context) {
        return getPreference(context, Constants.PREF_USER_LASTNAME, "");
    }

    /**
     * Saves user lastname to preferences
     *
     * @param context
     * @param value
     */
    public static void setUserLastname(Context context, String value) {
        setPreference(context, Constants.PREF_USER_LASTNAME, value);
    }

    /**
     * Returns user token saved in SharedPreferences
     *
     * @return
     */
    public static String getUserToken(Context context) {
        return getPreference(context, Constants.PREF_USER_TOKEN, "");
    }

    /**
     * Saves user access token to preferences
     *
     * @param context
     * @param value
     */
    public static void setUserToken(Context context, String value) {
        setPreference(context, Constants.PREF_USER_TOKEN, value);
    }

    /**
     * Returns user is a developer status
     *
     * @param context
     * @return
     */
    public static boolean isUserDeveloper(Context context) {
        return getPreference(context, Constants.PREF_DEVELOPER_STATUS, false);
    }

    /**
     * Saves user developer status into SharedPreferences
     *
     * @param context
     * @param value
     */
    public static void setDeveloperStatus(Context context, boolean value) {
        setPreference(context, Constants.PREF_DEVELOPER_STATUS, value);
    }

    /**
     * Returns GCM was sent flag
     *
     * @param context
     * @return
     */
    public static boolean isGcmTokenWasSent(Context context) {
        return getPreference(context, Constants.PREF_GCM_TOKEN_SENT, false);
    }

    /**
     * Saves GCM was sent flag into SharedPreferences
     *
     * @param context
     * @param value
     */
    public static void setGcmTokenWasSent(Context context, boolean value) {
        setPreference(context, Constants.PREF_GCM_TOKEN_SENT, value);
    }

    /**
     * Returns ModulesTutorialShown flag
     *
     * @param context
     * @return
     */
    public static boolean isModulesTutorialShown(Context context) {
        return getPreference(context, Constants.PREF_MODULES_TUTORIAL, false);
    }

    /**
     * Saves ModulesTutorialShown flag into SharedPreferences
     *
     * @param context
     * @param value
     */
    public static void setModulesTutorialShown(Context context, boolean value) {
        setPreference(context, Constants.PREF_MODULES_TUTORIAL, value);
    }

    /**
     * Returns user picture filename
     *
     * @param context
     * @return
     */
    public static String getUserPicFilename(Context context) {
        return getPreference(context, Constants.PREF_USER_PIC, "");
    }

    /**
     * Saves user picture to preferences
     *
     * @param context
     * @param value
     */
    public static void setUserPicFilename(Context context, String value) {
        setPreference(context, Constants.PREF_USER_PIC, value);
    }

    /**
     * Returns custom assistance endpoint
     *
     * @param context
     * @return
     */
    public static String getCustomEndpoint(Context context) {
        return getPreference(context, Constants.PREF_CUSTOM_ENDPOINT, "");
    }

    /**
     * Saves custom assistance endpoint
     *
     * @param context
     * @param value
     */
    public static void setCustomEndpoint(Context context, String value) {
        setPreference(context, Constants.PREF_CUSTOM_ENDPOINT, value);
    }
}
