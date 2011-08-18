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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jxl.Workbook;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The HelloWorld producer.
 */
public class ExcellProducer extends DefaultProducer {

	private static final transient Logger LOG = LoggerFactory.getLogger(ExcellProducer.class);

	private ExcellEndpoint endpoint;

	protected WritableFont boldYellowFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD); 	
	protected WritableCellFormat boldYellow = new WritableCellFormat(boldYellowFont);
	{
		try {
			boldYellow.setBackground(Colour.YELLOW);
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}

	public ExcellProducer(ExcellEndpoint endpoint) {
		super(endpoint);
		this.endpoint = endpoint;
	}

	public void process(Exchange exchange) throws Exception {


		File input = new File(endpoint.getFile());
		LOG.info("Creating Excell workbook '" + input.getAbsolutePath() + "'.");

		try {
			WritableWorkbook workbook = Workbook.createWorkbook(input);

			/** Write meta-data*/
			WritableSheet headerdata = workbook.createSheet(endpoint.getHeaderSheet(), 0);
			headerdata.addCell(new Label(0, 0, "Header Field", boldYellow));
			headerdata.addCell(new Label(1, 0, "Value", boldYellow));

			int row = 1;
			Iterator<Entry<String, Object>> it = exchange.getIn().getHeaders().entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Object> entry = it.next();

				headerdata.addCell(new Label(0, row, entry.getKey()));
				headerdata.addCell(new Label(1, row, entry.getValue().toString()));
				row++;
			}

			if (exchange.getIn().getBody() instanceof List) {
				List objects = (List) exchange.getIn().getBody();
				Object object = objects.get(0);
				writeSheet(workbook, object.getClass().getName(), 1, objects);
			}
			else if (exchange.getIn().getBody() instanceof Map) {

				int sheetNumber = 1;
				Iterator<Entry<String, List>> it2 = ((Map) exchange.getIn().getBody()).entrySet().iterator();
				while (it2.hasNext()) {
					Entry<String, List> entry = it2.next();
					writeSheet(workbook, entry.getKey(), sheetNumber, entry.getValue());
					sheetNumber++;
				}
			}

			workbook.write();
			workbook.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void writeSheet(WritableWorkbook workbook, String sheetName, int sheetNumber, List data) {

		if (data.isEmpty()) {
			return;
		}
		
		int row = 0;

		WritableSheet bodydata = workbook.createSheet(sheetName, 1);

		try{ 

			/** First column is the class name.*/
			bodydata.addCell(new Label(0, 0, "Class"));

			/** Create row 0 fields based on the first object. */
			Object object = data.get(0);

			if (object instanceof String) {
				int column = 1;
				bodydata.addCell(new Label(column, 0, "Value"));
				
				row = 1;
				for (Object anObject : data) {
					column = 0;
					bodydata.addCell(new Label(column, row, anObject.getClass().getName()));

					column++;
					bodydata.addCell(new Label(column, row, (String) anObject));

					row++;
				}				
			}
			else {
				int column = 1;
				for (Field field : getAllFields(object.getClass())) {
					bodydata.addCell(new Label(column, 0, field.getName()));
					column++;
				}

				/** Use reflection to detect body type. */
				row = 1;
				for (Object anObject : data) {
					column = 0;
					bodydata.addCell(new Label(column, row, object.getClass().getName()));

					column++;
					for (Field field : getAllFields(anObject.getClass())) {
						field.setAccessible(true);
						bodydata.addCell(new Label(column, row, field.get(anObject).toString()));
						column++;
					}

					row++;
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	protected Field[] getAllFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<Field>();
		fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		if (clazz.getSuperclass() != null) {
			fields.addAll(Arrays.asList(getAllFields(clazz.getSuperclass())));
		}
		return fields.toArray(new Field[] {});
	}
}
