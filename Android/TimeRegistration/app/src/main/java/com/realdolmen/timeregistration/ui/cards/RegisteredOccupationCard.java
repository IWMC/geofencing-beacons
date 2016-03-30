package com.realdolmen.timeregistration.ui.cards;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.util.DateUtil;
import com.realdolmen.timeregistration.util.RegisteredOccupationCardClickListener;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisteredOccupationCard extends OccupationCard<RegisteredOccupation> {

	private RegisteredOccupationCardClickListener editClickListener;

	public RegisteredOccupationCard(Context context) {
		super(context);
		init(null);
	}

	public RegisteredOccupationCard(ViewGroup parent) {
		super(parent);
	}

	public void setOnEditClickListener(RegisteredOccupationCardClickListener listener) {
		editClickListener = listener;
	}

	@OnClick(R.id.occupation_card_edit)
	void onEditClicked() {
		if (editClickListener != null)
			editClickListener.onClick(data);
	}

	@Override
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

	public void bind(RegisteredOccupation o) {
		data = o;
		title.setText(data.getOccupation().getName());
		description.setVisibility(VISIBLE);
		if (data.getRegisteredStart() != null)
			description.setText(DateUtil.formatToHours(DateUtil.toLocal(data.getRegisteredStart())) + " - " + (DateUtil.toLocal(data.getRegisteredEnd()) == null ? "Ongoing..." : DateUtil.formatToHours(DateUtil.toLocal(data.getRegisteredEnd()))));
		else
			description.setText("No duration set!");
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
	public void onUpdateSelectionState(RegisteredOccupation selectedItem) {
		if (selectedItem.equals(data)) {
			setSelected(true);
		} else {
			setSelected(false);
		}
	}

	public RegisteredOccupation getData() {
		return data;
	}
}
