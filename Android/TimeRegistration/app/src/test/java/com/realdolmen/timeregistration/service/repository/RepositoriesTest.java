package com.realdolmen.timeregistration.service.repository;

import android.content.Context;
import android.os.Build;

import com.realdolmen.timeregistration.BuildConfig;
import com.realdolmen.timeregistration.ui.login.LoginActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = Build.VERSION_CODES.JELLY_BEAN, constants = BuildConfig.class)
public class RepositoriesTest {

	private Context context;

	@Before
	public void setup() {
		context = Robolectric.setupActivity(LoginActivity.class);
	}
	@Test(expected = IllegalArgumentException.class)
	public void testLoadOccupationRepositoryWithNullContextThrowsIllegalArgumentException() {
		Repositories.loadOccupationRepository(null, null);
	}

	@Test(expected = IllegalStateException.class)
	public void testOccupationRepositoryThrowsIllegalStateExceptionWhenNotLoaded() {
		Repositories.occupationRepository();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLoadRegisteredOccupationRepositoryWithNullContextThrowsIllegalArgumentException() {
		Repositories.loadRegisteredOccupationRepository(null, null);
	}

	@Test(expected = IllegalStateException.class)
	public void testRegisteredOccupationRepositoryThrowsIllegalStateExceptionWhenNotLoaded() {
		Repositories.registeredOccupationRepository();
	}

}