package com.realdolmen.timeregistration.ui.dayregistration;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.data.OccupationRecyclerAdapter;
import com.realdolmen.timeregistration.service.BackendService;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class DayRegistrationFragment extends Fragment {

    @Bind(R.id.day_registration_card_recycler)
    RecyclerView recyclerView;

    private DayRegistrationActivity parent;

    public DayRegistrationFragment() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof DayRegistrationActivity) {
            parent = (DayRegistrationActivity) context;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setAdapter(new OccupationRecyclerAdapter(parent.getDataForCurrentDate()));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_day_registration, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

}
