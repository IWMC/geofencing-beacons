package com.realdolmen.timeregistration.util.adapters.dayregistration;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.ui.cards.OccupationCard;
import com.realdolmen.timeregistration.ui.cards.RegularOccupationCard;

public class OccupationViewHolder extends RecyclerView.ViewHolder {

	private OccupationCard<Occupation> view;


	public OccupationViewHolder(View itemView) {
		super(itemView);
		this.view = (RegularOccupationCard) itemView;
	}

	public OccupationCard<Occupation> getView() {
		return view;
	}

	public void setData(Occupation data) {
		view.bind(data);
	}

	public void onUpdateSelectionState(Occupation occ) {
		view.onUpdateSelectionState(occ);
	}
}
