package com.realdolmen.timeregistration.service;

import android.content.Context;
import android.support.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.realdolmen.timeregistration.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A backend interface to facilitate communication with the backend. It also manages caching using SQLite.
 */
public class BackendService {

    private static final String HOST = "http://10.16.26.89";

    private static final String
            API_LOGIN_URI = HOST + "/api/user/login",
            API_LIST_EMPLOYEES = HOST + "/api/employees";

    private Context context;

    private static final Gson compactGson = new GsonBuilder().create();

    private static final Map<Context, BackendService> contextMap = new HashMap<>();

    /**
     * Creates an instance of {@link BackendService} if it does not yet exist for the given {@link Context} or returns it if it does.
     * @param context The context used in the {@link BackendService}.
     * @return an instance of {@link BackendService} given a {@link Context}.
     */
    public static BackendService with(@NonNull Context context) {
        if (contextMap.containsKey(context)) {
            return contextMap.get(context);
        }

        BackendService service = new BackendService(context);
        contextMap.put(context, service);
        return service;
    }

    public BackendService(Context context) {
        this.context = context;
    }

    /**
     * Interface designed to implement callbacks for the UI to respond to network events.
     * @param <E> Generic type of the onSuccess data parameter.
     */
    public interface RequestCallback<E> {
        /**
         * Called when the request succeeded. A request succeeds when the http response code is in the error range.
         * @param data The data produced from the successful response.
         */
        void onSuccess(E data);

        /**
         * Called when the request fails. A request can fail if the http response code is in the error range or the response is invalid.
         * @param error The error produces by Volley. There is also a {@link GenericVolleyError} for custom errors in case of invalid responses.
         */
        void onError(VolleyError error);
    }

    /**
     * Sends a login request to the backend. The {@link User} is converted to JSON using {@link Gson}.
     * @param user The user that contains the username and password to use for authentication.
     * @param callback The {@link com.realdolmen.timeregistration.service.BackendService.RequestCallback<User>} used to inform the UI of network events.
     */
    public void login(@NonNull final User user, final @NonNull RequestCallback<User> callback) {

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, API_LOGIN_URI, compactGson.toJson(user), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response.has("token")) {
                    try {
                        user.setJwtToken(response.getString("token"));
                        callback.onSuccess(user);
                    } catch (JSONException e) {
                        callback.onError(new GenericVolleyError(e.getMessage()));
                    }
                } else {
                    callback.onError(new GenericVolleyError("Server response does not contain jwt token in JSON format"));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(error);
            }
        });

        Volley.newRequestQueue(context).add(request);
    }
}
