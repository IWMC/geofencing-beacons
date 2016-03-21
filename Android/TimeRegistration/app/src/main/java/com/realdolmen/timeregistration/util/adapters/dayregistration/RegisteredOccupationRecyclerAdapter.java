package com.realdolmen.timeregistration.util.adapters.dayregistration;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.util.ObservableRegisteredOccupationAdapterCallback;

import java.util.List;

public class RegisteredOccupationRecyclerAdapter extends RecyclerView.Adapter<RegisteredOccupationViewHolder> {

	private ObservableList<RegisteredOccupation> data;

	public List<RegisteredOccupation> getData() {
		return data;
	}

	private ObservableRegisteredOccupationAdapterCallback callback;

	public void setData(List<RegisteredOccupation> newData) {
		if (data != null) {
			data.clear();
			if (callback != null)
				data.removeOnListChangedCallback(callback);
			callback = new ObservableRegisteredOccupationAdapterCallback(this);
			data.addOnListChangedCallback(callback);
			data.addAll(newData);
		} else {
			data = new ObservableArrayList<>();
			callback = new ObservableRegisteredOccupationAdapterCallback(this);
			data.addOnListChangedCallback(callback);
			data.addAll(newData);
		}
		notifyDataSetChanged();
	}

	public RegisteredOccupationRecyclerAdapter(ObservableArrayList<RegisteredOccupation> data) {
		setData(data);
		data.addOnListChangedCallback(new ObservableRegisteredOccupationAdapterCallback(this));
	}

	@Override
	public RegisteredOccupationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.occupation_card, parent, false);
		return new RegisteredOccupationViewHolder(v);
	}

	@Override
	public void onBindViewHolder(RegisteredOccupationViewHolder holder, int position) {
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
