package com.realdolmen.timeregistration;

public interface RC {
	interface action {
		interface fromNotification {
			String ADD_SINGLE_RESULT = "com.realdolmen.timeregistration.geofencing.ADD_SINGLE_RESULT";
			String ADD_MULTI_RESULT = "com.realdolmen.timeregistration.geofencing.ADD_MULTI_RESULT";
			String REMOVE_SINGLE_RESULT = "com.realdolmen.timeregistration.geofencing.REMOVE_SINGLE_RESULT";
			String REMOVE_MULTI_RESULT = "com.realdolmen.timeregistration.geofencing.REMOVE_MULTI_RESULT";
		}

		interface addOccupation {
			String ACTION_ADD = "com.realdolmen.occupation.add", ACTION_EDIT = "com.realdolmen.occupation.edit";
		}

		interface login {
			String RE_AUTHENTICATION = "com.realdolmen.timeregistration.login.reauth";
		}
	}

	interface actionExtras {
		interface fromNotifications {
			interface addSingleResult {
				String OCCUPATION_ID = "com.realdolmen.timeregistration.geofencing.ADD_SINGLE_RESULT.OCCUPATION_ID";
				String TIME_DETECTED = "com.realdolmen.timeregistration.geofencing.ADD_SINGLE_RESULT.TIME_DETECTED";
			}

			interface addMultiResult {
				String GEOFENCE_EVENT = "com.realdolmen.timeregistration.geofencing.ADD_MULTI_RESULT.GEOFENCE_EVENT";
				String TIME_DETECTED = "com.realdolmen.timeregistration.geofencing.ADD_MULTI_RESULT.TIME_DETECTED";
				String BEACON_EVENT = "com.realdolmen.timeregistration.geofencing.ADD_MULTI_RESULT.BEACON_EVENT";
			}

			interface removeSingleResult {
				String OCCUPATION_ID = "com.realdolmen.timeregistration.geofencing.REMOVE_SINGLE_RESULT.OCCUPATION_ID";
				String TIME_DETECTED = "com.realdolmen.timeregistration.geofencing.REMOVE_SINGLE_RESULT.TIME_DETECTED";
			}

			interface removeMultiResult {
				String GEOFENCE_EVENT = "com.realdolmen.timeregistration.geofencing.REMOVE_MULTI_RESULT.GEOFENCE_EVENT";
				String BEACON_EVENT = "com.realdolmen.timeregistration.geofencing.REMOVE_MULTI_RESULT.BEACON_EVENT";
				String TIME_DETECTED = "com.realdolmen.timeregistration.geofencing.REMOVE_MULTI_RESULT.TIME_DETECTED";
			}
		}

		interface addOccupation {
			String START_DATE = "SD", END_DATE = "ED", BASE_DATE = "BD", SELECTED_OCCUPATION = "SO", EDITING_OCCUPATION = "EO";
		}
	}

	interface arguments {
		interface registrationFragment {
			String DATE_PARAM = "DATE";
		}
	}

	interface resultCodes {
		interface addOccupation {
			int ADD_RESULT_CODE = 1, EDIT_RESULT_CODE = 2;
		}
		interface bluetooth {
			int ENABLE_REQUEST_RESULT = 3;
		}
	}

	interface geofencing {
		interface events {
			String GOOGLE_API_CONNECTION_FAILED = "com.realdolmen.timeregistration.geofencing.GAPI_CONN_FAILED";
			String FENCES_ADD_SUCCESS = "com.realdolmen.timeregistration.geofencing.FENCES_ADD_SUCCESS";
			String FENCES_ADD_FAIL = "com.realdolmen.timeregistration.geofencing.FENCES_ADD_FAIL";
			String GEOFENCE_ERROR = "com.realdolmen.timeregistration.geofencing.FENCE_ERROR";
		}

		interface requests {
			int CONNECTION_FAILED_RESOLUTION_REQUEST = 9000;
			String RECEIVE_GEOFENCE_REQUEST = "com.aol.android.geofence.ACTION_RECEIVE_GEOFENCE";
		}

		String LOCATION_SERVICES_CATEGORY = "com.realdolmen.location.CATEGORY";

		int POLL_INTERVAL = other.DEV_MODE ? 5000 : 10 * 60 * 1000;
	}

	interface provider {
		interface Codes {
			int ROOT = 1, REGISTERED_OCCUPATIONS = 2, AVAILABLE_OCCUPATIONS = 3;
		}
	}

	interface backend {

		String HOST = "http://10.16.26.76";

		interface urls {
			String API_LOGIN_URI = HOST + "/api/user/login",
					API_VALIDATE_TOKEN = HOST + "/api/user/validate/%s",
					API_GET_REGISTERED_OCCUPATIONS = HOST + "/api/occupations/registration/?date=%d",
					API_CONFIRM_OCCUPATIONS = HOST + "/api/occupations/registration/%d/confirm",
					API_ADD_OCCUPATION_REGISTRATION = HOST + "/api/occupations/registration",
					API_GET_OCCUPATIONS = HOST + "/api/occupations/available",
					API_GET_REGISTERED_OCCUPATIONS_RANGE = HOST + "/api/occupations/registration/range?date=%d&count=%d",
					API_REMOVE_REGISTERED_OCCUPATION = HOST + "/api/occupations/registration/%d",
					API_GET_BEACONS = HOST + "/api/beacons/me",
					API_GET_BEACON_BY_ID = HOST + "/api/beacons/%s";
		}
	}

	interface beacon {
		String UUID = "906bbd3df3264669b1466c225a04d935";
		boolean MEASURE_RANGE = false;
		boolean SAVE_POWER = false;
		int PROCESS_DELAY = 2000;
	}

	interface dtypes {
		int OCCUPATION_DTYPE = 1;
		int PROJECT_DTYPE = 2;
		int TASK_DTYPE = 3;
	}

	interface pref {
		String SHARED_PREFERENCES = "com.realdolmen.timeregistration.preferences";
		String KEY_LAST_LOGGED_IN = "lli";
	}

	interface other {
		boolean DEV_MODE = true;
		boolean KEEP_NOTIFICATIONS = false;
	}

}
