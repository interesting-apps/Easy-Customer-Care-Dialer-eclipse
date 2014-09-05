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

	public static final String CREATE_ANDROID_METADATA_TABLE_QUERY = "CREATE TABLE android_metadata "
			+ "('locale' TEXT NOT NULL DEFAULT ('en_US'))";
	public static final String CREATE_COUNTRY_TABLE_QUERY = "CREATE TABLE country "
			+ "('name' text primary key , 'country_code' INTEGER NOT NULL )";
	public static final String CREATE_COMPANY_TABLE_QUERY = "CREATE TABLE company "
			+ "('company_id' INTEGER primary key autoincrement ,"
			+ "'name' Text NOT NULL,    'last_updated' Date)";
	public static final String CREATE_COUNTRY_CONSISTSOF_COMPANY_TABLE_QUERY = "CREATE TABLE country_consistsof_company "
			+ "('country_name' Text NOT NULL, 'company_id' INTEGER, "
			+ "'top_rated' boolean default ('false'), "
			+ "primary key ('country_name', 'company_id'), "
			+ "foreign key('country_name') references country('country_name') on delete cascade on update cascade, "
			+ "foreign key('company_id') references company('company_id') on delete cascade on update cascade)";
	public static final String CREATE_CONTACT_TABLE_QUERY = "CREATE TABLE contact "
			+ "('phone_number' Text NOT NULL primary key, "
			+ "'email_id' Text, 'department_name' Text default ('All'))";
	public static final String CREATE_OPTIONS_TABLE_QUERY = "CREATE TABLE options "
			+ "('phone_number' Text NOT NULL, 'level' integer NOT NULL, "
			+ "'option_number' integer NOT NULL, "
			+ "'option_text' Text NOT NULL, 'wait_time' integer default 0, "
			+ "primary key('phone_number', 'level', 'option_number'), "
			+ "foreign key('phone_number') references contact('phone_number') on delete cascade on update cascade)";
	public static final String CREATE_COMPANY_HAS_CONTACT_TABLE_QUERY = "CREATE TABLE company_has_contact "
			+ "('company_id' Text NOT NULL,'phone_number' Text NOT NULL, "
			+ "primary key('company_id', 'phone_number'), "
			+ "foreign key('phone_number') references contact('phone_number') on delete cascade on update cascade, "
			+ "foreign key('company_id') references company('company_id') on delete cascade on update cascade)";
	public static final String CREATE_DATABASE_VERSION_TABLE = "create table database_version (version int)";

	public static final String ENABLE_FOREIGN_KEY_QUERY = "PRAGMA foreign_keys=ON;";

	public static final String APP_PACKAGE = "com.apps.interestingapps.easycustomercaredialer";
	public static final String PREFERENCES_FILE_NAME = APP_PACKAGE
			+ ".preferences";

}
