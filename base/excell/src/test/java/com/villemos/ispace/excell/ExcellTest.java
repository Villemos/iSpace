package com.villemos.ispace.excell;


import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
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

		Map<String, List<TestClass>> objects = new HashMap<String, List<TestClass>>();

		List<TestClass> sheet1objects = new ArrayList<TestClass>();
		sheet1objects.add(new TestClass("string1",new Date(1), 1, 1, 1, false));
		sheet1objects.add(new TestClass("string2",new Date(2), 2, 2, 2, false));
		sheet1objects.add(new TestClass("string3",new Date(3), 3, 3, 3, false));
		sheet1objects.add(new TestClass("string4",new Date(4), 4, 4, 4, false));

		List<TestClass> sheet2objects = new ArrayList<TestClass>();
		sheet2objects.add(new TestClass("string5",new Date(5), 5, 5, 5, false));
		sheet2objects.add(new TestClass("string6",new Date(6), 6, 6, 6, false));
		sheet2objects.add(new TestClass("string7",new Date(7), 7, 7, 7, false));
		sheet2objects.add(new TestClass("string8",new Date(8), 8, 8, 8, false));

		objects.put("Sheet1", sheet1objects);
		objects.put("Sheet2", sheet2objects);



		/** Insert single object. */
		Exchange exchange = new DefaultExchange(context);
		exchange.getIn().setHeader("filename", "test.xls");
		exchange.getIn().setBody(sheet1objects.get(0));		
		storeDefaultRoute.send(exchange);

		/** Check the file. */
		try {
			File file = new File("test.xls");
			Workbook workbook = Workbook.getWorkbook(file);

			assertTrue(workbook != null);
			assertTrue(workbook.getSheet("data") != null);

			/** Check that the headers have been set as expected. */
			assertTrue(workbook.getSheet("data").getCell(0, 0).getContents().equals("Class"));
			assertTrue(workbook.getSheet("data").getCell(0, 1).getContents().equals(TestClass.class.getName()));
			
			int column = 1;
			for (Field field : TestClass.class.getFields()) {
				assertTrue(workbook.getSheet("data").getCell(column, 0).getContents().equals(field.getName()));

				if (workbook.getSheet("data").getCell(column, 1).getType().toString().equals("Date")) {
					// assertTrue(workbook.getSheet("data").getCell(column, 1).getContents().equals(field.get(sheet1objects.get(0)).toString()));					
				}
				else {
					assertTrue(workbook.getSheet("data").getCell(column, 1).getContents().equals(field.get(sheet1objects.get(0)).toString()));
				}

				column++;
			}

			workbook.close();
			boolean result = file.delete();
			assertTrue(result);
		} catch (Exception e) {
			e.printStackTrace();
		}



		/** Insert List object. */
		exchange = new DefaultExchange(context);
		exchange.getIn().setHeader("filename", "test.xls");
		exchange.getIn().setBody(sheet1objects);		
		storeDefaultRoute.send(exchange);

		/** Check the file. */
		try {
			File file = new File("test.xls");
			Workbook workbook = Workbook.getWorkbook(file);

			assertTrue(workbook != null);
			assertTrue(workbook.getSheet("data") != null);

			/** Check that the headers have been set as expected. */
			assertTrue(workbook.getSheet("data").getCell(0, 0).getContents().equals("Class"));

			int column = 1;
			for (Field field : TestClass.class.getFields()) {
				assertTrue(workbook.getSheet("data").getCell(column, 0).getContents().equals(field.getName()));

				int row = 1;
				for (TestClass object : sheet1objects) {
					assertTrue(workbook.getSheet("data").getCell(0, row).getContents().equals(TestClass.class.getName()));
					if (workbook.getSheet("data").getCell(column, row).getType().toString().equals("Date")) {
						// assertTrue(workbook.getSheet("data").getCell(column, 1).getContents().equals(field.get(sheet1objects.get(0)).toString()));					
					}
					else {
						assertTrue(workbook.getSheet("data").getCell(column, row).getContents().equals(field.get(object).toString()));
					}
					row++;
				}

				column++;
			}

			workbook.close();
			boolean result = file.delete();
			assertTrue(result);
		} catch (Exception e) {
			e.printStackTrace();
		}


		/** Insert Map object. */
		exchange = new DefaultExchange(context);
		exchange.getIn().setHeader("filename", "test.xls");
		exchange.getIn().setBody(objects);		
		storeDefaultRoute.send(exchange);

		/** Check the file. */
		try {
			File file = new File("test.xls");
			Workbook workbook = Workbook.getWorkbook(file);

			assertTrue(workbook != null);
			assertTrue(workbook.getSheet("Sheet1") != null);
			assertTrue(workbook.getSheet("Sheet2") != null);
			
			/** Check that the headers have been set as expected. */
			assertTrue(workbook.getSheet("Sheet1").getCell(0, 0).getContents().equals("Class"));
			int column = 1;
			for (Field field : TestClass.class.getFields()) {
				assertTrue(workbook.getSheet("Sheet1").getCell(column, 0).getContents().equals(field.getName()));

				int row = 1;
				for (TestClass object : sheet1objects) {
					assertTrue(workbook.getSheet("Sheet1").getCell(0, row).getContents().equals(TestClass.class.getName()));
					if (workbook.getSheet("Sheet1").getCell(column, row).getType().toString().equals("Date")) {
						// assertTrue(workbook.getSheet("data").getCell(column, 1).getContents().equals(field.get(sheet1objects.get(0)).toString()));					
					}
					else {
						assertTrue(workbook.getSheet("Sheet1").getCell(column, row).getContents().equals(field.get(object).toString()));
					}
					row++;
				}

				column++;
			}

			/** Check that the headers have been set as expected. */
			assertTrue(workbook.getSheet("Sheet2").getCell(0, 0).getContents().equals("Class"));
			column = 1;
			for (Field field : TestClass.class.getFields()) {
				assertTrue(workbook.getSheet("Sheet2").getCell(column, 0).getContents().equals(field.getName()));

				int row = 1;
				for (TestClass object : sheet2objects) {
					assertTrue(workbook.getSheet("Sheet2").getCell(0, row).getContents().equals(TestClass.class.getName()));
					if (workbook.getSheet("Sheet2").getCell(column, row).getType().toString().equals("Date")) {
						// assertTrue(workbook.getSheet("data").getCell(column, 1).getContents().equals(field.get(sheet1objects.get(0)).toString()));					
					}
					else {
						assertTrue(workbook.getSheet("Sheet2").getCell(column, row).getContents().equals(field.get(object).toString()));
					}
					row++;
				}

				column++;
			}

			workbook.close();
			boolean result = file.delete();
			assertTrue(result);
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	@DirtiesContext
	@Test
	public void testStreamInsert() {

	}	
}
