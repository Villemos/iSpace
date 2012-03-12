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

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.villemos.ispace.core.utilities.EndpointConfigurer;

public class ExcellProducer extends DefaultProducer {

	private static final transient Logger LOG = LoggerFactory.getLogger(ExcellProducer.class);

	private ExcellEndpoint endpoint;

	public ExcellProducer(ExcellEndpoint endpoint) {
		super(endpoint);
		this.endpoint = endpoint;
	}

	@Override
	@Handler
	public void process(Exchange exchange) throws Exception {
		EndpointConfigurer.configure(exchange.getIn().getHeaders(), endpoint, "excell");

		if (exchange.getIn().getBody() == null) {
			endpoint.getConsumer().consume(exchange);
		}
		else {
			endpoint.getWorkbookFormatter().add(exchange, (ExcellEndpoint) getEndpoint());
		}
	}
}
