package com.apps.interestingapps.easycustomercaredialer;

import java.util.Arrays;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.apps.interestingapps.easycustomercaredialer.common.DatabaseHelper;
import com.apps.interestingapps.easycustomercaredialer.common.EccdConstants;

public class EasyCustomerCareDialerActivity extends ActionBarActivity {

	private String TAG = "EasyCustomerCareDialerActivity";
	private DatabaseHelper databaseHelper;
	private TextView countryNotSelectedTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		countryNotSelectedTextView = (TextView) findViewById(R.id.country_not_selected_text_view);

		String userCountry = getUserFrequentlyCalledCountry();
		if (userCountry == null || userCountry.length() <= 0) {
			countryNotSelectedTextView.setVisibility(View.VISIBLE);
		} else {
			Log.i(TAG, "Found User country: " + userCountry);
			loadCountryData(userCountry);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (databaseHelper != null) {
			databaseHelper.close();
		}
	}

	private String getUserFrequentlyCalledCountry() {
		SharedPreferences prefs = getSharedPreferences(EccdConstants.PREFERENCES_FILE_NAME,
				0);
		String userCountry = prefs.getString(getResources().getString(R.string.frequently_called_country),
				"");
		if (userCountry.length() > 0) {
			Log.i(TAG, "User country is: " + userCountry);
			return userCountry;
		}
		/*
		 * User hasn't added the frequently called country. Ask the user for a
		 * country.
		 */
		showSelectFrequentlyCalledCountryDialog();
		Log.i(TAG, "User country is: " + userCountry);
		return userCountry;
	}

	private void showSelectFrequentlyCalledCountryDialog() {
		// Dialog dialog = new Dialog(this);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.select_country_dialog_title));
		final String[] countryNames = getResources().getStringArray(R.array.country_names);
		Arrays.sort(countryNames);
		final SharedPreferences prefs = getSharedPreferences(EccdConstants.PREFERENCES_FILE_NAME,
				0);
		final SharedPreferences.Editor prefsEditor = prefs.edit();
		String prefsCountry = prefs.getString(getResources().getString(R.string.frequently_called_country),
				"");
		prefsEditor.putString(getResources().getString(R.string.frequently_called_country),
				prefsCountry);
		builder.setSingleChoiceItems(countryNames, -1, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				String userCountry = countryNames[which];
				prefsEditor.putString(getResources().getString(R.string.frequently_called_country),
						userCountry);
			}
		});
		builder.setPositiveButton(getResources().getString(R.string.ok_button_text),
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						prefsEditor.commit();
						dialog.dismiss();
						String userCountry = prefs.getString(getResources().getString(R.string.frequently_called_country),
								"");
						if (userCountry.length() > 0) {
							loadCountryData(userCountry);
						}
					}
				});
		builder.setNegativeButton(getResources().getString(R.string.cancel_button_text),
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}

				});
		// now that the dialog is set up, it's time to show it
		builder.show();
	}

	private void loadCountryData(String userCountry) {
		if (userCountry.length() > 0) {
			countryNotSelectedTextView.setVisibility(View.INVISIBLE);
			/*
			 * TODO: Add code to copy the database according to the given country.
			 */
			databaseHelper = DatabaseHelper.initializeDatabase(getApplicationContext());
		} else {
			Log.e(TAG, "UserCountry is not specified to load data.");
		}
	}
}
