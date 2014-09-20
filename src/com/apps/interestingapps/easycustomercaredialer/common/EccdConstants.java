package com.apps.interestingapps.easycustomercaredialer.common;

public class EccdConstants {

	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "eccd_1.db";
	public static final String DB_PATH = "/data/data/com.apps.interestingapps.easycustomercaredialer/databases/";

	public static final String ANDROID_METADATA_TABLE = "android_metadata";
	public static final String COUNTRY_TABLE = "country";
	public static final String COMPANY_TABLE = "company";
	public static final String COUNTRY_CONSISTSOF_COMPANY_TABLE = "country_consistsof_company";
	public static final String CONTACT_TABLE = "contact";
	public static final String OPTIONS_TABLE = "options";
	public static final String COMPANY_HAS_CONTACT_TABLE = "company_has_contact";
	public static final String DATABASE_VERSION_TABLE = "database_version";

	public static final String DATABASE_VERSION_COLUMN = "database_version";
	public static final String COMPANY_ID_COLUMN = "company_id";
	public static final String COUNTRY_NAME_COLUMN = "country_name";
	public static final String COMPANY_NAME_COLUMN = "company_name";
	public static final String PHONE_NUMBER_COLUMN = "phone_number";
	public static final String LEVEL_COLUMN = "level";
	public static final String PARENT_OPTION_NUMBER_COLUMN = "parent_option_number";
	public static final String OPTION_NUMBER_COLUMN = "option_number";
	public static final String OPTION_TEXT_COLUMN = "option_text";
	public static final String WAIT_TIME_COLUMN = "wait_time";

	public static final String ENABLE_FOREIGN_KEY_QUERY = "PRAGMA foreign_keys=ON;";

	public static final String APP_PACKAGE = "com.apps.interestingapps.easycustomercaredialer";
	public static final String PREFERENCES_FILE_NAME = APP_PACKAGE
			+ ".preferences";

	public static final String DATA_SEPARATOR = "->";
}
