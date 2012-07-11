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
package com.villemos.ispace.ktree;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.villemos.ispace.core.utilities.EndpointConfigurer;

public class KtreeCrawlerProducer extends DefaultProducer {

	private static final Log LOG = LogFactory.getLog(KtreeCrawlerProducer.class);

	protected KtreeAccessor crawler = null;

	public KtreeCrawlerProducer(Endpoint endpoint, CamelContext camelContext) {
		super(endpoint);
		crawler = new KtreeAccessor(endpoint, null, camelContext);		
	}

	public void process(Exchange exchange) throws Exception {
		
		EndpointConfigurer.configure(exchange.getIn().getHeaders(), getEndpoint(), "ktree");
		
		/** Retrieve all documents. */
		crawler.doPoll();	

				/** See if the exchange triggering the exchange already contains a Map for the 
		 * result. If yes, then add the results of the latest crawl. This allows the 
		 * component to be 'chained'. */
		Object body = exchange.getIn().getBody(); 
		if (body != null && body instanceof Map) {
			/** Get the results. The results is inserted as a Map into a Camel Message. */
			LOG.debug("Adding results to existing IN body.");
			((Map)exchange.getIn().getBody()).putAll(crawler.getResults());
		}
		else {
			LOG.debug("Replacing IN body with the results.");
			exchange.getIn().setBody(crawler.getResults());
		}		
	}
}
