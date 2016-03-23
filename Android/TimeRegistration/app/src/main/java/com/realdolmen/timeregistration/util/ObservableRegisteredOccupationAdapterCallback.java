package com.realdolmen.timeregistration.util;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.util.adapters.dayregistration.RegisteredOccupationRecyclerAdapter;

/**
 * Seperated implementation of {@link android.databinding.ObservableList.OnListChangedCallback}
 * to use in {@link RegisteredOccupationRecyclerAdapter}.
 */
public class ObservableRegisteredOccupationAdapterCallback extends ObservableList.OnListChangedCallback<ObservableArrayList<RegisteredOccupation>> {

	private RegisteredOccupationRecyclerAdapter adapter;

	public ObservableRegisteredOccupationAdapterCallback(RegisteredOccupationRecyclerAdapter adapter) {
		this.adapter = adapter;
	}

	@Override
	public void onChanged(ObservableArrayList<RegisteredOccupation> sender) {
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onItemRangeChanged(ObservableArrayList<RegisteredOccupation> sender, int positionStart, int itemCount) {
		adapter.notifyItemRangeChanged(positionStart, itemCount);
	}

	@Override
	public void onItemRangeInserted(ObservableArrayList<RegisteredOccupation> sender, int positionStart, int itemCount) {
		adapter.notifyItemRangeInserted(positionStart, itemCount);
	}

	@Override
	public void onItemRangeMoved(ObservableArrayList<RegisteredOccupation> sender, int fromPosition, int toPosition, int itemCount) {
		adapter.notifyItemMoved(fromPosition, toPosition);
	}

	@Override
	public void onItemRangeRemoved(ObservableArrayList<RegisteredOccupation> sender, int positionStart, int itemCount) {
		adapter.notifyItemRangeRemoved(positionStart, itemCount);
	}
}
