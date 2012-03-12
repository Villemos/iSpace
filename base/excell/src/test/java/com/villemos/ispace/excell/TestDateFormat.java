package com.villemos.ispace.excell;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;




public class TestDateFormat  {

	@Test
	public void testDateFormatRead() {
		
		// String test = "Mon Sep 26 16:31:00 CEST 2011";		
		// DateFormat format = new SimpleDateFormat("EEE MMM d HH:mm:ss 'CEST' yyyy");

		String test = "Mon Sep 26 16:31:00 CEST 2011";		
		DateFormat format = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.UK);
		
		try {
			Date theDate = format.parse(test);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
