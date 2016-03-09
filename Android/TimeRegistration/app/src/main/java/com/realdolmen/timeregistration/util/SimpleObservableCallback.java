package com.realdolmen.timeregistration.util;

import android.databinding.ObservableList;

/**
 * Simple implementation of {@link android.databinding.ObservableList.OnListChangedCallback} so you
 * don't have to always implement all methods.
 *
 * @param <E> The type of the sender
 */
public class SimpleObservableCallback<E extends ObservableList<?>> extends ObservableList.OnListChangedCallback<E> {
	@Override
	public void onChanged(E sender) {
		return;
	}

	@Override
	public void onItemRangeChanged(E sender, int positionStart, int itemCount) {
		return;
	}

	@Override
	public void onItemRangeInserted(E sender, int positionStart, int itemCount) {
		return;
	}

	@Override
	public void onItemRangeMoved(E sender, int fromPosition, int toPosition, int itemCount) {
		return;
	}

	@Override
	public void onItemRangeRemoved(E sender, int positionStart, int itemCount) {
		return;
	}
}
