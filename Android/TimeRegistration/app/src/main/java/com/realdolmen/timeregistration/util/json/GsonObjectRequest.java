package com.realdolmen.timeregistration.util.json;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.realdolmen.timeregistration.model.Occupation;

import org.joda.time.DateTime;

import java.io.UnsupportedEncodingException;
import java.util.Map;


/**
 * JsonObjectRequest that uses Gson as the deserializer.
 *
 * @param <T> The class type of the target deserialized class.
 */
public class GsonObjectRequest<T> extends Request<T> {
	private final Gson gson = new GsonBuilder()
			.registerTypeAdapter(DateTime.class, new DateDeserializer())
			.registerTypeAdapter(DateTime.class, new DateSerializer())
			.registerTypeAdapter(Occupation.class, new OccupationDeserializer())
			.create();

	private final Class<T> clazz;
	private final Map<String, String> headers;
	private final Response.Listener<T> listener;

	/**
	 * Make a GET request and return a parsed object from JSON.
	 *
	 * @param url     URL of the request to make
	 * @param clazz   Relevant class object, for Gson's reflection
	 * @param headers Map of request headers
	 */
	public GsonObjectRequest(String url, Class<T> clazz, Map<String, String> headers,
							 Response.Listener<T> listener, Response.ErrorListener errorListener) {
		super(Request.Method.GET, url, errorListener);
		this.clazz = clazz;
		this.headers = headers;
		this.listener = listener;
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		return headers != null ? headers : super.getHeaders();
	}

	@Override
	protected void deliverResponse(T response) {
		listener.onResponse(response);
	}

	@Override
	protected Response<T> parseNetworkResponse(NetworkResponse response) {
		try {
			String json = new String(
					response.data,
					HttpHeaderParser.parseCharset(response.headers));
			return Response.success(
					gson.fromJson(json, clazz),
					HttpHeaderParser.parseCacheHeaders(response));
		} catch (UnsupportedEncodingException e) {
			return Response.error(new ParseError(e));
		} catch (JsonSyntaxException e) {
			return Response.error(new ParseError(e));
		}
	}
}