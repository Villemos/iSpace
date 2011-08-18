/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.villemos.ispace.excell;

import java.io.File;
import java.lang.reflect.Field;
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

	public ExcellConsumer(ExcellEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
		this.endpoint = endpoint;
	}

	@Override
	protected int poll() throws Exception {

		Map<String, List<Object>> data = new HashMap<String, List<Object>>();

		Exchange exchange = getEndpoint().createExchange();

		/** Read input file. */
		File input = new File(endpoint.getFile());
		try {
			Workbook workbook = Workbook.getWorkbook(input);

			for (String sheetName : workbook.getSheetNames()) {

				List<Object> dataList = new ArrayList<Object>();

				if (sheetName.equals("header")) {
					continue;
				}

				Sheet bodySheet = workbook.getSheet(sheetName);

				/** Read the class name. */
				String className = bodySheet.getCell(0, 1).getContents().toString();

				if (className.contains("java.lang.String") == false) {

					/** Get the class specification of the class to use. May be a specialized class. */

					/** Read the sheet headers. */
					Map<Integer, String> fields = new HashMap<Integer, String>();
					int column = 1;
					while (column < bodySheet.getColumns()) {
						fields.put(column, bodySheet.getCell(column, 0).getContents().toString());	
						column++;
					}				

					int row = 1;
					while (row < bodySheet.getRows()) {
						Class cls = Class.forName(className);
						Object instance = cls.newInstance();
						column = 1;
						while (column < bodySheet.getColumns()) {
							Field field = instance.getClass().getField(fields.get(column));
							field.setAccessible(true);
							field.set(instance, bodySheet.getCell(column, row).getContents());
							column++;
						}

						dataList.add(instance);

						row++;
					}
				}
				else {
					int row = 1;
					while (row < bodySheet.getRows()) {
						if (bodySheet.getCell(0, row).getContents().equals("")) {
							break;
						}

						dataList.add(bodySheet.getCell(1, row).getContents().toString());

						row++;
					}
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
					data.put(sheetName, dataList);
				}
			}

			if (endpoint.isStream() == false) {
				exchange.getIn().setBody(data);

				getAsyncProcessor().process(exchange, new AsyncCallback() {
					public void done(boolean doneSync) {
						LOG.trace("Done processing URL");
					}
				});
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return 1;
	}
}
