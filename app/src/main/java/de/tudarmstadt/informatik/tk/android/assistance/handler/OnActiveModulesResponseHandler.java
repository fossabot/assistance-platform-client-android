package de.tudarmstadt.informatik.tk.android.assistance.handler;

import java.util.Set;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public interface OnActiveModulesResponseHandler {

    void onActiveModulesReceived(Set<String> activeModules, Response response);

    void onActiveModulesFailed(RetrofitError error);
}
