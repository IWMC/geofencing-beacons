package com.realdolmen.timeregistration.service;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.realdolmen.timeregistration.util.json.GsonObjectRequest;
import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.model.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A backend interface to facilitate communication with the backend. It also manages caching using SQLite.
 */
public class BackendService {

	private static final String HOST = "http://10.16.26.90";

	private static final String
			API_LOGIN_URI = HOST + "/api/user/login",
			API_GET_REGISTERED_OCCUPATIONS = HOST + "/api/occupations/registration/?start=%d&end=%d",
			API_CONFIRM_OCCUPATIONS = HOST + "/api/occupations/registration/%d/confirm",
			API_ADD_OCCUPATION_REGISTRATION = HOST + "/api/occupations/registration";

	private Context context;

	private static final Gson compactGson = new GsonBuilder().create();

	private static final Map<Context, BackendService> contextMap = new HashMap<>();

	private static Session currentSession;

	private RequestQueue requestQueue;

	public static Session getCurrentSession() {
		return currentSession;
	}

	public static void setSession(@NonNull Session session) {
		currentSession = session;
	}

	public static boolean isAuthenticated() {
		return currentSession != null && currentSession.getJwtToken() != null && !currentSession.getJwtToken().isEmpty();
	}

	/**
	 * Uses {@link String#format(String, Object...)} to format a parametrized URL.
	 *
	 * @param url  Parametrized URL
	 * @param args Parameters to fill in into the URL.
	 * @return Filled in URL
	 */
	public String params(String url, Object... args) {
		return String.format(url, args);
	}

	/**
	 * Makes a network request to retrieve all the user's occupations between a start and end date.
	 *
	 * @param start    The starting date
	 * @param end      The end date
	 * @param callback {@link RequestCallback#onSuccess(Object)}
	 *                 is called when the server returned 200 OK. When the {@link GsonObjectRequest} could not parse the answer
	 *                 {@link RequestCallback#onError(VolleyError)}
	 *                 is returned.
	 */
	public void getOccupationsInDateRange(Date start, Date end, final RequestCallback<List<RegisteredOccupation>> callback) {
		GsonObjectRequest req = new GsonObjectRequest<>(params(API_GET_REGISTERED_OCCUPATIONS, start.getTime(), end.getTime()), RegisteredOccupation[].class
				, auth(), new Response.Listener<RegisteredOccupation[]>() {
			@Override
			public void onResponse(RegisteredOccupation[] response) {
				callback.onSuccess(Arrays.asList(response));
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				callback.onError(error);
			}
		});

		requestQueue.add(req);
	}

	/**
	 * Creates an instance of {@link BackendService} if it does not yet exist for the given
	 * {@link Context} or returns it if it does.
	 *
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
		requestQueue = Volley.newRequestQueue(context);
	}

	/**
	 * Sends a login request to the backend. The {@link Session} is converted to JSON using {@link Gson}.
	 *
	 * @param session  The session that contains the username and password to use for authentication.
	 * @param callback The {@link RequestCallback< Session >}
	 *                 used to inform the UI of network events.
	 */
	public void login(@NonNull final Session session, final @NonNull RequestCallback<Session> callback) {

		JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, API_LOGIN_URI,
				compactGson.toJson(session), new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				if (response.has("token")) {
					try {
						session.setJwtToken(response.getString("token"));
						callback.onSuccess(session);
						setSession(session);
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

		requestQueue.add(request);
	}

	/**
	 * Adds the Authorization HTTP header (with JWT token) to the request's header.
	 *
	 * @param originalHeaders Original headers
	 * @return New headers map
	 */
	private Map<String, String> auth(@Nullable Map<String, String> originalHeaders) {
		Map<String, String> headers = new HashMap<>();

		if(originalHeaders != null && !originalHeaders.isEmpty()) {
			for(Map.Entry<String, String> entry : originalHeaders.entrySet()) {
				headers.put(entry.getKey(), entry.getValue());
			}
		}

		if (isAuthenticated()) {
			headers.put("Authorization", currentSession.getJwtToken());
		}

		return headers;
	}

	public void confirmOccupations(Date date, final RequestCallback callback) {
		JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, params(API_CONFIRM_OCCUPATIONS, date.getTime()), "", new Response.Listener() {
			@Override
			public void onResponse(Object response) {
				callback.onSuccess(response);
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				callback.onError(error);
			}
		}) {
			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				return auth(super.getHeaders());
			}
		};

		requestQueue.add(req);
	}

	private Map<String, String> auth() {
		return auth(null);
	}
}
