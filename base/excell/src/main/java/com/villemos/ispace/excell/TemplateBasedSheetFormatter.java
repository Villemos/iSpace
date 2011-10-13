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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

		Map<String, String> fieldNamesFromComments = new HashMap<String, String>();
		Pattern fieldInComment = Pattern.compile("field=\'(.*?)\'");

		int column = 0;
		while (column < sheet.getColumns()) {
			if (sheet.getCell(column, 0).getClass() != jxl.biff.EmptyCell.class) {
				String columnName = sheet.getCell(column, 0).getContents().toString();
				name2id.put(columnName, column);

				/** See if there is a comment. If yes, see if it contains entries in the format
				 * field='[class field name]'. Extract these and use them as a map. */
				if (sheet.getCell(column, 0).getCellFeatures() != null) {
					String comment = sheet.getCell(column, 0).getCellFeatures().getComment();
					if (comment != null) {
						Matcher matcher = fieldInComment.matcher(comment);
						while (matcher.find()) {
							fieldNamesFromComments.put(matcher.group(1), columnName);
						}
					}
				}
			}
			column++;
		}				

		/** The sheet may have a header, in which case the first row(s) may already
		 * have been set. We start at the first free row, unless a starting row
		 * has been configured. */
		int row = endpoint.getStartRow() == -1 ? sheet.getRows() : endpoint.getStartRow();  

		Map<String, String> fieldNames = endpoint.getFieldNames();
		if (fieldNames == null) {
			fieldNames = fieldNamesFromComments;
		}

		if (fieldNames.isEmpty()) {
			LOG.error("The configuration variable 'fieldNames' must be set on the Excell Endpoint when using template based generation.");
		}

		/** Iterate through the objects. */
		for (Object object : objects) {

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
		
		/** If in append mode, then set the row on the endpoint so that we start from that point. */
		if (endpoint.isAppendMode()) {
			endpoint.setStartRow(row);
		}
	}
}
