/**
 * villemos solutions [space^] (http://www.villemos.com) 
 * Probe. Send. Act. Emergent solution. 
 * Copyright 2011 Gert Villemos
 * All Rights Reserved.
 * 
 * Released under the Apache license, version 2.0 (do what ever
 * you want, just dont claim ownership).
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of villemos solutions, and its suppliers
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to villemos solutions
 * and its suppliers and may be covered by European and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * 
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from villemos solutions.
 * 
 * And it wouldn't be nice either.
 * 
 */
package com.villemos.ispace.excell;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.WritableSheet;

public class DefaultSheetFormatter implements ISheetFormatter {

	@Override
	public void add(Collection objects, WritableSheet sheet) {

		int row = 1;

		/** Map of field name to column index. */
		Map<String, Integer> columns = new HashMap<String, Integer>();
		
		for (Object object : objects) {

			try {
				sheet.addCell(new Label(0, row, object.getClass().getName()));			

				for (Field field : getFields(object)) {
					
					if (columns.containsKey(field.getName()) == false) {
						columns.put(field.getName(), sheet.getColumns());
						sheet.addCell(new Label(sheet.getColumns(), row, field.getName()));
					}
					
					field.setAccessible(true);

					if (field.getType().getName().contains("java.util.Date")) {
						sheet.addCell(new DateTime(columns.get(field.getName()), row, (Date) field.get(object)));								
					}
					else {
						sheet.addCell(new Label(columns.get(field.getName()), row, field.get(object).toString()));
					}
				} 
			}
			catch (Exception e) {
				e.printStackTrace();
			}				

			row++;
		}		
	}

	protected Field[] getFields(Object object) {
		return object.getClass().getFields();
	}
}
