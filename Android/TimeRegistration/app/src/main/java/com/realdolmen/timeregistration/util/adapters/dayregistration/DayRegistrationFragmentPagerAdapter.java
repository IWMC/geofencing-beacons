package com.realdolmen.timeregistration.util.adapters.dayregistration;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.realdolmen.timeregistration.ui.dayregistration.DayRegistrationActivity;
import com.realdolmen.timeregistration.ui.dayregistration.DayRegistrationFragment;
import com.realdolmen.timeregistration.util.DateUtil;

/**
 * Fragment adapter for the {@link android.support.v4.view.ViewPager} of {@link DayRegistrationActivity}.
 */
public class DayRegistrationFragmentPagerAdapter extends FragmentPagerAdapter {

	private DayRegistrationActivity activity;

	public DayRegistrationFragmentPagerAdapter(DayRegistrationActivity activity, FragmentManager fm) {
		super(fm);
		this.activity = activity;
	}

	@Override
	public Fragment getItem(int position) {
		DayRegistrationFragment fragment = new DayRegistrationFragment();
		Bundle args = new Bundle();
		args.putSerializable(DayRegistrationFragment.DATE_PARAM, activity.getDates().get(position));
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return DateUtil.nameForDate(activity, activity.getDates().get(position));
	}

	@Override
	public int getCount() {
		return activity.getDates().size();
	}

}
