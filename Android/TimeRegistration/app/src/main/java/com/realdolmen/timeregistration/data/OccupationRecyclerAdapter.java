package com.realdolmen.timeregistration.data;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.model.Occupation;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by BCCAZ45 on 3/03/2016.
 */
public class OccupationRecyclerAdapter extends RecyclerView.Adapter<OccupationRecyclerAdapter.CardViewHolder> {

    private List<Occupation> data;

    public OccupationRecyclerAdapter(List<Occupation> data) {
        this.data = data;
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

    public static class CardViewHolder extends RecyclerView.ViewHolder {

        private View view;
        private Occupation data;

        @Bind(R.id.occupation_card_title)
        TextView title;

        @Bind(R.id.occupation_card_description)
        TextView description;

        public void update() {
            title.setText(data.getName());
            if(data.getDescription().isEmpty()) {
                description.setVisibility(View.GONE);
            } else {
                description.setVisibility(View.VISIBLE);
            }
            description.setText(data.getDescription());
        }

        public CardViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            ButterKnife.bind(this, view);
        }

        public View getView() {
            return view;
        }

        public void setData(Occupation data) {
            this.data = data;
            update();
        }

        @Override
        public String toString() {
            return data.getName();
        }
    }
}
