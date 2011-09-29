package com.villemos.ispace.excell;

import java.util.Date;

public class TestClassSpecialization extends TestClass {

	public TestClassSpecialization(String aString, Date aDate, int aInteger,
			double aDouble, float aFloat, boolean aBoolean, String newField) {
		super(aString, aDate, aInteger, aDouble, aFloat, aBoolean);
		this.newField = newField;
	}

	public String newField;	
}
