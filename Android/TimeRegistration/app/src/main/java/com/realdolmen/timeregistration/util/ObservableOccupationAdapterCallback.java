package com.realdolmen.timeregistration.util;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.util.adapters.dayregistration.OccupationRecyclerAdapter;
import com.realdolmen.timeregistration.util.adapters.dayregistration.OccupationRecyclerAdapter;

/**
 * Seperated implementation of {@link ObservableList.OnListChangedCallback}
 * to use in {@link OccupationRecyclerAdapter}.
 */
public class ObservableOccupationAdapterCallback extends ObservableList.OnListChangedCallback<ObservableArrayList<Occupation>> {

	private OccupationRecyclerAdapter adapter;

	public ObservableOccupationAdapterCallback(OccupationRecyclerAdapter adapter) {
		this.adapter = adapter;
	}

	@Override
	public void onChanged(ObservableArrayList<Occupation> sender) {
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onItemRangeChanged(ObservableArrayList<Occupation> sender, int positionStart, int itemCount) {
		adapter.notifyItemRangeChanged(positionStart, itemCount);
	}

	@Override
	public void onItemRangeInserted(ObservableArrayList<Occupation> sender, int positionStart, int itemCount) {
		adapter.notifyItemRangeInserted(positionStart, itemCount);
	}

	@Override
	public void onItemRangeMoved(ObservableArrayList<Occupation> sender, int fromPosition, int toPosition, int itemCount) {
		adapter.notifyItemMoved(fromPosition, toPosition);
	}

	@Override
	public void onItemRangeRemoved(ObservableArrayList<Occupation> sender, int positionStart, int itemCount) {
		adapter.notifyItemRangeRemoved(positionStart, itemCount);
	}
}
