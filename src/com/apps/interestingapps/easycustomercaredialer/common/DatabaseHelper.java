package com.apps.interestingapps.easycustomercaredialer.common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Class to handle database creation and updates
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	private static DatabaseHelper databaseHelper;
	private Context context;
	private SQLiteDatabase database;
	private static final String TAG = "DatabaseHelper";
	private volatile boolean isDatabaseUpdated = false;
	private static int openConnections = 0;

	private DatabaseHelper(Context context) {
		super(context,
				EccdConstants.DATABASE_NAME,
				null,
				EccdConstants.DATABASE_VERSION);
		this.context = context;
	}

	public static DatabaseHelper initializeDatabase(Context contextForDatabase) {
		DatabaseHelper databaseHelper = DatabaseHelper.getInstance(contextForDatabase);
		try {
			databaseHelper.createDataBase();
			databaseHelper.openDatabase();
			Log.i(TAG, "Database opened");
		} catch (IOException e) {
			Log.i(TAG, "Error occurred while opening database.");
			e.printStackTrace();
		}
		return databaseHelper;
	}

	public static DatabaseHelper getInstance(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}
		if (databaseHelper == null) {
			databaseHelper = new DatabaseHelper(context);
		}
		synchronized (databaseHelper) {
			openConnections++;
		}
		return databaseHelper;
	}

	/**
	 * Method called when database is created
	 */
	@Override
	public void onCreate(SQLiteDatabase database) {

	}

	public synchronized boolean isDatabaseUpdated() {
		return isDatabaseUpdated;
	}

	public synchronized void setDatabaseUpdated(boolean isDatabaseUpdated) {
		this.isDatabaseUpdated = isDatabaseUpdated;
	}

	/**
	 * Method to create a database. The database is copied from assests folder if
	 * it doesn't exists already
	 *
	 * @throws IOException
	 */
	public void createDataBase() throws IOException {
		boolean dbExist = checkDataBase();
		SQLiteDatabase tempDatabase = null;
		if (!dbExist) {
			try {
				Log.i(TAG, "Database file does not exists");
				tempDatabase = getReadableDatabase();
				copyDataBase();
				tempDatabase.close();
			} catch (IOException e) {
				if (tempDatabase != null) {
					tempDatabase.close();
				}
				if (database != null) {
					closeDatabase();
				}
				throw new Error("Error copying database", e);
			}
		}
	}

	/**
	 * Checks if database file exists, and it can be opened
	 *
	 * @return true if the database can be opened
	 */
	private boolean checkDataBase() {
		SQLiteDatabase checkDB = null;
		boolean dbExist = true;
		try {
			String myPath = EccdConstants.DB_PATH + EccdConstants.DATABASE_NAME;
			checkDB = getReadableDatabase();
			// checkDB = SQLiteDatabase.openDatabase(myPath, null,
			// SQLiteDatabase.OPEN_READONLY);
		} catch (Exception e) {
			// Some error occurred. Override the existing database to avoid
			// errors
			dbExist = false;
		}
		if (checkDB != null) {
			String query = "SELECT * FROM SQLITE_MASTER";
			dbExist = false;
			try {
				Cursor cursor = checkDB.rawQuery(query, null);
				while (cursor.moveToNext()) {
					String tableName = cursor.getString(cursor.getColumnIndex("name"));
					if (tableName.equalsIgnoreCase(EccdConstants.COMPANY_TABLE)) {
						dbExist = true;
						break;
					}
				}
				cursor.close();
				checkDB.close();
			} catch (Exception e) {
				e.printStackTrace();
				if (checkDB != null) {
					checkDB.close();
				}
			}
		} else {
			dbExist = false;
		}
		Log.i(context.getClass().getName(), "DB exists: " + dbExist);
		return dbExist;
	}

	/**
	 * Copy the database file from assests to database folder of the app
	 *
	 * @throws IOException
	 */
	private void copyDataBase() throws IOException {
		// Open your local db as the input stream
		InputStream myInput = context.getAssets()
				.open(EccdConstants.DATABASE_NAME);
		// Path to the just created empty db
		String outFileName = EccdConstants.DB_PATH + EccdConstants.DATABASE_NAME;
		;
		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);
		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}
		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
		Log.i(context.getClass().getName(), "Successfully copied the file");
	}

	/**
	 * Open a connection to database in read/write mode
	 *
	 * @throws SQLException
	 */
	public void openDatabase() throws SQLException {
		// Open the database
		String myPath = EccdConstants.DB_PATH + EccdConstants.DATABASE_NAME;
		database = SQLiteDatabase.openDatabase(myPath,
				null,
				SQLiteDatabase.OPEN_READWRITE);
		database.execSQL(EccdConstants.ENABLE_FOREIGN_KEY_QUERY);
		upgradeToLatestDatabaseVersion();
	}

	/**
	 * Close the database if its open
	 */
	public void closeDatabase() {
		if (database != null) {
			synchronized (databaseHelper) {
				if (openConnections > 0) {
					openConnections--;
					if (openConnections == 0) {
						database.close();
						databaseHelper.close();
						database = null;
					}
				}
			}
		}
	}

	public void upgradeToLatestDatabaseVersion() {
		int currentDatabaseVersion = getCurrentDatabaseVersion();
		if (currentDatabaseVersion < EccdConstants.DATABASE_VERSION) {
			/*
			 * Code to upgrade database depending on the current database version.
			 */
		}
	}

	public int getCurrentDatabaseVersion() {
		Cursor cursor = database.query(EccdConstants.DATABASE_VERSION_TABLE,
				null,
				null,
				null,
				null,
				null,
				null);
		cursor.moveToNext();
		int currentDatabaseVersion = cursor.getInt(cursor.getColumnIndex(EccdConstants.DATABASE_VERSION_COLUMN));
		cursor.close();
		return currentDatabaseVersion;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}