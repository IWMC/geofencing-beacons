package com.realdolmen.timeregistration.util.adapters.dayregistration;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.service.repository.Repositories;
import com.realdolmen.timeregistration.ui.OccupationCard;
import com.realdolmen.timeregistration.ui.RegisteredOccupationCard;
import com.realdolmen.timeregistration.util.RegisteredOccupationCardClickListener;

import org.joda.time.DateTime;

import java.util.List;

public class RegisteredOccupationRecyclerAdapter extends RecyclerView.Adapter<RegisteredOccupationViewHolder> {

	private DateTime date;

	private RegisteredOccupationCardClickListener onEditClickListener;

	public RegisteredOccupationRecyclerAdapter(DateTime date) {
		this.date = date;
	}

	public RegisteredOccupationRecyclerAdapter(DateTime selectedDate, RegisteredOccupationCardClickListener listener) {
		this.date = selectedDate;
		onEditClickListener = listener;
	}

	public void setOnEditClickListener(RegisteredOccupationCardClickListener listener) {
		onEditClickListener = listener;
	}

	@Override
	public RegisteredOccupationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		RegisteredOccupationCard regOccView = new RegisteredOccupationCard(parent);
		regOccView.setEditable(true);
		regOccView.setOnEditClickListener(onEditClickListener);
		return new RegisteredOccupationViewHolder(regOccView);
	}

	@Override
	public void onBindViewHolder(RegisteredOccupationViewHolder holder, int position) {
		holder.setData(Repositories.registeredOccupationRepository().getAll(date).get(position));
	}

	@Override
	public int getItemCount() {
		return Repositories.registeredOccupationRepository().getAll(date).size();
	}

	public List<RegisteredOccupation> getData() {
		return Repositories.registeredOccupationRepository().getAll(date);
	}
}
