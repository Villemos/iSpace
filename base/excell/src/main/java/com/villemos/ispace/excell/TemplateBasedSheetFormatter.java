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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jxl.write.WritableSheet;

public class TemplateBasedSheetFormatter extends DefaultSheetFormatter {
	
	private static final transient Logger LOG = LoggerFactory.getLogger(TemplateBasedSheetFormatter.class);

	public TemplateBasedSheetFormatter() {
		super();
	}

	@Override
	public void add(Collection objects, WritableSheet sheet, ExcellEndpoint endpoint) {
		/** Meta data already exist. */
		
		/** Read the column IDs. */
		Map<String, Integer> name2id = new HashMap<String, Integer>();

		int column = 0;
		while (column < sheet.getColumns()) {
			if (sheet.getCell(column, 0).getClass() != jxl.biff.EmptyCell.class) {
				name2id.put(sheet.getCell(column, 0).getContents().toString(), column);	
			}
			column++;
		}				
		
		/** The sheet may have a header, in which case the first row(s) may already
		 * have been set. We start at the first free row, unless a starting row
		 * has been configured. */
		int row = endpoint.getStartRow() == -1 ? sheet.getRows() : endpoint.getStartRow();  
		
		/** Iterate through the objects. */
		for (Object object : objects) {
		
			Map<String, String> fieldNames = endpoint.getFieldNames(); 
			if (fieldNames == null) {
				LOG.error("The configuration variable 'fieldNames' must be set on the Excell Endpoint when using template based generation.");
			}
			
			/** Iterate through the field maps that we have. */
			Iterator<Entry<String, String>> it = fieldNames.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				
				/** Get field from object. */
				try {
					Field field = object.getClass().getField(entry.getKey());
					
					field.setAccessible(true);
					
					String fieldName = field.getName();
					String columnName = fieldNames.get(fieldName);
					
					if (columnName == null) {
						LOG.error("Column name '" + fieldName + "' not configured!");
					}
					
					column = name2id.get(columnName);
					
					insertCell(field, sheet, column, row, object);
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
			
			row++;
		}		
	}
}
