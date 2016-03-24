package com.realdolmen.timeregistration.util.adapters.dayregistration;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.util.DateUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RegisteredOccupationViewHolder extends RecyclerView.ViewHolder {

	private View view;
	private RegisteredOccupation data;

	@Bind(R.id.occupation_card_title)
	TextView title;

	@Bind(R.id.occupation_card_description)
	TextView description;

	public void update() {
		title.setText(data.getOccupation().getName());
		description.setVisibility(View.VISIBLE);
		if (data.getRegisteredStart() != null)
			description.setText(DateUtil.formatToHours(DateUtil.toLocal(data.getRegisteredStart())) + " - " + (DateUtil.toLocal(data.getRegisteredEnd()) == null ? "Ongoing..." : DateUtil.formatToHours(DateUtil.toLocal(data.getRegisteredEnd()))));
		else
			description.setText("No duration set!");
	}

	public RegisteredOccupationViewHolder(View itemView) {
		super(itemView);
		this.view = itemView;
		ButterKnife.bind(this, view);
	}

	public View getView() {
		return view;
	}

	public void setData(RegisteredOccupation data) {
		this.data = data;
		update();
	}

	public RegisteredOccupation getData() {
		return data;
	}

	@Override
	public String toString() {
		return data.getOccupation().getName();
	}
}
