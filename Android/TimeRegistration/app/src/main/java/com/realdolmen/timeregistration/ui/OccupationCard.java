package com.realdolmen.timeregistration.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.andexert.library.RippleView;
import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.model.Occupation;

import butterknife.Bind;
import butterknife.ButterKnife;

public class OccupationCard extends FrameLayout {

	@Bind(R.id.occupation_card_title)
	TextView title;

	@Bind(R.id.occupation_card_description)
	TextView description;

	@Bind(R.id.occupation_card_edit)
	RippleView editButton;

	private FrameLayout card;

	private Occupation data;

	public OccupationCard(Context context) {
		super(context);
		init(null);
	}

	public OccupationCard(ViewGroup parent) {
		super(parent.getContext());
		init(parent);
	}

	public OccupationCard(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(null);
	}

	private void init(ViewGroup parent) {
		FrameLayout view = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.occupation_card, parent, false);
		ButterKnife.bind(this, view);
		editButton.setVisibility(GONE);
		LayoutParams params = new LayoutParams(view.getLayoutParams());
		int leftRight = 0;//(int) view.getContext().getResources().getDimension(R.dimen.activity_horizontal_margin);
		int top = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, view.getContext().getResources().getDisplayMetrics());
		params.setMargins(leftRight, top, leftRight, 0);
		view.setLayoutParams(params);

		FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		setLayoutParams(frameParams);
		addView(view);
		card = view;
	}

	public void bind(Occupation o) {
		data = o;
		title.setText(o.getName());
		if(o.getDescription() != null && !o.getDescription().isEmpty()) {
			description.setVisibility(VISIBLE);
			description.setText(o.getDescription());
		} else {
			description.setText("");
			description.setVisibility(GONE);
		}
	}

	public void setSelected(boolean flag) {
		 card.setSelected(flag);
	}

	public void onUpdateSelectionState(Occupation selectedItem) {
		if(selectedItem.equals(data)) {
			setSelected(true);
		} else {
			setSelected(false);
		}
	}

	public Occupation getOccupation() {
		return data;
	}
}
