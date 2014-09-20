package com.apps.interestingapps.easycustomercaredialer.common;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

public class EccdUtilities {

	/**
	 * Converts a cursor into a list of Strings. This method assumes that the
	 * given columnName corresponds to String datatype.
	 *
	 * @param cursor
	 * @param columnName
	 * @return null if cursor is null or columnName is null or columnName doesn't
	 *         exists in the cursor, otherwise a list of values in the column
	 *         with columnName in the given cursor.
	 */
	public static List<String> convertCursorToList(Cursor cursor,
			String columnName) {
		List<String> result = new ArrayList<String>();
		if (cursor == null || columnName == null) {
			return null;
		}
		int columnIndex = cursor.getColumnIndex(columnName);
		if (columnIndex < 0) {
			return null;
		}
		while (cursor.moveToNext()) {
			result.add(cursor.getString(columnIndex));
		}
		return result;
	}
}
