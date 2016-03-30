package com.realdolmen.timeregistration.ui.dayregistration;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;

import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.service.repository.LoadCallback;
import com.realdolmen.timeregistration.service.repository.Repositories;
import com.realdolmen.timeregistration.util.DateUtil;
import com.realdolmen.timeregistration.util.UTC;
import com.realdolmen.timeregistration.util.adapters.dayregistration.OccupationRecyclerAdapter;

import org.joda.time.DateTime;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.realdolmen.timeregistration.util.DateUtil.enforceUTC;
import static com.realdolmen.timeregistration.util.DateUtil.toLocal;
import static com.realdolmen.timeregistration.util.DateUtil.toUTC;

public class AddOccupationActivity extends AppCompatActivity {

	private static final String LOG_TAG = AddOccupationActivity.class.getSimpleName();

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

	public static final String START_DATE = "SD", END_DATE = "ED", BASE_DATE = "BD", SELECTED_OCCUPATION = "SO", EDITING_OCCUPATION = "EO";

	private static final String EDIT_MODE = "EDIT_MODE";
	public static final int ADD_RESULT_CODE = 1, EDIT_RESULT_CODE = 2;

	public static final String ACTION_ADD = "com.realdolmen.occupation.add", ACTION_EDIT = "com.realdolmen.occupation.edit";

	@UTC
	private DateTime startDate, endDate, baseDate;

	@Bind(R.id.add_occupation_date_title_container)
	TableRow titleContainer;

	private RegisteredOccupation registeredOccupationToBeEdited;

	private boolean editMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_occupation);
		ButterKnife.bind(this);

		DateTime bd = (DateTime) getIntent().getSerializableExtra(BASE_DATE);
		if (bd == null)
			throw new IllegalArgumentException("A base date must be passed as serializable extra");
		enforceUTC(bd);
		baseDate = bd;

		if (getIntent().getAction().equals(ACTION_EDIT)) {
			if (!getIntent().hasExtra(EDITING_OCCUPATION)) {
				throw new IllegalArgumentException("When in edit mode, a RegisteredOccupation (EDITING_OCCUPATION) is required as extra!");
			}
			editMode = true;
			registeredOccupationToBeEdited = (RegisteredOccupation) getIntent().getSerializableExtra(EDITING_OCCUPATION);
		}
		initToolbar();
		initRecycler();
		initFields();
	}

	private void initFields() {
		title.setText(DateUtil.formatToDay(baseDate));
		if (editMode) {
			titleContainer.setClickable(true);
			titleContainer.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					System.out.println("Works");
				}
			});
		}
		updateDateButtons();
	}

	@OnClick(R.id.add_occupation_startTime)
	void openStartDateSelectionDialog() {
		TimePickerDialog dialog = null;
		if (!editMode) {
			dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					startDate = toLocal(baseDate).withHourOfDay(hourOfDay).withMinuteOfHour(minute);
					updateDateButtons();
				}
			}, DateTime.now().getHourOfDay(), DateTime.now().getMinuteOfHour(), DateFormat.is24HourFormat(this));
		} else {
			dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					startDate = toLocal(baseDate).withHourOfDay(hourOfDay).withMinuteOfHour(minute);
					updateDateButtons();
				}
			}, toLocal(startDate).getHourOfDay(), startDate.getMinuteOfHour(), DateFormat.is24HourFormat(this));
		}
		dialog.show();
	}

	@OnClick(R.id.add_occupation_endTime)
	void openEndDateSelectionDialog() {

		TimePickerDialog dialog;
		if (!editMode) {
			dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					endDate = toLocal(baseDate).withHourOfDay(hourOfDay).withMinuteOfHour(minute);
					updateDateButtons();
				}
			}, DateTime.now().getHourOfDay(), DateTime.now().getMinuteOfHour(), DateFormat.is24HourFormat(this));
		} else {
			dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					endDate = toLocal(baseDate).withHourOfDay(hourOfDay).withMinuteOfHour(minute);
					updateDateButtons();
				}
			}, toLocal(endDate).getHourOfDay(), endDate.getMinuteOfHour(), DateFormat.is24HourFormat(this));
		}

		dialog.show();
	}

	void updateDateButtons() {
		if (!editMode) {
			if (startDate == null) {
				enforceUTC(baseDate);
				startDate = toLocal(baseDate);
			}

			if (endDate == null) {
				enforceUTC(baseDate);
				endDate = toLocal(baseDate);
			}
		} else {
			if (startDate == null)
				startDate = toLocal(registeredOccupationToBeEdited.getRegisteredStart());

			if (endDate == null)
				endDate = toLocal(registeredOccupationToBeEdited.getRegisteredEnd());
		}
		System.out.println("Update buttons! START: " + startDate + " END: " + endDate);
		startButton.setText(DateUtil.formatToHours(startDate, DateFormat.is24HourFormat(getApplicationContext())));
		endButton.setText(DateUtil.formatToHours(endDate, DateFormat.is24HourFormat(getApplicationContext())));
	}

	private void initToolbar() {
		setSupportActionBar(bar);
		if (editMode)
			getSupportActionBar().setTitle(R.string.edit_occupation_title);
		else
			getSupportActionBar().setTitle(R.string.add_occupation_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private void initRecycler() {

		if (initializing) {
			return;
		}
		initializing = true;
		recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

		Repositories.loadOccupationRepository(this, new LoadCallback() {
			@Override
			public void onResult(Result result, Throwable error) {
				if (result == Result.SUCCESS) {
					adapter = new OccupationRecyclerAdapter(recycler);
					recycler.setAdapter(adapter);
					if (editMode)
						adapter.setSelectedItem(registeredOccupationToBeEdited.getOccupation());
					initializing = false;
				} else {
					Snackbar.make(findViewById(android.R.id.content), R.string.add_occupation_fetch_error, Snackbar.LENGTH_LONG).setAction("Retry", new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							initializing = false;
							initRecycler();
						}
					}).show();
					Log.e(LOG_TAG, "Could not fetch occupations!", error);
					initializing = false;
				}
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
				if (!editMode) {
					i.putExtra(SELECTED_OCCUPATION, adapter.getSelectedItem());
					i.putExtra(START_DATE, toUTC(startDate));
					i.putExtra(END_DATE, toUTC(endDate));
				} else {
					registeredOccupationToBeEdited.setOccupation(adapter.getSelectedItem());
					registeredOccupationToBeEdited.setRegisteredStart(toUTC(startDate));
					registeredOccupationToBeEdited.setRegisteredEnd(toUTC(endDate));
					i.putExtra(EDITING_OCCUPATION, registeredOccupationToBeEdited);
				}
				setResult(RESULT_OK, i);
				finish();
			}
		}
		return false;
	}

	private boolean validate() {
		if (endDate.isBefore(startDate.toInstant())) {
			return alert(getString(R.string.add_occupation_invalid_dates), false);
		}

		if (adapter.getSelectedItem() == null) {
			return alert(getString(R.string.add_occupation_no_selection), false);
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
		startDate = DateUtil.toUTC(startDate);
		endDate = DateUtil.toUTC(endDate);
		baseDate = DateUtil.toUTC(baseDate);
		outState.putSerializable(START_DATE, startDate);
		outState.putSerializable(END_DATE, endDate);
		outState.putSerializable(BASE_DATE, baseDate);
		outState.putBoolean(EDIT_MODE, editMode);
		if (adapter != null && adapter.getSelectedItem() != null)
			outState.putSerializable(SELECTED_OCCUPATION, adapter.getSelectedItem());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		startDate = toLocal((DateTime) savedInstanceState.getSerializable(START_DATE));
		endDate = toLocal((DateTime) savedInstanceState.getSerializable(END_DATE));
		baseDate = toLocal((DateTime) savedInstanceState.getSerializable(BASE_DATE));
		editMode = savedInstanceState.getBoolean(EDIT_MODE);
		if (savedInstanceState.getSerializable(SELECTED_OCCUPATION) != null && adapter != null)
			adapter.setSelectedItem((Occupation) savedInstanceState.getSerializable(SELECTED_OCCUPATION));
		updateDateButtons();
	}
}
