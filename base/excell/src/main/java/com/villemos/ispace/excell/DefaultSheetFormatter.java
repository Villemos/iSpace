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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jxl.Cell;
import jxl.format.CellFormat;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableHyperlink;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class DefaultSheetFormatter implements ISheetFormatter {

	private static final transient Logger LOG = LoggerFactory.getLogger(DefaultSheetFormatter.class);

	/** Flag indicating whether we should insert the class name in each 
	 * column 0 entry. This is needed if the data is intended to be 'consumed' by
	 * the Excell component later (unless all objects are of the same type and the
	 * type is configured explicitly on the Excell consumer). */
	public boolean includeClassName = true;	

	@Override
	public void add(Collection objects, WritableSheet sheet, ExcellEndpoint endpoint) {

		/** Row 0 is header, so start from row 1. */
		int row = 1;

		/** Map of object class to Fields, to ensure we only have to get the fields ones. */
		Map<String, List<Field>> classFields = new HashMap<String, List<Field>>();

		/** Map of field name to column index. */
		Map<String, Integer> columns = new HashMap<String, Integer>();

		for (Object object : objects) {

			try {
				/** If we have been configured to include the class name, do so! 
				 * Notice that this automatically creates column 0. All later 
				 * inserts to a column will be in columns =< 1. */
				if (includeClassName == true) {
					sheet.addCell(new Label(0, row, object.getClass().getName()));
				}

				/** See if we have already identified the fields for this kind of 
				 * object.*/
				List<Field> fields = classFields.get(object.getClass().getName());
				if (fields == null) {
					/** Identify the fields for this kind of object and cache them. */
					fields = new ArrayList<Field>();
					getFields(object.getClass(), fields, endpoint);
					classFields.put(object.getClass().getName(), fields);
				}

				/** Go through all fields we need to insert for this kind of object. */
				for (Field field : fields) {

					/** Create row 0 header entry if needed, i.e. if this field is not known. */
					if (columns.containsKey(field.getName()) == false) {
						columns.put(field.getName(), sheet.getColumns());
						sheet.addCell(new Label(sheet.getColumns(), 0, field.getName()));
					}

					/** Ensure that we can access the field. */
					field.setAccessible(true);

					insertCell(field, sheet, columns.get(field.getName()), row, object);
				} 
			}
			catch (Exception e) {
				e.printStackTrace();
			}				

			row++;
		}		
	}

	protected void insertCell(Field field, WritableSheet sheet, int column, int row, Object object) throws RowsExceededException, WriteException, IllegalArgumentException, IllegalAccessException {

		/** Dates are weird and must be handled explicitly. */
		if (field.getType().getName().equals(Date.class.getName())) {
			sheet.addCell(new DateTime(column, row, (Date) field.get(object)));								
		}
		else if (field.getType().getName().equals(URL.class.getName())) {
			sheet.addHyperlink(new WritableHyperlink(column, row, (URL) field.get(object)));
		}
		else {
			sheet.addCell(new Label(column, row, field.get(object).toString()));
		}
	}

	protected List<Field> getFields(Class object, List<Field> fields, ExcellEndpoint endpoint) {

		/** If a specific subset of fields have been configured, then 
		 * only take these. */
		if (endpoint.getFieldNames() != null) {
			for (String fieldName : endpoint.getFieldNames().keySet()) {
				try {
					fields.add(object.getClass().getField(fieldName));
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					LOG.error("Field with name '" + fieldName + "' does not exist for object of class '" + object.getClass().getName() + "'.");
					e.printStackTrace();
				}
			}
		}
		/** Else take all fields.*/
		else {
			recursivlyGetFields(object, fields);
		}

		return fields;
	}

	protected List<Field> recursivlyGetFields(Class object, List<Field> fields) {
		/** Add all fields of this class. */
		for (Field field : object.getDeclaredFields()) {
			fields.add(field);
		}

		/** Recursivly get super class fields. */
		if (object.getSuperclass() != null) {
			recursivlyGetFields(object.getSuperclass(), fields);
		}

		return fields; 
	}
}
