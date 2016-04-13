package com.realdolmen.timeregistration;

public interface Constants {
	interface actions {
		interface fromNotifications {
			String ADD_SINGLE_RESULT = "com.realdolmen.timeregistration.geofencing.ADD_SINGLE_RESULT";
		}

		interface addOccupation {
			String ACTION_ADD = "com.realdolmen.occupation.add", ACTION_EDIT = "com.realdolmen.occupation.edit";
		}
	}

	interface actionExtras {
		interface fromNotifications {
			interface addSingleResult {
				String OCCUPATION_ID = "com.realdolmen.timeregistration.geofencing.ADD_SINGLE_RESULT.OCCUPATION_ID";
			}
		}

		interface addOccupation {
			String START_DATE = "SD", END_DATE = "ED", BASE_DATE = "BD", SELECTED_OCCUPATION = "SO", EDITING_OCCUPATION = "EO";
		}
	}

	interface resultCodes {
		interface addOccupation {
			int ADD_RESULT_CODE = 1, EDIT_RESULT_CODE = 2;
		}
	}

	interface geofencing {
		interface events {
			String GOOGLE_API_CONNECTION_FAILED = "com.realdolmen.timeregistration.geofencing.GAPI_CONN_FAILED";
			String GEOFENCING_FENCES_ADD_SUCCESS = "com.realdolmen.timeregistration.geofencing.FENCES_ADD_SUCCESS";
			String GEOFENCING_FENCES_ADD_FAIL = "com.realdolmen.timeregistration.geofencing.FENCES_ADD_FAIL";
		}

		interface requests {
			int CONNECTION_FAILED_RESOLUTION_REQUEST = 9000;
			String RECEIVE_GEOFENCE_REQUEST = "com.aol.android.geofence.ACTION_RECEIVE_GEOFENCE";
		}

		String LOCATION_SERVICES_CATEGORY = "com.realdolmen.location.CATEGORY";

		int POLL_INTERVAL = DEV_MODE ? 5000 : 10 * 60 * 1000;
	}

	interface provider {
		interface Codes {
			int ROOT = 1, REGISTERED_OCCUPATIONS = 2, AVAILABLE_OCCUPATIONS = 3;
		}
	}

	interface backend {

		String HOST = "http://10.16.26.142";

		interface urls {
			String API_LOGIN_URI = HOST + "/api/user/login",
					API_GET_REGISTERED_OCCUPATIONS = HOST + "/api/occupations/registration/?date=%d",
					API_CONFIRM_OCCUPATIONS = HOST + "/api/occupations/registration/%d/confirm",
					API_ADD_OCCUPATION_REGISTRATION = HOST + "/api/occupations/registration",
					API_GET_OCCUPATIONS = HOST + "/api/occupations/available",
					API_GET_REGISTERED_OCCUPATIONS_RANGE = HOST + "/api/occupations/registration/range?date=%d&count=%d",
					API_REMOVE_REGISTERED_OCCUPATION = HOST + "/api/occupations/registration/%d";
		}
	}

	interface dtypes {
		int OCCUPATION_DTYPE = 1;
		int PROJECT_DTYPE = 2;
	}

	boolean DEV_MODE = true;
}
