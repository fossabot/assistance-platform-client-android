package de.tudarmstadt.informatik.tk.assistance.fragment.settings;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import de.tudarmstadt.informatik.tk.assistance.R;
import de.tudarmstadt.informatik.tk.assistance.activity.SettingsActivity;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.user.profile.ProfileResponseDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.user.profile.UpdateProfileRequestDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.user.profile.UserSocialServiceDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.user.UserApi;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.ApiGenerator;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.assistance.util.ValidationUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 29.06.2015
 */
public class UserProfileSettingsFragment extends Fragment {

    private static final String TAG = UserProfileSettingsFragment.class.getSimpleName();

    private static final String IMAGE_TYPE_FILTER = "image/*";
    private static final String USER_PIC_NAME = "user_pic";

    private Toolbar mParentToolbar;

    private String userToken;

//    @Bind(R.id.userPicVIew)
//    protected CircularImageView userPicView;

    @Bind(R.id.firstname)
    protected EditText firstnameText;

    private String firstname;

    @Bind(R.id.lastname)
    protected EditText lastnameText;

    private String lastname;

    @Bind(R.id.social_account_google)
    protected EditText socialAccountGoogleText;

    private String socialAccountGoogle;

    @Bind(R.id.social_account_facebook)
    protected EditText socialAccountFacebookText;

    private String socialAccountFacebook;

    @Bind(R.id.social_account_live)
    protected EditText socialAccountLiveText;

    private String socialAccountLive;

    @Bind(R.id.social_account_twitter)
    protected EditText socialAccountTwitterText;

    private String socialAccountTwitter;

    @Bind(R.id.social_account_github)
    protected EditText socialAccountGithubText;

    private String socialAccountGithub;

    public UserProfileSettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mParentToolbar = ((SettingsActivity) getActivity()).getToolBar();
        mParentToolbar.setTitle(R.string.settings_header_user_profile_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = null;

        // request user profile from server
        userToken = PreferenceUtils.getUserToken(getActivity().getApplicationContext());

        if (!userToken.isEmpty()) {

            view = inflater.inflate(R.layout.fragment_preference_user_profile, container, false);

            ButterKnife.bind(this, view);

//            String filename = UserUtils.getUserPicFilename(getActivity().getApplicationContext());
//
//            if (!filename.isEmpty()) {
//                File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + Config.USER_PIC_PATH + "/" + filename + ".jpg");
//
//                if (file.exists()) {
//                    Log.d(TAG, "File exists");
//
//                    Picasso.with(getActivity().getApplicationContext())
//                            .load(file)
//                            .placeholder(R.drawable.no_image)
//                            .into(userPicView);
//                } else {
//                    Log.d(TAG, "File NOT exists");
//                }
//            } else {
//                Log.d(TAG, "user pic filename NOT exists");
//                userPicView.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.no_image));
//            }

            UserApi userApi = ApiGenerator.getInstance(getActivity().getApplicationContext()).create(UserApi.class);
            userApi.getUserProfileFull(userToken, new Callback<ProfileResponseDto>() {

                @Override
                public void success(ProfileResponseDto profileResponse, Response response) {
                    Log.d(TAG, "Successfully received the user profile!");

                    fillupFullUserProfile(profileResponse);
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d(TAG, "Failed while getting full user profile");
                }
            });
        }

        return view;
    }

    /**
     * Populate user profile fields
     *
     * @param profileResponse
     */
    private void fillupFullUserProfile(ProfileResponseDto profileResponse) {

        firstnameText.setText(profileResponse.getFirstname());
        lastnameText.setText(profileResponse.getLastname());

        List<UserSocialServiceDto> socialServices = profileResponse.getSocialServices();

        if (!socialServices.isEmpty()) {
            String serviceName;

            for (UserSocialServiceDto service : socialServices) {
                if (service == null) {
                    continue;
                }

                serviceName = service.getName();

                if (serviceName.equals(UserSocialServiceDto.TYPE_GOOGLE)) {
                    socialAccountGoogleText.setText(serviceName);
                }

                if (serviceName.equals(UserSocialServiceDto.TYPE_FACEBOOK)) {
                    socialAccountFacebookText.setText(serviceName);
                }

                if (serviceName.equals(UserSocialServiceDto.TYPE_LIVE)) {
                    socialAccountLiveText.setText(serviceName);
                }

                if (serviceName.equals(UserSocialServiceDto.TYPE_TWITTER)) {
                    socialAccountTwitterText.setText(serviceName);
                }

                if (serviceName.equals(UserSocialServiceDto.TYPE_GITHUB)) {
                    socialAccountGithubText.setText(serviceName);
                }
            }
        }
    }

//    @OnClick(R.id.userPicVIew)
//    protected void onUserPhotoClicked() {
//        Log.d(TAG, "User clicked selection of an image");
//        pickImage();
//    }

    /*
    *   Starts intent to pick some image
     */
//    public void pickImage() {
//
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType(IMAGE_TYPE_FILTER);
//        startActivityForResult(intent, R.id.userPicVIew);
//    }

    @OnTextChanged({R.id.firstname,
            R.id.lastname,
            R.id.social_account_google,
            R.id.social_account_facebook,
            R.id.social_account_live,
            R.id.social_account_twitter,
            R.id.social_account_github})
    void onFocusChanged(CharSequence text) {
        Log.d(TAG, text.toString());
        isUserInputOK();
    }

    /**
     * Validates user input
     */
    private boolean isUserInputOK() {

        firstname = firstnameText.getText().toString().trim();
        lastname = lastnameText.getText().toString().trim();

        socialAccountGoogle = socialAccountGoogleText.getText().toString().trim();

        if (!TextUtils.isEmpty(socialAccountGoogle)) {

            if (!ValidationUtils.isValidEmail(socialAccountGoogle)) {
                socialAccountGoogleText.setError(getString(R.string.error_invalid_email));
                socialAccountGoogleText.requestFocus();
                return false;
            }
        }

        socialAccountFacebook = socialAccountFacebookText.getText().toString().trim();

        if (!TextUtils.isEmpty(socialAccountFacebook)) {

            if (!ValidationUtils.isValidEmail(socialAccountFacebook)) {
                socialAccountFacebookText.setError(getString(R.string.error_invalid_email));
                socialAccountFacebookText.requestFocus();
                return false;
            }
        }

        socialAccountLive = socialAccountLiveText.getText().toString().trim();

        if (!TextUtils.isEmpty(socialAccountLive)) {

            if (!ValidationUtils.isValidEmail(socialAccountLive)) {
                socialAccountLiveText.setError(getString(R.string.error_invalid_email));
                socialAccountLiveText.requestFocus();
                return false;
            }
        }

        socialAccountTwitter = socialAccountTwitterText.getText().toString().trim();

        if (!TextUtils.isEmpty(socialAccountTwitter)) {

            if (!ValidationUtils.isValidEmail(socialAccountTwitter)) {
                socialAccountTwitterText.setError(getString(R.string.error_invalid_email));
                socialAccountTwitterText.requestFocus();
                return false;
            }
        }

        socialAccountGithub = socialAccountGithubText.getText().toString().trim();

        if (!TextUtils.isEmpty(socialAccountGithub)) {

            if (!ValidationUtils.isValidEmail(socialAccountGithub)) {
                socialAccountGithubText.setError(getString(R.string.error_invalid_email));
                socialAccountGithubText.requestFocus();
                return false;
            }
        }

        return true;
    }

    /**
     * Saves user profile -> send request to server
     */
    private void updateUserProfile() {

        Log.d(TAG, "updateUserProfile() invoked");

        UpdateProfileRequestDto request = new UpdateProfileRequestDto();

        request.setFirstname(firstname);
        request.setLastname(lastname);

        PreferenceUtils.setUserFirstname(getActivity().getApplicationContext(), firstname);
        PreferenceUtils.setUserLastname(getActivity().getApplicationContext(), lastname);

        List<UserSocialServiceDto> socialServices = new ArrayList<>();

        // GOOGLE
        UserSocialServiceDto googleService = new UserSocialServiceDto();
        googleService.setName(UserSocialServiceDto.TYPE_GOOGLE);
        googleService.setEmail(socialAccountGoogle);

        socialServices.add(googleService);

        // FACEBOOK
        UserSocialServiceDto facebookService = new UserSocialServiceDto();
        facebookService.setName(UserSocialServiceDto.TYPE_FACEBOOK);
        facebookService.setEmail(socialAccountFacebook);

        socialServices.add(facebookService);

        // LIVE
        UserSocialServiceDto liveService = new UserSocialServiceDto();
        liveService.setName(UserSocialServiceDto.TYPE_LIVE);
        liveService.setEmail(socialAccountLive);

        socialServices.add(liveService);

        // TWITTER
        UserSocialServiceDto twitterService = new UserSocialServiceDto();
        twitterService.setName(UserSocialServiceDto.TYPE_TWITTER);
        twitterService.setEmail(socialAccountTwitter);

        socialServices.add(twitterService);

        // GITHUB
        UserSocialServiceDto githubService = new UserSocialServiceDto();
        githubService.setName(UserSocialServiceDto.TYPE_GITHUB);
        githubService.setEmail(socialAccountGithub);

        socialServices.add(githubService);

        // set services
        request.setServices(socialServices);

        /**
         * SEND UPDATED USER PROFILE TO SERVER
         */
        UserApi userApi = ApiGenerator.getInstance(getActivity().getApplicationContext()).create(UserApi.class);
        userApi.updateUserProfile(userToken, request, new Callback<Void>() {

            @Override
            public void success(Void aVoid, Response response) {
                Log.d(TAG, "Successfully updated user profile!");
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "Failed while updating user profile");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (requestCode == R.id.userPicVIew && resultCode == Activity.RESULT_OK) {
//            if (data == null) {
//                Toaster.showLong(getActivity(), R.string.error_select_new_user_photo);
//                return;
//            }
//
//            String oldFilename = UserUtils.getUserPicFilename(getActivity().getApplicationContext());
//            Log.d(TAG, "old user pic filename: " + (oldFilename.isEmpty() ? "empty" : oldFilename));
//
//            // process selected image and show it to user
//            try {
//                Uri uri = data.getData();
//
//                InputStream inputStream = getActivity().getApplicationContext().getContentResolver().openInputStream(uri);
//
//                CommonUtils.saveFile(getActivity().getApplicationContext(), uri, oldFilename);
//
//                CircularImageView image = ButterKnife.findById(getActivity(), R.id.userPicVIew);
//                image.setImageDrawable(Drawable.createFromStream(inputStream, USER_PIC_NAME));
//
//            } catch (FileNotFoundException e) {
//                Log.e(TAG, "User pic file not found!");
//            }
//        }
    }

    @Override
    public void onStop() {
        Log.d(TAG, "User pressed back and profile is updating...");
        updateUserProfile();

        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
