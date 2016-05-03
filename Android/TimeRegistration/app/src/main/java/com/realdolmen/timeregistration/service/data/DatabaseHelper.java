package com.realdolmen.timeregistration.service.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.realdolmen.timeregistration.R;
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
			Log.d(TAG, "Creating user table");
			TableUtils.createTable(connectionSource, User.class);
			Log.i(TAG, "Database created");
		} catch (Exception e) {
			Log.e(TAG, "Failed to create tables", e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			TableUtils.dropTable(connectionSource, User.class, true);
			onCreate(database, connectionSource);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}
