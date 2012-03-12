package com.villemos.ispace.directoryassembler;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.impl.DefaultProducer;

import com.villemos.ispace.core.utilities.EndpointConfigurer;


public class DocumentAssemblerProducer extends DefaultProducer {

	protected DocumentRetriever crawler = null;
	
	public DocumentAssemblerProducer(DefaultEndpoint endpoint) {
		super(endpoint);
		crawler = new DocumentRetriever(endpoint, null);
	}

	public void process(Exchange exchange) throws Exception {
		EndpointConfigurer.configure(exchange.getIn().getHeaders(), getEndpoint(), "ktree");

		crawler.doPoll(exchange);	
	}
}
