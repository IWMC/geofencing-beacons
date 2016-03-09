package com.realdolmen.timeregistration.util.adapters.dayregistration;

import com.realdolmen.timeregistration.ui.dayregistration.DayRegistrationFragment;

/**
 * Used to determine the content state of the {@link OccupationRecyclerAdapter}.
 */
public interface AdapterState {
	void doNotify(DayRegistrationFragment owner, OccupationRecyclerAdapter adapter);

	class NewlyEmptyState implements AdapterState {

		@Override
		public void doNotify(DayRegistrationFragment owner, OccupationRecyclerAdapter adapter) {
			owner.showEmptyLabel();
			owner.setState(new KnownEmptyState());
		}
	}

	class KnownEmptyState implements AdapterState {

		@Override
		public void doNotify(DayRegistrationFragment owner, OccupationRecyclerAdapter adapter) {
			if (adapter.getData().isEmpty()) {
				owner.setState(new NewlyEmptyState());
				owner.getState().doNotify(owner, adapter);
			} else {
				owner.showRecycler();
				owner.setState(new FilledState());
			}
		}
	}

	class FilledState implements AdapterState {

		@Override
		public void doNotify(DayRegistrationFragment owner, OccupationRecyclerAdapter adapter) {
			if (adapter.getData().isEmpty()) {
				owner.setState(new NewlyEmptyState());
				owner.getState().doNotify(owner, adapter);
			} else {
				owner.showRecycler();
				owner.setState(new FilledState());
				//TODO: Revise to have an additional newly filled state
			}
		}
	}
}

