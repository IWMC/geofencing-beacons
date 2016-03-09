package com.realdolmen.timeregistration.util;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import com.realdolmen.timeregistration.model.RegisteredOccupation;

/**
 * Created by BCCAZ45 on 7/03/2016.
 */
public class ObservableAdapterCallback extends ObservableList.OnListChangedCallback<ObservableArrayList<RegisteredOccupation>> {

    private OccupationRecyclerAdapter adapter;

    public ObservableAdapterCallback(OccupationRecyclerAdapter adapter) {
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
