package com.realdolmen.timeregistration.ui.dayregistration;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.service.BackendService;

import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DayRegistrationActivity extends AppCompatActivity {


    @Bind(R.id.day_registration_toolbar)
    Toolbar bar;

    @Bind(R.id.day_registration_tabbar)
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_registration);
        ButterKnife.bind(this);
        setSupportActionBar(bar);
        getSupportActionBar().setTitle("Registration");
        generateDayTabs();
        getSupportFragmentManager().beginTransaction().add(R.id.day_registration_fragment_container, new DayRegistrationFragment())
                .setCustomAnimations(android.R.anim.fade_in, 0, 0, android.R.anim.fade_out).commit();
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_home_24dp);

    }

    private void generateDayTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Today"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(R.drawable.ic_home_24dp);
        }
        if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
    }

    public List<Occupation> getDataForCurrentDate() {
        return BackendService.with(this).getOccupationsByDate(new Date());
    }
}
