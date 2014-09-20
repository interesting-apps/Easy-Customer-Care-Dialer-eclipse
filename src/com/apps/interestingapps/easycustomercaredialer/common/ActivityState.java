package com.apps.interestingapps.easycustomercaredialer.common;

public enum ActivityState {

	COUNTRY_DATA_SCREEN(100),

	COMPANY_DATA_SCREEN(0),

	PHONE_NUMBER_DATA_SCREEN(1),

	OPTION_DATA_SCREEN(2),

	OTHER_SCREEN(-1);

	private int stateIndex;

	private ActivityState(int stateIndex) {
		this.stateIndex = stateIndex;
	}

	public int getActvityState() {
		return this.stateIndex;
	}
}
