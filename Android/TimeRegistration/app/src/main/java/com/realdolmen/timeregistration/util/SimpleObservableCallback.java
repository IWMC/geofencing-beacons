package com.realdolmen.timeregistration.util;

import android.databinding.ObservableList;

/**
 * Created by BCCAZ45 on 7/03/2016.
 */
public class SimpleObservableCallback<E extends ObservableList<?>> extends ObservableList.OnListChangedCallback<E> {
    @Override
    public void onChanged(E sender) {
        System.out.println("");
    }

    @Override
    public void onItemRangeChanged(E sender, int positionStart, int itemCount) {
        System.out.println("");
    }

    @Override
    public void onItemRangeInserted(E sender, int positionStart, int itemCount) {
        System.out.println("");
    }

    @Override
    public void onItemRangeMoved(E sender, int fromPosition, int toPosition, int itemCount) {
        System.out.println("");
    }

    @Override
    public void onItemRangeRemoved(E sender, int positionStart, int itemCount) {
        System.out.println("");
    }
}
