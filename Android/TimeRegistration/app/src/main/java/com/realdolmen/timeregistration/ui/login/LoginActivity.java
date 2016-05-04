package com.realdolmen.timeregistration.ui.login;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.VolleyError;
import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.service.GenericVolleyError;
import com.realdolmen.timeregistration.service.data.UserManager;
import com.realdolmen.timeregistration.service.repository.BeaconRepository;
import com.realdolmen.timeregistration.service.repository.Repositories;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;

import java.util.Arrays;
import java.util.Collection;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity implements BeaconConsumer {

	private static final String TAG = "LOGIN";
	private static final boolean DEBUG = true;
	@Bind(R.id.login_username)
	EditText username;

	@Bind(R.id.login_password)
	EditText password;

	@Bind(R.id.login_login_button)
	Button loginButton;

	private boolean loggingIn;
	private boolean ignoreDismiss = false;

	private BeaconManager beaconManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final ProgressDialog loginProgress = new ProgressDialog(this);
		loginProgress.setIndeterminate(true);
		loginProgress.setMessage(getString(R.string.login_logging_in));
		loginProgress.show();
		loginProgress.setCanceledOnTouchOutside(false);
		loginProgress.setCancelable(false);
		beaconManager = BeaconManager.getInstanceForApplication(this);
		beaconManager.getBeaconParsers().add(new BeaconParser().
				setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));

		UserManager.with(this).checkLocalLogin().done(new DoneCallback<Void>() {
			@Override
			public void onDone(Void result) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (!ignoreDismiss)
							loginProgress.dismiss();
					}
				});
				loggingIn = false;
				onSuccessfulLogin();
			}
		}).fail(new FailCallback<Throwable>() {
			@Override
			public void onFail(Throwable result) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (!ignoreDismiss)
							loginProgress.dismiss();
					}
				});
				loggingIn = false;
				if (result instanceof IllegalStateException) {
					//Proceed with regular login procedure
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							setContentView(R.layout.activity_login);
							ButterKnife.bind(LoginActivity.this);
							if (DEBUG) {
								username.setText("brentc");
								password.setText("Bla123");
							}
						}
					});
				} else {
					//Network unavailable
					Snackbar.make(findViewById(R.id.day_registration_root_view), "Network exception", Snackbar.LENGTH_LONG).show();
				}
			}
		});
	}

	private void onSuccessfulLogin() {
		Log.d(TAG, "onSuccessfulLogin: Login successful");
		beaconManager.bind(this);
		Repositories.loadBeaconRepository(this).done(new DoneCallback<BeaconRepository>() {
			@Override
			public void onDone(BeaconRepository result) {
				Log.d(TAG, "onDone: " + Arrays.toString(result.getAll().toArray()));
			}
		}).fail(new FailCallback<Throwable>() {
			@Override
			public void onFail(Throwable result) {
				Log.d(TAG, "onFail: Getting beacons failed", result);
			}
		});
//		finish();
//		startActivity(new Intent(getApplicationContext(), DayRegistrationActivity.class));
	}

	@Override
	protected void onDestroy() {
		ignoreDismiss = true;
		beaconManager.unbind(this);
		super.onDestroy();
	}

	@OnClick(R.id.login_login_button)
	public void doLogin() {
		if (loggingIn) {
			return;
		}
		if (validate()) {
			loggingIn = true;
			doLoginStep2();
		}
	}

	private void doLoginStep2() {
		final ProgressDialog loginProgress = new ProgressDialog(this);
		loginProgress.setIndeterminate(true);
		loginProgress.setMessage(getString(R.string.login_logging_in));
		loginProgress.show();
		loginProgress.setCanceledOnTouchOutside(false);
		loginProgress.setCancelable(false);

		UserManager.with(this).doLogin(username.getText().toString(), password.getText().toString()).done(new DoneCallback<Void>() {
			@Override
			public void onDone(Void result) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (!ignoreDismiss)
							loginProgress.dismiss();
					}
				});
				loggingIn = false;
				onSuccessfulLogin();
			}
		}).fail(new FailCallback<Throwable>() {
			@Override
			public void onFail(Throwable result) {

				if (result instanceof GenericVolleyError) {
					final Snackbar bar = Snackbar.make(findViewById(android.R.id.content), R.string.login_server_unreachable, Snackbar.LENGTH_INDEFINITE);
					bar.setAction(R.string.login_retry, new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							bar.dismiss();
							doLogin();
						}
					}).show();

					new android.os.Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							bar.dismiss();
						}
					}, 10000);
				} else if (result instanceof VolleyError) {
					VolleyError error = (VolleyError) result;
					if (error.networkResponse != null) {
						if (error.networkResponse.statusCode == 400) {
							Snackbar.make(findViewById(android.R.id.content), R.string.login_incorrect_credentials, Snackbar.LENGTH_LONG).show();
						} else {
							Snackbar.make(findViewById(android.R.id.content), R.string.login_generic_error, Snackbar.LENGTH_LONG).show();
						}
					} else {
						Snackbar.make(findViewById(android.R.id.content), R.string.login_generic_error, Snackbar.LENGTH_LONG).show();
					}
				} else {
					Snackbar.make(findViewById(R.id.day_registration_root_view), "An error occured.", Snackbar.LENGTH_LONG).show();
				}

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (!ignoreDismiss)
							loginProgress.dismiss();
					}
				});
				loggingIn = false;
			}
		});
	}

	private boolean validate() {
		boolean valid = true;
		if (username.getText().toString().trim().isEmpty()) {
			username.setError(getString(R.string.login_empty_field));
			valid = false;
		}

		if (password.getText().toString().trim().isEmpty()) {
			password.setError(getString(R.string.login_empty_field));
			valid = false;
		}

		return valid && !loggingIn;
	}

	@Override
	public void onBeaconServiceConnect() {
		beaconManager.setMonitorNotifier(new MonitorNotifier() {
			@Override
			public void didEnterRegion(Region region) {
				Log.d(TAG, "didEnterRegion: " + region);
			}

			@Override
			public void didExitRegion(Region region) {
				Log.d(TAG, "didExitRegion: " + region);
			}

			@Override
			public void didDetermineStateForRegion(int i, Region region) {
				Log.d(TAG, "didDetermineStateForRegion: " + i + " - " + region);
			}
		});

		beaconManager.setRangeNotifier(new RangeNotifier() {
			@Override
			public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
					for(Beacon beacon : beacons) {
						Log.d(TAG, String.format("Beacon '%s' mapped to %s is %.2f meters away.", beacon.getId1(), beacon.getId2(), beacon.getDistance()));
					}
			}
		});

		try {
			beaconManager.startRangingBeaconsInRegion(new Region("E20A39F473F54BC4A12F17D1AD07A961", null, null, null));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
