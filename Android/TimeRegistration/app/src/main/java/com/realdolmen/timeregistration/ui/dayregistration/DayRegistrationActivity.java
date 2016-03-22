package com.realdolmen.timeregistration.ui.dayregistration;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.github.clans.fab.FloatingActionButton;
import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.service.ResultCallback;
import com.realdolmen.timeregistration.service.repository.LoadCallback;
import com.realdolmen.timeregistration.service.repository.Repositories;
import com.realdolmen.timeregistration.util.DateUtil;
import com.realdolmen.timeregistration.util.UTC;
import com.realdolmen.timeregistration.util.adapters.dayregistration.DayRegistrationFragmentPagerAdapter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DayRegistrationActivity extends AppCompatActivity {


	@Bind(R.id.day_registration_toolbar)
	Toolbar bar;

	@Bind(R.id.day_registration_tabbar)
	TabLayout tabLayout;

	@Bind(R.id.day_registration_viewpager)
	CustomViewPager viewPager;

	@Bind(R.id.day_registration_confirm_fab)
	FloatingActionButton confirmFab;

	private boolean doubleBack;

	private DayRegistrationFragmentPagerAdapter pagerAdapter;

	private List<DateTime> dates = new ArrayList<>();

	public static final String SELECTED_DAY = "SELECTED_DAY";

	private DateTime currentDate;

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
		dates = DateUtil.pastWorkWeek();
		pagerAdapter = new DayRegistrationFragmentPagerAdapter(this, getSupportFragmentManager());
		viewPager.setAdapter(pagerAdapter);
		tabLayout.setupWithViewPager(viewPager);
	}

	public List<DateTime> getDates() {
		return dates;
	}

	public DateTime getCurrentDate() {
		return currentDate;
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
		Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.day_registration_viewpager + ":" + viewPager.getCurrentItem());
		if (page != null) {
			if (page instanceof DayRegistrationFragment) {
				DayRegistrationFragment fragment = (DayRegistrationFragment) page;
				fragment.refreshData();
			}
		}
	}

	public boolean isDateConfirmed(@UTC DateTime date) {
		DateUtil.enforceUTC(date);
		List<RegisteredOccupation> data = Repositories.registeredOccupationRepository().getAll(date);
		if (data == null || data.isEmpty()) {
			return false;
		}
		boolean isConfirmed = true;
		for (RegisteredOccupation occupation : data) {
			if (!occupation.isConfirmed())
				isConfirmed = false;

		}
		return isConfirmed;
	}

	public int getStateIcon(DateTime date) {
		DateUtil.enforceNotUTC(date, "Dates have to be local to provide proper state icons!");
		if (isDateConfirmed(date)) {
			return R.drawable.ic_assignment_turned_in_24dp;
		}
		return R.drawable.ic_assignment_late_24dp;
	}

	private void selectToday() {
		viewPager.setCurrentItem(tabLayout.getTabCount() - 1);
	}
	//endregion

	@OnClick(R.id.day_registration_confirm_fab)
	public void doConfirm() {
		confirm(currentDate, null);
	}

	@OnClick(R.id.day_registration_add_fab)
	public void openAddOccupation() {
		Intent i = new Intent(this, AddOccupationActivity.class);
		i.putExtra(AddOccupationActivity.BASE_DATE, dates.get(viewPager.getCurrentItem()));
		startActivityForResult(i, AddOccupationActivity.RESULT_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == AddOccupationActivity.RESULT_CODE) {
			if (resultCode == RESULT_OK) {
				Occupation occ = (Occupation) data.getSerializableExtra(AddOccupationActivity.SELECTED_OCCUPATION);
				DateTime start = (DateTime) data.getSerializableExtra(AddOccupationActivity.START_DATE);
				DateTime end = (DateTime) data.getSerializableExtra(AddOccupationActivity.END_DATE);
				DateUtil.enforceUTC(start, "Received start date that is not in UTC!");
				DateUtil.enforceUTC(end, "Received end date that is not in UTC!");
				handleNewlyRegisteredOccupation(occ, start, end);
			}
		}
	}

	public void setCurrentDate(DateTime currentDate) {
		this.currentDate = currentDate;
	}

	public void confirm(@NonNull DateTime date, @Nullable final ResultCallback callback) {
		confirmFab.setIndeterminate(true);
		confirmFab.setEnabled(false);

		//TODO: Edit this to the new repository system.
		/*BackendService.with(this).confirmOccupations(date, new ResultCallback() {
			@Override
			public void onSuccess(Object data) {
				if (callback != null)
					callback.onSuccess(data);
				refreshTabIcons();
				confirmFab.setIndeterminate(false);
				confirmFab.setEnabled(false);
				Snackbar.make(findViewById(android.R.id.content), R.string.day_registration_confirm_message, Snackbar.LENGTH_LONG).show();
			}

			@Override
			public void onError(VolleyError error) {
				if (callback != null)
					callback.onError(error);
				confirmFab.setIndeterminate(false);
				confirmFab.setEnabled(true);
			}
		});*/
	}

	public void refreshTabIcons() {
		for (int i = 0; i < tabLayout.getTabCount(); i++) {
			TabLayout.Tab tab = tabLayout.getTabAt(i);
			tab.setIcon(getStateIcon(dates.get(i)));
		}
	}

	private void handleNewlyRegisteredOccupation(Occupation occ, @UTC DateTime start, @UTC DateTime end) {
		DateUtil.enforceUTC(start, "Start date must be in UTC format!");
		DateUtil.enforceUTC(end, "End date must be in UTC format!");
		final RegisteredOccupation ro = new RegisteredOccupation();
		ro.setRegisteredStart(start);
		ro.setRegisteredEnd(end);
		ro.setOccupation(occ);

		Repositories.loadRegisteredOccupationRepository(this, new LoadCallback() {
			@Override
			public void onResult(Result result, Throwable error) {
				if (result == Result.SUCCESS) {
					Repositories.registeredOccupationRepository().save(DayRegistrationActivity.this, ro, new ResultCallback<RegisteredOccupation>() {
						@Override
						public void onResult(@NonNull Result result, @Nullable RegisteredOccupation data, @Nullable VolleyError error) {
							if (result == Result.SUCCESS) {
								Snackbar.make(findViewById(android.R.id.content), "The registration has been saved.", Snackbar.LENGTH_LONG).show();
                                refreshCurrent();
							} else if (error != null) {
								Snackbar.make(findViewById(android.R.id.content), "Your registration was not saved!", Snackbar.LENGTH_LONG).show();
							}
						}
					});
				}
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_refresh) {
			refreshCurrent();
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

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		viewPager.setCurrentItem(savedInstanceState.getInt(SELECTED_DAY));
	}
	//endregion
}
