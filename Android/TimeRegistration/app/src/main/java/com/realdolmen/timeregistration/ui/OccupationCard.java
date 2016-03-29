package com.realdolmen.timeregistration.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.andexert.library.RippleView;
import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.util.adapters.dayregistration.Adaptable;

import butterknife.Bind;

public abstract class OccupationCard<E> extends FrameLayout implements Adaptable<E> {

	@Bind(R.id.occupation_card_title)
	TextView title;

	@Bind(R.id.occupation_card_description)
	TextView description;

	@Bind(R.id.occupation_card_edit)
	RippleView editButton;

	FrameLayout frame;

	E data;

	private boolean editable;

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
		updateViewState();
	}

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

	abstract void init(ViewGroup parent);

	@Override
	public abstract void bind(E o);

	@Override
	public void unbind() {
		//Empty
	}

	@Override
	public abstract void updateViewState();

	public void onUpdateSelectionState(E selectedItem) {
	}

	public E getData() {
		return data;
	}
}
