package com.realdolmen.timeregistration.util.adapters.dayregistration;

import android.util.Log;

import com.realdolmen.timeregistration.ui.dayregistration.DayRegistrationFragment;

/**
 * Used to determine the content state of the {@link RegisteredOccupationRecyclerAdapter}.
 */
public interface AdapterState {

	String TAG = "AdapterState";

	void doNotify(DayRegistrationFragment owner, RegisteredOccupationRecyclerAdapter adapter);

	class NewlyEmptyState implements AdapterState {

		@Override
		public void doNotify(DayRegistrationFragment owner, RegisteredOccupationRecyclerAdapter adapter) {
			owner.showEmptyLabel();
			owner.setState(new KnownEmptyState());
			Log.i(TAG, "(NewlyEmptyState) Showing empty label and setting state to KnownEmptyState");
		}
	}

	class KnownEmptyState implements AdapterState {

		@Override
		public void doNotify(DayRegistrationFragment owner, RegisteredOccupationRecyclerAdapter adapter) {
			if (adapter.getData().isEmpty()) {
				owner.setState(new NewlyEmptyState());
				Log.i(TAG, "(KnownEmptyState) Adapter data is empty, setting state to NewlyEmptyState");
				owner.getState().doNotify(owner, adapter);
			} else {
				owner.showRecycler();
				owner.setState(new FilledState());
				Log.i(TAG, "(KnownEmptyState) Adapter data is present, setting state to FilledState");
			}
		}
	}

	class FilledState implements AdapterState {

		@Override
		public void doNotify(DayRegistrationFragment owner, RegisteredOccupationRecyclerAdapter adapter) {
			if (adapter.getData().isEmpty()) {
				owner.setState(new NewlyEmptyState());
				Log.i(TAG, "(FilledState) Adapter data is empty, setting state to NewlyEmptyState");
				owner.getState().doNotify(owner, adapter);
			} else {
				owner.showRecycler();
				owner.setState(new FilledState());
				Log.i(TAG, "(FilledState) Adapter data is present, setting state to FilledState");
				//TODO: (maybe) Revise to have an additional newly filled state
			}
		}
	}
}

