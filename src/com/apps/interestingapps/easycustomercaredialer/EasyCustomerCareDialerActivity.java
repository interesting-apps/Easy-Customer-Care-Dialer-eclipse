package com.apps.interestingapps.easycustomercaredialer;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.apps.interestingapps.easycustomercaredialer.adapters.EccdArrayAdapter;
import com.apps.interestingapps.easycustomercaredialer.common.ActivityState;
import com.apps.interestingapps.easycustomercaredialer.common.DatabaseHelper;
import com.apps.interestingapps.easycustomercaredialer.common.EccdConstants;
import com.apps.interestingapps.easycustomercaredialer.common.EccdUtilities;

public class EasyCustomerCareDialerActivity extends Activity {

	private String TAG = "EasyCustomerCareDialerActivity";
	private DatabaseHelper databaseHelper;
	private TextView countryNotSelectedTextView,
			companyNameCompanyPhoneNumberScreenTextView;
	private EditText enterCountryNameEditText;
	private ListView companyListView, companyPhoneNumberListView,
			optionNumberListView;
	private Stack<SimpleCursorAdapter> cursorAdapterStack;
	// private SimpleCursorAdapter companyNameDataAdapter;
	private EccdArrayAdapter<String> companyNameDataArrayAdapter;
	private SimpleCursorAdapter phoneNumberDataAdapter;
	private SimpleCursorAdapter optionNumberDataAdapter;
	private ActivityState activityState;
	private String userSpecifiedCountry;
	private String userEnteredCompanyName = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		initializeStaticViews();
		cursorAdapterStack = new Stack<SimpleCursorAdapter>();
		activityState = ActivityState.OTHER_SCREEN;

		String userCountry = getUserFrequentlyCalledCountry();
		if (userCountry == null || userCountry.length() <= 0) {
			countryNotSelectedTextView.setVisibility(View.VISIBLE);
			enterCountryNameEditText.setVisibility(View.GONE);
			companyListView.setVisibility(View.GONE);
		} else {
			Log.i(TAG, "Found User country: " + userCountry);
			userSpecifiedCountry = userCountry;
			initializeDatabase(userCountry);
		}
	}

	private void initializeStaticViews() {
		countryNotSelectedTextView = (TextView) findViewById(R.id.country_not_selected_text_view);
		enterCountryNameEditText = (EditText) findViewById(R.id.enter_company_name_edit_text);
		companyListView = (ListView) findViewById(R.id.company_list_view);
		companyPhoneNumberListView = (ListView) findViewById(R.id.company_phone_number_list_view);
		companyNameCompanyPhoneNumberScreenTextView = (TextView) findViewById(R.id.company_name_company_phone_screen_text_view);
		optionNumberListView = (ListView) findViewById(R.id.option_list_view);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (databaseHelper != null) {
			databaseHelper.close();
		}
	}

	@Override
	public void onBackPressed() {
		String[] companyPhoneNumberAndOptionsString = parseCompanyNamePhoneNumberAndOptionsString();
		String companyName = null;
		int companyId = -1;
		int defaultLevel = 1;
		int level = 0;
		int parentOptionNumber = 0;
		String phoneNumber = null;
		switch (activityState) {
		case COUNTRY_DATA_SCREEN:
			// Companies are currently shown
			hideCompaniesViews();
			super.onBackPressed();
		case COMPANY_DATA_SCREEN:
			// Phone numbers are currently shown
			hidePhoneNumbersViews();
			loadCompaniesForCountry(userSpecifiedCountry);
			break;
		case PHONE_NUMBER_DATA_SCREEN:
			// Option numbers are currently shown
			/*
			 * Hide options view only when the first level options are shown. if
			 * the second level and above options are shown, don't hide the options
			 * views
			 */
			hideOptionsViews();
			if (companyPhoneNumberAndOptionsString != null
					&& companyPhoneNumberAndOptionsString.length > 0) {
				companyName = companyPhoneNumberAndOptionsString[activityState.getActvityState() - 1];
				companyId = databaseHelper.findCompanyId(userSpecifiedCountry,
						companyName);
				loadPhoneNumbersForCompany(companyName, companyId);
			}
			break;
		case OPTION_DATA_SCREEN:
			// child options of currently selected option is shown
			if (companyPhoneNumberAndOptionsString != null
					&& companyPhoneNumberAndOptionsString.length > 1) {
				phoneNumber = companyPhoneNumberAndOptionsString[activityState.getActvityState() - 1];
				level = defaultLevel
						+ (companyPhoneNumberAndOptionsString.length - activityState.getActvityState())
						- 1;
				if (companyPhoneNumberAndOptionsString.length > 3) {
					parentOptionNumber = Integer.parseInt(companyPhoneNumberAndOptionsString[companyPhoneNumberAndOptionsString.length - 2]);
				}
				String currentText = companyNameCompanyPhoneNumberScreenTextView.getText()
						.toString();
				String udpatedText = currentText.substring(0,
						currentText.lastIndexOf(EccdConstants.DATA_SEPARATOR));
				companyNameCompanyPhoneNumberScreenTextView.setText(udpatedText);
				loadOptionsForPhoneNumber(phoneNumber,
						level,
						parentOptionNumber,
						false);
			}
			break;
		case OTHER_SCREEN:
			super.onBackPressed();
			break;
		default:
			super.onBackPressed();
			break;
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
							initializeDatabase(userCountry);
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

	private void initializeDatabase(String userCountry) {
		if (userCountry.length() > 0) {

			countryNotSelectedTextView.setVisibility(View.GONE);
			enterCountryNameEditText.setVisibility(View.VISIBLE);
			/*
			 * TODO: Add code to copy the database according to the given country.
			 */
			databaseHelper = DatabaseHelper.initializeDatabase(getApplicationContext());
			loadCompaniesForCountry(userCountry);
		} else {
			Log.e(TAG, "UserCountry is not specified to load data.");
		}
	}

	private void loadCompaniesForCountry(String userCountry) {
		activityState = ActivityState.COUNTRY_DATA_SCREEN;
		Cursor companyNamesCursor = databaseHelper.getCompaniesForCountry(userCountry);

		List<String> companyNames = EccdUtilities.convertCursorToList(companyNamesCursor,
				EccdConstants.COMPANY_NAME_COLUMN);
		if (companyNames != null) {
			companyNameDataArrayAdapter = new EccdArrayAdapter<>(getApplicationContext(),
					R.layout.company_list_view_layout,
					companyNames);
			// companyListView.setVisibility(View.VISIBLE);
			showCompaniesViews();
			companyListView.setAdapter(companyNameDataArrayAdapter);
			addTextWatcherToCompanyNameEditText();
			if (userEnteredCompanyName != null
					&& userEnteredCompanyName.length() > 0) {
				companyNameDataArrayAdapter.getFilter()
						.filter(userEnteredCompanyName);
			}
			companyListView.setOnItemClickListener(new CompanyNameOnItemClickListener(userCountry));
		}
		if (companyNamesCursor != null) {
			companyNamesCursor.close();
		}
	}

	private void showCompaniesViews() {
		companyListView.setVisibility(View.VISIBLE);
	}

	private void hideCompaniesViews() {
		companyListView.setVisibility(View.GONE);
	}

	private void loadPhoneNumbersForCompany(String companyName, int companyId) {
		activityState = ActivityState.COMPANY_DATA_SCREEN;
		Cursor companyHasContactCursor = databaseHelper.getPhoneNumberForCompany(companyId);
		String[] columns = { EccdConstants.PHONE_NUMBER_COLUMN };
		int[] to = { R.id.phone_number_company_phone_number_screen_text_view };

		phoneNumberDataAdapter = new SimpleCursorAdapter(getApplicationContext(),
				R.layout.company_phone_number_list_view_layout,
				companyHasContactCursor,
				columns,
				to,
				0);
		// companyListView.setVisibility(View.GONE);
		// companyNameCompanyPhoneNumberScreenTextView.setVisibility(View.VISIBLE);
		// enterCountryNameEditText.setVisibility(View.GONE);
		showPhoneNumbersViews();
		companyNameCompanyPhoneNumberScreenTextView.setText(companyName);
		// companyPhoneNumberListView.setVisibility(View.VISIBLE);
		companyPhoneNumberListView.setAdapter(phoneNumberDataAdapter);

		/*
		 * This would show the options present at level 1. The subsequent options
		 * will be shown by on click of the shown options.
		 */
		companyPhoneNumberListView.setOnItemClickListener(new PhoneNumberOnItemClickListener(1,
				0));
	}

	private void showPhoneNumbersViews() {
		companyListView.setVisibility(View.GONE);
		companyNameCompanyPhoneNumberScreenTextView.setVisibility(View.VISIBLE);
		userEnteredCompanyName = enterCountryNameEditText.getText().toString();
		enterCountryNameEditText.setVisibility(View.GONE);
		companyPhoneNumberListView.setVisibility(View.VISIBLE);
	}

	private void hidePhoneNumbersViews() {
		companyListView.setVisibility(View.VISIBLE);
		companyNameCompanyPhoneNumberScreenTextView.setVisibility(View.GONE);
		enterCountryNameEditText.setVisibility(View.VISIBLE);
		enterCountryNameEditText.setText(userEnteredCompanyName);
		companyPhoneNumberListView.setVisibility(View.GONE);
	}

	private void loadOptionsForPhoneNumber(String phoneNumber,
			int level,
			int parentOptionNumber,
			boolean append) {
		if (level <= 1) {
			activityState = ActivityState.PHONE_NUMBER_DATA_SCREEN;
		} else {
			activityState = ActivityState.OPTION_DATA_SCREEN;
		}
		Cursor optionsCursor = databaseHelper.getOptionsForPhoneNumber(phoneNumber,
				level,
				parentOptionNumber);
		if (optionsCursor.getCount() > 0) {
			if (parentOptionNumber > 0 && append == true) {
				String companyNamePhoneNumberAndLevels = companyNameCompanyPhoneNumberScreenTextView.getText()
						.toString();
				companyNamePhoneNumberAndLevels = companyNamePhoneNumberAndLevels
						+ EccdConstants.DATA_SEPARATOR + parentOptionNumber;

				companyNameCompanyPhoneNumberScreenTextView.setText(companyNamePhoneNumberAndLevels);
			}
			String[] columns = { EccdConstants.OPTION_NUMBER_COLUMN,
					EccdConstants.OPTION_TEXT_COLUMN, EccdConstants.WAIT_TIME_COLUMN };
			int[] to = { R.id.option_number_option_screen_text_view,
					R.id.option_text_option_screen_text_view,
					R.id.wait_time_option_screen_text_view };

			optionNumberDataAdapter = new SimpleCursorAdapter(getApplicationContext(),
					R.layout.option_number_list_view_layout,
					optionsCursor,
					columns,
					to,
					0);
			showOptionsViews();
			// companyPhoneNumberListView.setVisibility(View.GONE);
			// companyNameCompanyPhoneNumberScreenTextView.setVisibility(View.VISIBLE);
			String companyNamePhoneNumberAndLevels = companyNameCompanyPhoneNumberScreenTextView.getText()
					.toString();
			/*
			 * If the current level is 1, then set the text now, otherwise set the
			 * text on click of the option
			 */
			if (level == 1 && append == true) {
				companyNamePhoneNumberAndLevels = companyNamePhoneNumberAndLevels
						+ EccdConstants.DATA_SEPARATOR + phoneNumber;
			}
			companyNameCompanyPhoneNumberScreenTextView.setText(companyNamePhoneNumberAndLevels);
			// optionNumberListView.setVisibility(View.VISIBLE);
			optionNumberListView.setAdapter(optionNumberDataAdapter);
			optionNumberListView.setOnItemClickListener(new PhoneNumberOnItemClickListener(level + 1,
					parentOptionNumber));
		} else {
			Log.d(TAG, (level - 1) + " is the last level of options. ");
		}
	}

	private void showOptionsViews() {
		companyPhoneNumberListView.setVisibility(View.GONE);
		companyNameCompanyPhoneNumberScreenTextView.setVisibility(View.VISIBLE);
		optionNumberListView.setVisibility(View.VISIBLE);
	}

	private void hideOptionsViews() {
		companyPhoneNumberListView.setVisibility(View.VISIBLE);
		companyNameCompanyPhoneNumberScreenTextView.setVisibility(View.GONE);
		optionNumberListView.setVisibility(View.GONE);
	}

	private void addTextWatcherToCompanyNameEditText() {
		enterCountryNameEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void
					onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
				// When user changed the Text
				if (companyNameDataArrayAdapter != null) {
					companyNameDataArrayAdapter.getFilter().filter(cs);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0,
					int arg1,
					int arg2,
					int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
	}

	private class CompanyNameOnItemClickListener implements OnItemClickListener {

		private String userCountry;

		public CompanyNameOnItemClickListener(String userCountry) {
			this.userCountry = userCountry;
		}

		@Override
		public void onItemClick(AdapterView<?> parent,
				View view,
				int position,
				long id) {
			String clickedCompanyName = (String) parent.getItemAtPosition(position);
			int clickedCompanyId = databaseHelper.findCompanyId(userCountry,
					clickedCompanyName);
			loadPhoneNumbersForCompany(clickedCompanyName, clickedCompanyId);

		}
	}

	private class PhoneNumberOnItemClickListener implements OnItemClickListener {
		private int level;
		private int parentOptionNumber;

		public PhoneNumberOnItemClickListener(int level, int parentOptionNumber) {
			this.level = level;
			this.parentOptionNumber = parentOptionNumber;
		}

		@Override
		public void onItemClick(AdapterView<?> parent,
				View view,
				int position,
				long id) {
			activityState = ActivityState.OPTION_DATA_SCREEN;
			SQLiteCursor clickedPhoneNumberItemCursor = (SQLiteCursor) parent.getItemAtPosition(position);
			String clickedPhoneNumber = clickedPhoneNumberItemCursor.getString(clickedPhoneNumberItemCursor.getColumnIndex(EccdConstants.PHONE_NUMBER_COLUMN));
			int currentParentOptionNumber = this.parentOptionNumber;
			SQLiteCursor clickedOptionNumberItemCursor = (SQLiteCursor) parent.getItemAtPosition(position);
			int optionColumnIndex = clickedPhoneNumberItemCursor.getColumnIndex(EccdConstants.OPTION_NUMBER_COLUMN);
			/*
			 * If the parent listview does not contain
			 * EccdConstants.OPTION_NUMBER_COLUMN, it means the listner is called
			 * by clicking on companyPhoneNumberListView and not on
			 * optionNumberListView
			 */
			if (optionColumnIndex > -1) {
				currentParentOptionNumber = clickedOptionNumberItemCursor.getInt(optionColumnIndex);
			}

			loadOptionsForPhoneNumber(clickedPhoneNumber,
					level,
					currentParentOptionNumber,
					true);
		}
	}

	private String[] parseCompanyNamePhoneNumberAndOptionsString() {
		if (companyNameCompanyPhoneNumberScreenTextView.getVisibility() == View.VISIBLE) {
			String[] strArray = companyNameCompanyPhoneNumberScreenTextView.getText()
					.toString()
					.split(EccdConstants.DATA_SEPARATOR);
			return strArray;
		} else {
			return null;
		}

	}
}
