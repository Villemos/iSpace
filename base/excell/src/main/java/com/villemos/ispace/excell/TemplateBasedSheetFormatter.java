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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.WritableSheet;

public class TemplateBasedSheetFormatter implements ISheetFormatter {
	
	protected Map<String, String> field2column;

	protected int startRow = -1;
	
	public TemplateBasedSheetFormatter(Map<String, String> field2column) {
		super();
		this.field2column = field2column;
	}

	@Override
	public void add(Collection objects, WritableSheet sheet) {
		/** Meta data already exist. */
		
		/** Ready the column IDs. */
		Map<String, Integer> name2id = new HashMap<String, Integer>();

		int column = 0;
		while (column < sheet.getColumns()) {
			name2id.put(sheet.getCell(column, 0).getContents().toString(), column);	
			column++;
		}				
		
		/** The sheet may have a header, in which case the first row(s) may already
		 * have been set. We start at the first free row, unless a starting row
		 * has been configured. */
		int row = startRow == -1 ? sheet.getRows() : startRow;  
		
		/** Iterate through the objects. */
		for (Object object : objects) {
		
			/** Iterate through the field maps that we have. */
			Iterator<Entry<String, String>> it = field2column.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				
				/** Get field from object. */
				try {
					Field field = object.getClass().getField(entry.getKey());
					
					field.setAccessible(true);
					
					String fieldName = field.getName();
					String columnName = field2column.get(fieldName);
					column = name2id.get(columnName);
					
					if (field.getType().getName().contains("java.util.Date")) {
						sheet.addCell(new DateTime(column, row, (Date) field.get(object)));								
					}
					else {
						sheet.addCell(new Label(column, row, field.get(object).toString()));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
			
			row++;
		}		
	}

	public Map<String, String> getField2column() {
		return field2column;
	}

	public void setField2column(Map<String, String> field2column) {
		this.field2column = field2column;
	}

	public int getStartRow() {
		return startRow;
	}

	public void setStartRow(int startRow) {
		this.startRow = startRow;
	}
}
