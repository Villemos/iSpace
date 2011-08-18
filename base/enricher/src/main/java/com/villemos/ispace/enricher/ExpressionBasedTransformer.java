package com.villemos.ispace.enricher;

import org.apache.camel.Exchange;

public class ExpressionBasedTransformer {

	protected String expression = "\\<.*?\\>";	
	protected String replacement = "";
	
	public void transform(Exchange exchange) {
		exchange.getIn().setBody(( (String) exchange.getIn().getBody()).replaceAll(expression, replacement).replaceAll("\\s+", " "));
	}
}
