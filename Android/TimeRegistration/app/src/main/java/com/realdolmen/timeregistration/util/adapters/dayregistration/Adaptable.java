package com.realdolmen.timeregistration.util.adapters.dayregistration;

public interface Adaptable<E> {
	void bind(E data);
	void unbind();
	void updateViewState();
}
