package com.realdolmen.timeregistration.ui.cards;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.model.Occupation;

import butterknife.ButterKnife;

public class RegularOccupationCard extends OccupationCard<Occupation> {

	private OnClickListener editClickListener;

	public RegularOccupationCard(Context context) {
		super(context);
		init(null);
	}

	public RegularOccupationCard(ViewGroup parent) {
		super(parent);
	}

	void init(ViewGroup parent) {
		FrameLayout view = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.occupation_card, parent, false);
		ButterKnife.bind(this, view);
		LayoutParams params = new LayoutParams(view.getLayoutParams());
		int leftRight = 0;//(int) view.getContext().getResources().getDimension(R.dimen.activity_horizontal_margin);
		int top = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, view.getContext().getResources().getDisplayMetrics());
		params.setMargins(leftRight, top, leftRight, 0);
		view.setLayoutParams(params);

		LayoutParams frameParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		setLayoutParams(frameParams);
		addView(view);
		frame = view;
		updateViewState();
	}

	public void bind(Occupation o) {
		data = o;
		title.setText(o.getName());
		if (o.getDescription() != null && !o.getDescription().isEmpty()) {
			description.setVisibility(VISIBLE);
			description.setText(o.getDescription());
		} else {
			description.setText("");
			description.setVisibility(GONE);
		}
	}

	@Override
	public void unbind() {
		//Empty
	}

	@Override
	public void updateViewState() {
		if (isEditable()) {
			editButton.setVisibility(VISIBLE);
		} else {
			editButton.setVisibility(GONE);
		}
	}

	public void setSelected(boolean flag) {
		frame.setSelected(flag);
	}

	@Override
	public void onUpdateSelectionState(Occupation selectedItem) {
		if (selectedItem.equals(data)) {
			setSelected(true);
		} else {
			setSelected(false);
		}
	}

	public Occupation getData() {
		return data;
	}
}
