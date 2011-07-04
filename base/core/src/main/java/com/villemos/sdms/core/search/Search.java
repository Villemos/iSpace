package com.villemos.sdms.core.search;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultExchange;

public class Search {

	protected CamelContext context = null;
	
	protected ProducerTemplate producer = null;
	
	
	public String search(String search) {
		
		Exchange exchange = new DefaultExchange(context, ExchangePattern.InOut);
		exchange.getIn().setBody(search);
		Exchange returnValue = producer.send("direct:search", exchange);
		
		return (String) returnValue.getIn().getBody();
	} 	
}
