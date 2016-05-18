package com.realdolmen.timeregistration.service.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.realdolmen.timeregistration.RC;
import com.realdolmen.timeregistration.model.LoginRequest;
import com.realdolmen.timeregistration.model.User;
import com.realdolmen.timeregistration.service.repository.BackendService;
import com.realdolmen.timeregistration.util.exception.MissingTokenException;
import com.realdolmen.timeregistration.util.exception.NoSuchUserException;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserManager {

	private static final String TAG = UserManager.class.getSimpleName();

	private static final UserManager INSTANCE = new UserManager();
	private Context context;

	private SharedPreferences preferences;

	private Database db;

	private User loggedInUser;

	private ExecutorService executor = Executors.newSingleThreadExecutor();

	public static UserManager with(@NonNull Context context) {
		INSTANCE.context = context;
		if (INSTANCE.preferences == null) {
			INSTANCE.preferences = context.getSharedPreferences(RC.pref.SHARED_PREFERENCES, Context.MODE_PRIVATE);
		}
		INSTANCE.db = Database.with(context);
		return INSTANCE;
	}

	/**
	 * Checks (asynchronously) if there is a user that has previously logged in and that their token is valid or not.
	 * If the shared preferences contain the ID of a last logged in user, the user will be pulled
	 * from the local SQLite database and if their token is present, it will be validated with the backend.
	 * <p/>
	 * Currently, an internet connection is still required // TODO: 2/05/2016 allow offline access
	 * <p/>
	 * Upon receiving an answer from the backend, if the token is valid, the promise is resolved with no data.
	 * If the token is invalid a {@link MissingTokenException} is passed on to the rejection of the promise.
	 * <p/>
	 * If there is no last logged in user a {@link NoSuchUserException} is passed on to the rejection of the promise.
	 *
	 * @return A promise that returns void on success and Throwable on fail.
	 */
	public Promise<Void, Throwable, Void> checkLocalLogin() {
		final Deferred<Void, Throwable, Void> def = new DeferredObject<>();
		executor.submit(new Runnable() {
			@Override
			public void run() {
				if (preferences.contains(RC.pref.KEY_LAST_LOGGED_IN)) {
					int userId = preferences.getInt(RC.pref.KEY_LAST_LOGGED_IN, -1);
					if (userId != -1) {
						db.getUserById(userId).done(new DoneCallback<User>() {
							@Override
							public void onDone(final User result) {
								if (result == null) {
									preferences.edit().remove(RC.pref.KEY_LAST_LOGGED_IN).apply();
									def.reject(new NoSuchUserException("User not found"));
									return;
								}
								if (result.getToken() != null && !result.getToken().isEmpty()) {
									BackendService.with(context).validateLoginToken(result.getToken()).done(new DoneCallback<Boolean>() {
										@Override
										public void onDone(Boolean valid) {
											//token is valid
											Log.d(TAG, "onDone: stored token is valid");
											loggedInUser = result;
											def.resolve(null);
										}
									}).fail(new FailCallback<Throwable>() {
										@Override
										public void onFail(Throwable result) {
											//token is invalid
											Log.d(TAG, "onFail: stored token is invalid");
											def.reject(new MissingTokenException("Stored token could not be verified"));
										}
									});
								}
							}
						}).fail(new FailCallback<Throwable>() {
							@Override
							public void onFail(Throwable result) {
								Log.d(TAG, "onFail: Unable to get user", result);
								preferences.edit().remove(RC.pref.KEY_LAST_LOGGED_IN).apply();
								def.reject(result);
							}
						});
					}
				} else {
					Log.d(TAG, "No user ID present in shared prefs");
					def.reject(new NoSuchUserException("There is no user ID present in the shared preferences."));
				}
			}
		});
		return def.promise();
	}

	/**
	 * Asynchronously submits a login request to the backend. If the result is positive, a new user with
	 * the given credentials and resulting token is stored. Please note that the user's password is <b>NOT</b> stored
	 * in the database.
	 * <p/>
	 * When the user is added to the local database, their id is stored in shared preferences as last logged in user
	 * and the promise is resolved.
	 * <p/>
	 * If the login request failed, the promise is rejected with the causing exception.
	 *
	 * @param username The username of the user
	 * @param password The password of the user
	 * @return A promise that returns void on success and Throwable on fail.
	 */
	public Promise<Void, Throwable, Void> doLogin(@NonNull final String username, @NonNull final String password) {
		final Deferred<Void, Throwable, Void> def = new DeferredObject<>();
		executor.submit(new Runnable() {
			@Override
			public void run() {
				LoginRequest request = new LoginRequest(username, password);
				BackendService.with(context).login(request).done(new DoneCallback<LoginRequest>() {
					@Override
					public void onDone(LoginRequest result) {
						//successful login
						db.addUser(result.toUser()).done(new DoneCallback<User>() {
							@Override
							public void onDone(User result) {
								loggedInUser = result;
								preferences.edit().putInt(RC.pref.KEY_LAST_LOGGED_IN, loggedInUser.getId()).apply();
								Log.d(TAG, "onDone: Saving user " + loggedInUser.getId() + " to shared prefs");
								def.resolve(null);
							}
						});
					}
				}).fail(new FailCallback<Throwable>() {
					@Override
					public void onFail(Throwable result) {
						//failed login
						Log.d(TAG, "onFail: Login failed", result);
						def.reject(result);
					}
				});
			}
		});
		return def.promise();
	}

	public User getLoggedInUser() {
		return loggedInUser;
	}

	public static void logout(@NonNull Context context) {
		Database.with(context).deleteUser(with(context).loggedInUser);
		with(null).loggedInUser.setToken(null);
		with(null).loggedInUser = null;
	}
}
