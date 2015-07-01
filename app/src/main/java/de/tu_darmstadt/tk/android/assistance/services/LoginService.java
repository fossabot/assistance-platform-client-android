package de.tu_darmstadt.tk.android.assistance.services;

import de.tu_darmstadt.tk.android.assistance.models.http.request.LoginRequest;
import de.tu_darmstadt.tk.android.assistance.models.http.response.LoginResponse;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public interface LoginService {

    @POST("/users/login")
    void loginUser(@Body LoginRequest body, Callback<LoginResponse> callback);
}
