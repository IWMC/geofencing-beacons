package com.realdolmen.timeregistration.ui.dayregistration;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.databinding.ObservableArrayList;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.VolleyError;
import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.service.BackendService;
import com.realdolmen.timeregistration.service.RequestCallback;
import com.realdolmen.timeregistration.util.DateUtil;
import com.realdolmen.timeregistration.util.adapters.dayregistration.OccupationRecyclerAdapter;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddOccupationActivity extends AppCompatActivity {


	@Bind(R.id.add_occupation_toolbar)
	Toolbar bar;

	@Bind(R.id.add_occupation_recycler)
	RecyclerView recycler;

	@Bind(R.id.add_occupation_date_title)
	TextView title;

	@Bind(R.id.add_occupation_startTime)
	Button startButton;

	@Bind(R.id.add_occupation_endTime)
	Button endButton;

	private boolean initializing;

	private OccupationRecyclerAdapter adapter;

	public static final String START_DATE = "SD", END_DATE = "ED", BASE_DATE = "BD", SELECTED_OCCUPATION = "SO";
	public static final int RESULT_CODE = 1;

	private Date startDate, endDate, baseDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_occupation);
		ButterKnife.bind(this);
		Date bd = (Date) getIntent().getSerializableExtra(BASE_DATE);
		if (bd == null) {
			throw new IllegalStateException("A base date must be passed as serializable extra");
		}

		baseDate = bd;
		initToolbar();
		initRecycler();
		initFields();
	}

	private void initFields() {
		title.setText(DateUtil.formatToDay(baseDate));
		updateDateButtons();
	}

	@OnClick(R.id.add_occupation_startTime)
	void openStartDateSelectionDialog() {
		Calendar calendar = Calendar.getInstance();
		TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
				cal.set(Calendar.MINUTE, minute);
				startDate = cal.getTime();
				updateDateButtons();
			}
		}, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(this));

		dialog.show();
	}

	@OnClick(R.id.add_occupation_endTime)
	void openEndDateSelectionDialog() {
		Calendar calendar = Calendar.getInstance();
		TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
				cal.set(Calendar.MINUTE, minute);
				endDate = cal.getTime();
				updateDateButtons();
			}
		}, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(this));

		dialog.show();
	}

	void updateDateButtons() {
		if (startDate == null) {
			startDate = new Date();
		}

		if (endDate == null) {
			endDate = new Date();
		}
		startButton.setText(DateUtil.formatToHours(startDate, DateFormat.is24HourFormat(getApplicationContext())));
		endButton.setText(DateUtil.formatToHours(endDate, DateFormat.is24HourFormat(getApplicationContext())));
	}

	private void initToolbar() {
		setSupportActionBar(bar);
		getSupportActionBar().setTitle("Add Occupation");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private void initRecycler() {

		if (initializing) {
			return;
		}
		initializing = true;
		recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
		final ObservableArrayList<Occupation> obs = new ObservableArrayList<>();
		adapter = new OccupationRecyclerAdapter(obs, recycler);
		recycler.setAdapter(adapter);
		BackendService.with(this).getRelevantOccupations(new RequestCallback<List<Occupation>>() {
			@Override
			public void onSuccess(List<Occupation> data) {
				obs.addAll(data);
				initializing = false;
			}

			@Override
			public void onError(VolleyError error) {
				Snackbar.make(findViewById(android.R.id.content), "Could not fetch occupations!", Snackbar.LENGTH_LONG).setAction("Retry", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						initRecycler();
					}
				}).show();
				initializing = false;
			}
		});
	}

	@Override
	public void onBackPressed() {
		//TODO: Maybe a dialog to confirm that the user wants to discard his changes
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_occupation_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.add_occupation_done) {
			if (validate()) {
				Intent i = new Intent();
				i.putExtra(SELECTED_OCCUPATION, adapter.getSelectedItem());
				i.putExtra(START_DATE, startDate);
				i.putExtra(END_DATE, endDate);
				setResult(RESULT_OK, i);
				finish();
			}
		}
		return false;
	}

	private boolean validate() {
		if (endDate.before(startDate)) {
			return alert("End Time cannot be before Start Date", false);
		}

		if (adapter.getSelectedItem() == null) {
			return alert("You must select an occupation!", false);
		}

		return true;
	}

	private boolean alert(String message, boolean result) {
		AlertDialog dialog = new AlertDialog.Builder(this).setMessage(message).setTitle("Incorrect fields").create();
		dialog.show();
		return result;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(START_DATE, startDate);
		outState.putSerializable(END_DATE, endDate);
		outState.putSerializable(BASE_DATE, baseDate);
		if (adapter.getSelectedItem() != null)
			outState.putSerializable(SELECTED_OCCUPATION, adapter.getSelectedItem());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		startDate = (Date) savedInstanceState.getSerializable(START_DATE);
		endDate = (Date) savedInstanceState.getSerializable(END_DATE);
		baseDate = (Date) savedInstanceState.getSerializable(BASE_DATE);
		if (savedInstanceState.getSerializable(SELECTED_OCCUPATION) != null)
			adapter.setSelectedItem((Occupation) savedInstanceState.getSerializable(SELECTED_OCCUPATION));
		updateDateButtons();
	}
}
