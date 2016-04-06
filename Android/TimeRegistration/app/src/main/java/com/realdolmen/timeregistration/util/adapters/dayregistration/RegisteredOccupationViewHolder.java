package com.realdolmen.timeregistration.util.adapters.dayregistration;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.ui.cards.RegisteredOccupationCard;

public class RegisteredOccupationViewHolder extends RecyclerView.ViewHolder {

	private RegisteredOccupationCard view;

	public RegisteredOccupationViewHolder(View v) {
		super(v);
		this.view = (RegisteredOccupationCard) itemView;
	}

	public void update() {
		if (view != null)
			view.updateViewState();
	}

	public View getView() {
		return view;
	}

	public void setData(RegisteredOccupation data) {
		view.bind(data);
		update();
	}

	public RegisteredOccupation getData() {
		return view.getData();
	}

	@Override
	public String toString() {
		return view.getData().getOccupation().getName();
	}

}
