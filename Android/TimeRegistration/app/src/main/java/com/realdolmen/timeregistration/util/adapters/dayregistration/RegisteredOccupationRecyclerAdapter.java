package com.realdolmen.timeregistration.util.adapters.dayregistration;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.service.repository.Repositories;

import org.joda.time.DateTime;

import java.util.List;

public class RegisteredOccupationRecyclerAdapter extends RecyclerView.Adapter<RegisteredOccupationViewHolder> {

	private DateTime date;

	public RegisteredOccupationRecyclerAdapter(DateTime date) {
		this.date = date;
	}

	@Override
	public RegisteredOccupationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.occupation_card, parent, false);
		return new RegisteredOccupationViewHolder(v);
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
