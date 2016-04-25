package com.realdolmen.timeregistration.ui.dayregistration;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.location.Geofence;
import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.RC;
import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.model.Project;
import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.service.ResultCallback;
import com.realdolmen.timeregistration.service.location.geofence.GeoService;
import com.realdolmen.timeregistration.service.location.geofence.GeofenceRequester;
import com.realdolmen.timeregistration.service.repository.BackendService;
import com.realdolmen.timeregistration.service.repository.LoadCallback;
import com.realdolmen.timeregistration.service.repository.OccupationRepository;
import com.realdolmen.timeregistration.service.repository.RegisteredOccupationRepository;
import com.realdolmen.timeregistration.service.repository.Repositories;
import com.realdolmen.timeregistration.ui.login.LoginActivity;
import com.realdolmen.timeregistration.util.DateUtil;
import com.realdolmen.timeregistration.util.UTC;
import com.realdolmen.timeregistration.util.adapters.dayregistration.DayRegistrationFragmentPagerAdapter;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.DoneFilter;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.realdolmen.timeregistration.RC.action.addOccupation.ACTION_ADD;
import static com.realdolmen.timeregistration.RC.action.addOccupation.ACTION_EDIT;
import static com.realdolmen.timeregistration.RC.actionExtras.addOccupation.BASE_DATE;
import static com.realdolmen.timeregistration.RC.actionExtras.addOccupation.EDITING_OCCUPATION;
import static com.realdolmen.timeregistration.RC.actionExtras.addOccupation.END_DATE;
import static com.realdolmen.timeregistration.RC.actionExtras.addOccupation.SELECTED_OCCUPATION;
import static com.realdolmen.timeregistration.RC.actionExtras.addOccupation.START_DATE;
import static com.realdolmen.timeregistration.RC.resultCodes.addOccupation.ADD_RESULT_CODE;
import static com.realdolmen.timeregistration.RC.resultCodes.addOccupation.EDIT_RESULT_CODE;


public class DayRegistrationActivity extends AppCompatActivity {

	private static final String LOG_TAG = DayRegistrationActivity.class.getSimpleName();

	//region UI fields

	@Bind(R.id.day_registration_toolbar)
	Toolbar bar;

	@Bind(R.id.day_registration_tabbar)
	TabLayout tabLayout;

	@Bind(R.id.day_registration_viewpager)
	CustomViewPager viewPager;

	@Bind(R.id.day_registration_confirm_fab)
	FloatingActionButton confirmFab;

	@Bind(R.id.day_registration_fab_menu)
	FloatingActionMenu fabMenu;
	//endregion

	private boolean doubleBack;
	private DayRegistrationFragmentPagerAdapter pagerAdapter;
	private List<DateTime> dates = new ArrayList<>();
	public static final String SELECTED_DAY = "SELECTED_DAY";
	private GeofenceRequester geofenceRequester;
	private SuggestionDialogs suggestionDialogs = new SuggestionDialogs(this);

	private BroadcastReceiver geofenceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch (intent.getAction()) {
				case RC.geofencing.events.GOOGLE_API_CONNECTION_FAILED:
					Toast.makeText(DayRegistrationActivity.this, "Unable to connect to the Google API", Toast.LENGTH_LONG).show();
					break;
				case RC.geofencing.events.FENCES_ADD_FAIL:
					Toast.makeText(DayRegistrationActivity.this, "Fences were not correctly added!", Toast.LENGTH_LONG).show();
					break;
			}
		}
	};

	//region Initialization methods

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_day_registration);
		ButterKnife.bind(this);
		initSupportActionBar();
		initViewPager();
		refreshTabIcons();
		selectToday();
		initLocationServices();

		if (getIntent() != null) {
			onNewIntent(getIntent());
		}
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		if (intent != null && intent.getAction() != null) {
			suggestionDialogs.handleNewIntent(intent);
		}
	}

	@Override
	protected void onResume() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(RC.geofencing.events.GOOGLE_API_CONNECTION_FAILED);
		intentFilter.addAction(RC.geofencing.events.FENCES_ADD_SUCCESS);
		registerReceiver(geofenceReceiver, intentFilter);
		super.onResume();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(geofenceReceiver);
		super.onPause();
	}

	private void initLocationServices() {
		startService(new Intent(this, GeoService.class));
		geofenceRequester = new GeofenceRequester(this);
		refreshGeofences(false);
	}

	private void initSupportActionBar() {
		setSupportActionBar(bar);
		getSupportActionBar().setTitle(R.string.day_registration_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_home_24dp);
	}

	private void initViewPager() {
		viewPager.setSwipePagingEnabled(false);
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				refreshTabIcons();
				refreshFabs(dates.get(position));
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
		dates = DateUtil.pastWorkWeek();
		pagerAdapter = new DayRegistrationFragmentPagerAdapter(this, getSupportFragmentManager());
		viewPager.setAdapter(pagerAdapter);
		tabLayout.setupWithViewPager(viewPager);
	}

	public List<DateTime> getDates() {
		return dates;
	}

	public DateTime getCurrentDate() {
		return dates.get(viewPager.getCurrentItem());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.standard, menu);
		return true;
	}

	@Override
	public void onBackPressed() {
		if (doubleBack) {
			finishAffinity();
			return;
		} else {
			doubleBack = true;
			Toast.makeText(this, "Press back again to exit!", Toast.LENGTH_SHORT).show();
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					doubleBack = false;
				}
			}, 2000);
		}
		if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
		} else {
			getSupportActionBar().setDisplayHomeAsUpEnabled(false);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
			getSupportActionBar().setIcon(R.drawable.ic_home_24dp);
		}
		if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			getSupportFragmentManager().popBackStack();
		}
	}
	//endregion

	//region Data methods
	public void getDataForDate(@UTC final DateTime date, final ResultCallback<List<RegisteredOccupation>> callback) {
		DateUtil.enforceUTC(date);

		Repositories.loadRegisteredOccupationRepository(this, new LoadCallback() {
			@Override
			public void onResult(Result result, Throwable error) {
				if (result == Result.SUCCESS) { //data successfully loaded
					List<RegisteredOccupation> filteredData = Repositories.registeredOccupationRepository().getAll(date);
					if (callback != null)
						callback.onResult(ResultCallback.Result.SUCCESS, filteredData, null);
				}
			}
		});
	}

	public void refreshCurrent() {
		refreshView(viewPager.getCurrentItem());
	}

	public Promise<Boolean, Object, Object> isDateConfirmed(@UTC final DateTime date) {
		final Deferred<Boolean, Object, Object> lateConfirm = new DeferredObject<>();
		Repositories.loadRegisteredOccupationRepository(this, new LoadCallback() {
			@Override
			public void onResult(Result result, Throwable error) {
				if (result == Result.FAIL) {
					lateConfirm.resolve(false);
					return;
				}

				lateConfirm.resolve(Repositories.registeredOccupationRepository().isConfirmed(date));
			}
		});

		return lateConfirm.promise();
	}

	public Promise<Integer, Object, Object> getStateIcon(@UTC DateTime date) {
		DateUtil.enforceUTC(date, "Dates have to be local to provide proper state icons!");
		Promise<Boolean, Object, Object> promise = isDateConfirmed(date);
		return promise.then(new DoneFilter<Boolean, Integer>() {
			@Override
			public Integer filterDone(Boolean result) {
				if (result) {
					return R.drawable.ic_assignment_turned_in_24dp;
				}
				return R.drawable.ic_assignment_late_24dp;
			}
		});
	}

	private void selectToday() {
		viewPager.setCurrentItem(tabLayout.getTabCount() - 1);
	}


	@OnClick(R.id.day_registration_confirm_fab)
	public void doConfirm() {
		confirm(getCurrentDate(), null);
	}

	@OnClick(R.id.day_registration_add_fab)
	public void openAddOccupation() {
		Intent i = new Intent(this, AddOccupationActivity.class);
		i.setAction(ACTION_ADD);
		i.putExtra(BASE_DATE, dates.get(viewPager.getCurrentItem()));
		startActivityForResult(i, ADD_RESULT_CODE);
	}

	public void openEditOccupation(final RegisteredOccupation ro) {
		final Intent i = new Intent(this, AddOccupationActivity.class);
		i.setAction(ACTION_EDIT);
		final DateTime element = dates.get(viewPager.getCurrentItem());
		Repositories.loadRegisteredOccupationRepository(this).done(new DoneCallback<RegisteredOccupationRepository>() {
			@Override
			public void onDone(RegisteredOccupationRepository result) {
				i.putExtra(BASE_DATE, element);
				i.putExtra(EDITING_OCCUPATION, ro);
				startActivityForResult(i, EDIT_RESULT_CODE);
			}
		});
	}

	public void refreshGeofences(final boolean refresh) {
		Repositories.loadOccupationRepository(this).done(new DoneCallback<OccupationRepository>() {
			@Override
			public void onDone(OccupationRepository result) {
				List<Geofence> geofences = new ArrayList<>();
				for (Project project : Repositories.occupationRepository().getAllProjects()) {
					geofences.addAll(project.getGeofences());
				}
				geofenceRequester.addGeofences(geofences);
				if (refresh) {
					geofenceRequester.disconnect();
					geofenceRequester.connect();
				}
			}
		});
	}

	public void refreshView(final int position) {
		final DateTime date = dates.get(position);
		refreshTabIcons();
		refreshFabs(date);
		//region loadRegisteredOccupationRepo

		Repositories.loadRegisteredOccupationRepository(this).done(new DoneCallback<RegisteredOccupationRepository>() {
			@Override
			public void onDone(RegisteredOccupationRepository result) {
				result.reload(DayRegistrationActivity.this).done(new DoneCallback<RegisteredOccupationRepository>() {
					@Override
					public void onDone(RegisteredOccupationRepository result) {
						Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.day_registration_viewpager + ":" + position);
						if (page != null) {
							if (page instanceof DayRegistrationFragment) {
								final DayRegistrationFragment fragment = (DayRegistrationFragment) page;
								isDateConfirmed(date).always(new AlwaysCallback<Boolean, Object>() {
									@Override
									public void onAlways(Promise.State state, Boolean resolved, Object rejected) {
										fragment.setDeletable(!resolved);
										fragment.refreshData();
									}
								});
								Log.d(LOG_TAG, "Refreshed views on index " + position + " -> " + page.getClass().getSimpleName());
							} else {
								Log.w(LOG_TAG, "Refreshing views did not refresh the fragment data -> " + page.getClass().getSimpleName());
							}
						}
					}
				});
			}
		});
		//endregion

		Repositories.occupationRepository().reload(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ADD_RESULT_CODE) {
			if (resultCode == RESULT_OK) {
				Occupation occ = (Occupation) data.getSerializableExtra(SELECTED_OCCUPATION);
				DateTime start = (DateTime) data.getSerializableExtra(START_DATE);
				DateTime end = (DateTime) data.getSerializableExtra(END_DATE);
				DateUtil.enforceUTC(start, "Received start date that is not in UTC!");
				DateUtil.enforceUTC(end, "Received end date that is not in UTC!");
				handleNewlyRegisteredOccupation(occ, start, end);
			}
		} else if (requestCode == EDIT_RESULT_CODE) {
			if (resultCode == RESULT_OK) {
				handleUpdatedRegisteredOccupation((RegisteredOccupation) data.getSerializableExtra(EDITING_OCCUPATION));
			}
		}
	}

	public void setCurrentDate(DateTime currentDate) {
		//this.currentDate = currentDate;
	}

	public void confirm(@NonNull final DateTime date, @Nullable final ResultCallback callback) {
		if (Repositories.registeredOccupationRepository().hasOngoingOccupations(date)) {
			Snackbar.make(findViewById(R.id.day_registration_root_view), R.string.day_registration_confirm_ongoing_occupations, Snackbar.LENGTH_LONG).show();
			return;
		}
		confirmFab.setIndeterminate(true);
		confirmFab.setEnabled(false);
		Repositories.loadRegisteredOccupationRepository(this).done(new DoneCallback<RegisteredOccupationRepository>() {
			@Override
			public void onDone(RegisteredOccupationRepository result) {
				result.confirmDate(DayRegistrationActivity.this, date).done(new DoneCallback<Integer>() {
					@Override
					public void onDone(Integer result) {
						if (callback != null)
							callback.onResult(ResultCallback.Result.SUCCESS, result, null);
						System.out.println("Confirm status code: " + result);
						refreshCurrent();
						confirmFab.setIndeterminate(false);
						confirmFab.setEnabled(true);
						Snackbar.make(findViewById(R.id.day_registration_root_view), R.string.day_registration_confirm_message, Snackbar.LENGTH_LONG).show();
					}
				}).fail(new FailCallback<VolleyError>() {
					@Override
					public void onFail(VolleyError result) {
						if (callback != null)
							callback.onResult(ResultCallback.Result.FAIL, null, result);

						System.out.println("Confirm status code: " + result.toString());
						confirmFab.setIndeterminate(false);
						confirmFab.setEnabled(true);
						Snackbar.make(findViewById(R.id.day_registration_root_view), "There was a problem confirming your registration!", Snackbar.LENGTH_LONG).show();
					}
				});
			}
		});
	}

	public void refreshTabIcons() {
		for (int i = 0; i < tabLayout.getTabCount(); i++) {
			final TabLayout.Tab tab = tabLayout.getTabAt(i);
			getStateIcon(dates.get(i)).done(new DoneCallback<Integer>() {
				@Override
				public void onDone(Integer result) {
					tab.setIcon(result);
				}
			});
		}
	}

	void handleUpdatedRegisteredOccupation(final RegisteredOccupation ro) {
		if (ro == null) {
			return;
		}
		DateUtil.enforceUTC(ro.getRegisteredStart());
		DateUtil.enforceUTC(ro.getRegisteredEnd());
		Repositories.loadRegisteredOccupationRepository(this).done(new DoneCallback<RegisteredOccupationRepository>() {
			@Override
			public void onDone(RegisteredOccupationRepository result) {
				result.save(DayRegistrationActivity.this, ro, new ResultCallback<Long>() {
					@Override
					public void onResult(@NonNull Result result, @Nullable Long data, @Nullable VolleyError error) {
						System.out.println("" + result + data + error);
						refreshCurrent();
					}
				});
			}
		});
	}

	void handleNewlyRegisteredOccupation(@NonNull Occupation occ, @NonNull @UTC DateTime start, @Nullable @UTC DateTime end) {
		DateUtil.enforceUTC(start, "Start date must be in UTC format!");
		if (end != null)
			DateUtil.enforceUTC(end, "End date must be in UTC format!");
		final RegisteredOccupation ro = new RegisteredOccupation();
		ro.setRegisteredStart(start);
		if (end != null)
			ro.setRegisteredEnd(end);
		ro.setOccupation(occ);

		Repositories.loadRegisteredOccupationRepository(this, new LoadCallback() {
			@Override
			public void onResult(Result result, Throwable error) {
				if (result == Result.SUCCESS) {
					Repositories.registeredOccupationRepository().save(DayRegistrationActivity.this, ro, new ResultCallback<Long>() {
						@Override
						public void onResult(@NonNull Result result, @Nullable Long data, @Nullable VolleyError error) {
							if (result == Result.SUCCESS) {
								Snackbar.make(findViewById(R.id.day_registration_root_view), R.string.registration_notify_saved, Snackbar.LENGTH_LONG).show();
								refreshCurrent();
							} else if (error != null) {
								Snackbar.make(findViewById(R.id.day_registration_root_view), R.string.registration_notify_not_saved, Snackbar.LENGTH_LONG).show();
							}
						}
					});
				}
			}
		});
	}

	//endregion

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_refresh) {
			refreshCurrent();
			refreshGeofences(true);
			return true;
		} else if (item.getItemId() == R.id.menu_logout) {
			finish();
			geofenceRequester.disconnect();
			Repositories.logout();
			startActivity(new Intent(this, LoginActivity.class));
			return true;
		}
		return false;
	}

	//region Save Instance State
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(SELECTED_DAY, tabLayout.getSelectedTabPosition());
	}

	public void refreshFabs(DateTime currentDate) {
		isDateConfirmed(currentDate).done(new DoneCallback<Boolean>() {
			@Override
			public void onDone(Boolean result) {
				if (result) {
					fabMenu.setVisibility(View.GONE);
				} else {
					fabMenu.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		viewPager.setCurrentItem(savedInstanceState.getInt(SELECTED_DAY));
	}
	//endregion
}
