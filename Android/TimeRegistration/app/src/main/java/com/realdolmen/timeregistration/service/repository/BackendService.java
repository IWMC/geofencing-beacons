package com.realdolmen.timeregistration.service.repository;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.model.Session;
import com.realdolmen.timeregistration.service.GenericVolleyError;
import com.realdolmen.timeregistration.service.ResultCallback;
import com.realdolmen.timeregistration.util.DateUtil;
import com.realdolmen.timeregistration.util.UTC;
import com.realdolmen.timeregistration.util.json.DateSerializer;
import com.realdolmen.timeregistration.util.json.GsonObjectRequest;

import org.jetbrains.annotations.TestOnly;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.realdolmen.timeregistration.RC.backend.urls.API_ADD_OCCUPATION_REGISTRATION;
import static com.realdolmen.timeregistration.RC.backend.urls.API_CONFIRM_OCCUPATIONS;
import static com.realdolmen.timeregistration.RC.backend.urls.API_GET_OCCUPATIONS;
import static com.realdolmen.timeregistration.RC.backend.urls.API_GET_REGISTERED_OCCUPATIONS;
import static com.realdolmen.timeregistration.RC.backend.urls.API_GET_REGISTERED_OCCUPATIONS_RANGE;
import static com.realdolmen.timeregistration.RC.backend.urls.API_LOGIN_URI;
import static com.realdolmen.timeregistration.RC.backend.urls.API_REMOVE_REGISTERED_OCCUPATION;

/**
 * A backend interface to facilitate communication with the backend. It also manages caching using SQLite.
 */
public class BackendService {

	private Context context;
	private static final Gson compactGson = new GsonBuilder().registerTypeAdapter(DateTime.class, new DateSerializer()).create();
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

	public static class Testing {

		@TestOnly
		public static void setBackendService(Context context, BackendService service) {
			contextMap.put(context, service);
		}
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
	 * Makes a network request to retrieve all the user's occupations between a date and end date.
	 *
	 * @param date     The starting date
	 * @param callback {@link ResultCallback#onResult(ResultCallback.Result, Object, VolleyError)}
	 *                 is called with SUCCESS result when the server returned 200 OK. When the {@link GsonObjectRequest} could not parse the answer
	 *                 {@link ResultCallback#onResult(ResultCallback.Result, Object, VolleyError)} with FAIL result is called.
	 */
	public void getRegisteredOccupationsByDate(@UTC @NonNull DateTime date, @NonNull final ResultCallback<List<RegisteredOccupation>> callback) {
		if (callback == null) {
			throw new NullPointerException("Callback cannot be null!");
		}

		if (date == null) {
			callback.onResult(ResultCallback.Result.FAIL, null,
							  new GenericVolleyError(new NullPointerException("Date should not be null!"))
			);
			return;
		}

		if (DateUtil.enforceUTC(date, callback)) {
			return;
		}

		GsonObjectRequest req = new GsonObjectRequest<>(params(
				API_GET_REGISTERED_OCCUPATIONS,
				date.toDateTime(DateTimeZone.UTC).getMillis()
		), RegisteredOccupation[].class
				, auth(), new Response.Listener<RegisteredOccupation[]>() {
			@Override
			public void onResponse(RegisteredOccupation[] response) {
				callback.onResult(ResultCallback.Result.SUCCESS, Arrays.asList(response), null);
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				callback.onResult(ResultCallback.Result.FAIL, null, error);
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
	 * @param callback The {@link ResultCallback < Session >}
	 *                 used to inform the UI of network events.
	 */
	public void login(@NonNull final Session session, final @NonNull ResultCallback<Session> callback) {

		JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, API_LOGIN_URI,
														  compactGson.toJson(session), new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				if (response.has("token")) {
					try {
						session.setJwtToken(response.getString("token"));
						setSession(session);
						callback.onResult(ResultCallback.Result.SUCCESS, session, null);
					} catch (JSONException e) {
						callback.onResult(ResultCallback.Result.FAIL, null, new GenericVolleyError(e.getMessage()));
					}
				} else {
					callback.onResult(ResultCallback.Result.FAIL, null, new GenericVolleyError("Server response does not contain jwt token in JSON format"));
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				callback.onResult(ResultCallback.Result.FAIL, null, error);
			}
		}
		);

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

		if (originalHeaders != null && !originalHeaders.isEmpty()) {
			for (Map.Entry<String, String> entry : originalHeaders.entrySet()) {
				headers.put(entry.getKey(), entry.getValue());
			}
		}

		if (isAuthenticated()) {
			headers.put("Authorization", currentSession.getJwtToken());
		}

		return headers;
	}

	public void confirmOccupations(@UTC DateTime date, @NonNull final ResultCallback<Integer> callback) {
		if (callback == null) {
			throw new NullPointerException("Callback may not be null!");
		}

		if (date == null) {
			callback.onResult(ResultCallback.Result.FAIL, null,
							  new GenericVolleyError(new NullPointerException("Date may not be null!"))
			);
			return;
		}

		if (DateUtil.enforceUTC(date, callback)) {
			return;
		}

		final AtomicInteger statusCode = new AtomicInteger(0);

		JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, params(
				API_CONFIRM_OCCUPATIONS,
				date.getMillis()
		), "", new Response.Listener() {
			@Override
			public void onResponse(Object response) {
				callback.onResult(ResultCallback.Result.SUCCESS, statusCode.get(), null);
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				callback.onResult(ResultCallback.Result.FAIL, null, error);
			}
		}) {
			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				return auth(super.getHeaders());
			}

			@Override
			protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
				statusCode.set(response.statusCode);
				if (response.data.length == 0) {
					response = new NetworkResponse(response.statusCode, "{}".getBytes(), response.headers, response.notModified);
				}
				return super.parseNetworkResponse(response);
			}
		};

		requestQueue.add(req);
	}

	private Map<String, String> auth() {
		return auth(null);
	}

	public void getRelevantOccupations(@NonNull final ResultCallback<List<Occupation>> resultCallback) {
		if (resultCallback == null) {
			throw new NullPointerException("Callback cannot be null!");
		}
		GsonObjectRequest req = new GsonObjectRequest<>(API_GET_OCCUPATIONS, Occupation[].class
				, auth(), new Response.Listener<Occupation[]>() {
			@Override
			public void onResponse(Occupation[] response) {
				resultCallback.onResult(ResultCallback.Result.SUCCESS, Arrays.asList(response), null);
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				resultCallback.onResult(ResultCallback.Result.FAIL, null, error);
			}
		});

		requestQueue.add(req);
	}

	public void getRegisteredOccupationsRangeUntilNow(@UTC @NonNull DateTime originDate, int count, @NonNull final ResultCallback<List<RegisteredOccupation>> callback) {
		if (callback == null) {
			throw new NullPointerException("Callback cannot be null!");
		}

		if (originDate == null) {
			callback.onResult(ResultCallback.Result.FAIL, null, new GenericVolleyError(new NullPointerException("Origin date cannot be null!")));
			return;
		}

		if (count <= 0) {
			count = 1;
		}

		DateUtil.enforceUTC(originDate, "Origin date must be in UTC format!", callback);
		GsonObjectRequest req = new GsonObjectRequest(params(API_GET_REGISTERED_OCCUPATIONS_RANGE, originDate.getMillis(), count), RegisteredOccupation[].class, auth(), new Response.Listener<RegisteredOccupation[]>() {
			@Override
			public void onResponse(RegisteredOccupation[] response) {
				callback.onResult(ResultCallback.Result.SUCCESS, Arrays.asList(response), null);
			}

		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				callback.onResult(ResultCallback.Result.FAIL, null, error);
			}
		});
		requestQueue.add(req);
	}

	public void saveOccupation(@NonNull RegisteredOccupation ro, @NonNull final ResultCallback<Long> callback) {
		saveOccupation(false, ro, callback);
	}

	public void saveOccupation(final boolean existing, @NonNull RegisteredOccupation ro, @NonNull final ResultCallback<Long> callback) {
		if (callback == null) {
			throw new NullPointerException("Callback cannot be null!");
		}

		if (ro == null) {
			callback.onResult(ResultCallback.Result.FAIL, null, new GenericVolleyError(new NullPointerException("Registered occupation cannot be null!")));
			return;
		}

		final AtomicInteger statusCode = new AtomicInteger(0);

		Request req = new JsonObjectRequest(existing ? Request.Method.PUT : Request.Method.POST, API_ADD_OCCUPATION_REGISTRATION, compactGson.toJson(ro), new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				if (!existing && response.has("id"))
					try {
						callback.onResult(ResultCallback.Result.SUCCESS, response.getLong("id"), null);
					} catch (JSONException e) {
						callback.onResult(ResultCallback.Result.FAIL, null, new GenericVolleyError(e));
					}
				else if (existing) {
					if (statusCode.get() == 204) {
						callback.onResult(ResultCallback.Result.SUCCESS, null, null);
					} else {
						callback.onResult(ResultCallback.Result.FAIL, null, new GenericVolleyError("Unable to update backend (status code: " + statusCode + ")"));
					}
				} else
					callback.onResult(ResultCallback.Result.FAIL, null, new GenericVolleyError(new NullPointerException("Server did not return id!")));
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				callback.onResult(ResultCallback.Result.FAIL, null, error);
			}
		}) {
			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				return auth(super.getHeaders());
			}

			@Override
			protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
				statusCode.set(response.statusCode);
				if (response.data.length == 0) {
					response = new NetworkResponse(response.statusCode, "{}".getBytes(), response.headers, response.notModified);
				}
				return super.parseNetworkResponse(response);
			}
		};

		requestQueue.add(req);
	}

	public void removeRegisteredOccupation(long elementId, @NonNull final ResultCallback<Integer> callback) {
		if (callback == null) {
			throw new NullPointerException("Callback cannot be null");
		}

		final AtomicInteger statusCode = new AtomicInteger(0);

		JsonObjectRequest req = new JsonObjectRequest(
				Request.Method.DELETE,
				params(API_REMOVE_REGISTERED_OCCUPATION, elementId),
				"",
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						callback.onResult(ResultCallback.Result.SUCCESS, statusCode.get(), null);
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						callback.onResult(ResultCallback.Result.FAIL, error.networkResponse != null ? error.networkResponse.statusCode : -1, error);
					}
				}
		) {

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				return auth(super.getHeaders());
			}

			@Override
			protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
				statusCode.set(response.statusCode);
				if (response.data.length == 0) {
					response = new NetworkResponse(response.statusCode, "{}".getBytes(), response.headers, response.notModified);
				}
				return super.parseNetworkResponse(response);
			}
		};

		requestQueue.add(req);

	}
}
