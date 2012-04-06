package com.villemos.ispace.excell;


import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


@ContextConfiguration (locations={"NamingTest-context.xml"})
public class NamingTest extends AbstractJUnit4SpringContextTests  {

	class DataObject {
		public String value1 = null;
		public String value2 = null;
		public DataObject(String value1, String value2) {
			super();
			this.value1 = value1;
			this.value2 = value2;
		}
	}
	
	@Autowired
	protected CamelContext context = null;

	@Autowired
	protected ProducerTemplate producer = null;

	@Produce(uri = "direct:startSimple")
	protected ProducerTemplate startSimple = null;

	@Produce(uri = "direct:startTemplate")
	protected ProducerTemplate startTemplate = null;

	protected Map<String, List<DataObject>> documentList1 = new HashMap<String, List<DataObject>>();
	{
		List<DataObject> list = new ArrayList<DataObject>();
		list.add(new DataObject("a1", "a2"));
		list.add(new DataObject("b1", "b2"));
		list.add(new DataObject("c1", "c2"));
		list.add(new DataObject("d1", "d2"));
		list.add(new DataObject("e1", "e2"));
		
		documentList1.put("Test", list);
	} 
	
	
	protected void checkFileExist(String filename) {
		File file = new File(filename);
		assertTrue(file.exists());
		file.delete();		
	}

	protected void checkFileExistPattern(String filenamePattern) {
		File dir = new File("./tmp");
		assertTrue(dir.exists());

		/** template_1_2012-03-13-23-13-11.xls template_(\d)_\d{4}-\d{2}-\d{2}-\d{2}-\d{2}-\d{2}.xls */
		boolean found = false;
		for (File file : dir.listFiles()) {
			if (file.getName().matches(filenamePattern)) {
				found = true;
				file.delete();
			}
			else {
				file.delete();
			}
		}
		
		assertTrue(found);
	}

	@DirtiesContext
	@Test
	public void testSimpleName() {
		startSimple.send(createExchange());
		checkFileExist("./tmp/unformatted.xls");

		Exchange exchange = createExchange();
		exchange.getIn().setHeader("excellfilename", "./tmp/newname.xls");
		startSimple.send(exchange);
		checkFileExist("./tmp/newname.xls");

	}	
	
	@DirtiesContext
	@Test
	public void testTemplateName() {

		/** Create file with default ID and Timestamp*/
		startTemplate.send(createExchange());
		checkFileExistPattern("template_1_\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}.xls");
		
		/** Send ID with exchange. */
		Exchange exchange = createExchange();
		exchange.getIn().setHeader("excellfilenameId", "2");
		startTemplate.send(exchange);
		checkFileExistPattern("template_2_\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}.xls");

		exchange.getIn().setHeader("excellfilenameId", "3");
		startTemplate.send(exchange);
		checkFileExistPattern("template_3_\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}.xls");

	}
	
	
	private Exchange createExchange() {
		Exchange exchange = new DefaultExchange(context);
		exchange.getIn().setBody(documentList1);
		return exchange;
	}

}
