package com.realdolmen.timeregistration.util.adapters.dayregistration;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.realdolmen.timeregistration.ui.dayregistration.DayRegistrationActivity;
import com.realdolmen.timeregistration.ui.dayregistration.DayRegistrationFragment;
import com.realdolmen.timeregistration.util.DateUtil;

import java.util.Date;
import java.util.List;

/**
 * Fragment adapter for the {@link android.support.v4.view.ViewPager} of {@link DayRegistrationActivity}.
 */
public class DayRegistrationFragmentPagerAdapter extends FragmentPagerAdapter {

	private List<Date> dates;

	private DayRegistrationActivity activity;

	public DayRegistrationFragmentPagerAdapter(DayRegistrationActivity activity, FragmentManager fm, List<Date> dates) {
		super(fm);
		this.dates = dates;
		this.activity = activity;
	}

	@Override
	public Fragment getItem(int position) {
		DayRegistrationFragment fragment = new DayRegistrationFragment();
		Bundle args = new Bundle();
		args.putSerializable(DayRegistrationFragment.DATE_PARAM, dates.get(position));
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public CharSequence getPageTitle(int position) {
//        Drawable icon = null;
//        if(activity.isDateConfirmed(dates.get(position))) {
//            icon = activity.getResources().getDrawable(R.drawable.ic_assignment_turned_in_24dp);
//        } else {
//            icon = activity.getResources().getDrawable(R.drawable.ic_assignment_late_24dp);
//        }
//        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
//        SpannableString sb = new SpannableString("   " + DateUtil.nameForDate(dates.get(position)));
//        ImageSpan imageSpan = new ImageSpan(icon, ImageSpan.ALIGN_BOTTOM);
//        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        return sb;
		return DateUtil.nameForDate(dates.get(position));
	}

	@Override
	public int getCount() {
		return dates.size();
	}
}
