package com.villemos.ispace.assembler;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.impl.DefaultProducer;

import com.villemos.ispace.core.utilities.EndpointConfigurer;


public class DirectoryAssemblerProducer extends DefaultProducer {

	protected DocumentRetriever crawler = null;
	
	public DirectoryAssemblerProducer(DefaultEndpoint endpoint) {
		super(endpoint);
		crawler = new DocumentRetriever(endpoint, null);
	}

	public void process(Exchange exchange) throws Exception {
		EndpointConfigurer.configure(exchange.getIn().getHeaders(), getEndpoint(), "ktree");

		crawler.doPoll(exchange);	
	}
}
