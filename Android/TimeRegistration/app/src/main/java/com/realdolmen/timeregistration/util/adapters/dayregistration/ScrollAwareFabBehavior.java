package com.realdolmen.timeregistration.util.adapters.dayregistration;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FabBehavior;
import android.util.AttributeSet;
import android.view.View;

import com.github.clans.fab.FloatingActionMenu;
import com.realdolmen.timeregistration.R;

/**
 * Behavior used to hide the FAB when scrolling down in a
 * {@link com.realdolmen.timeregistration.ui.dayregistration.DayRegistrationFragment}.
 */
@SuppressWarnings("unused")
public class ScrollAwareFabBehavior extends FabBehavior {

	private int toolbarHeight;

	public ScrollAwareFabBehavior(Context context, AttributeSet set) {
		super(context, set);
		this.toolbarHeight = getToolbarHeight(context);
	}

	int getToolbarHeight(Context context) {
		final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
				new int[]{R.attr.actionBarSize});
		int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
		styledAttributes.recycle();

		return toolbarHeight;
	}


	@Override
	public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionMenu child, View dependency) {
		return dependency instanceof AppBarLayout;
	}

	@Override
	public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionMenu fab, View dependency) {
		if (dependency instanceof AppBarLayout) {
			CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
			int fabBottomMargin = lp.bottomMargin;
			int distanceToScroll = fab.getHeight() + fabBottomMargin;
			float ratio = (float) dependency.getY() / (float) toolbarHeight;
			fab.setTranslationY(-distanceToScroll * ratio);
		}
		return true;
	}

}
