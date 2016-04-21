package com.realdolmen.timeregistration.repository;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.realdolmen.timeregistration.BuildConfig;
import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.model.Session;
import com.realdolmen.timeregistration.service.ResultCallback;
import com.realdolmen.timeregistration.service.repository.BackendService;
import com.realdolmen.timeregistration.ui.login.LoginActivity;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = Build.VERSION_CODES.JELLY_BEAN, constants = BuildConfig.class)
public class BackendServiceTest {

	private Context dummyContext1, dummyContext2;

	@Mock
	private RequestQueue requestQueue;

	@Mock
	private ResultCallback resultCallback;

	@InjectMocks
	private BackendService backend;

	private Session session = new Session("test", "password");

	private final ObjectWrapper<Request> requestWrapper = new ObjectWrapper<>();

	private RegisteredOccupation dummyOccupation = new RegisteredOccupation();

	private class ObjectWrapper<E> {
		private E object;

		public ObjectWrapper() {
		}

		public ObjectWrapper(E object) {
			this.object = object;
		}

		public E getObject() {
			return object;
		}

		public void setObject(E object) {
			this.object = object;
		}
	}

	@Before
	public void setUp() throws Exception {
		dummyContext1 = Robolectric.setupActivity(LoginActivity.class);
		dummyContext2 = Robolectric.setupActivity(LoginActivity.class);
		backend = BackendService.with(dummyContext1);
		MockitoAnnotations.initMocks(this);

		when(requestQueue.add(any(Request.class))).then(new Answer<Request>() {
			@Override
			public Request answer(InvocationOnMock invocationOnMock) throws Throwable {
				Request req = (Request) invocationOnMock.getArguments()[0];
				requestWrapper.setObject(req);
				return req;
			}
		});
	}

	@Test
	public void testWithProperlyDistinguishesContexts() throws Exception {
		BackendService service1 = BackendService.with(dummyContext1);
		BackendService service2 = BackendService.with(dummyContext2);

		assertNotEquals("Instances of different contexts should not be the same", service1, service2);
		assertEquals("Same service instance should be returned", service1, BackendService.with(dummyContext1));
		assertEquals("Same service instance should be returned", service2, BackendService.with(dummyContext2));
		assertNotEquals("Instances of different contexts should not be the same", service2, BackendService.with(dummyContext1));
	}

	@Test
	public void testGetRegisteredOccupationsUntilNowWithNullDateFails() {
		BackendService service = BackendService.with(dummyContext1);
		service.getRegisteredOccupationsRangeUntilNow(null, 1, new ResultCallback<List<RegisteredOccupation>>() {
			@Override
			public void onResult(@NonNull Result result, @Nullable List<RegisteredOccupation> data, @Nullable VolleyError error) {
				assertEquals("Result of a request with null date should fail.", Result.FAIL, result);
				assertNull("Data cannot be filled in so it should be null", data);
				assertNotNull("An error should exist!", error);
				assertTrue("The cause of the error should be a NullPointerException!", error.getCause() instanceof NullPointerException);
			}
		});
	}

	public void testGetRegisteredOccupationsUntilNowWithNonUTCDateFails() {
		BackendService service = BackendService.with(dummyContext1);
		service.getRegisteredOccupationsRangeUntilNow(new DateTime(DateTimeZone.forID("+01:00")), 1, new ResultCallback<List<RegisteredOccupation>>() {
			@Override
			public void onResult(@NonNull Result result, @Nullable List<RegisteredOccupation> data, @Nullable VolleyError error) {
				assertEquals("Result should be failed!", Result.FAIL, result);
				assertNotNull(error);
				assertNotNull(error.getCause());
				assertTrue("Cause of the error should be instanceof IllegalStateException", error.getCause() instanceof IllegalStateException);

			}
		});
	}

	@Test
	public void testWithNullInputFails() {
		BackendService service = BackendService.with(dummyContext1);
		service.saveOccupation(null, new ResultCallback() {
			@Override
			public void onResult(@NonNull Result result, @Nullable Object data, @Nullable VolleyError error) {
				assertEquals("Result should be failed!", Result.FAIL, result);
				assertNotNull(error);
				assertNotNull(error.getCause());
				assertTrue("Cause of the error should be instanceof NullPointerException", error.getCause() instanceof NullPointerException);
			}
		});
	}

	@Test(expected = NullPointerException.class)
	public void testSaveOccupationWithNullCallbackFails() {
		BackendService service = BackendService.with(dummyContext1);
		service.saveOccupation(null, null);
	}

	@Test(expected = NullPointerException.class)
	public void testSaveOccupationWithNullCallbackAndNonNullInputFails() {
		BackendService service = BackendService.with(dummyContext1);
		service.saveOccupation(dummyOccupation, null);
	}

	@Test(expected = NullPointerException.class)
	public void testGetRelevantOccupationsWithNullCallbackThrowsException() {
		BackendService service = BackendService.with(dummyContext1);
		service.getRelevantOccupations(null);
	}

	@Test
	public void testConfirmOccupationsWithNullDateAndValidCallbackFails() {
		BackendService service = BackendService.with(dummyContext1);
		service.confirmOccupations(null, new ResultCallback() {
			@Override
			public void onResult(@NonNull Result result, @Nullable Object data, @Nullable VolleyError error) {
				assertEquals("Result should be failed!", Result.FAIL, result);
				assertNotNull("Error should not be null!", error);
				assertNotNull("Error's cause should not be null!", error.getCause());
				assertTrue("Error cause should be NullPointerException", error.getCause() instanceof NullPointerException);
			}
		});
	}

	@Test
	public void testConfirmOccupationsWithNonUTCAndValidCallbackFails() {
		BackendService service = BackendService.with(dummyContext1);
		service.confirmOccupations(new DateTime(DateTimeZone.forID("+01:00")), new ResultCallback() {
			@Override
			public void onResult(@NonNull Result result, @Nullable Object data, @Nullable VolleyError error) {
				assertEquals("Result should be failed!", Result.FAIL, result);
				assertNotNull("Error should not be null!", error);
				assertNotNull("Error's cause should not be null!", error.getCause());
				assertTrue("Error cause should be IllegalStateException", error.getCause() instanceof IllegalStateException);
			}
		});
	}

	@Test(expected = NullPointerException.class)
	public void testConfirmOccupationsWithNonUTCDateAndNullCallbackThrowsNPE() {
		BackendService service = BackendService.with(dummyContext1);
		service.confirmOccupations(new DateTime(DateTimeZone.forID("+01:00")), null);
	}

	@Test(expected = NullPointerException.class)
	public void testConfirmOccupationsWithUTCDateAndNullCallbackThrowsNPE() {
		BackendService service = BackendService.with(dummyContext1);
		service.confirmOccupations(new DateTime(DateTimeZone.UTC), null);
	}

//	@Test
//	public void testAuthWithNoAuthenticationDoesNothing() throws InvocationTargetException, IllegalAccessException {
//		when(mockedSession.getJwtToken()).thenReturn("");
//		Map<String, String> someHeaders = new HashMap<>();
//		someHeaders.put("1", "Content");
//		Map<String, String> resultMap = (Map<String, String>) getPublicMethod(backend, "auth").invoke(backend, someHeaders);
//		assertEquals("Headers size should be one more!", someHeaders.size() + 1, resultMap.size());
//		assertNull("Authorization header should not be present for a user that is not authenticated!", resultMap.get("Authorization"));
//	}

//	@Test
//	public void testAuthWithNullHeadersReturnsHeaderWithAuthorization() throws InvocationTargetException, IllegalAccessException {
//		BackendService service = BackendService.with(dummyContext1);
//		Map<String, String> resultMap = (Map<String, String>) getPublicMethod(service, "auth").invoke(service, null);
//		assertEquals("Headers size should be one!", 1, resultMap.size());
//		assertNotNull("Authorization header should be present for a logged in user!", resultMap.get("Authorization"));
//	}

//	@Test
//	public void testAuthWithOriginalHeadersReturnsHeadersWithAuthorizationAppended() throws InvocationTargetException, IllegalAccessException {
//		BackendService service = BackendService.with(dummyContext1);
//		Map<String, String> someHeaders = new HashMap<>();
//		someHeaders.put("1", "Content");
//		Map<String, String> resultMap = (Map<String, String>) getPublicMethod(service, "auth").invoke(service, someHeaders);
//		assertEquals("Headers size should be one more!", someHeaders.size() + 1, resultMap.size());
//		assertNotNull("Authorization header should be present for a logged in user!", resultMap.get("Authorization"));
//	}

	@Test
	public void testGetRegisteredOccupationsByDateWithNullDateFailsWithNPE() {
		BackendService service = BackendService.with(dummyContext1);
		service.getRegisteredOccupationsByDate(null, new ResultCallback<List<RegisteredOccupation>>() {
			@Override
			public void onResult(@NonNull Result result, @Nullable List<RegisteredOccupation> data, @Nullable VolleyError error) {
				assertEquals("Result should be failed!", Result.FAIL, result);
				assertNotNull("Error should not be null!", error);
				assertNotNull("Error's cause should not be null!", error.getCause());
				assertTrue("Error cause should be NullPointerException", error.getCause() instanceof NullPointerException);
			}
		});
	}

	@Test(expected = NullPointerException.class)
	public void testGetRegisteredOccupationsByDateWithNullCallbackThrowsNPE() {
		BackendService service = BackendService.with(dummyContext1);
		service.getRegisteredOccupationsByDate(new DateTime(DateTimeZone.UTC), null);
	}

	@Test
	public void testGetRegisteredOccupationsByDateWithNonUTCDateAndValidCallbackFailsWithISE() {
		BackendService service = BackendService.with(dummyContext1);
		service.getRegisteredOccupationsByDate(new DateTime(DateTimeZone.forID("+01:00")), new ResultCallback<List<RegisteredOccupation>>() {
			@Override
			public void onResult(@NonNull Result result, @Nullable List<RegisteredOccupation> data, @Nullable VolleyError error) {
				assertEquals("Result should be failed!", Result.FAIL, result);
				assertNotNull("Error should not be null!", error);
				assertNotNull("Error's cause should not be null!", error.getCause());
				assertTrue("Error cause should be IllegalStateException because the date is not UTC", error.getCause() instanceof IllegalStateException);
			}
		});
	}

	@Test(expected = NullPointerException.class)
	public void testRemoveRegisteredOccupationWithNullCallbackThrowsNullPointerException() throws Exception {
		BackendService service = BackendService.with(dummyContext1);
		service.removeRegisteredOccupation(0, null);
	}

	public void makeEverythingPublic(Object o) {
		Method[] methods = o.getClass().getDeclaredMethods();
		Field[] fields = o.getClass().getDeclaredFields();

		for (Method m : methods) {
			m.setAccessible(true);
		}

		for (Field f : fields) {
			f.setAccessible(true);
		}
	}

	public void makeEverythingPublic(Class o) {
		Method[] methods = o.getDeclaredMethods();
		Field[] fields = o.getDeclaredFields();

		for (Method m : methods) {
			m.setAccessible(true);
		}

		for (Field f : fields) {
			f.setAccessible(true);
		}
	}

	public Method getPublicMethod(Object o, String name) {
		Method f = null;
		try {
			f = o.getClass().getDeclaredMethod(name);
			f.setAccessible(true);
		} catch (NoSuchMethodException e) {
			throw new NullPointerException("Field does not exist!");
		}
		return f;
	}

}
