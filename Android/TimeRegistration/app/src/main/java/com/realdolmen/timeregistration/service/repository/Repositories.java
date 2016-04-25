package com.realdolmen.timeregistration.service.repository;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.VolleyError;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.jetbrains.annotations.TestOnly;

/**
 * Factory class for all the data repositories the app will use.
 */
public class Repositories {

	private static OccupationRepository occupationRepository;
	private static RegisteredOccupationRepository registeredOccupationRepository;

	private static boolean testMode = false;

	public static void logout() {
		try {
			occupationRepository().clear();
			registeredOccupationRepository().clear();
		} catch (Exception e) {
		}
		BackendService.getCurrentSession().setJwtToken("");
	}

	public static class Testing {

		@TestOnly
		public static void setOccupationRepository(OccupationRepository r) {
			occupationRepository = r;
			testMode = true;
		}

		@TestOnly
		public static void setRegisteredOccupationRepository(RegisteredOccupationRepository r) {
			registeredOccupationRepository = r;
			testMode = true;
		}

	}

	public static void loadOccupationRepository(@NonNull Context context, @Nullable final LoadCallback loadCallback) {
		if (testMode) {
			if (loadCallback != null)
				loadCallback.onResult(LoadCallback.Result.SUCCESS, null);
			return;
		}
		if (context == null) {
			throw new IllegalArgumentException("Context cannot be null!");
		}
		if (occupationRepository == null) {
			occupationRepository = new OccupationRepository(context, loadCallback);
		} else if (!occupationRepository.isLoaded()) {
			occupationRepository.reload(context).always(new AlwaysCallback<OccupationRepository, VolleyError>() {
				@Override
				public void onAlways(Promise.State state, OccupationRepository resolved, VolleyError rejected) {
					if (loadCallback != null) {
						occupationRepository.addOnLoadCallback(loadCallback);
					}
				}
			});
		} else if (loadCallback != null) {
			occupationRepository.addOnLoadCallback(loadCallback);
		}
	}

	public static Promise<OccupationRepository, Throwable, Object> loadOccupationRepository(@NonNull Context context) {
		final Deferred<OccupationRepository, Throwable, Object> def = new DeferredObject<>();
		loadOccupationRepository(context, new LoadCallback() {
			@Override
			public void onResult(Result result, Throwable error) {
				if (result == Result.SUCCESS)
					def.resolve(occupationRepository);
				else
					def.reject(error);
			}
		});
		return def.promise();
	}

	public static Promise<RegisteredOccupationRepository, Object, Object> loadRegisteredOccupationRepository(@NonNull Context context) {
		final Deferred<RegisteredOccupationRepository, Object, Object> def = new DeferredObject<>();
		loadRegisteredOccupationRepository(context, new LoadCallback() {
			@Override
			public void onResult(Result result, Throwable error) {
				if (result == Result.SUCCESS)
					def.resolve(registeredOccupationRepository);
				else
					def.reject(error);
			}
		});
		return def.promise();
	}

	public static void loadRegisteredOccupationRepository(@NonNull Context context, @Nullable final LoadCallback loadCallback) {
		if (context == null) {
			throw new IllegalArgumentException("Context cannot be null!");
		}
		if (registeredOccupationRepository == null) {
			registeredOccupationRepository = new RegisteredOccupationRepository(context, loadCallback);
		} else if (!registeredOccupationRepository.isLoaded()) {
			registeredOccupationRepository.reload(context).always(new AlwaysCallback<RegisteredOccupationRepository, VolleyError>() {
				@Override
				public void onAlways(Promise.State state, RegisteredOccupationRepository resolved, VolleyError rejected) {
					if (loadCallback != null) {
						registeredOccupationRepository.addOnLoadCallback(loadCallback);
					}
				}
			});
		} else if (loadCallback != null) {
			registeredOccupationRepository.addOnLoadCallback(loadCallback);
		}
	}

	public static RegisteredOccupationRepository registeredOccupationRepository() {
		if (registeredOccupationRepository == null || !registeredOccupationRepository.isLoaded()) {
			throw new IllegalStateException("Repository must be loaded before using it!");
		}
		return registeredOccupationRepository;
	}

	public static OccupationRepository occupationRepository() {
		if (occupationRepository == null || !occupationRepository.isLoaded()) {
			throw new IllegalStateException("Repository must be loaded before using it!");
		}
		return occupationRepository;
	}
}
