package com.realdolmen.timeregistration.ui.dayregistration;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
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
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                if(swipeDir != ItemTouchHelper.RIGHT) {
                    return;
                }
                new AlertDialog.Builder(getContext()).setTitle("Delete?").setMessage("Do you want to delete " + viewHolder + "?").setIcon(R.drawable.ic_delete_24dp).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        recyclerView.getAdapter().notifyDataSetChanged();
                    }
                }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        recyclerView.getAdapter().notifyItemRemoved(viewHolder.getAdapterPosition());
                        //TODO: remove item from datase
                        dialog.dismiss();
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                        recyclerView.getAdapter().notifyDataSetChanged();
                    }
                }).show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_day_registration, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

}
