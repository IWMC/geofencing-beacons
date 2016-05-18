package com.realdolmen.timeregistration.ui.login;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.VolleyError;
import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.RC;
import com.realdolmen.timeregistration.service.GenericVolleyError;
import com.realdolmen.timeregistration.service.data.UserManager;
import com.realdolmen.timeregistration.service.location.beacon.BeaconDwellService;
import com.realdolmen.timeregistration.ui.dayregistration.DayRegistrationActivity;
import com.realdolmen.timeregistration.util.exception.NoInternetException;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity implements ServiceConnection {

	private static final String TAG = "LOGIN";
	private static final boolean DEBUG = true;

	@BindView(R.id.login_username)
	EditText username;

	@BindView(R.id.login_password)
	EditText password;

	@BindView(R.id.login_login_button)
	Button loginButton;

	private boolean loggingIn;
	private boolean ignoreDismiss = false;

	private BeaconDwellService dwellManager;
	private boolean canNotifyService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final ProgressDialog loginProgress = new ProgressDialog(this);
		loginProgress.setIndeterminate(true);
		loginProgress.setMessage(getString(R.string.login_logging_in));
		loginProgress.show();
		loginProgress.setCanceledOnTouchOutside(false);
		loginProgress.setCancelable(false);
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
				if (result instanceof NoInternetException) {
					setContentView(R.layout.activity_login);
					ButterKnife.bind(LoginActivity.this);
					if (DEBUG) {
						username.setText("brentc");
						password.setText("Bla123");
					}
					Snackbar.make(findViewById(R.id.login_root_view), R.string.login_no_internet, Snackbar.LENGTH_LONG).show();
				} else if (result instanceof IllegalStateException) {
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
					Snackbar.make(findViewById(R.id.login_root_view), R.string.login_unknown_error, Snackbar.LENGTH_LONG).show();
				}
			}
		});
	}

	private void onSuccessfulLogin() {
		Log.d(TAG, "onSuccessfulLogin: Login successful");
		finish();
		if (getIntent() != null && getIntent().getAction() != null && getIntent().getAction().equals(RC.action.login.RE_AUTHENTICATION)) {
			bindService(new Intent(this, BeaconDwellService.class), this, 0);
			canNotifyService = true;
		} else {
			startActivity(new Intent(getApplicationContext(), DayRegistrationActivity.class));
		}
	}

	@Override
	protected void onDestroy() {
		ignoreDismiss = true;
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
				} else if (result instanceof NoInternetException) {
					Snackbar.make(findViewById(R.id.login_root_view), R.string.login_no_internet, Snackbar.LENGTH_LONG).show();
				} else {
					Snackbar.make(findViewById(R.id.login_root_view), "An error occured.", Snackbar.LENGTH_LONG).show();
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
	public void onServiceConnected(ComponentName name, IBinder service) {
		if (service instanceof BeaconDwellService.Binder) {
			dwellManager = ((BeaconDwellService.Binder) service).getService();
		}

		if (canNotifyService) {
			dwellManager.positiveLoginResult();
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {

	}
}
