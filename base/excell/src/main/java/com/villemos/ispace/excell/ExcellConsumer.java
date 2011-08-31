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
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.Sheet;
import jxl.Workbook;

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
		File input = new File(endpoint.getFile());

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

		List<Object> dataList = new ArrayList<Object>();

		try {
			Workbook workbook = Workbook.getWorkbook(input);

			for (String sheetName : workbook.getSheetNames()) {

				if (sheetName.equals("metadata")) {
					continue;
				}

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

					Class cls = Class.forName(className);
					Object instance = cls.newInstance();

					column = 1;
					while (column < bodySheet.getColumns()) {
						try {
							Field field = instance.getClass().getField(fields.get(column));
							field.setAccessible(true);

							if (field.get(instance) instanceof List) {

								/** Detect method to use to set the value. */
								Type type = field.getGenericType();
								ParameterizedType pt = (ParameterizedType) type;

								/** Find the method. */
								Type listType = pt.getActualTypeArguments()[0];

								try {

									for (String valueStr : bodySheet.getCell(column, row).getContents().split("\\|\\|")) {
										Method add = null;
										Object value = null;
										if (listType.toString().contains(String.class.getName())) {
											value = valueStr;
											add = List.class.getDeclaredMethod("add", Object.class);
										}
										else if (listType.getClass().isAssignableFrom(Long.class)) {
											value = Long.parseLong(valueStr);
											add = List.class.getDeclaredMethod("add", Long.class);
										}
										else if (listType.getClass().isAssignableFrom(Float.class)) {
											value = Float.parseFloat(valueStr);
											add = List.class.getDeclaredMethod("add", Float.class);
										}
										else if (listType.getClass().isAssignableFrom(Integer.class)) {
											value = Integer.parseInt(valueStr);
											add = List.class.getDeclaredMethod("add", Integer.class);
										}
										else if (listType.getClass().isAssignableFrom(Double.class)) {
											value = Double.parseDouble(valueStr);
											add = List.class.getDeclaredMethod("add", Double.class);
										}
										else {

										}

										add.invoke(field.get(instance), value);
									}
								}
								catch(Exception e) {
									e.printStackTrace();
								}
							}
							else if (field.get(instance) instanceof Map) {
								/** Detect method to use to set the value. */
								Type type = field.getGenericType();
								ParameterizedType pt = (ParameterizedType) type;

								/** Find the method. */
								Type listType = pt.getActualTypeArguments()[1];

								try {
									String[] elements = bodySheet.getCell(column, row).getContents().split("=");
									String key = elements[0];
									String valueStr = elements[1]; 									

									Method put = null;
									Object value = null;
									
									Class params[] = new Class[2];
									params[0] = String.class;

									String valueElements[] = valueStr.split("\\|\\|"); 
									if (valueElements.length > 1) {
										value = new ArrayList<String>();
										for (String element : valueElements) {
											((List)value).add(element);
										}
										params[0] = Object.class;
										params[1] = Object.class;
										put = Map.class.getDeclaredMethod("put", params);

									}
									else if (listType.toString().contains(String.class.getName())) {
										value = valueStr;
										params[1] = String.class;
										put = List.class.getDeclaredMethod("put", params);
									}
									else if (listType.getClass().isAssignableFrom(Long.class)) {
										value = Long.parseLong(valueStr);
										params[1] = Long.class;
										put = List.class.getDeclaredMethod("put", params);
									}
									else if (listType.getClass().isAssignableFrom(Float.class)) {
										value = Float.parseFloat(valueStr);
										params[1] = Float.class;
										put = List.class.getDeclaredMethod("put", params);
									}
									else if (listType.getClass().isAssignableFrom(Integer.class)) {
										value = Integer.parseInt(valueStr);
										params[1] = Integer.class;
										put = List.class.getDeclaredMethod("put", params);
									}
									else if (listType.getClass().isAssignableFrom(Double.class)) {
										value = Double.parseDouble(valueStr);
										params[1] = Double.class;
										put = List.class.getDeclaredMethod("put", params);
									}
									else {

									}

									Object keyValue[] = new Object[2];
									keyValue[0] = key;
									keyValue[1] = value;
									put.invoke(field.get(instance), keyValue);
								}
								catch(Exception e) {
									e.printStackTrace();
								}								
							}
							else {
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
								else {

								}
							}
						}
						catch(Exception e) {
							/** Exceptions will be thrown if this object doesnt have the field 
							 * name. Catch and ignore.*/
						}

						column++;
					}

					dataList.add(instance);

					row++;

				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		if (endpoint.isStream()) {

			for (Object body : dataList) {
				Exchange newExchange = getEndpoint().createExchange();
				newExchange.getIn().setBody(body);

				getAsyncProcessor().process(newExchange, new AsyncCallback() {
					public void done(boolean doneSync) {
						LOG.trace("Done processing URL");
					}
				});
			}

		}
		else {
			Exchange newExchange = getEndpoint().createExchange();
			newExchange.getIn().setBody(dataList);

			getAsyncProcessor().process(newExchange, new AsyncCallback() {
				public void done(boolean doneSync) {
					LOG.trace("Done processing URL");
				}
			});
		}
		return 1;
	}
}
