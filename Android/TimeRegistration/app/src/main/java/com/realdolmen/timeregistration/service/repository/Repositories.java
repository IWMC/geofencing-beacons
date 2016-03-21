package com.realdolmen.timeregistration.service.repository;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Factory class for all the data repositories the app will use.
 */
public class Repositories {

	private static OccupationRepository occupationRepository;
	private static RegisteredOccupationRepository registeredOccupationRepository;

	public static void loadOccupationRepository(@NonNull Context context, @Nullable LoadCallback loadCallback) {
		if (occupationRepository == null) {
			occupationRepository = new OccupationRepository(context, loadCallback);
		} else if (loadCallback != null) {
			occupationRepository.addOnLoadCallback(loadCallback);
		}
	}

	public static void loadRegisteredOccupationRepository(@NonNull Context context, @Nullable LoadCallback loadCallback) {
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
