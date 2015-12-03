package de.tudarmstadt.informatik.tk.android.assistance.controller.modules;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tudarmstadt.informatik.tk.android.assistance.controller.CommonControllerImpl;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnActiveModulesResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnAvailableModulesResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.module.AvailableModuleResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.endpoint.ModuleEndpoint;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.modules.ModulesPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.endpoint.EndpointGenerator;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.PermissionUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public class ModulesControllerImpl extends
        CommonControllerImpl implements
        ModulesController {

    private static final String TAG = ModulesControllerImpl.class.getSimpleName();

    private final ModulesPresenter presenter;

    public ModulesControllerImpl(ModulesPresenter presenter) {
        super(presenter.getContext());
        this.presenter = presenter;
    }

    @Override
    public void requestAvailableModules(String userToken, final OnAvailableModulesResponseHandler availableModulesHandler) {

        final ModuleEndpoint moduleEndpoint = EndpointGenerator
                .getInstance(presenter.getContext())
                .create(ModuleEndpoint.class);

        moduleEndpoint.getAvailableModules(userToken,
                new Callback<List<AvailableModuleResponseDto>>() {

                    /**
                     * Successful HTTP response.
                     *
                     * @param availableModulesList
                     * @param response
                     */
                    @Override
                    public void success(final List<AvailableModuleResponseDto> availableModulesList,
                                        Response response) {
                        availableModulesHandler.onAvailableModulesSuccess(availableModulesList,
                                response);
                    }

                    /**
                     * Unsuccessful HTTP response due to network failure, non-2XX status code, or unexpected
                     * exception.
                     *
                     * @param error
                     */
                    @Override
                    public void failure(RetrofitError error) {
                        availableModulesHandler.onAvailableModulesError(error);
                    }
                });
    }

    @Override
    public void requestActiveModules(final String userToken,
                                     final OnActiveModulesResponseHandler handler) {

        final ModuleEndpoint moduleEndpoint = EndpointGenerator
                .getInstance(presenter.getContext())
                .create(ModuleEndpoint.class);

        moduleEndpoint.getActiveModules(userToken,
                new Callback<List<String>>() {

                    @Override
                    public void success(List<String> activeModules,
                                        Response response) {

                        handler.onActiveModulesReceived(activeModules, response);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        handler.onActiveModulesFailed(error);
                    }
                });
    }

    @Override
    public Set<String> getGrantedPermissions() {

        Map<String, String[]> mappings = PermissionUtils
                .getInstance(presenter.getContext())
                .getDangerousPermissionsToDtoMapping();

        Set<String> permissionsToAsk = new HashSet<>();

        long userId = PreferenceUtils.getCurrentUserId(presenter.getContext());

        List<DbModule> allActiveModules = daoProvider
                .getModuleDao()
                .getAllActive(userId);

        if (allActiveModules == null || allActiveModules.isEmpty()) {
            return Collections.emptySet();
        }

        for (DbModule module : allActiveModules) {

            if (module == null) {
                continue;
            }

            List<DbModuleCapability> capabilities = module.getDbModuleCapabilityList();

            for (DbModuleCapability cap : capabilities) {

                if (cap == null) {
                    continue;
                }

                final String[] perms = mappings == null ? null : mappings.get(cap.getType());

                if (perms == null) {
                    continue;
                }

                for (String perm : perms) {
                    if (PermissionUtils
                            .getInstance(presenter.getContext())
                            .isPermissionGranted(perm)) {

                        permissionsToAsk.add(perm);
                    }
                }
            }
        }

        return permissionsToAsk;
    }
}
