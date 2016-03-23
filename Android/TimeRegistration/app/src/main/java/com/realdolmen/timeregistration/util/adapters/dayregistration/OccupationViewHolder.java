package com.realdolmen.timeregistration.util.adapters.dayregistration;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.ui.OccupationCard;

public class OccupationViewHolder extends RecyclerView.ViewHolder {

	private OccupationCard view;


	public OccupationViewHolder(View itemView) {
		super(itemView);
		this.view = (OccupationCard) itemView;
	}

	public OccupationCard getView() {
		return view;
	}

	public void setData(Occupation data) {
		view.bind(data);
	}

	public void onUpdateSelectionState(Occupation occ) {
		view.onUpdateSelectionState(occ);
	}
}
