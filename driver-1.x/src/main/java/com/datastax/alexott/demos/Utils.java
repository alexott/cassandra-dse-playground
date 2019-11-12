package com.datastax.alexott.demos;

import com.datastax.driver.core.LocalDate;

public class Utils {

	public static LocalDate convertDate(final String date) {
		String[] arr = date.split("-");
		if (arr.length != 3)
			return null;
		return LocalDate.fromYearMonthDay(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]),
				Integer.parseInt(arr[2]));
	}
}
