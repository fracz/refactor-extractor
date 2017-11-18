package com.activeandroid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.activeandroid.serializer.TypeSerializer;

final class Registry {
	private Context mContext;
	private DatabaseHelper mDatabaseHelper;

	private boolean mIsInitialized = false;

	private Set<Model> mEntities;

	private HashMap<Class<?>, TypeSerializer> mParsers;
	private HashMap<Class<?>, String> mTableNames;
	private HashMap<Class<?>, ArrayList<Field>> mClassFields;
	private HashMap<Field, String> mColumnNames;

	// Hide constructor. Must use getInstance()
	private Registry() {
	}

	private static class InstanceHolder {
		public static final Registry instance = new Registry();
	}

	// Public methods

	public static Registry getInstance() {
		return InstanceHolder.instance;
	}

	public synchronized void initialize(Context context) {
		if (mIsInitialized) {
			return;
		}

		mContext = context.getApplicationContext();

		copyAttachedDatabase();

		if (Params.IS_TRIAL) {
			final boolean isEmulator = isEmulator();
			final int icon = android.R.drawable.stat_notify_error;
			String tickerText;
			String contentTitle;
			String contentText;
			String appName;

			try {
				PackageManager pm = mContext.getPackageManager();
				appName = pm.getApplicationInfo(mContext.getPackageName(), 0).loadLabel(pm).toString();
			}
			catch (NameNotFoundException e) {
				appName = "This application";
			}

			if (isEmulator) {
				tickerText = appName + " uses ActiveAndroid Trial";
				contentTitle = appName + " uses ActiveAndroid Trial";
				contentText = "Purchase ActiveAndroid before use on devices.";
			}
			else {
				tickerText = appName + " has been shut down";
				contentTitle = appName + " uses ActiveAndroid Trial";
				contentText = "ActiveAndroid Trial only works on the emulator.";
			}

			Intent contentIntent = new Intent(Intent.ACTION_VIEW);
			contentIntent.setData(Uri.parse("https://www.activeandroid.com/"));
			PendingIntent contentPendingIntent = PendingIntent.getActivity(mContext, 0, contentIntent, 0);

			Notification notification = new Notification(icon, tickerText, 0);
			notification.setLatestEventInfo(mContext, contentTitle, contentText, contentPendingIntent);

			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(-6532, notification);

			if (!isEmulator) {
				System.exit(0);
			}
		}

		mDatabaseHelper = new DatabaseHelper(mContext);
		mParsers = ReflectionUtils.getParsers();

		mEntities = new HashSet<Model>();
		mTableNames = new HashMap<Class<?>, String>();
		mClassFields = new HashMap<Class<?>, ArrayList<Field>>();
		mColumnNames = new HashMap<Field, String>();

		openDatabase();

		mIsInitialized = true;

		Log.v("ActiveAndroid initialized succesfully");
	}

	public synchronized void clearCache() {
		mEntities = new HashSet<Model>();
		mTableNames = new HashMap<Class<?>, String>();
		mClassFields = new HashMap<Class<?>, ArrayList<Field>>();
		mColumnNames = new HashMap<Field, String>();

		Log.v("Cache cleared");
	}

	public synchronized void dispose() {
		mDatabaseHelper = null;
		mParsers = null;

		mEntities = null;
		mTableNames = null;
		mClassFields = null;
		mColumnNames = null;

		closeDatabase();

		mIsInitialized = false;

		Log.v("ActiveAndroid disposed. Call initialize to use library.");
	}

	// Open/close database

	public synchronized SQLiteDatabase openDatabase() {
		return mDatabaseHelper.getWritableDatabase();
	}

	public synchronized void closeDatabase() {
		mDatabaseHelper.close();
	}

	// Non-public methods

	public Context getContext() {
		return mContext;
	}

	// Cache methods

	public synchronized void addClassFields(Class<?> type, ArrayList<Field> fields) {
		mClassFields.put(type, fields);
	}

	public synchronized void addColumnName(Field field, String columnName) {
		mColumnNames.put(field, columnName);
	}

	public synchronized void addEntities(Set<Model> entities) {
		mEntities.addAll(entities);
	}

	public synchronized void addEntity(Model entity) {
		mEntities.add(entity);
	}

	public synchronized void addTableName(Class<?> type, String tableName) {
		mTableNames.put(type, tableName);
	}

	public synchronized ArrayList<Field> getClassFields(Class<?> type) {
		return mClassFields.get(type);
	}

	public synchronized String getColumnName(Field field) {
		return mColumnNames.get(field);
	}

	public synchronized Model getEntity(Class<? extends Model> entityType, long id) {
		for (Model entity : mEntities) {
			if (entity != null) {
				if (entity.getClass() != null && entity.getClass() == entityType) {
					if (entity.getId() != null && entity.getId() == id) {
						return entity;
					}
				}
			}
		}

		return null;
	}

	public synchronized TypeSerializer getParserForType(Class<?> fieldType) {
		return mParsers.get(fieldType);
	}

	public synchronized String getTableName(Class<?> type) {
		return mTableNames.get(type);
	}

	public synchronized void removeEntity(Model entity) {
		mEntities.remove(entity);
	}

	// Private methods

	private void copyAttachedDatabase() {
		String dbName = ReflectionUtils.getDbName();
		final File dbPath = mContext.getDatabasePath(dbName);

		// If the database already exists, return
		if (dbPath.exists()) {
			return;
		}

		// Make sure we have a path to the file
		dbPath.getParentFile().mkdirs();

		InputStream inputStream = null;
		try {
			inputStream = mContext.getAssets().open(dbName + ".gz");
			dbName += ".gz";
		}
		catch (Exception e) {
		}

		try {
			if (inputStream == null) {
				inputStream = mContext.getAssets().open(dbName);
			}

			if (isGZIPFile(dbName)) {
				inputStream = new GZIPInputStream(inputStream);
			}

			final OutputStream output = new FileOutputStream(dbPath);

			byte[] buffer = new byte[1024];
			int length;

			while ((length = inputStream.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}

			output.flush();
			output.close();
			inputStream.close();
		}
		catch (IOException e) {
			Log.e("Failed to open file", e);
		}
	}

	private boolean isGZIPFile(final String dbName) {
		try {
			InputStream inputStream = mContext.getAssets().open(dbName);

			byte[] buffer = new byte[2];
			inputStream.read(buffer);
			inputStream.close();

			return (buffer[0] == (byte) 0x1f) && (buffer[1] == (byte) 0x8b);
		}
		catch (IOException e) {
			Log.e("Failed to open file", e);
		}

		return false;
	}

	private boolean isEmulator() {
		return android.os.Build.MODEL.equals("sdk") || android.os.Build.MODEL.equals("google_sdk");
	}
}