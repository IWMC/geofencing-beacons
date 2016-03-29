package com.realdolmen.timeregistration.util.adapters.dayregistration;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.service.repository.Repositories;
import com.realdolmen.timeregistration.ui.OccupationCard;
import com.realdolmen.timeregistration.ui.RegularOccupationCard;

public class OccupationRecyclerAdapter extends RecyclerView.Adapter<OccupationViewHolder> {

	private Occupation selectedItem;

	private RecyclerView owner;

	public OccupationRecyclerAdapter(RecyclerView owner) {
		this.owner = owner;
	}

	@Override
	public OccupationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		final OccupationCard<Occupation> card = new RegularOccupationCard(parent);
		card.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setSelectedItem(card.getData());
			}
		});
		return new OccupationViewHolder(card);
	}

	@Override
	public void onBindViewHolder(OccupationViewHolder holder, int position) {
		Occupation occ = Repositories.occupationRepository().get(position);
		holder.setData(occ);
		if (selectedItem != null)
			holder.onUpdateSelectionState(selectedItem);
	}

	@Override
	public int getItemCount() {
		return Repositories.occupationRepository().size();
	}

	public void setSelectedItem(Occupation occ) {
		selectedItem = occ;
		for (int i = 0; i < getItemCount(); i++) {
			OccupationCard<Occupation> card = (OccupationCard<Occupation>) owner.getChildAt(i);
			if (card != null)
				card.onUpdateSelectionState(selectedItem);
		}
	}

	public Occupation getSelectedItem() {
		return selectedItem;
	}
}
