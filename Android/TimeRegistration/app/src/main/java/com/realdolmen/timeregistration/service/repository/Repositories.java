package com.realdolmen.timeregistration.service.repository;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Factory class for all the data repositories the app will use.
 */
public class Repositories {

	private static OccupationRepository occupationRepository;

	public static void loadOccupationRepository(@NonNull Context context, @Nullable LoadCallback loadCallback) {
		if (occupationRepository == null || !occupationRepository.hasLoaded()) {
			occupationRepository = new OccupationRepository(context, loadCallback);
		} else if (loadCallback != null) {
			loadCallback.onResult(LoadCallback.Result.SUCCESS, null);
		}
	}

	public static OccupationRepository occupationRepository() {
		if (occupationRepository == null || !occupationRepository.hasLoaded()) {
			throw new IllegalStateException("Repository must be loaded before using it!");
		}
		return occupationRepository;
	}
}
