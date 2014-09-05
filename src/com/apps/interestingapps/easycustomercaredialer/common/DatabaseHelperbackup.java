package com.apps.interestingapps.easycustomercaredialer.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelperbackup extends SQLiteOpenHelper {

	private String TAG = "DatabaseHelper";
	private SQLiteDatabase database;
	private Context context;

	public DatabaseHelperbackup(Context context,
			String name,
			CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		this.context = context;
	}

	/**
	 * This method only copies the database file from Assets folder. It does not
	 * open database. Currently the methods @link {@link #getDatabaseVersion()}
	 * and @link {@link #enableForeignKeys()} open the database if its not
	 * already done. If some method is written that is expected to be run before
	 * these methods, then make sure that it opens the database before running
	 * any operation.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "On create called in database helper");
		Log.i(TAG, "Database path is: " + db.getPath());
		try {
			createDataBase(db);
		} catch (IOException e) {
			Log.i(TAG, "Error occurred while opening database.");
			e.printStackTrace();
		}
	}

	/**
	 * Method to create a database. The database is copied from assests folder if
	 * it doesn't exists already
	 *
	 * @throws IOException
	 */
	public void createDataBase(SQLiteDatabase db) throws IOException {
		boolean dbExist = checkDataBase(db);
		if (!dbExist) {
			Log.i(TAG, "Database does not contain expected tables.");
			copyDataBase(db);
		}
	}

	/**
	 * Checks if database file exists, and it can be opened
	 *
	 * @return true if the database can be opened
	 */
	private boolean checkDataBase(SQLiteDatabase db) {
		SQLiteDatabase checkDB = null;
		boolean dbExist = true;
		// try {
		// checkDB = getReadableDatabase();
		// } catch (Exception e) {
		// // Some error occurred. Override the existing database to avoid
		// // errors
		// Log.e(TAG, e.getMessage());
		// dbExist = false;
		// }
		if (db != null) {
			String query = "SELECT * FROM SQLITE_MASTER";
			dbExist = false;
			try {
				Cursor cursor = db.rawQuery(query, null);
				if (cursor != null) {
					Log.i(TAG, "DB has: " + cursor.getCount() + " rows.");
				}
				while (cursor.moveToNext()) {
					String tableName = cursor.getString(cursor.getColumnIndex("name"));
					if (tableName.equalsIgnoreCase(EccdConstants.COMPANY_TABLE)) {
						dbExist = true;
						break;
					}
				}
				cursor.close();
			} catch (Exception e) {
				e.printStackTrace();
				// if (checkDB != null) {
				// checkDB.close();
				// }
			}
		} else {
			dbExist = false;
		}
		Log.i(TAG, "DB exists: " + dbExist);
		return dbExist;
	}

	/**
	 * Copy the database file from assests to database folder of the app
	 *
	 * @throws IOException
	 */
	private void copyDataBase(SQLiteDatabase db) throws IOException {
		// Open your local db as the input stream
		// String outFileName = EccdConstants.DB_PATH +
		// EccdConstants.DATABASE_NAME;
		String outFileName = db.getPath();
		Log.i(TAG, "DB path is: " + outFileName);
		File dbFile = new File(outFileName);

		if (dbFile.exists()) {
			dbFile.delete();
			Log.i(TAG, "Database file deleted.");
		}

		InputStream myInput = context.getAssets()
				.open(EccdConstants.DATABASE_NAME);
		// Path to the just created empty db


		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName, false);
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
		Log.i(TAG, "Successfully copied the file");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG, "OnUpgrade in DatabaseHelper called.");
		Log.i(TAG, "Database version in On upgrade is: " + db.getVersion());
		Log.i(TAG, "Old version is: " + oldVersion + ", new version is: "
				+ newVersion);
	}

	public int getDatabaseVersion() {
		if (database == null) {
			database = getWritableDatabase();
		}
		return database.getVersion();
	}

	public void enableForeignKeys() {
		if (database == null) {
			database = getWritableDatabase();
		}
		database.execSQL(EccdConstants.ENABLE_FOREIGN_KEY_QUERY);
		Log.d(TAG, "Enabled Foreign Keys.");
	}

	/**
	 * Close the database if its open
	 */
	public void closeDatabase() {
		if (database != null) {
			Log.i(TAG, "closing database..");
			database.close();
		}
	}

	public Cursor getValuesFromContact() {
		return database.query(EccdConstants.CONTACT_TABLE,
				null,
				null,
				null,
				null,
				null,
				null);
	}

	public int insertIntoOptionsTable(String phoneNumber,
			int level,
			int optionNumber,
			String optionText,
			int waitTime) {
		ContentValues values = new ContentValues();
		values.put("phone_number", phoneNumber);
		values.put("level", level);
		values.put("option_number", optionNumber);
		values.put("option_text", optionText);
		values.put("wait_time", waitTime);

		int insertId = (int) database.insert(EccdConstants.OPTIONS_TABLE,
				null,
				values);
		return insertId;
	}
}
