package com.villemos.ispace.excell;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

import jxl.Cell;
import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;

public class ExcellFileConsumer {

	private static final transient Logger LOG = LoggerFactory.getLogger(ExcellFileConsumer.class);

	private final ExcellEndpoint endpoint;
	
	protected XStream xstream = new XStream();

	public ExcellFileConsumer(ExcellEndpoint endpoint) {
		this.endpoint = endpoint;
	}
	
	public void consume(Exchange exchange) {
		
		/** Read input file. */
		File input = new File(endpoint.getFilename());

		if (input.exists() == false) {
			LOG.warn("File '" + input.getAbsolutePath() + "' does not exist.");
			return;
		}

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
					String fieldName = bodySheet.getCell(column, 0).getContents().toString();

					/** A field map may have been defined on the endpoint. This can be used to map
					 * a logical column header name 'The Title' to a class field name 'title'. */
					if (endpoint.getFieldNames() != null) {
						if (endpoint.getFieldNames().containsKey(fieldName)) {
							fieldName = endpoint.getFieldNames().get(fieldName);
						}
					}

					fields.put(column, fieldName);	
					column++;
				}				

				/** A starting row may have been set. */
				int row = endpoint.getStartRow() == -1 ? 1 : endpoint.getStartRow();

				int endRow = endpoint.getEndRow() == -1 ? bodySheet.getRows() : 1;

				while (row < endRow) {

					/** The class name has either been configured on the Endpoint or should
					 * be a column in the sheet. */
					String className = endpoint.getClassName();

					if (className == null) {
						/** Read the class name. */
						className = bodySheet.getCell(endpoint.getClassColumn(), row).getContents().toString();
					}

					try {
						Class cls = Class.forName(className);
						Object instance = cls.newInstance();

						column = 1;
						while (column < bodySheet.getColumns()) {
							try {
								Field field = instance.getClass().getField(fields.get(column));
								field.setAccessible(true);

								//								if (field.getType().isAssignableFrom(String.class)) {
								//									field.set(instance, bodySheet.getCell(column, row).getContents());								
								//								}
								if (field.getType().isAssignableFrom(Long.class)) {
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

									if (cell.getContents().equals("No date")) {
										continue;
									}

									if (cell.getClass().getName().equals("jxl.read.biff.DateRecord")) {
										field.set(instance, ((DateCell) cell).getDate());
									}
									else if (endpoint.getDateFormat() != null) {
										SimpleDateFormat format = new SimpleDateFormat(endpoint.getDateFormat(), Locale.UK);
										field.set(instance, format.parse(cell.getContents()));
									}
									else {
										/** Hope that this is a string representing a long. */
										field.set(instance, new Date(Long.parseLong(cell.getContents())));
									}
								}
								else {
									if (endpoint.getDefaultEncoding().equals("string")) {
										field.set(instance, bodySheet.getCell(column, row).getContents());
									}
									else {
										field.set(instance, xstream.fromXML(bodySheet.getCell(column, row).getContents()));
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

		exchange.getIn().setBody(data);
	}
}
