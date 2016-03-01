package com.realdolmen.timeregistration;

import android.support.annotation.UiThread;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;

import java.util.ArrayList;
import java.util.Arrays;

public class UITestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NumberPicker spinner = (NumberPicker) findViewById(R.id.numberPickerExample);
        spinner.setMaxValue(200);
        spinner.setMinValue(0);
        spinner.setWrapSelectorWheel(true);
        setupRefreshSwipe((SwipeRefreshLayout) findViewById(R.id.refreshSwipeContainer));
        ListView refreshList = (ListView) findViewById(R.id.refreshListExample);

        refreshList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Arrays.asList("Refresh list item 1", "Item ")));
    }

    private void setupRefreshSwipe(final SwipeRefreshLayout swipeContainer) {
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override

            public void onRefresh() {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                swipeContainer.setRefreshing(false);
                            }
                        });

                    }
                }).start();

            }

        });

        // Configure the refreshing colors

        swipeContainer.setColorSchemeResources(R.color.refreshSpinnerStartColor, R.color.refreshSpinnerMiddleColor, R.color.refreshSpinnerEndColor);
    }
}
