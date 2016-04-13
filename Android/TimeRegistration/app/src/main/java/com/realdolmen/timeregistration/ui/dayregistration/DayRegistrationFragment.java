package com.realdolmen.timeregistration.ui.dayregistration;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.android.volley.VolleyError;
import com.realdolmen.timeregistration.RC;
import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.service.ResultCallback;
import com.realdolmen.timeregistration.service.repository.RegisteredOccupationRepository;
import com.realdolmen.timeregistration.service.repository.Repositories;
import com.realdolmen.timeregistration.util.DateUtil;
import com.realdolmen.timeregistration.util.RegisteredOccupationCardClickListener;
import com.realdolmen.timeregistration.util.adapters.dayregistration.AdapterState;
import com.realdolmen.timeregistration.util.adapters.dayregistration.RegisteredOccupationRecyclerAdapter;
import com.realdolmen.timeregistration.util.adapters.dayregistration.RegisteredOccupationViewHolder;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
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

	private DateTime selectedDate;

	private RegisteredOccupationRecyclerAdapter adapter;

	private boolean deletable = false;

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
	public void onViewCreated(final View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (getArguments() != null && !getArguments().isEmpty()) {
			selectedDate = (DateTime) getArguments().getSerializable(RC.arguments.registrationFragment.DATE_PARAM);
		} else {
			throw new IllegalStateException("DayRegistrationFragment requires a date argument.");
		}
		state = new AdapterState.NewlyEmptyState();
		DateUtil.enforceUTC(selectedDate);
		parent.setCurrentDate(selectedDate);
		adapter = new RegisteredOccupationRecyclerAdapter(selectedDate, new RegisteredOccupationCardClickListener() {
			@Override
			public void onClick(RegisteredOccupation ro) {
				onEditClick(ro);
			}
		});
		recyclerView.setAdapter(adapter);
		state.doNotify(this, adapter);
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
						final RegisteredOccupationViewHolder vh = (RegisteredOccupationViewHolder) viewHolder;
						Repositories.loadRegisteredOccupationRepository(getContext())
								.then(new DoneCallback<RegisteredOccupationRepository>() {
									@Override
									public void onDone(RegisteredOccupationRepository repo) {
										repo.remove(getContext(), vh.getData()).done(new DoneCallback<Integer>() {
											@Override
											public void onDone(Integer result) {
												if (result == 204) {
													recyclerView.getAdapter().notifyItemRemoved(viewHolder.getAdapterPosition());
													checkState();
												}
											}
										}).fail(new FailCallback<VolleyError>() {
											@Override
											public void onFail(VolleyError result) {
												recyclerView.getAdapter().notifyDataSetChanged();
												//TODO: handle error
											}
										});
									}
								});
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

			@Override
			public boolean isItemViewSwipeEnabled() {
				return deletable;
			}
		};
		ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
		itemTouchHelper.attachToRecyclerView(recyclerView);
	}

	public void setDeletable(boolean flag) {
		deletable = flag;
	}

	public void refreshData() {
		parent.getDataForDate(selectedDate, new ResultCallback<List<RegisteredOccupation>>() {
			@Override
			public void onResult(@NonNull Result result, @Nullable List<RegisteredOccupation> data, @Nullable VolleyError error) {
				if (result == Result.SUCCESS) {
					RegisteredOccupationRecyclerAdapter rora = new RegisteredOccupationRecyclerAdapter(selectedDate, new RegisteredOccupationCardClickListener() {
						@Override
						public void onClick(RegisteredOccupation ro) {
							onEditClick(ro);
						}
					});

					recyclerView.setAdapter(rora);
					rora.refreshViews();
				} else {
					Log.e(TAG, "Unable to get data for date: ", error);
				}
				checkState();
			}
		});
	}

	private void onEditClick(RegisteredOccupation ro) {
		parent.openEditOccupation(ro);
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
