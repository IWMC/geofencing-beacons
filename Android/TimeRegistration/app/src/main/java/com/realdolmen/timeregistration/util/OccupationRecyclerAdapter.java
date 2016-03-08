package com.realdolmen.timeregistration.util;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.ui.dayregistration.DayRegistrationFragment;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by BCCAZ45 on 3/03/2016.
 */
public class OccupationRecyclerAdapter extends RecyclerView.Adapter<OccupationRecyclerAdapter.CardViewHolder> {

    private ObservableList<RegisteredOccupation> data;

    public List<RegisteredOccupation> getData() {
        return data;
    }

    public void setData(ObservableList<RegisteredOccupation> newData) {
        this.data = newData;
        data.addOnListChangedCallback(new ObservableAdapterCallback(this));
    }

    public OccupationRecyclerAdapter(ObservableArrayList<RegisteredOccupation> data) {
        setData(data);
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.occupation_card, parent, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        holder.setData(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void removeItemAt(int adapterPosition) {
        data.remove(adapterPosition);
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {

        private View view;
        private RegisteredOccupation data;

        @Bind(R.id.occupation_card_title)
        TextView title;

        @Bind(R.id.occupation_card_description)
        TextView description;

        public void update() {
            title.setText(data.getOccupation().getName());
            if (data.getOccupation().getDescription().isEmpty()) {
                description.setVisibility(View.GONE);
            } else {
                description.setVisibility(View.VISIBLE);
            }

            if (data.getRegisteredStart() != null)
                description.setText(dateFormat.format(data.getRegisteredStart()) + " - " + (data.getRegisteredEnd() == null ? "Ongoing..." : dateFormat.format(data.getRegisteredEnd())));
            else
                description.setText("No duration set!");
        }

        private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

        public CardViewHolder(View itemView) {
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

        @Override
        public String toString() {
            return data.getOccupation().getName();
        }
    }

    public interface AdapterState {
        void doNotify(DayRegistrationFragment owner, OccupationRecyclerAdapter adapter);
    }

    public static class NewlyEmptyState implements AdapterState {

        @Override
        public void doNotify(DayRegistrationFragment owner, OccupationRecyclerAdapter adapter) {
            owner.showEmptyLabel();
            owner.setState(new KnownEmptyState());
        }
    }

    public static class KnownEmptyState implements AdapterState {

        @Override
        public void doNotify(DayRegistrationFragment owner, OccupationRecyclerAdapter adapter) {
            if (adapter.getData().isEmpty()) {
                owner.setState(new NewlyEmptyState());
                owner.getState().doNotify(owner, adapter);
            } else {
                owner.showRecycler();
                owner.setState(new FilledState());
            }
        }
    }

    public static class FilledState implements AdapterState {

        @Override
        public void doNotify(DayRegistrationFragment owner, OccupationRecyclerAdapter adapter) {
            if (adapter.getData().isEmpty()) {
                owner.setState(new NewlyEmptyState());
                owner.getState().doNotify(owner, adapter);
            } else {
                owner.showRecycler();
                owner.setState(new FilledState());
            }
        }
    }
}
