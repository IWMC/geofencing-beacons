package com.realdolmen.timeregistration.ui.dayregistration;


import android.content.Context;
import android.content.DialogInterface;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.service.ResultCallback;
import com.realdolmen.timeregistration.util.SimpleObservableCallback;
import com.realdolmen.timeregistration.util.adapters.dayregistration.AdapterState;
import com.realdolmen.timeregistration.util.adapters.dayregistration.RegisteredOccupationRecyclerAdapter;

import org.joda.time.DateTime;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class DayRegistrationFragment extends Fragment {

	public static final String TAG = "DayRegistration";

	@Bind(R.id.day_registration_card_recycler)
	RecyclerView recyclerView;

	@Bind(R.id.day_registration_emptyState)
	LinearLayout emptyStateLabel;

	private DayRegistrationActivity parent;

	private AdapterState state;

	public static final String DATE_PARAM = "DATE";

	private DateTime selectedDate;

	private ObservableArrayList<RegisteredOccupation> registeredOccupationList;
	private RegisteredOccupationRecyclerAdapter adapter;

	public AdapterState getState() {
		return state;
	}

	public void setState(AdapterState state) {
		this.state = state;
	}

	//Required empty constructor
	public DayRegistrationFragment() {

	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof DayRegistrationActivity) {
			parent = (DayRegistrationActivity) context;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		parent.setCurrentDate(selectedDate);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (getArguments() != null && !getArguments().isEmpty()) {
			selectedDate = (DateTime) getArguments().getSerializable(DATE_PARAM);
		} else {
			throw new IllegalStateException("DayRegistrationFragment requires a date argument.");
		}
		state = new AdapterState.NewlyEmptyState();
		parent.setCurrentDate(selectedDate);
		registeredOccupationList = new ObservableArrayList<>();
		adapter = new RegisteredOccupationRecyclerAdapter(registeredOccupationList);
		recyclerView.setAdapter(adapter);
		state.doNotify(this, adapter);
		registeredOccupationList.addOnListChangedCallback(new SimpleObservableCallback() {
			@Override
			public void onChanged(ObservableList sender) {
				checkState();
			}

			@Override
			public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount) {
				checkState();
			}

			@Override
			public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount) {
				checkState();
			}

			@Override
			public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {
				checkState();
			}

			@Override
			public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount) {
				checkState();
			}
		});
		Log.i(TAG, "(onViewCreated) Adding new adapter and state is " + state.getClass().getSimpleName());
		refreshData();
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
			@Override
			public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
				return false;
			}

			@Override
			public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
				if (swipeDir != ItemTouchHelper.RIGHT) {
					return;
				}
				new AlertDialog.Builder(getContext()).setTitle("Delete?").setMessage("Do you want to delete " + viewHolder + "?").setIcon(R.drawable.ic_delete_24dp).setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						recyclerView.getAdapter().notifyDataSetChanged();
					}
				}).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//TODO: remove item from datase
						((RegisteredOccupationRecyclerAdapter) recyclerView.getAdapter()).removeItemAt(viewHolder.getAdapterPosition());
						dialog.dismiss();
					}
				}).setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						dialog.dismiss();
						recyclerView.getAdapter().notifyDataSetChanged();
					}
				}).show();
			}
		};

		ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
		itemTouchHelper.attachToRecyclerView(recyclerView);
	}

	public void refreshData() {
		if (registeredOccupationList == null) {
			Log.i(TAG, "Refreshing data with new list");
			parent.getDataForDate(selectedDate, new ResultCallback<List<RegisteredOccupation>>() {
				@Override
				public void onSuccess(List<RegisteredOccupation> data) {
					adapter.setData(data);
					Log.i(TAG, "After setting data (of size " + data.size() + ") in adapter, state is " + state.getClass().getSimpleName());
				}

				@Override
				public void onError(VolleyError error) {
					if (error.networkResponse != null)
						Toast.makeText(getContext(), "" + error.networkResponse.statusCode, Toast.LENGTH_LONG).show();
					else {
						Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
						error.printStackTrace();
					}
				}
			});
		} else {
			Log.i(TAG, "Refreshing data with existing list");
			if (recyclerView.getAdapter() == null) {
				recyclerView.setAdapter(adapter);
			}

			if (recyclerView.getLayoutManager() == null) {
				recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
			}

			parent.getDataForDate(selectedDate, new ResultCallback<List<RegisteredOccupation>>() {
				@Override
				public void onSuccess(List<RegisteredOccupation> data) {
					Log.i(TAG, "Before setting data of size " + data.size() + " in adapter, state is " + state.getClass().getSimpleName());
					adapter.setData(data);
					Log.i(TAG, "After setting data (of size " + data.size() + ") in adapter, state is " + state.getClass().getSimpleName());
				}

				@Override
				public void onError(VolleyError error) {
					if (error.networkResponse != null)
						Toast.makeText(getContext(), "" + error.networkResponse.statusCode, Toast.LENGTH_LONG).show();
					else {
						Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
						error.printStackTrace();
					}
				}
			});

		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_day_registration, container, false);
		ButterKnife.bind(this, v);
		return v;
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
	}

	public void showEmptyLabel() {
		emptyStateLabel.setVisibility(View.VISIBLE);
		recyclerView.setVisibility(View.GONE);
	}

	public void showRecycler() {
		emptyStateLabel.setVisibility(View.GONE);
		recyclerView.setVisibility(View.VISIBLE);
	}

	private void checkState() {
		if (state != null) {
			state.doNotify(this, ((RegisteredOccupationRecyclerAdapter) recyclerView.getAdapter()));
		} else {
			Log.e(AdapterState.TAG, "State should not be null");
		}
	}
}
