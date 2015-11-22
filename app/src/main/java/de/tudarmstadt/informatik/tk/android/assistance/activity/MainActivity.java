package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.tudarmstadt.informatik.tk.android.assistance.BuildConfig;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.adapter.NewsAdapter;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.endpoint.ModuleEndpoint;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.endpoint.UserEndpoint;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.profile.ProfileResponse;
import de.tudarmstadt.informatik.tk.android.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbNews;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.endpoint.EndpointGenerator;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.DaoProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.PreferenceProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.service.GcmRegistrationIntentService;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.service.HarvesterService;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.DateUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.DeviceUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.GcmUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.Constants;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * Module information dashboard
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.06.2015
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private DaoProvider daoProvider;

    @Bind(R.id.toolbar)
    protected Toolbar mToolbar;

    @Bind(R.id.assistance_list)
    protected RecyclerView mRecyclerView;

    private Menu menu;

    private List<DbModule> installedModules;

    private List<DbNews> assistanceNews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean accessibilityServiceActivated = PreferenceProvider
                .getInstance(getApplicationContext())
                .getActivated();

        if (accessibilityServiceActivated) {
            initView();
        } else {

            Log.d(TAG, "Accessibility Service is NOT active! Showing tutorial...");

            Intent intent = new Intent(this, AccessibilityTutorialActivity.class);
            startActivityForResult(intent, Constants.INTENT_ACCESSIBILITY_SERVICE_IGNORED_RESULT);
        }
    }

    /**
     * Initializes this activity
     */
    private void initView() {

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle(R.string.main_activity_title);

        if (daoProvider == null) {
            daoProvider = DaoProvider.getInstance(getApplicationContext());
        }

        if (BuildConfig.DEBUG) {
            PreferenceUtils.setDeveloperStatus(getApplicationContext(), true);
        }

        long userId = PreferenceUtils.getCurrentUserId(getApplicationContext());

        assistanceNews = daoProvider.getNewsDao().get(userId);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new NewsAdapter(assistanceNews));

        if (assistanceNews.isEmpty()) {

            ButterKnife.findById(this, R.id.assistance_list).setVisibility(View.GONE);
            ButterKnife.findById(this, R.id.assistance_no_news).setVisibility(View.VISIBLE);
        }

        registerForPush();

        installedModules = daoProvider.getModuleDao().getAllActive(userId);

        Log.d(TAG, "Active modules: " + installedModules.size());

        if (installedModules == null || installedModules.isEmpty()) {

            stopHarvester();

            getActivatedModulesAndSave();

        } else {

            // user got some active modules
            startHarvester();
        }
    }

    /**
     * Asks backend for activated modules
     */
    private void getActivatedModulesAndSave() {

        final String userToken = PreferenceUtils.getUserToken(getApplicationContext());

        final ModuleEndpoint moduleEndpoint = EndpointGenerator
                .getInstance(getApplicationContext())
                .create(ModuleEndpoint.class);

        moduleEndpoint.getActiveModules(userToken,
                new Callback<List<String>>() {

                    @Override
                    public void success(List<String> activeModules,
                                        Response response) {

                        if (activeModules != null && !activeModules.isEmpty()) {

                            Log.d(TAG, "Modules activated:");
                            Log.d(TAG, activeModules.toString());

                            DbUser user = daoProvider
                                    .getUserDao()
                                    .getByToken(userToken);

                            if (user == null) {
                                return;
                            }

                            List<DbModule> modules = user.getDbModuleList();

                            if (modules.isEmpty()) {

                                for (String modulePackageName : activeModules) {


                                }

                            } else {

                                for (DbModule module : modules) {

                                    if (activeModules.contains(module.getPackageName())) {

                                        module.setActive(true);
                                    }
                                }

//                                daoProvider
//                                        .getModuleDao()
//                                        .u
                            }
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        showErrorMessages(error);
                    }
                });
    }

    /**
     * Starting harvesting service if not running
     */
    private void startHarvester() {

        if (!DeviceUtils.isServiceRunning(getApplicationContext(), HarvesterService.class)) {

            HarvesterServiceProvider.getInstance(
                    getApplicationContext())
                    .startSensingService();
        }
    }

    /**
     * Stopping harvesting service if not running
     */
    private void stopHarvester() {

        if (DeviceUtils.isServiceRunning(getApplicationContext(), HarvesterService.class)) {

            HarvesterServiceProvider.getInstance(
                    getApplicationContext())
                    .stopSensingService();
        }
    }

    /**
     * Registers for GCM push notifications
     */
    private void registerForPush() {

        boolean isTokenWasSent = PreferenceUtils.isGcmTokenWasSent(getApplicationContext());

        if (isTokenWasSent) {
            return;
        }

        // check for play services installation
        if (GcmUtils.isPlayServicesInstalled(this)) {

            Log.d(TAG, "Google Play Services are installed.");

            // starting registration GCM service
            Intent intent = new Intent(this, GcmRegistrationIntentService.class);
            startService(intent);

            PreferenceUtils.setGcmTokenWasSent(getApplicationContext(), true);

        } else {
            Log.d(TAG, "Google Play Services NOT installed.");

            PreferenceUtils.setGcmTokenWasSent(getApplicationContext(), false);

            // TODO: tell user that it is impossible without play services
            // or just make here impl without play services.
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.news_menu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, Constants.INTENT_SETTINGS_LOGOUT_RESULT);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Updates module menu
     */
    private void updateMenu() {

        Log.d(TAG, "Updating menu..");

        if (menu != null) {
            // TODO:
//            DbModule currentModule = getCurrentActiveModuleFromDrawer().getDbModule();
//            List<DbModuleInstallation> installedModules = currentModule.getDbModuleInstallationList();
//
//            MenuItem toggleModuleStateItem = menu.findItem(R.id.menu_module_toggle_state);
//
//            for (DbModuleInstallation installedModule : installedModules) {
//
//                if (installedModule.getModuleId().equals(currentModule.getId())) {
//                    toggleModuleStateItem.setChecked(installedModule.getActive());
//                }
//            }
        }
    }

    /**
     * Enables or disables currently selected module
     *
     * @param moduleState
     */
    private int toggleModuleState(boolean moduleState) {

        // TODO:
//        DbModule currentModule = getCurrentActiveModuleFromDrawer().getDbModule();
//
//        DbModuleInstallation moduleInstallation = dbProvider.getModuleInstallationForModuleByUserId(
//                currentModule.getUserId(),
//                currentModule.getId());
//
//        if (moduleInstallation == null) {
//            Toaster.showLong(getApplicationContext(), R.string.error_module_not_installed);
//        } else {
//
//            moduleInstallation.setActive(moduleState);
//
//            dbProvider.updateModuleInstallation(moduleInstallation);
//
//            if (moduleState) {
//                Log.d(TAG, "User DISABLED a module");
//                return 0;
//            } else {
//                Log.d(TAG, "User ENABLED a module");
//                return 1;
//            }
//
//        }

        return -1;
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy -> unbound resources");
        ButterKnife.unbind(this);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(TAG, "onActivityResult. RequestCode: " + requestCode + " resultCode: " + resultCode);

        switch (resultCode) {

            case Constants.INTENT_ACCESSIBILITY_SERVICE_IGNORED_RESULT:
            case Constants.INTENT_ACCESSIBILITY_SERVICE_ENABLED_RESULT:

                Log.d(TAG, "Back from accessibility service tutorial");

                initView();
                break;

            case Constants.INTENT_SETTINGS_LOGOUT_RESULT:

                Log.d(TAG, "Back from settings logout action");

                PreferenceUtils.clearUserCredentials(getApplicationContext());
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;

            default:
                Log.d(TAG, "Back from UNKNOWN result: " + resultCode);
        }

        switch (requestCode) {

            case Constants.INTENT_AVAILABLE_MODULES_RESULT:

                Log.d(TAG, "Back from available modules activity");

                if (PreferenceUtils.hasUserModules(getApplicationContext())) {

                    Log.d(TAG, "User have modules installed");

                    startHarvester();
                } else {
                    Log.d(TAG, "User have NO modules installed");
                }
                break;

            default:
                Log.d(TAG, "Back from UNKNOWN request: " + requestCode);
        }
    }

    /**
     * Requests user profile information
     */
    private void requestUserProfile() {

        String userToken = PreferenceUtils.getUserToken(getApplicationContext());

        UserEndpoint userservice = EndpointGenerator.getInstance(getApplicationContext()).create(UserEndpoint.class);
        userservice.getUserProfileShort(userToken, new Callback<ProfileResponse>() {

            @Override
            public void success(ProfileResponse profileResponse, Response response) {

                if (profileResponse == null) {
                    return;
                }

                PreferenceUtils.setUserFirstname(getApplicationContext(), profileResponse.getFirstname());
                PreferenceUtils.setUserLastname(getApplicationContext(), profileResponse.getLastname());
                PreferenceUtils.setUserEmail(getApplicationContext(), profileResponse.getPrimaryEmail());

                persistLogin(profileResponse);
            }

            @Override
            public void failure(RetrofitError error) {
                showErrorMessages(error);
            }
        });
    }

    /**
     * Updates existent user login or creates one in db
     *
     * @param profileResponse
     */
    private void persistLogin(ProfileResponse profileResponse) {

        // check already available user in db
        DbUser user = daoProvider.getUserDao().getByEmail(profileResponse.getPrimaryEmail());

        // check for user existence in the db
        if (user == null) {
            // no user found -> create one

            user = new DbUser();

            user.setFirstname(profileResponse.getFirstname());
            user.setLastname(profileResponse.getLastname());
            user.setPrimaryEmail(profileResponse.getPrimaryEmail());

            if (profileResponse.getJoinedSince() != null) {
                user.setJoinedSince(DateUtils.dateToISO8601String(new Date(profileResponse.getJoinedSince()), Locale.getDefault()));
            }

            if (profileResponse.getLastLogin() != null) {
                user.setLastLogin(DateUtils.dateToISO8601String(new Date(profileResponse.getLastLogin()), Locale.getDefault()));
            }

            user.setCreated(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));

            daoProvider.getUserDao().insert(user);

        } else {
            // found a user -> update for device and user information

            user.setFirstname(profileResponse.getFirstname());
            user.setLastname(profileResponse.getLastname());
            user.setPrimaryEmail(profileResponse.getPrimaryEmail());

            if (profileResponse.getJoinedSince() != null) {
                user.setJoinedSince(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));
            }

            if (profileResponse.getLastLogin() != null) {
                user.setLastLogin(DateUtils.dateToISO8601String(new Date(profileResponse.getLastLogin()), Locale.getDefault()));
            }

            daoProvider.getUserDao().update(user);
        }
    }

    @OnClick(R.id.show_available_modules)
    protected void onShowAvailableModules() {

        Intent intent = new Intent(this, AvailableModulesActivity.class);
        startActivityForResult(intent, Constants.INTENT_AVAILABLE_MODULES_RESULT);
    }

    /**
     * Processes error response from server
     *
     * @param retrofitError
     */
    protected void showErrorMessages(RetrofitError retrofitError) {

        Response response = retrofitError.getResponse();

        if (response != null) {

            int httpCode = response.getStatus();

            switch (httpCode) {
                case 400:
                    Toaster.showLong(getApplicationContext(), R.string.error_service_bad_request);
                    break;
                case 401:
                    Toaster.showLong(getApplicationContext(), R.string.error_user_login_not_valid);
                    PreferenceUtils.clearUserCredentials(getApplicationContext());
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
