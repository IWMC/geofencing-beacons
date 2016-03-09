package com.realdolmen.timeregistration.ui.dayregistration;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.service.BackendService;
import com.realdolmen.timeregistration.util.DateUtil;
import com.realdolmen.timeregistration.util.adapters.dayregistration.DayRegistrationFragmentPagerAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DayRegistrationActivity extends AppCompatActivity {


	@Bind(R.id.day_registration_toolbar)
	Toolbar bar;

	@Bind(R.id.day_registration_tabbar)
	TabLayout tabLayout;

	@Bind(R.id.day_registration_viewpager)
	CustomViewPager viewPager;

	private boolean doubleBack;

	private DayRegistrationFragmentPagerAdapter pagerAdapter;

	private Map<Date, List<RegisteredOccupation>> registeredOccupations = new HashMap<>();

	private List<Date> dates = new ArrayList<>();

	public static final String SELECTED_DAY = "SELECTED_DAY";

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
		pagerAdapter = new DayRegistrationFragmentPagerAdapter(this, getSupportFragmentManager(), dates);
		viewPager.setAdapter(pagerAdapter);
		tabLayout.setupWithViewPager(viewPager);
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
	public void getDataForDate(final Date date, final BackendService.RequestCallback<List<RegisteredOccupation>> callback) {
		if (registeredOccupations.containsKey(date)) {
			callback.onSuccess(registeredOccupations.get(date));
			return;
		}
		BackendService.with(this).getOccupationsInDateRange(date, date, new BackendService.RequestCallback<List<RegisteredOccupation>>() {
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
		if (DateUtil.isToday(date)) {
			return false;
		}
		return true;
	}

	public int getStateIcon(Date date) {
		int icon = 0;
		if (isDateConfirmed(date)) {
			icon = R.drawable.ic_assignment_turned_in_24dp;
		} else {
			icon = R.drawable.ic_assignment_late_24dp;
		}

		return icon;
	}

	private void selectToday() {
		viewPager.setCurrentItem(tabLayout.getTabCount() - 1);
	}
	//endregion

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
