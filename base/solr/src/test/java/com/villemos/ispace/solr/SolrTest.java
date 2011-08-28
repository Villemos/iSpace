package com.villemos.ispace.solr;

import java.util.Date;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests;

import com.villemos.ispace.api.BufferedCallbackClient;
import com.villemos.ispace.api.InformationObject;
import com.villemos.ispace.api.Options;
import com.villemos.ispace.api.ResultSet;

@ContextConfiguration (locations={"SolrTest-context.xml"})
public class SolrTest extends AbstractJUnit38SpringContextTests  {

	@Autowired
	protected CamelContext context = null;

	@Autowired
	protected ProducerTemplate producer = null;

	@Produce(uri = "direct:store")
	protected ProducerTemplate storeRoute = null;

	@Produce(uri = "direct:retrieve")
	protected ProducerTemplate retrieveRoute = null;

	@DirtiesContext
	@Test
	public void testSolrAccess() {

		/** Test deletion. */
		Exchange exchange = new DefaultExchange(context);
		exchange.getIn().getHeaders().put(Options.delete, null);
		exchange.getIn().getHeaders().put(Options.query, "*:*");
		exchange.getIn().getHeaders().put(Options.commit, null);
		storeRoute.send(exchange);		

		/** Ensure the local archive is empty. */
		Exchange countExchange = new DefaultExchange(context);
		countExchange.getIn().getHeaders().put(Options.count, "");
		countExchange.getIn().getHeaders().put(Options.query, "*:*");
		storeRoute.send(countExchange);
		assertTrue((Long) countExchange.getOut().getHeaders().get(Options.count) == 0);


		/** Inject a IO object. */
		InformationObject io = new InformationObject();
		io.hasUri = "testUriOne";
		io.hasTitle = "1.testTitle";
		io.isAttachedTo.add("1.testAttachedTo.1");   
		io.isAttachedTo.add("1.testAttachedTo.2");
		io.fromSource = "1.testSource";
		io.ofMimeType = "1.testMimetype";
		io.ofEntityType = "1.testEntitytype";
		io.withReferenceId = "1.testReferenceId";
		io.withIssue = "1";
		io.withRevision = "0";
		io.isPartOf = "1.testPartof";   
		io.hasPart.add("1.testPart.1");
		io.hasPart.add("1.testPart.2");
		io.withRawText = "This is a test text KEYWORDONE";
		io.withAttachedLog.add("1.testLog.1");
		io.withAttachedLog.add("1.testLog.2");

		io.dynamic.put("1.testDynamicField1", "testDynamicFieldValue.1");
		io.dynamic.put("1.testDynamicField1", "testDynamicFieldValue.2");
		io.dynamic.put("1.testDynamicField2", "testDynamicFieldValue.1");

		exchange = new DefaultExchange(context);
		exchange.getIn().setBody(io);
		exchange.getIn().getHeaders().put(Options.commit, null);
		storeRoute.send(exchange);

		/** Inject a second one. */
		io = new InformationObject();
		io.hasUri = "testUriTwo";
		io.hasTitle = "2.testTitle";
		io.isAttachedTo.add("2.testAttachedTo.1");   
		io.isAttachedTo.add("2.testAttachedTo.2");
		io.fromSource = "2.testSource";
		io.ofMimeType = "2.testMimetype";
		io.ofEntityType = "2.testEntitytype";
		io.withReferenceId = "2.testReferenceId";
		io.withIssue = "1";
		io.withRevision = "0";
		io.isPartOf = "2.testPartof";   
		io.hasPart.add("2.testPart.1");
		io.hasPart.add("2.testPart.2");
		io.withRawText = "This is a test text KEYWORDTWO";
		io.withAttachedLog.add("2.testLog.1");
		io.withAttachedLog.add("2.testLog.2");

		io.dynamic.put("1.testDynamicField1", "testDynamicFieldValue.1");
		io.dynamic.put("1.testDynamicField1", "testDynamicFieldValue.2");
		io.dynamic.put("2.testDynamicField2", "testDynamicFieldValue.1");

		exchange = new DefaultExchange(context);
		exchange.getIn().setBody(io);
		exchange.getIn().getHeaders().put(Options.commit, null);
		storeRoute.send(exchange);

		/** Insert a comment. */
		io = new InformationObject();
		io.hasUri = "testUriThree";
		io.hasTitle = "3.testTitle";
		io.isAttachedTo.add("testUriTwo");
		io.fromSource = "3.testSource";
		io.ofMimeType = "3.testMimetype";
		io.ofEntityType = "Comment";
		io.withReferenceId = "3.testReferenceId";
		io.withIssue = "1";
		io.withRevision = "0";
		io.isPartOf = "3.testPartof";   
		io.hasPart.add("3.testPart.1");
		io.hasPart.add("3.testPart.2");
		io.withRawText = "This is a test text KEYWORDTHREE";
		io.withAttachedLog.add("3.testLog.1");
		io.withAttachedLog.add("3.testLog.2");

		exchange = new DefaultExchange(context);
		exchange.getIn().setBody(io);
		exchange.getIn().getHeaders().put(Options.commit, null);
		storeRoute.send(exchange);


		/** Do a count search. */
		storeRoute.send(countExchange);
		assertTrue((Long) countExchange.getOut().getHeaders().get(Options.count) == 3);


		/** Search WITHOUT comments. */
		exchange = new DefaultExchange(context);
		exchange.getIn().getHeaders().put(Options.query, "KEYWORDTWO");
		storeRoute.send(exchange);
		assertTrue((Integer) exchange.getOut().getHeader(Options.count) == 1);
		assertTrue(((ResultSet) exchange.getOut().getBody()).informationobjects.size() == 1);
		assertTrue(((ResultSet) exchange.getOut().getBody()).informationobjects.get(0).comments.size() == 0);


		/** Search WITH comments. */
		exchange = new DefaultExchange(context);
		exchange.getIn().getHeaders().put(Options.query, "KEYWORDTWO");
		exchange.getIn().getHeaders().put(Options.comments, null);
		storeRoute.send(exchange);
		assertTrue((Integer) exchange.getOut().getHeader(Options.count) == 1);
		assertTrue(((ResultSet) exchange.getOut().getBody()).informationobjects.size() == 1);
		assertTrue(((ResultSet) exchange.getOut().getBody()).informationobjects.get(0).comments.size() == 1);


		/** Search WITH facets. */
		exchange = new DefaultExchange(context);
		exchange.getIn().getHeaders().put(Options.query, "KEYWORDTWO");
		exchange.getIn().getHeaders().put(Options.comments, null);
		exchange.getIn().getHeaders().put(Options.facets, null);
		storeRoute.send(exchange);
		assertTrue((Integer) exchange.getOut().getHeader(Options.count) == 1);
		assertTrue(((ResultSet) exchange.getOut().getBody()).informationobjects.size() == 1);
		assertTrue(((ResultSet) exchange.getOut().getBody()).informationobjects.get(0).comments.size() == 1);
		assertTrue(((ResultSet) exchange.getOut().getBody()).facets.size() > 0);

		/** Test deletion of a single IO. */
		exchange = new DefaultExchange(context);
		exchange.getIn().getHeaders().put(Options.delete, "testUriOne");
		exchange.getIn().getHeaders().put(Options.commit, null);
		storeRoute.send(exchange);

		/** Search WITH comments for teh document we just deleted, i.e. should not find anything. */
		exchange = new DefaultExchange(context);
		exchange.getIn().getHeaders().put(Options.query, "KEYWORDONE");
		exchange.getIn().getHeaders().put(Options.comments, null);
		exchange.getIn().getHeaders().put(Options.facets, null);
		storeRoute.send(exchange);
		assertTrue((Integer) exchange.getOut().getHeader(Options.count) == 0);
		assertTrue(((ResultSet) exchange.getOut().getBody()).informationobjects.size() == 0);
		assertTrue(((ResultSet) exchange.getOut().getBody()).facets.size() > 0);

		/** Test streaming. */
		BufferedCallbackClient client = new BufferedCallbackClient();

		/** Search WITHOUT comments. */
		exchange = new DefaultExchange(context);
		exchange.getIn().getHeaders().put(Options.query, "KEYWORDTWO");
		exchange.getIn().getHeaders().put(Options.stream, client);
		storeRoute.send(exchange);
		assertTrue(client.getInformationObjects().size() == 1);
		assertTrue(client.getInformationObjects().get(0).comments.size() == 0);

		client.clear();

		/** Search WITH comments. */
		exchange = new DefaultExchange(context);
		exchange.getIn().getHeaders().put(Options.query, "KEYWORDTWO");
		exchange.getIn().getHeaders().put(Options.comments, null);
		exchange.getIn().getHeaders().put(Options.stream, client);
		storeRoute.send(exchange);
		assertTrue(client.getInformationObjects().size() == 1);
		assertTrue(client.getInformationObjects().get(0).comments.size() == 1);

		client.clear();

		/** Search WITH facets. */
		exchange = new DefaultExchange(context);
		exchange.getIn().getHeaders().put(Options.query, "KEYWORDTWO");
		exchange.getIn().getHeaders().put(Options.comments, null);
		exchange.getIn().getHeaders().put(Options.facets, null);
		exchange.getIn().getHeaders().put(Options.stream, client);
		storeRoute.send(exchange);
		assertTrue(client.getInformationObjects().size() == 1);
		assertTrue(client.getInformationObjects().get(0).comments.size() == 1);
		assertTrue(client.getFacets().size() > 0);

		client.clear();

		/** Search WITH facets that should not return anything. */
		exchange = new DefaultExchange(context);
		exchange.getIn().getHeaders().put(Options.query, "KEYWORDONE");
		exchange.getIn().getHeaders().put(Options.comments, null);
		exchange.getIn().getHeaders().put(Options.facets, null);
		exchange.getIn().getHeaders().put(Options.stream, client);
		storeRoute.send(exchange);
		assertTrue(client.getInformationObjects().size() == 0);
		assertTrue(client.getFacets().size() > 0);

		client.clear();
	}

	@DirtiesContext
	@Test
	public void testSolrContinuedDelivery() {

		/** Test deletion. */
		Exchange exchange = new DefaultExchange(context);
		exchange.getIn().getHeaders().put(Options.delete, null);
		exchange.getIn().getHeaders().put(Options.query, "*:*");
		exchange.getIn().getHeaders().put(Options.commit, null);
		storeRoute.send(exchange);		

		/** Ensure the local archive is empty. */
		Exchange countExchange = new DefaultExchange(context);
		countExchange.getIn().getHeaders().put(Options.count, "");
		countExchange.getIn().getHeaders().put(Options.query, "*:*");
		storeRoute.send(countExchange);
		assertTrue((Long) countExchange.getOut().getHeaders().get(Options.count) == 0);


		/** Inject a high number of documents, without forcing a commit. */
		Date start = new Date();
		for (int count = 0; count < 2000; count++) {

			/** Inject a IO object. */
			InformationObject io = new InformationObject();
			io.hasUri = "testUri" + count;
			io.hasTitle = "1.testTitle";
			io.isAttachedTo.add("1.testAttachedTo.1");   
			io.isAttachedTo.add("1.testAttachedTo.2");
			io.fromSource = "1.testSource";
			io.ofMimeType = "1.testMimetype";
			io.ofEntityType = "1.testEntitytype";
			io.withReferenceId = "1.testReferenceId";
			io.withIssue = "1";
			io.withRevision = "0";
			io.isPartOf = "1.testPartof";   
			io.hasPart.add("1.testPart.1");
			io.hasPart.add("1.testPart.2");
			io.withRawText = "This is a test text KEYWORDONE";
			io.withAttachedLog.add("1.testLog.1");
			io.withAttachedLog.add("1.testLog.2");

			io.dynamic.put("1.testDynamicField1", "testDynamicFieldValue.1");
			io.dynamic.put("1.testDynamicField1", "testDynamicFieldValue.2");
			io.dynamic.put("1.testDynamicField2", "testDynamicFieldValue.1");

			exchange = new DefaultExchange(context);
			exchange.getIn().setBody(io);
			storeRoute.send(exchange);
		}

		/** Inject comments. */
		for (int count = 2000; count < 3000; count++) {

			/** Inject a IO object. */
			InformationObject io = new InformationObject();
			io.hasUri = "testUri" + count;
			io.hasTitle = "1.testTitle";   
			io.isAttachedTo.add("testUri" + (count - 2000));
			io.fromSource = "1.testSource";
			io.ofMimeType = "1.testMimetype";
			io.ofEntityType = "1.testEntitytype";
			io.withReferenceId = "1.testReferenceId";
			io.withIssue = "1";
			io.withRevision = "0";
			io.isPartOf = "1.testPartof";   
			io.hasPart.add("1.testPart.1");
			io.hasPart.add("1.testPart.2");
			io.withRawText = "This is a test text KEYWORDONE";
			io.withAttachedLog.add("1.testLog.1");
			io.withAttachedLog.add("1.testLog.2");

			io.dynamic.put("1.testDynamicField1", "testDynamicFieldValue.1");
			io.dynamic.put("1.testDynamicField1", "testDynamicFieldValue.2");
			io.dynamic.put("1.testDynamicField2", "testDynamicFieldValue.1");

			exchange = new DefaultExchange(context);
			exchange.getIn().setBody(io);
			storeRoute.send(exchange);
		}

		Date end = new Date();
		double duration = (double) (end.getTime() - start.getTime()) / 1000d;
		
		logger.info("Inserted 3000 entries in " + duration + " seconds. An average of " + 3000 / duration + " entries per second.");
		
		
		/** Force commit. */
		exchange = new DefaultExchange(context);
		exchange.getIn().getHeaders().put(Options.commit, null);
		storeRoute.send(exchange);

		start = new Date();
		
		/** Retrieve. */
		BufferedCallbackClient client = new BufferedCallbackClient();
		
		/** Search WITH facets, retrieving an EQUAL number of 100. */
		exchange = new DefaultExchange(context);
		exchange.getIn().getHeaders().put(Options.query, "test");
		exchange.getIn().getHeaders().put(Options.comments, null);
		exchange.getIn().getHeaders().put(Options.facets, null);
		exchange.getIn().getHeaders().put(Options.stream, client);
		exchange.getIn().getHeaders().put(Options.rows, 500);
		storeRoute.send(exchange);
		assertTrue(client.getInformationObjects().size() == 500);
		assertTrue(client.getFacets().size() > 0);

		end = new Date();
		duration = (double) (end.getTime() - start.getTime()) / 1000d;
		logger.info("Retrieved 500 entries in " + duration + " seconds. An average of " + 500 / duration + " entries per second.");
		start = new Date();
		
		client.clear();

		/** Search WITH facets, retrieving an DECIMAL number of 100. */
		exchange = new DefaultExchange(context);
		exchange.getIn().getHeaders().put(Options.query, "test");
		exchange.getIn().getHeaders().put(Options.comments, null);
		exchange.getIn().getHeaders().put(Options.facets, null);
		exchange.getIn().getHeaders().put(Options.stream, client);
		exchange.getIn().getHeaders().put(Options.rows, 175);
		storeRoute.send(exchange);
		assertTrue(client.getInformationObjects().size() == 175);
		assertTrue(client.getFacets().size() > 0);

		end = new Date();
		duration = (double) (end.getTime() - start.getTime()) / 1000d;
		logger.info("Retrieved 175 entries in " + duration + " seconds. An average of " + 175 / duration + " entries per second.");
		start = new Date();

		client.clear();

		/** Search WITH facets, retrieving LESS than 100. */
		exchange = new DefaultExchange(context);
		exchange.getIn().getHeaders().put(Options.query, "test");
		exchange.getIn().getHeaders().put(Options.comments, null);
		exchange.getIn().getHeaders().put(Options.facets, null);
		exchange.getIn().getHeaders().put(Options.stream, client);
		exchange.getIn().getHeaders().put(Options.rows, 50);
		storeRoute.send(exchange);
		assertTrue(client.getInformationObjects().size() == 50);
		assertTrue(client.getFacets().size() > 0);

		end = new Date();
		duration = (double) (end.getTime() - start.getTime()) / 1000d;
		logger.info("Retrieved 50 entries in " + duration + " seconds. An average of " + 50 / duration + " entries per second.");
		start = new Date();

		client.clear();
		
		/** Search WITH facets, retrieving MORE than 3000. */
		exchange = new DefaultExchange(context);
		exchange.getIn().getHeaders().put(Options.query, "test");
		exchange.getIn().getHeaders().put(Options.comments, null);
		exchange.getIn().getHeaders().put(Options.facets, null);
		exchange.getIn().getHeaders().put(Options.stream, client);
		exchange.getIn().getHeaders().put(Options.rows, 4000);
		storeRoute.send(exchange);
		assertTrue(client.getInformationObjects().size() == 3000);
		assertTrue(client.getFacets().size() > 0);

		end = new Date();
		duration = (double) (end.getTime() - start.getTime()) / 1000d;
		logger.info("Retrieved 3000 entries in " + duration + " seconds. An average of " + 3000 / duration + " entries per second.");

	}	
}
