package com.realdolmen.timeregistration.util.adapters.dayregistration;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.util.ObservableAdapterCallback;

import java.util.List;

public class OccupationRecyclerAdapter extends RecyclerView.Adapter<CardViewHolder> {

	private ObservableList<RegisteredOccupation> data;

	public List<RegisteredOccupation> getData() {
		return data;
	}

	public void setData(ObservableList<RegisteredOccupation> newData) {
		this.data = newData;
		data.addOnListChangedCallback(new ObservableAdapterCallback(this));
	}

	public OccupationRecyclerAdapter(ObservableArrayList<RegisteredOccupation> data) {
		setData(data);
	}

	@Override
	public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.occupation_card, parent, false);
		return new CardViewHolder(v);
	}

	@Override
	public void onBindViewHolder(CardViewHolder holder, int position) {
		holder.setData(data.get(position));
	}

	@Override
	public int getItemCount() {
		return data.size();
	}

	public void removeItemAt(int adapterPosition) {
		data.remove(adapterPosition);
	}

}
