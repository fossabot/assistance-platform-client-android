package de.tudarmstadt.informatik.tk.assistance.activity;

import android.R.integer;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageButton;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.Unbinder;
import de.tudarmstadt.informatik.tk.assistance.App;
import de.tudarmstadt.informatik.tk.assistance.Constants;
import de.tudarmstadt.informatik.tk.assistance.R.id;
import de.tudarmstadt.informatik.tk.assistance.R.layout;
import de.tudarmstadt.informatik.tk.assistance.R.string;
import de.tudarmstadt.informatik.tk.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.assistance.presenter.login.LoginPresenter;
import de.tudarmstadt.informatik.tk.assistance.presenter.login.LoginPresenterImpl;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.assistance.util.CommonUtils;
import de.tudarmstadt.informatik.tk.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.assistance.view.LoginView;
import de.tudarmstadt.informatik.tk.assistance.view.SplashView;

/**
 * A login screen that offers login via email/password
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.06.2015
 */
public class LoginActivity extends
        AppCompatActivity implements
        LoginView {

    private static final String TAG = LoginActivity.class.getSimpleName();

    @BindView(id.email)
    public AppCompatEditText mEmailTextView;

    @BindView(id.password)
    public AppCompatEditText mPasswordView;

    @BindView(id.login_progress)
    public ContentLoadingProgressBar mProgressView;

    @BindView(id.login_form)
    public NestedScrollView mLoginFormView;

    @BindView(id.sign_in_button)
    public AppCompatButton mLoginButton;

    // SOCIAL BUTTONS
    @BindView(id.ibFacebookLogo)
    public AppCompatImageButton mFacebookLogo;

    @BindView(id.ibGooglePlusLogo)
    public AppCompatImageButton mGooglePlusLogo;

    @BindView(id.ibLiveLogo)
    public AppCompatImageButton mLiveLogo;

    @BindView(id.ibTwitterLogo)
    public AppCompatImageButton mTwitterLogo;

    @BindView(id.ibGithubLogo)
    public AppCompatImageButton mGithubLogo;

    private Unbinder unbinder;

    private boolean mBackButtonPressedOnce;

    private Handler uiThreadHandler = new Handler();
    private SplashView mSplashView;

    private LoginPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!App.isInitialized) {
            android.util.Log.d(TAG, "Initializing...");
            App.init(getApplicationContext());
        }

        setPresenter(new LoginPresenterImpl(this));
        presenter.initView();
    }

    @Override
    public void loadMainActivity() {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ActivityCompat.startActivity(this, intent, null);
        finish();
    }

    @Override
    public void setPresenter(LoginPresenter presenter) {
        this.presenter = presenter;
        this.presenter.setView(this);
    }

    @Override
    public void showProgress(final boolean isShowing) {

        int shortAnimTime = getResources().getInteger(integer.config_shortAnimTime);

        mLoginFormView.setVisibility(isShowing ? View.GONE : View.VISIBLE);

        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                isShowing ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(isShowing ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(isShowing ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                isShowing ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(isShowing ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void setLoginButtonEnabled(boolean isEnabled) {

        if (mLoginButton != null) {
            mLoginButton.setEnabled(isEnabled);
        }
    }

    @Override
    public void hideKeyboard() {
        CommonUtils.hideKeyboard(this, getCurrentFocus());
    }

    @Override
    public void loadSplashView() {

        // first -> load splash screen
        CommonUtils.hideSystemUI(getWindow());

        // init splash screen view
        if (mSplashView == null) {
            mSplashView = new SplashView(this);
        }

        mSplashView.setSplashScreenEvent(() -> uiThreadHandler.post(presenter::getSplashView));

        // show splash screen
        setContentView(mSplashView);
    }

    @Override
    public void showSystemUI() {
        CommonUtils.showSystemUI(getWindow());
    }

    @Override
    public void setContent() {

        setContentView(layout.activity_login);
        setTitle(string.login_activity_title);

        unbinder = ButterKnife.bind(this);
    }

    @Override
    public void setDebugViewInformation() {
        mEmailTextView.setText("test123@test.de");
        mPasswordView.setText("test123");
    }

    @Override
    public void requestFocus(View view) {
        view.requestFocus();
    }

    @Override
    public void showErrorPasswordInvalid() {
        mPasswordView.setError(getString(string.error_invalid_password));
        mPasswordView.requestFocus();
    }

    @Override
    public void showErrorEmailRequired() {
        mEmailTextView.setError(getString(string.error_field_required));
        mEmailTextView.requestFocus();
    }

    @Override
    public void showErrorEmailInvalid() {
        mEmailTextView.setError(getString(string.error_invalid_email));
        mEmailTextView.requestFocus();
    }

    @OnClick(id.sign_in_button)
    protected void onUserLogin() {

        presenter.attemptLogin(
                mEmailTextView.getText().toString().trim(),
                mPasswordView.getText().toString().trim());
    }

    @OnClick(id.tvRegister)
    protected void onRegisterPressed() {
        Intent intent = new Intent(this, RegisterActivity.class);
        ActivityCompat.startActivity(this, intent, null);
    }

    @OnClick(id.tvPasswordReset)
    protected void onPasswordResetPressed() {

        Toaster.showLong(this, string.feature_is_under_construction);
//        Intent intent = new Intent(this, ResetPasswordActivity.class);
//        ActivityCompat.startActivity(this, intent, null);
    }

    @OnClick(id.ibFacebookLogo)
    protected void onFacebookLogoPressed() {
        Toast.makeText(this, string.feature_is_under_construction, Toast.LENGTH_SHORT).show();
    }

    @OnClick(id.ibGooglePlusLogo)
    protected void onGooglePlusLogoPressed() {
        Toast.makeText(this, string.feature_is_under_construction, Toast.LENGTH_SHORT).show();
    }

    @OnClick(id.ibLiveLogo)
    protected void onLiveLogoPressed() {
        Toast.makeText(this, string.feature_is_under_construction, Toast.LENGTH_SHORT).show();
    }

    @OnClick(id.ibTwitterLogo)
    protected void onTwitterLogoPressed() {
        Toast.makeText(this, string.feature_is_under_construction, Toast.LENGTH_SHORT).show();
    }

    @OnClick(id.ibGithubLogo)
    protected void onGithubLogoPressed() {
        Toast.makeText(this, string.feature_is_under_construction, Toast.LENGTH_SHORT).show();
    }

    @OnEditorAction(id.email)
    protected boolean onEditorAction(KeyEvent key) {

        presenter.attemptLogin(
                mEmailTextView.getText().toString().trim(),
                mPasswordView.getText().toString().trim());

        return true;
    }

    @Override
    public void onBackPressed() {

        if (mBackButtonPressedOnce) {
            super.onBackPressed();
            return;
        }

        mBackButtonPressedOnce = true;

        Toaster.showLong(this, string.action_back_button_pressed_once);

        new Handler().postDelayed(() -> mBackButtonPressedOnce = false, Constants.BACK_BUTTON_DELAY_MILLIS);
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        mSplashView = null;
        uiThreadHandler = null;
        super.onDestroy();
    }

    @Override
    public void initView() {

        // just init EventBus there
        HarvesterServiceProvider.getInstance(getApplicationContext());

        presenter.checkAutologin(PreferenceUtils.getUserToken(getApplicationContext()));
    }

    @Override
    public void startLoginActivity() {
        // we are already here
    }

    @Override
    public void clearErrors() {
        mEmailTextView.setError(null);
        mPasswordView.setError(null);
    }

    @Override
    public void showServiceUnavailable() {
        Toaster.showLong(getApplicationContext(), string.error_service_not_available);
    }

    @Override
    public void showServiceTemporaryUnavailable() {
        Toaster.showLong(getApplicationContext(), string.error_server_temporary_unavailable);
    }

    @Override
    public void showUnknownErrorOccurred() {
        Toaster.showLong(getApplicationContext(), string.error_unknown);
    }

    @Override
    public void showUserForbidden() {
        Toaster.showLong(getApplicationContext(), string.error_user_login_not_valid);
    }

    @Override
    public void showActionProhibited() {
        // empty
    }

    @Override
    public void showRetryLaterNotification() {
        Toaster.showLong(getApplicationContext(), string.error_service_retry_later);
    }

    @Override
    public void askPermissions(Set<String> permsRequired, Set<String> permsOptional) {
        // empty
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}