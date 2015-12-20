package de.tudarmstadt.informatik.tk.android.assistance.util;

import android.text.TextUtils;

import de.tudarmstadt.informatik.tk.android.assistance.Config;

/**
 * Validates various types of user's input
 * <p/>
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public class ValidationUtils {

    private ValidationUtils() {
    }

    /**
     * Email validation for user input
     *
     * @param target
     * @return
     */
    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    /**
     * Password length test
     *
     * @param password
     * @return
     */
    public static boolean isPasswordLengthValid(String password) {
        return password.length() > Config.PASSWORD_MIN_LENGTH;
    }

    /**
     * User's token validation
     *
     * @param token
     * @return
     */
    public static boolean isUserTokenValid(String token) {
        return token != null && !token.trim().isEmpty();
    }
}