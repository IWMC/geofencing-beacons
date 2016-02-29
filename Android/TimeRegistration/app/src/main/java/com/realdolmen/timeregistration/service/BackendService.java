package com.realdolmen.timeregistration.service;

import android.content.Context;

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

/**
 * Created by BCCAZ45 on 29/02/2016.
 */
public class BackendService {

    private final static BackendService instance = new BackendService();

    private static final String HOST = "http://127.0.0.1";

    private static final String
            API_LOGIN_URI = HOST + "/api/user/login",
            API_LIST_EMPLOYEES = HOST + "/api/employees";

    private Context context;

    private final Gson compactGson = new GsonBuilder().create();

    public static BackendService with(Context context) {
        instance.context = context;
        return instance;
    }

    public interface RequestCallback<E> {
        void onSuccess(E data);

        void onError(VolleyError error);
    }

    public void login(final User user, final RequestCallback<User> callback) {

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, API_LOGIN_URI, compactGson.toJson(user), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(response.has("token")) {
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
