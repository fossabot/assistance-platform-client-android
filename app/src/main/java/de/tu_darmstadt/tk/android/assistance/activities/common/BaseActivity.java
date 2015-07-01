package de.tu_darmstadt.tk.android.assistance.activities.common;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.models.http.HttpErrorCode;
import de.tu_darmstadt.tk.android.assistance.models.http.response.ErrorResponse;
import de.tu_darmstadt.tk.android.assistance.utils.Constants;
import de.tu_darmstadt.tk.android.assistance.utils.Toaster;
import de.tu_darmstadt.tk.android.assistance.utils.Utils;

/**
 * Base activity for common stuff
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected boolean mBackButtonPressedOnce;

    public BaseActivity() {
        this.mBackButtonPressedOnce = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Processes error response from server
     *
     * @param errorResponse
     */
    protected void handleError(ErrorResponse errorResponse, String TAG) {

        Integer apiResponseCode = errorResponse.getCode();
        String apiMessage = errorResponse.getMessage();
        int httpResponseCode = errorResponse.getStatusCode();
        HttpErrorCode.ErrorCode apiErrorType = HttpErrorCode.fromCode(apiResponseCode);

        Log.d(TAG, "Response status: " + httpResponseCode);
        Log.d(TAG, "Response code: " + apiResponseCode);
        Log.d(TAG, "Response message: " + apiMessage);

        if (httpResponseCode == 400) {

            switch (apiErrorType) {

                case EMAIL_ALREADY_EXISTS:
                    Toaster.showLong(this, R.string.error_email_exists);
                    break;
                default:
                    Toaster.showLong(this, R.string.error_unknown);
                    break;
            }

            Utils.showKeyboard(getApplicationContext(), getCurrentFocus());
        }
    }

    /**
     * Returns user email saved in SharedPreferences
     *
     * @return
     */
    protected String getUserEmail() {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String userEmail = sp.getString(Constants.PREF_USER_EMAIL, "");

        return userEmail;
    }

    /**
     * Returns user token saved in SharedPreferences
     *
     * @return
     */
    protected String getUserToken() {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        return sp.getString(Constants.PREF_USER_TOKEN, "");
    }
}
