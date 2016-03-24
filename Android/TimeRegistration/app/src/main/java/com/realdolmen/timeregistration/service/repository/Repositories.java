package com.realdolmen.timeregistration.service.repository;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

/**
 * Factory class for all the data repositories the app will use.
 */
public class Repositories {

	private static OccupationRepository occupationRepository;
	private static RegisteredOccupationRepository registeredOccupationRepository;

	public static void loadOccupationRepository(@NonNull Context context, @Nullable LoadCallback loadCallback) {
		if (context == null) {
			throw new IllegalArgumentException("Context cannot be null!");
		}
		if (occupationRepository == null) {
			occupationRepository = new OccupationRepository(context, loadCallback);
		} else if (loadCallback != null) {
			occupationRepository.addOnLoadCallback(loadCallback);
		}
	}

	public static Promise<RegisteredOccupationRepository, Object, Object> loadRegisteredOccupationRepository(@NonNull Context context) {
		final Deferred<RegisteredOccupationRepository, Object, Object> def = new DeferredObject<>();
		loadRegisteredOccupationRepository(context, new LoadCallback() {
			@Override
			public void onResult(Result result, Throwable error) {
				def.resolve(registeredOccupationRepository);
			}
		});
		return def.promise();
	}

	public static void loadRegisteredOccupationRepository(@NonNull Context context, @Nullable LoadCallback loadCallback) {
		if (context == null) {
			throw new IllegalArgumentException("Context cannot be null!");
		}
		if (registeredOccupationRepository == null) {
			registeredOccupationRepository = new RegisteredOccupationRepository(context, loadCallback);
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
