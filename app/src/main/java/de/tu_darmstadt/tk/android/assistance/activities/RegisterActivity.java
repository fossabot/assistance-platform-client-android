package de.tu_darmstadt.tk.android.assistance.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.activities.common.BaseActivity;
import de.tu_darmstadt.tk.android.assistance.models.http.request.RegistrationRequest;
import de.tu_darmstadt.tk.android.assistance.models.http.response.ErrorResponse;
import de.tu_darmstadt.tk.android.assistance.models.http.response.RegistrationResponse;
import de.tu_darmstadt.tk.android.assistance.services.RegistrationService;
import de.tu_darmstadt.tk.android.assistance.services.ServiceGenerator;
import de.tu_darmstadt.tk.android.assistance.utils.Constants;
import de.tu_darmstadt.tk.android.assistance.utils.InputValidation;
import de.tu_darmstadt.tk.android.assistance.utils.Utils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RegisterActivity extends BaseActivity {

    @Bind(R.id.register_email)
    protected EditText mUserEmail;
    @Bind(R.id.register_password1)
    protected EditText mUserPassword1;
    @Bind(R.id.register_password2)
    protected EditText mUserPassword2;
    @Bind(R.id.sign_up_button)
    protected Button mSignUp;
    private String TAG = RegisterActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle(R.string.register_activity_title);

        ButterKnife.bind(this);
    }

    /**
     * Registration button
     */
    @OnClick(R.id.sign_up_button)
    protected void onUserSignUp() {

        String email = mUserEmail.getText().toString().trim();
        String password1 = mUserPassword1.getText().toString();
        String password2 = mUserPassword2.getText().toString();

        if (isInputOK(email, password1, password2)) {
            doRegisterUser(email, password1);
        }
    }

    /**
     * Validates user's input
     *
     * @return
     */
    private boolean isInputOK(String email, String password1, String password2) {

        // reset all errors
        mUserEmail.setError(null);
        mUserPassword1.setError(null);
        mUserPassword2.setError(null);

        // EMPTY FIELDS CHECK
        if (TextUtils.isEmpty(email)) {
            mUserEmail.setError(getString(R.string.error_field_required));
            mUserEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password1)) {
            mUserPassword1.setError(getString(R.string.error_field_required));
            mUserPassword1.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password2)) {
            mUserPassword2.setError(getString(R.string.error_field_required));
            mUserPassword2.requestFocus();
            return false;
        }

        // NOT VALID EMAIL
        if (!InputValidation.isValidEmail(email)) {
            mUserEmail.setError(getString(R.string.error_invalid_email));
            mUserEmail.requestFocus();
            return false;
        }

        // NOT EQUAL PASSWORDS
        if (!password1.equals(password2)) {
            mUserPassword1.setError(getString(R.string.error_not_same_passwords));
            mUserPassword2.setError(getString(R.string.error_not_same_passwords));
            return false;
        }

        // NOT VALID LENGTH
        if (!InputValidation.isPasswordLengthValid(password1)) {
            mUserPassword1.setError(getString(R.string.error_invalid_password));
            mUserPassword2.setError(getString(R.string.error_invalid_password));
            mUserPassword1.requestFocus();
            return false;
        }

        Utils.hideKeyboard(getApplicationContext(), getCurrentFocus());

        return true;
    }

    private void doRegisterUser(String email, String password) {

//        String passwordHashed = Utils.generateSHA256(password);
        String passwordHashed = password;

        // forming a login request
        RegistrationRequest request = new RegistrationRequest();
        request.setUserEmail(email);
        request.setPassword(passwordHashed);

        // calling api service
        RegistrationService service = ServiceGenerator.createService(RegistrationService.class);
        service.registerUser(request, new Callback<RegistrationResponse>() {

            @Override
            public void success(RegistrationResponse apiResponse, Response response) {
                showLoginScreen(apiResponse);
                Log.d(TAG, "success! userId: " + apiResponse.getUserId());
            }

            @Override
            public void failure(RetrofitError error) {

                ErrorResponse errorResponse = (ErrorResponse) error.getBodyAs(ErrorResponse.class);
                errorResponse.setStatusCode(error.getResponse().getStatus());

                handleError(errorResponse, TAG);
            }
        });
    }

    /**
     * Shows login screen if registration was successful
     *
     * @param registrationResponse
     */
    private void showLoginScreen(RegistrationResponse registrationResponse) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(Constants.INTENT_USER_ID, registrationResponse.getUserId());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ButterKnife.unbind(this);
    }
}
