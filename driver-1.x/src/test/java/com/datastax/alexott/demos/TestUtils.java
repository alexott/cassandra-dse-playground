package com.datastax.alexott.demos;

import com.datastax.driver.core.LocalDate;

import junit.framework.TestCase;

public class TestUtils extends TestCase {

	public void testDate() {
		assertEquals(LocalDate.fromYearMonthDay(2017,11,22),  Utils.convertDate("2017-11-22"));
	}
	
}
