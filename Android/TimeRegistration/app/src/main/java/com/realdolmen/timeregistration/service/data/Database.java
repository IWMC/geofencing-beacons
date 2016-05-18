package com.realdolmen.timeregistration.service.data;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.model.Project;
import com.realdolmen.timeregistration.model.User;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Database {

	private static final String TAG = Database.class.getSimpleName();
	public static final int DATABASE_VERSION = 11;
	public static final String DATABASE_NAME = "timeregistration.db";

	private DatabaseHelper helper;
	private ExecutorService dbWorker = Executors.newSingleThreadExecutor();
	private ExecutorService resolver = Executors.newCachedThreadPool();

	private UserDao _userDao;
	private OccupationDao _occupationDao;
	private ProjectDao _projectDao;

	private Context context;

	private static Database instance;

	private Database() {
		//helper = new DatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static Database with(Context context) {
		if (instance == null) {
			instance = new Database();
			instance.helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
		}
		instance.context = context;
		return instance;
	}

	private <A, B, C> void resolve(final Deferred<A, B, C> deferred, final A data) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				deferred.resolve(data);
				return null;
			}
		}.executeOnExecutor(resolver);
	}

	private <A, B, C> void reject(final Deferred<A, B, C> deferred, final B data) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				deferred.reject(data);
				return null;
			}
		}.executeOnExecutor(resolver);
	}

	private UserDao userDao() {
		if (_userDao == null)
			try {
				_userDao = helper.getDao(User.class);
				Log.d(TAG, "Successfully created user dao");
			} catch (Exception e) {
				Log.e(TAG, "Failed to create user dao", e);
			}

		return _userDao;
	}

	private OccupationDao occupationDao() {
		if (_occupationDao == null)
			try {
				_occupationDao = helper.getDao(Occupation.class);
				Log.d(TAG, "Successfully created occupation dao");
			} catch (Exception e) {
				Log.e(TAG, "Failed to create occupation dao", e);
			}

		return _occupationDao;
	}

	private ProjectDao projectDao() {
		if (_projectDao == null)
			try {
				_projectDao = helper.getDao(Project.class);
				Log.d(TAG, "Successfully created user dao");
			} catch (Exception e) {
				Log.e(TAG, "Failed to create user dao", e);
			}

		return _projectDao;
	}

	public synchronized Promise<User, Throwable, Void> addUser(final User user) {
		final Deferred<User, Throwable, Void> def = new DeferredObject<>();
		dbWorker.submit(new Runnable() {
			@Override
			public void run() {
				try {
					List<User> users = userDao().queryForEq("username", user.getUsername());
					if(users.isEmpty()) {
						User dbUser = userDao().createIfNotExists(user);
						Log.d(TAG, "User created");
						resolve(def, dbUser);
					} else {
						user.setId(users.get(0).getId());
						userDao().update(user);
						Log.d(TAG, "User updated");
						resolve(def, user);
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "Failed to create user", e);
					reject(def, e);
				}
			}
		});
		return def.promise();
	}


	public synchronized Promise<List<User>, Throwable, Object> getUsers() {
		final Deferred<List<User>, Throwable, Object> def = new DeferredObject<>();
		dbWorker.submit(new Runnable() {
			@Override
			public void run() {
				try {
					List<User> users = userDao().queryForAll();
					resolve(def, users);
				} catch (Exception e) {
					Log.e(TAG, "getUsers() query failed", e);
					reject(def, e);
				}
			}
		});
		return def.promise();
	}

	public synchronized Promise<User, Throwable, Void> getUserByName(@NonNull final String name) {
		final Deferred<User, Throwable, Void> def = new DeferredObject<>();
		dbWorker.submit(new Runnable() {
			@Override
			public void run() {
				try {
					List<User> users = userDao().queryForEq("username", name);
					if(!users.isEmpty()) {
						resolve(def, users.get(0));
					} else {
						resolve(def, null);
					}
				} catch (Exception e) {
					Log.e(TAG, "getUserByName() failed", e);
					reject(def, e);
				}
			}
		});
		return def.promise();
	}

	public synchronized Promise<User, Throwable, Void> getUserById(final int id) {
		final Deferred<User, Throwable, Void> def = new DeferredObject<>();
		dbWorker.submit(new Runnable() {
			@Override
			public void run() {
				try {
					User user = userDao().queryForId(id);
					if(user != null) {
						Log.d(TAG, "run: getUserById found user");
						resolve(def, user);
					} else {
						Log.d(TAG, "run: getUserById did not find user");
						resolve(def, null);
					}
				} catch (Exception e) {
					Log.e(TAG, "getUserById() failed", e);
					reject(def, e);
				}
			}
		});
		return def.promise();
	}

	public synchronized Promise<Integer, Throwable, Void> deleteUser(final User user) {
		final Deferred<Integer, Throwable, Void> def = new DeferredObject<>();
		dbWorker.submit(new Runnable() {
			@Override
			public void run() {
				try {
					int result = userDao().delete(user);
					resolve(def, result);
				} catch (Exception e) {
					Log.e(TAG, "deleteUser() failed", e);
					reject(def, e);
				}
			}
		});
		return def.promise();
	}

	public synchronized Promise<List<Occupation>, Throwable, Void> getOccupations() {
		final Deferred<List<Occupation>, Throwable, Void> def = new DeferredObject<>();
		dbWorker.submit(new Runnable() {
			@Override
			public void run() {
				try {
					List<Occupation> occs = occupationDao().queryForAll();
					resolve(def, occs);
				} catch (Exception e) {
					reject(def, e);
				}
			}
		});
		return def.promise();
	}

	public Promise<Void, Throwable, Void> populateOccupations(final List<Occupation> occs) {
		final Deferred<Void, Throwable, Void> def = new DeferredObject<>();
		dbWorker.submit(new Runnable() {
			@Override
			public void run() {
				for(Occupation o : occs) {
					try {
						if (o instanceof Project) {
							projectDao().createIfNotExists((Project) o);
						} else {
							occupationDao().createIfNotExists(o);
						}
					} catch (Exception e) {
						reject(def, e);
						break;
					}
				}

				resolve(def, null);
			}
		});
		return def.promise();
	}

	public void updateUser(User loggedInUser) {

	}
}
