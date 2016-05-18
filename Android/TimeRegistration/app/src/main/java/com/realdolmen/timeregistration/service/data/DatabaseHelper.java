package com.realdolmen.timeregistration.service.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.realdolmen.timeregistration.model.BeaconAction;
import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.model.Project;
import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.model.User;

import java.sql.SQLException;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String TAG = DatabaseHelper.class.getSimpleName();

	public DatabaseHelper(Context context) {
		super(context, Database.DATABASE_NAME, null, Database.DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
		try {
			Log.d(TAG, "Creating tables");
			TableUtils.createTable(connectionSource, User.class);
			TableUtils.createTable(connectionSource, Occupation.class);
			TableUtils.createTable(connectionSource, Project.class);
			TableUtils.createTable(connectionSource, RegisteredOccupation.class);
			TableUtils.createTable(connectionSource, BeaconAction.class);
			Log.i(TAG, "Database created");
		} catch (Exception e) {
			Log.e(TAG, "Failed to create tables", e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			TableUtils.dropTable(connectionSource, User.class, true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			TableUtils.dropTable(connectionSource, Occupation.class, true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			TableUtils.dropTable(connectionSource, Project.class, true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			TableUtils.dropTable(connectionSource, RegisteredOccupation.class, true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			TableUtils.dropTable(connectionSource, BeaconAction.class, true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		onCreate(database, connectionSource);


	}
}
