package de.tu_darmstadt.tk.android.assistance.activities.common;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.FrameLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.activities.LoginActivity;
import de.tu_darmstadt.tk.android.assistance.callbacks.DrawerCallback;
import de.tu_darmstadt.tk.android.assistance.fragments.DrawerFragment;
import de.tu_darmstadt.tk.android.assistance.models.http.HttpErrorCode;
import de.tu_darmstadt.tk.android.assistance.models.http.response.ErrorResponse;
import de.tu_darmstadt.tk.android.assistance.utils.PreferencesUtils;
import de.tu_darmstadt.tk.android.assistance.utils.Toaster;
import de.tu_darmstadt.tk.android.assistance.utils.UserUtils;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Base activity for common stuff
 */
public class DrawerActivity extends AppCompatActivity implements DrawerCallback {

    protected boolean mBackButtonPressedOnce;

    protected Toolbar mToolbar;

    protected FrameLayout frameLayout;

    protected DrawerFragment mDrawerFragment;

    protected DrawerLayout drawerLayout;

    protected String mUserEmail;

    public DrawerActivity() {
        this.mBackButtonPressedOnce = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_base);

        mToolbar = ButterKnife.findById(this, R.id.toolbar_actionbar);

        mUserEmail = UserUtils.getUserEmail(getApplicationContext());

        frameLayout = ButterKnife.findById(this, R.id.container_frame);
        drawerLayout = ButterKnife.findById(this, R.id.drawer_layout);

        setSupportActionBar(mToolbar);
        setupDrawer(mToolbar);
    }

    protected void setupDrawer(Toolbar mToolbar) {

        mDrawerFragment = (DrawerFragment) getFragmentManager().findFragmentById(R.id.drawer_fragment);
        mDrawerFragment.setup(R.id.drawer_fragment, drawerLayout, mToolbar);
        mDrawerFragment.setUserData("Wladimir Schmidt", mUserEmail, BitmapFactory.decodeResource(getResources(), R.drawable.no_user_pic));
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
                    HttpErrorCode.ErrorCode apiErrorType = HttpErrorCode.fromCode(apiResponseCode);

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

    @Override
    public void onBackPressed() {
        if (mDrawerFragment.isDrawerOpen()) {
            mDrawerFragment.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
    }
}
