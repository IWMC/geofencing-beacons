package com.realdolmen.timeregistration.util.adapters.dayregistration;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.ui.OccupationCard;
import com.realdolmen.timeregistration.util.ObservableOccupationAdapterCallback;
import com.realdolmen.timeregistration.util.SimpleObservableCallback;

import java.util.List;

public class OccupationRecyclerAdapter extends RecyclerView.Adapter<OccupationViewHolder> {

	private ObservableList<Occupation> data;

	public List<Occupation> getData() {
		return data;
	}

	private Occupation selectedItem;

	private RecyclerView owner;

	public void setData(ObservableList<Occupation> newData) {
		this.data = newData;
		data.addOnListChangedCallback(new ObservableOccupationAdapterCallback(this));
	}

	public OccupationRecyclerAdapter(ObservableArrayList<Occupation> data, RecyclerView owner) {
		this.owner = owner;
		setData(data);
	}

	@Override
	public OccupationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		final OccupationCard card = new OccupationCard(parent);
		card.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setSelectedItem(card.getOccupation());
			}
		});
		return new OccupationViewHolder(card);
	}

	@Override
	public void onBindViewHolder(OccupationViewHolder holder, int position) {
		Occupation occ = data.get(position);
		holder.setData(occ);
		if (selectedItem != null)
			holder.onUpdateSelectionState(selectedItem);
	}

	@Override
	public int getItemCount() {
		return data.size();
	}

	public void setSelectedItem(Occupation occ) {
		selectedItem = occ;
		for (int i = 0; i < getItemCount(); i++) {
			OccupationCard card = (OccupationCard) owner.getChildAt(i);
			if (card != null)
				card.onUpdateSelectionState(selectedItem);
		}
	}

	public Occupation getSelectedItem() {
		return selectedItem;
	}
}
