package de.tudarmstadt.informatik.tk.android.assistance.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.models.api.error.ErrorResponse;
import de.tudarmstadt.informatik.tk.android.assistance.models.api.login.LoginRequest;
import de.tudarmstadt.informatik.tk.android.assistance.models.api.login.LoginResponse;
import de.tudarmstadt.informatik.tk.android.assistance.models.api.login.UserDevice;
import de.tudarmstadt.informatik.tk.android.assistance.services.ServiceGenerator;
import de.tudarmstadt.informatik.tk.android.assistance.services.UserService;
import de.tudarmstadt.informatik.tk.android.assistance.utils.CommonUtils;
import de.tudarmstadt.informatik.tk.android.assistance.utils.Constants;
import de.tudarmstadt.informatik.tk.android.assistance.utils.HardwareUtils;
import de.tudarmstadt.informatik.tk.android.assistance.utils.InputValidation;
import de.tudarmstadt.informatik.tk.android.assistance.utils.PreferencesUtils;
import de.tudarmstadt.informatik.tk.android.assistance.utils.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.utils.UserUtils;
import de.tudarmstadt.informatik.tk.android.assistance.views.SplashView;
import de.tudarmstadt.informatik.tk.android.kraken.db.Device;
import de.tudarmstadt.informatik.tk.android.kraken.db.DeviceDao;
import de.tudarmstadt.informatik.tk.android.kraken.db.Login;
import de.tudarmstadt.informatik.tk.android.kraken.db.LoginDao;
import de.tudarmstadt.informatik.tk.android.kraken.utils.DatabaseManager;
import de.tudarmstadt.informatik.tk.android.kraken.utils.DateUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private final String TAG = LoginActivity.class.getSimpleName();

    @Bind(R.id.email)
    protected AutoCompleteTextView mEmailTextView;

    @Bind(R.id.password)
    protected EditText mPasswordView;

    @Bind(R.id.login_progress)
    protected View mProgressView;

    @Bind(R.id.login_form)
    protected ScrollView mLoginFormView;

    @Bind(R.id.tvRegister)
    protected TextView mRegisterLink;

    @Bind(R.id.tvPasswordReset)
    protected TextView mResetPassLink;

    @Bind(R.id.sign_in_button)
    protected Button mLoginButton;

    // SOCIAL BUTTONS
    @Bind(R.id.ibFacebookLogo)
    protected ImageButton mFacebookLogo;

    @Bind(R.id.ibGooglePlusLogo)
    protected ImageButton mGooglePlusLogo;

    @Bind(R.id.ibLiveLogo)
    protected ImageButton mLiveLogo;

    @Bind(R.id.ibTwitterLogo)
    protected ImageButton mTwitterLogo;

    @Bind(R.id.ibGithubLogo)
    protected ImageButton mGithubLogo;

    private boolean mBackButtonPressedOnce;

    private Handler uiThreadHandler = new Handler();
    private SplashView mSplashView;

    private String email;
    private String password;

    private long currentDeviceId = -1;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String userToken = UserUtils.getUserToken(getApplicationContext());

        if (userToken != null && !userToken.isEmpty()) {
            Log.d(TAG, "User token found. Launching main activity!");
            loadMainActivity();
            return;
        } else {
            Log.d(TAG, "User token NOT found");
        }

        // first -> load splash screen
        hideSystemUI();

        // init splash screen view
        if (mSplashView == null) {
            mSplashView = new SplashView(this);
        }

        // Set an event handler on the SplashView object, so that as soon
        // as it completes drawing we are
        // informed.  In response to that cue, we will *then* put up the main view,
        // replacing the content view of the main activity with that main view.
        mSplashView.setSplashScreenEvent(new SplashView.SplashScreenEvent() {
            @Override
            public void onSplashDrawComplete() {
                uiThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        launchMainView(savedInstanceState);
                    }
                });
            }
        });

        // show splash screen
        setContentView(mSplashView);
    }

    /**
     * Setup main activity
     *
     * @param savedInstanceState
     */
    public void launchMainView(Bundle savedInstanceState) {

        showSystemUI();

        setContentView(R.layout.activity_login);
        setTitle(R.string.login_activity_title);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent != null) {
            Long userId = intent.getLongExtra("user_id", -1);
            if (userId != -1) {
                Toaster.showLong(this, R.string.register_successful);
            }
        }

        populateAutoComplete();
    }

    /**
     * Get user's emails
     */
    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        CommonUtils.hideKeyboard(this, getCurrentFocus());

        // disable button to reduce flood of requests
        if (mLoginButton != null) {
            mLoginButton.setEnabled(false);
        }

        // Reset errors.
        mEmailTextView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        email = mEmailTextView.getText().toString();
        password = mPasswordView.getText().toString();

        boolean isAnyErrors = false;
        View focusView = null;

        // check for password
        if (!TextUtils.isEmpty(password) && !InputValidation.isPasswordLengthValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            isAnyErrors = true;
        }

        // check for email address
        if (TextUtils.isEmpty(email)) {
            mEmailTextView.setError(getString(R.string.error_field_required));
            focusView = mEmailTextView;
            isAnyErrors = true;
        } else {
            if (!InputValidation.isValidEmail(email)) {
                mEmailTextView.setError(getString(R.string.error_invalid_email));
                focusView = mEmailTextView;
                isAnyErrors = true;
            }
        }

        if (isAnyErrors) {
            // enables login button
            if (mLoginButton != null) {
                mLoginButton.setEnabled(true);
            }
            focusView.requestFocus();

            // show again the keyboard
//            CommonUtils.showKeyboard(this, focusView);

        } else {
            doLogin();
        }
    }

    /**
     * Login procedure
     */
    private void doLogin() {

        showProgress(true);

        // checking device information
        // if we had already logged in in the past
        DeviceDao deviceDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getDeviceDao();

        List<Device> devices = deviceDao
                .queryBuilder()
                .where(DeviceDao.Properties.Device_identifier.eq(HardwareUtils.getAndroidId(this)))
                .limit(1)
                .build()
                .list();

        Long serverDeviceId = null;

        // user was already logged in
        if (devices.size() > 0) {
            // take a server device_id
            Device device = devices.get(0);
            serverDeviceId = device.getLogin().getServer_device_id();
            if (serverDeviceId.equals(0)) {
                serverDeviceId = null;
            }
        }

        /**
         * Forming a login request
         */
        LoginRequest request = new LoginRequest();
        request.setUserEmail(email);
        request.setPassword(password);

        UserDevice userDevice = new UserDevice();

        if (serverDeviceId != null) {
            userDevice.setId(serverDeviceId);
        }

        userDevice.setOs(Constants.PLATFORM_NAME);
        userDevice.setOsVersion(HardwareUtils.getAndroidVersion());
        userDevice.setBrand(HardwareUtils.getDeviceBrandName());
        userDevice.setModel(HardwareUtils.getDeviceModelName());
        userDevice.setDeviceId(HardwareUtils.getAndroidId(this));

        request.setDevice(userDevice);

        /**
         * Logging in the user
         */
        UserService service = ServiceGenerator.createService(UserService.class);
        service.loginUser(request, new Callback<LoginResponse>() {

            @Override
            public void success(LoginResponse apiResponse, Response response) {
                saveLoginGoNext(apiResponse);
                Log.d(TAG, "User token received: " + apiResponse.getUserToken());
            }

            @Override
            public void failure(RetrofitError error) {

                // enables login button
                if (mLoginButton != null) {
                    mLoginButton.setEnabled(true);
                }

                showErrorMessages(TAG, error);
                showProgress(false);
            }
        });
    }

    /**
     * Saves user device into database
     *
     * @param loginResponse
     */
    private void saveLoginIntoDb(LoginResponse loginResponse) {

        String createdDate = DateUtils.dateToISO8601String(new Date(), Locale.getDefault());

        Login login = new Login();

        login.setServer_device_id(loginResponse.getDeviceId());
        login.setLast_email(email);
        login.setToken(loginResponse.getUserToken());
        login.setCreated(createdDate);

        LoginDao loginDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getLoginDao();

        List<Login> logins = loginDao
                .queryBuilder()
                .where(LoginDao.Properties.Token.eq(loginResponse.getUserToken()))
                .limit(1)
                .build()
                .list();

        // check if that login was already saved in the system
        if (logins.size() == 0) {
            // no such login found -> insert new login information into db
            loginDao.insert(login);
        } else {
            // found existing login
            login = logins.get(0);
        }


        DeviceDao deviceDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getDeviceDao();

        List<Device> devices = deviceDao
                .queryBuilder()
                .where(DeviceDao.Properties.Device_identifier.eq(HardwareUtils.getAndroidId(this)))
                .limit(1)
                .build()
                .list();

        // check if the device already exists in the db
        if (devices.size() == 0) {
            // no such device found in db -> insert new

            Device device = new Device();
            device.setOs(Constants.PLATFORM_NAME);
            device.setOs_version(HardwareUtils.getAndroidVersion());
            device.setBrand(HardwareUtils.getDeviceBrandName());
            device.setModel(HardwareUtils.getDeviceModelName());
            device.setDevice_identifier(HardwareUtils.getAndroidId(this));
            device.setCreated(createdDate);
            device.setLogin(login);

            currentDeviceId = deviceDao.insert(device);

        } else {
            currentDeviceId = devices.get(0).getId();
        }
    }

    /**
     * Loads next user screen
     *
     * @param loginApiResponse
     */
    private void saveLoginGoNext(LoginResponse loginApiResponse) {

        String token = loginApiResponse.getUserToken();

        if (InputValidation.isUserTokenValid(token)) {
            Log.d(TAG, "Token is valid. Proceeding with login...");

            showProgress(false);
            saveLoginIntoDb(loginApiResponse);
            saveLoginData(token);
            loadMainActivity();

        } else {
            Toaster.showLong(this, R.string.error_user_token_not_valid);
            Log.d(TAG, "User token is INVALID!");
        }
    }

    /**
     * Saves user data needed for successful login into SharedPreferences
     *
     * @param token
     */
    private void saveLoginData(String token) {

        UserUtils.saveUserToken(getApplicationContext(), token);
        UserUtils.saveUserEmail(getApplicationContext(), email);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);

        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailTextView.setAdapter(adapter);
    }

    @OnClick(R.id.sign_in_button)
    protected void onUserLogin() {
        attemptLogin();
    }

    @OnClick(R.id.tvRegister)
    protected void onRegisterPressed() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.tvPasswordReset)
    protected void onPasswordResetPressed() {
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.ibFacebookLogo)
    protected void onFacebookLogoPressed() {
        Toast.makeText(this, "oauth tbd", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.ibGooglePlusLogo)
    protected void onGooglePlusLogoPressed() {
        Toast.makeText(this, "oauth tbd", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.ibLiveLogo)
    protected void onLiveLogoPressed() {
        Toast.makeText(this, "oauth tbd", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.ibTwitterLogo)
    protected void onTwitterLogoPressed() {
        Toast.makeText(this, "oauth tbd", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.ibGithubLogo)
    protected void onGithubLogoPressed() {
        Toast.makeText(this, "oauth tbd", Toast.LENGTH_SHORT).show();
    }

    @OnEditorAction(R.id.email)
    protected boolean onEditorAction(KeyEvent key) {
        attemptLogin();
        return true;
    }

    /**
     * Disables back button for user
     * and starts main activity
     */
    private void loadMainActivity() {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.INTENT_CURRENT_DEVICE_ID, currentDeviceId);
        startActivity(intent);
        finish();
    }

    private void hideSystemUI() {

        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public void onBackPressed() {

        if (mBackButtonPressedOnce) {
            super.onBackPressed();
            return;
        }

        mBackButtonPressedOnce = true;

        Toaster.showLong(this, R.string.action_back_button_pressed_once);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                mBackButtonPressedOnce = false;
            }
        }, Constants.BACK_BUTTON_DELAY_MILLIS);
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        Log.d(TAG, "onDestroy -> unbound resources");
        super.onDestroy();
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Processes error response from server
     *
     * @param TAG
     * @param retrofitError
     */
    protected void showErrorMessages(String TAG, RetrofitError retrofitError) {

        Response response = retrofitError.getResponse();

        if (response != null) {

            int httpCode = response.getStatus();

            switch (httpCode) {
                case 400:
                    ErrorResponse errorResponse = (ErrorResponse) retrofitError.getBodyAs(ErrorResponse.class);
                    errorResponse.setStatusCode(httpCode);

                    Integer apiResponseCode = errorResponse.getCode();
                    String apiMessage = errorResponse.getMessage();
                    int httpResponseCode = errorResponse.getStatusCode();

                    Log.d(TAG, "Response status: " + httpResponseCode);
                    Log.d(TAG, "Response code: " + apiResponseCode);
                    Log.d(TAG, "Response message: " + apiMessage);

                    break;
                case 401:
                    Toaster.showLong(getApplicationContext(), R.string.error_user_login_not_valid);
                    PreferencesUtils.clearUserCredentials(getApplicationContext());
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    break;
                case 404:
                    Toaster.showLong(getApplicationContext(), R.string.error_service_not_available);
                    break;
                case 503:
                    Toaster.showLong(getApplicationContext(), R.string.error_server_temporary_unavailable);
                    break;
                default:
                    Toaster.showLong(getApplicationContext(), R.string.error_unknown);
                    break;
            }
        } else {
            Toaster.showLong(getApplicationContext(), R.string.error_service_not_available);
        }
    }
}

