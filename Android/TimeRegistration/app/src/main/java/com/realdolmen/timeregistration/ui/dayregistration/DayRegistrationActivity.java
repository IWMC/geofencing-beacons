package com.realdolmen.timeregistration.ui.dayregistration;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.github.clans.fab.FloatingActionButton;
import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.service.BackendService;
import com.realdolmen.timeregistration.service.RequestCallback;
import com.realdolmen.timeregistration.util.DateUtil;
import com.realdolmen.timeregistration.util.adapters.dayregistration.DayRegistrationFragmentPagerAdapter;

import java.util.ArrayList;
import java.util.Date;
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

	private Map<Date, List<RegisteredOccupation>> registeredOccupations = new HashMap<>();

	private List<Date> dates = new ArrayList<>();

	public static final String SELECTED_DAY = "SELECTED_DAY";

	private Date currentDate;

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

	public List<Date> getDates() {
		return dates;
	}

	public Date getCurrentDate() {
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
	public void getDataForDate(final Date date, final RequestCallback<List<RegisteredOccupation>> callback) {
		if (registeredOccupations.containsKey(date)) {
			callback.onSuccess(registeredOccupations.get(date));
			return;
		}
		BackendService.with(this).getOccupationsInDateRange(date, date, new RequestCallback<List<RegisteredOccupation>>() {
			@Override
			public void onSuccess(List<RegisteredOccupation> data) {
				registeredOccupations.put(date, data);
				callback.onSuccess(data);
			}

			@Override
			public void onError(VolleyError error) {
				callback.onError(error);
			}
		});
	}

	public boolean isDateConfirmed(Date date) {
		List<RegisteredOccupation> data = registeredOccupations.get(date);
		if(data == null || data.isEmpty()) {
			return false;
		}
		boolean isConfirmed = true;
		for (RegisteredOccupation occupation : data) {
			if (!occupation.isConfirmed())
				isConfirmed = false;

		}
		return isConfirmed;
	}

	public int getStateIcon(Date date) {
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
		startActivity(i);
	}

	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}

	public void confirm(@NonNull Date date, @Nullable final RequestCallback callback) {
		confirmFab.setIndeterminate(true);
		confirmFab.setEnabled(false);
		BackendService.with(this).confirmOccupations(date, new RequestCallback() {
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
		});
	}

	public void refreshTabIcons() {
		for (int i = 0; i < tabLayout.getTabCount(); i++) {
			TabLayout.Tab tab = tabLayout.getTabAt(i);
			tab.setIcon(getStateIcon(dates.get(i)));
		}
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
