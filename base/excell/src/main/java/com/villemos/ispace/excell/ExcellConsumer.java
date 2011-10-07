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

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jxl.Cell;
import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.DateTime;
import jxl.write.biff.DateRecord;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The HelloWorld consumer.
 */
public class ExcellConsumer extends ScheduledPollConsumer {

	private static final transient Logger LOG = LoggerFactory.getLogger(ExcellConsumer.class);

	private final ExcellEndpoint endpoint;

	protected long lastPollTime = 0;

	public ExcellConsumer(ExcellEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
		this.endpoint = endpoint;
	}

	@Override
	protected int poll() throws Exception {

		/** Read input file. */
		File input = new File(endpoint.getFilename());

		if (input.exists() == false) {
			LOG.warn("File '" + input.getAbsolutePath() + "' does not exist.");
			return 0;
		}

		/** Did it change since last time?*/
		if (input.lastModified() == lastPollTime) {
			LOG.trace("Excell crawler ignoring input file. File has not changed.");
			return 1;
		}
		lastPollTime = input.lastModified();

		Map<String, List<Object>> data = new TreeMap<String, List<Object>>();
		
		try {
			Workbook workbook = Workbook.getWorkbook(input);

			for (String sheetName : workbook.getSheetNames()) {
				List<Object> dataList = new ArrayList<Object>();
				
				Sheet bodySheet = workbook.getSheet(sheetName);

				/** Read the sheet headers. */
				Map<Integer, String> fields = new HashMap<Integer, String>();
				int column = 1;
				while (column < bodySheet.getColumns()) {
					fields.put(column, bodySheet.getCell(column, 0).getContents().toString());	
					column++;
				}				

				/** Go through all rows. */
				int row = 1;
				while (row < bodySheet.getRows()) {

					/** Read the class name. */
					String className = bodySheet.getCell(0, 1).getContents().toString();

					try {
						Class cls = Class.forName(className);
						Object instance = cls.newInstance();

						column = 1;
						while (column < bodySheet.getColumns()) {
							try {
								Field field = instance.getClass().getField(fields.get(column));
								field.setAccessible(true);

								if (field.getType().isAssignableFrom(String.class)) {
									field.set(instance, bodySheet.getCell(column, row).getContents());								
								}
								else if (field.getType().isAssignableFrom(Long.class)) {
									field.set(instance, Long.parseLong(bodySheet.getCell(column, row).getContents()));
								}
								else if (field.getType().isAssignableFrom(Float.class)) {
									field.set(instance, Float.parseFloat(bodySheet.getCell(column, row).getContents()));
								}
								else if (field.getType().isAssignableFrom(Integer.class)) {
									field.set(instance, Integer.parseInt(bodySheet.getCell(column, row).getContents()));
								}
								else if (field.getType().isAssignableFrom(Double.class)) {
									field.set(instance, Double.parseDouble(bodySheet.getCell(column, row).getContents()));
								}
								else if (field.getType().isAssignableFrom(URL.class)) {
									field.set(instance, new URL(bodySheet.getCell(column, row).getContents()));
								}								
								else if (field.getType().isAssignableFrom(Date.class)) {
									Cell cell = bodySheet.getCell(column, row);
									if (cell.getClass().getName().equals("jxl.read.biff.DateRecord")) {
										field.set(instance, ((DateCell) cell).getDate());
									}
									else if (endpoint.getDateFormat() != null) {
										SimpleDateFormat format = new SimpleDateFormat(endpoint.getDateFormat());
										field.set(instance, format.parse(cell.getContents()));
									}
									else {
										/** Hope that this is a string representing a long. */
										field.set(instance, new Date(Long.parseLong(cell.getContents())));
									}
								}

								else {

								}
							}
							catch(Exception e) {
								/** Exceptions will be thrown if this object doesnt have the field 
								 * name. Catch and ignore.*/
							}

							column++;
						}

						dataList.add(instance);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					row++;
				}
				
				data.put(sheetName, dataList);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		if (endpoint.isStream()) {

//			for (Object body : dataList) {
//				Exchange newExchange = getEndpoint().createExchange();
//				newExchange.getIn().setBody(body);
//
//				getAsyncProcessor().process(newExchange, new AsyncCallback() {
//					public void done(boolean doneSync) {
//						LOG.trace("Done processing URL");
//					}
//				});
//			}

		}
		else {
			Exchange newExchange = getEndpoint().createExchange();
			newExchange.getIn().setBody(data);

			getAsyncProcessor().process(newExchange, new AsyncCallback() {
				public void done(boolean doneSync) {
					LOG.trace("Done processing URL");
				}
			});
		}
		return 1;
	}
}
