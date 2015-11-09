package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.adapter.AvailableModulesAdapter;
import de.tudarmstadt.informatik.tk.android.assistance.adapter.PermissionAdapter;
import de.tudarmstadt.informatik.tk.android.assistance.event.ModuleInstallEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.ModuleShowMoreInfoEvent;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.endpoint.ModuleEndpoint;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.module.AvailableModuleResponse;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.module.ModuleCapabilityResponse;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.module.ToggleModuleRequest;
import de.tudarmstadt.informatik.tk.android.assistance.model.item.PermissionListItem;
import de.tudarmstadt.informatik.tk.android.assistance.util.ConverterUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferencesUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.util.UserUtils;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModule;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbUser;
import de.tudarmstadt.informatik.tk.android.kraken.model.api.endpoint.EndpointGenerator;
import de.tudarmstadt.informatik.tk.android.kraken.provider.DaoProvider;
import de.tudarmstadt.informatik.tk.android.kraken.provider.HarvesterServiceProvider;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Shows a list of available assistance modules
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.06.2015
 */
public class AvailableModulesActivity extends AppCompatActivity {

    private static final String TAG = AvailableModulesActivity.class.getSimpleName();

    private Toolbar mToolbar;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mAvailableModulesRecyclerView;

    private RecyclerView permissionRequiredRecyclerView;

    private RecyclerView permissionOptionalRecyclerView;

    private DaoProvider daoProvider;

    private Map<String, AvailableModuleResponse> mAvailableModuleResponses;

    private List<String> mActiveModules;

    private List<DbModule> mModules;

    private SwipeRefreshLayout.OnRefreshListener onRefreshHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_modules);

        if (daoProvider == null) {
            daoProvider = DaoProvider.getInstance(getApplicationContext());
        }

        mToolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle(R.string.module_list_activity_title);

        mAvailableModulesRecyclerView = ButterKnife.findById(this, R.id.moduleListRecyclerView);
        mAvailableModulesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mSwipeRefreshLayout = ButterKnife.findById(this, R.id.module_list_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        if (onRefreshHandler == null) {
            onRefreshHandler = new SwipeRefreshLayout.OnRefreshListener() {

                @Override
                public void onRefresh() {

                    mSwipeRefreshLayout.setRefreshing(true);

                    // request new modules infomation
                    requestAvailableModules();
                }
            };
        }

        mSwipeRefreshLayout.setOnRefreshListener(onRefreshHandler);
        mSwipeRefreshLayout.setNestedScrollingEnabled(true);

        // register this activity to events
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        loadModules();
    }

    /**
     * Loads module list from db or in case its empty
     * loads it from server
     */
    private void loadModules() {

        String userEmail = UserUtils.getUserEmail(getApplicationContext());

        DbUser user = daoProvider.getUserDao().getUserByEmail(userEmail);

        if (user == null) {
            UserUtils.doLogout(getApplicationContext());
            finish();
            return;
        }

        List<DbModule> installedModules = user.getDbModuleList();

        // no modules was found -> request from server
        if (installedModules.isEmpty()) {
            Log.d(TAG, "Module list not found in db. Requesting from server...");

            requestAvailableModules();
        }
    }

    /**
     * Request available modules from service
     */
    private void requestAvailableModules() {

        final String userToken = UserUtils.getUserToken(getApplicationContext());

        // calling api service
        final ModuleEndpoint moduleEndpoint = EndpointGenerator
                .getInstance(getApplicationContext())
                .create(ModuleEndpoint.class);

        moduleEndpoint.getAvailableModules(userToken,
                new Callback<List<AvailableModuleResponse>>() {

                    /**
                     * Successful HTTP response.
                     *
                     * @param availableModulesResponse
                     * @param response
                     */
                    @Override
                    public void success(final List<AvailableModuleResponse> availableModulesResponse,
                                        Response response) {

                        if (availableModulesResponse != null &&
                                !availableModulesResponse.isEmpty()) {

                            Log.d(TAG, availableModulesResponse.toString());

                            boolean hasUserRequestedActiveModules = UserUtils.hasUserRequestedActiveModules(getApplicationContext());

                            if (hasUserRequestedActiveModules) {
                                mSwipeRefreshLayout.setRefreshing(false);
                                processAvailableModules(availableModulesResponse);
                                return;
                            }

                            // get list of already activated modules
                            moduleEndpoint.getActiveModules(userToken, new Callback<List<String>>() {

                                @Override
                                public void success(List<String> activeModules,
                                                    Response response) {

                                    mSwipeRefreshLayout.setRefreshing(false);
                                    UserUtils.saveUserRequestedActiveModules(getApplicationContext(), true);

                                    if (activeModules != null && !activeModules.isEmpty()) {

                                        Log.d(TAG, activeModules.toString());

                                        mActiveModules = activeModules;
                                    }

                                    processAvailableModules(availableModulesResponse);
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    showErrorMessages(error);
                                    mSwipeRefreshLayout.setRefreshing(false);
                                    processAvailableModules(availableModulesResponse);
                                }
                            });

                        } else {
                            mAvailableModulesRecyclerView.setAdapter(new AvailableModulesAdapter(Collections.EMPTY_LIST));
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }

                    /**
                     * Unsuccessful HTTP response due to network failure, non-2XX status code, or unexpected
                     * exception.
                     *
                     * @param error
                     */
                    @Override
                    public void failure(RetrofitError error) {
                        showErrorMessages(error);
                        mAvailableModulesRecyclerView.setAdapter(new AvailableModulesAdapter(Collections.EMPTY_LIST));
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    /**
     * Populates and saves available modules
     *
     * @param availableModulesResponse
     */
    private void processAvailableModules(List<AvailableModuleResponse> availableModulesResponse) {

        // show list of modules to user
        populateAvailableModuleList(availableModulesResponse);
    }

    /**
     * Show available modules to user
     *
     * @param availableModulesResponse
     */
    private void populateAvailableModuleList(List<AvailableModuleResponse> availableModulesResponse) {

        mAvailableModuleResponses = new HashMap<>();
        mModules = new ArrayList<>();

        for (AvailableModuleResponse module : availableModulesResponse) {

            Log.d(TAG, module.toString());

            mModules.add(ConverterUtils.convertModule(module));

            // for easy access later on
            mAvailableModuleResponses.put(module.getModulePackage(), module);
        }

        mAvailableModulesRecyclerView.setAdapter(new AvailableModulesAdapter(mModules));
    }

    /**
     * On module install event
     *
     * @param event
     */
    public void onEvent(ModuleInstallEvent event) {
        Log.d(TAG, "Received installation event. Module id: " + event.getModuleId());

        showPermissionDialog(event.getModuleId());
    }

    /**
     * On module show more info event
     *
     * @param event
     */
    public void onEvent(ModuleShowMoreInfoEvent event) {
        Log.d(TAG, "Received show more info event. Module id: " + event.getModuleId());

        showMoreModuleInformationDialog(event.getModuleId());
    }

    /**
     * Shows more information about an assistance module
     *
     * @param modulePackageName
     */
    private void showMoreModuleInformationDialog(String modulePackageName) {

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_more_info_module, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);

        final AvailableModuleResponse selectedModule = mAvailableModuleResponses.get(modulePackageName);

        dialogBuilder.setPositiveButton(R.string.button_ok_text, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "User tapped more information about the " + selectedModule.getTitle() + " module");
            }
        });

        dialogBuilder.setTitle(selectedModule.getTitle());

        TextView moreInfoFull = ButterKnife.findById(dialogView, R.id.module_more_info);
        moreInfoFull.setText(selectedModule.getDescriptionFull());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Shows a permission dialog to user
     * Each permission is used actually by a module
     *
     * @param moduleId
     */
    private void showPermissionDialog(final String moduleId) {

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_permissions, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setPositiveButton(R.string.button_accept_text, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "User accepted module permissions.");

                installModule(moduleId);
            }
        });

        AvailableModuleResponse selectedModule = mAvailableModuleResponses.get(moduleId);

        TextView title = ButterKnife.findById(dialogView, R.id.module_permission_title);
        title.setText(selectedModule.getTitle());

        CircularImageView imageView = ButterKnife.findById(dialogView, R.id.module_permission_icon);

        Picasso.with(this)
                .load(selectedModule.getLogo())
                .placeholder(R.drawable.no_image)
                .into(imageView);

        List<ModuleCapabilityResponse> requiredSensors = selectedModule.getSensorsRequired();
        List<ModuleCapabilityResponse> optionalSensors = selectedModule.getSensorsOptional();

        List<PermissionListItem> requiredModuleSensors = new ArrayList<>();
        List<PermissionListItem> optionalModuleSensors = new ArrayList<>();

        if (requiredSensors != null) {
            for (ModuleCapabilityResponse capability : requiredSensors) {
                PermissionListItem item = new PermissionListItem(capability.getType(), true);
                requiredModuleSensors.add(item);
            }
        }

        if (optionalSensors != null) {
            for (ModuleCapabilityResponse capability : optionalSensors) {
                PermissionListItem item = new PermissionListItem(capability.getType(), false);
                optionalModuleSensors.add(item);
            }
        }

        permissionRequiredRecyclerView = ButterKnife.findById(dialogView, R.id.module_permission_required_list);
        permissionRequiredRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        permissionRequiredRecyclerView.setAdapter(new PermissionAdapter(requiredModuleSensors));

        permissionOptionalRecyclerView = ButterKnife.findById(dialogView, R.id.module_permission_optional_list);
        permissionOptionalRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        permissionOptionalRecyclerView.setAdapter(new PermissionAdapter(optionalModuleSensors));

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Saves information into db / install a module for user
     *
     * @param modulePackageName
     */
    private void installModule(final String modulePackageName) {

        Log.d(TAG, "Installation of a module " + modulePackageName + " has started...");
        Log.d(TAG, "Requesting service...");

        String userToken = UserUtils.getUserToken(getApplicationContext());

        ToggleModuleRequest toggleModuleRequest = new ToggleModuleRequest();
        toggleModuleRequest.setModuleId(modulePackageName);

        ModuleEndpoint moduleEndpoint = EndpointGenerator.getInstance(
                getApplicationContext())
                .create(ModuleEndpoint.class);

        moduleEndpoint.activateModule(userToken, toggleModuleRequest,
                new Callback<Void>() {

                    @Override
                    public void success(Void aVoid, Response response) {

                        if (response.getStatus() == 200 || response.getStatus() == 204) {
                            Log.d(TAG, "Module is activated!");
                            saveModuleInstallationInDb(modulePackageName);
                            Log.d(TAG, "Installation has finished!");

                            HarvesterServiceProvider
                                    .getInstance(getApplicationContext())
                                    .startSensingService();

                        } else {
                            Log.d(TAG, "FAIL: service responded with code: " + response.getStatus());
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        showErrorMessages(error);

                        Log.d(TAG, "Installation has failed!");
                    }
                });
    }

    /**
     * Saves module installations status on device
     */
    private void saveModuleInstallationInDb(final String modulePackageName) {

        String userEmail = UserUtils.getUserEmail(getApplicationContext());

        DbUser user = daoProvider.getUserDao().getUserByEmail(userEmail);

        if (user == null) {
            Log.d(TAG, "Installation cancelled: user is null");
            UserUtils.doLogout(getApplicationContext());
            finish();
            return;
        }

        DbModule module = daoProvider
                .getModuleDao()
                .getModuleByPackageIdUserId(modulePackageName, user.getId());

        if (module == null) {
            AvailableModuleResponse moduleInfo = mAvailableModuleResponses.get(modulePackageName);
            module = ConverterUtils.convertModule(moduleInfo);
        }

        module.setActive(true);

        Long installId = daoProvider.getModuleDao().insertModule(module);

        if (installId == null) {
            Toaster.showLong(getApplicationContext(), R.string.module_installation_unsuccessful);
        } else {
            UserUtils.saveUserHasModules(getApplicationContext(), true);
            Toaster.showLong(getApplicationContext(), R.string.module_installation_successful);
        }

        Log.d(TAG, "Installation id: " + installId);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy -> unbound resources");

        ButterKnife.unbind(this);

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:

                onBackPressed();
                finish();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
//                    ErrorResponse errorResponse = (ErrorResponse) retrofitError.getBodyAs(ErrorResponse.class);
//                    errorResponse.setStatusCode(httpCode);
//
//                    Integer apiResponseCode = errorResponse.getCode();
//                    String apiMessage = errorResponse.getMessage();
//                    int httpResponseCode = errorResponse.getStatusCode();
//
//                    Log.d(TAG, "Response status: " + httpResponseCode);
//                    Log.d(TAG, "Response code: " + apiResponseCode);
//                    Log.d(TAG, "Response message: " + apiMessage);

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
//                    Toaster.showLong(getApplicationContext(), R.string.error_service_not_available);
                    break;
                case 503:
//                    Toaster.showLong(getApplicationContext(), R.string.error_server_temporary_unavailable);
                    break;
                default:
//                    Toaster.showLong(getApplicationContext(), R.string.error_unknown);
                    break;
            }
        } else {
//            Toaster.showLong(getApplicationContext(), R.string.error_service_not_available);
        }
    }
}
