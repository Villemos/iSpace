package com.villemos.ispace.excell;


import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.Workbook;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;


@ContextConfiguration (locations={"ExcellTest-context.xml"})
public class ExcellTest extends AbstractJUnit4SpringContextTests  {

	@Autowired
	protected CamelContext context = null;

	@Autowired
	protected ProducerTemplate producer = null;

	@Produce(uri = "direct:store")
	protected ProducerTemplate storeDefaultRoute = null;

	@Produce(uri = "direct:retrieve")
	protected ProducerTemplate retrieveRoute = null;


	@DirtiesContext
	@Test
	public void testDefaultInsert() {

		Map<String, List<Object>> objects = new HashMap<String, List<Object>>();

		List<Object> sheet1objects = new ArrayList<Object>();
		sheet1objects.add(new TestClass("string1",new Date(1), 1, 1, 1, false));
		sheet1objects.add(new TestClass("string2",new Date(2), 2, 2, 2, false));
		sheet1objects.add(new TestClass("string3",new Date(3), 3, 3, 3, false));
		sheet1objects.add(new TestClass("string4",new Date(4), 4, 4, 4, false));

		List<Object> sheet2objects = new ArrayList<Object>();
		sheet2objects.add(new TestClass("string5",new Date(5), 5, 5, 5, false));
		sheet2objects.add(new TestClass("string6",new Date(6), 6, 6, 6, false));
		sheet2objects.add(new TestClass("string7",new Date(7), 7, 7, 7, false));
		sheet2objects.add(new TestClass("string8",new Date(8), 8, 8, 8, false));

		objects.put("Sheet1", sheet1objects);
		objects.put("Sheet2", sheet2objects);


		/** Insert single object. */
		
		/** Clean up the file. */
		String filename = "test-single-insert.xls";
		cleanFile("test-single-insert.xls");

		sendData(filename, sheet1objects.get(0));

		/** Check the file. */
		try {
			File file = new File(filename);
			assertTrue(file.exists());
			Workbook workbook = Workbook.getWorkbook(file);

			checkData(workbook, "data", Arrays.asList(new Object[] {sheet1objects.get(0)}));

			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
		}



		/** Insert List object. */
		filename = "test-list-insert.xls";
		cleanFile(filename);
		
		sendData(filename, sheet1objects);

		/** Check the file. */
		try {
			File file = new File(filename);
			assertTrue(file.exists());
			Workbook workbook = Workbook.getWorkbook(file);

			checkData(workbook, "data", sheet1objects);

			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
		}


		/** Insert Map object. */
		filename = "test-map-insert.xls"; 
		cleanFile(filename);
		
		sendData(filename, objects);
		
		/** Check the file. */
		try {
			File file = new File(filename);
			assertTrue(file.exists());
			Workbook workbook = Workbook.getWorkbook(file);

			checkData(workbook, "Sheet1", sheet1objects);
			checkData(workbook, "Sheet2", sheet2objects);

			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		/** Send a list with a mix of objects. */
		sheet1objects.add(new TestClassSpecialization("string9", new Date(9), 9, 9, 9, true, "StringNew0"));
		sheet1objects.add(new TestClassUnrelated("StringNewUnrelated", 10));

		/** Insert new List object. */
		filename = "test-list-insert-mixed.xls"; 
		cleanFile(filename);
		
		sendData(filename, objects);
		
		/** Check the file. */
		try {
			File file = new File(filename);
			assertTrue(file.exists());
			Workbook workbook = Workbook.getWorkbook(file);

			// checkData(workbook, "data", sheet1objects);

			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void sendData(String filename, Object data) {
		Exchange exchange = new DefaultExchange(context);
		exchange.getIn().setHeader("filename", filename);
		exchange.getIn().setBody(data);		
		storeDefaultRoute.send(exchange);
	}

	private void checkData(Workbook workbook, String sheet, List<Object> asList) throws IllegalArgumentException, IllegalAccessException {
		
		assertTrue(workbook != null);
		assertTrue(workbook.getSheet(sheet) != null);

		int column = 1;
		for (Field field : TestClass.class.getFields()) {
			assertTrue(workbook.getSheet(sheet).getCell(column, 0).getContents().equals(field.getName()));

			int row = 1;
			for (Object object : asList) {
				assertTrue(workbook.getSheet(sheet).getCell(0, row).getContents().equals(TestClass.class.getName()));
				if (workbook.getSheet(sheet).getCell(column, row).getType().toString().equals("Date")) {
					// assertTrue(workbook.getSheet("data").getCell(column, 1).getContents().equals(field.get(sheet1objects.get(0)).toString()));					
				}
				else {
					assertTrue(workbook.getSheet(sheet).getCell(column, row).getContents().equals(field.get(object).toString()));
				}
				row++;
			}

			column++;
		}
	}

	protected void cleanFile(String name) {
		File file = new File(name);
		if (file.exists()) {
			file.delete();
		}
	}
	
	@DirtiesContext
	@Test
	public void testStreamInsert() {

	}	
}
