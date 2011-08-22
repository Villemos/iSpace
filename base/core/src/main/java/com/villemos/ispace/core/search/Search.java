package com.villemos.ispace.core.search;


import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.impl.DefaultExchange;

import com.villemos.ispace.Fields;
import com.villemos.ispace.core.io.ResultSet;

public class Search {

	protected CamelContext context = null;

	public ResultSet search(String search) {
		return doSearch(search, true, null);
	}

	/**
	 * Method for doing a keyword search, with the result streamed back to the user.
	 * 
	 * @param search
	 * @param callback
	 */
	public void search(String search, ICallback callback) {
		doSearch(search, true, callback);	
	} 	

	public ResultSet search(String search, boolean facets) {
		return doSearch(search, facets, null);
	}

	public void search(String search, boolean facets, ICallback callback) {
		doSearch(search, facets, callback);
	}

	protected ResultSet doSearch(String search, boolean facets, ICallback callback) {
		
		Exchange exchange = new DefaultExchange(context, ExchangePattern.InOut);
		exchange.getIn().setHeader(Fields.query, search);
		exchange.getIn().setHeader(Fields.stream, callback);
		exchange.getIn().setHeader(Fields.facetquery, facets);
		context.createProducerTemplate().send("direct:search", exchange);
		
		return (ResultSet) exchange.getOut().getBody();
	}

	
	public CamelContext getContext() {
		return context;
	}

	public void setContext(CamelContext context) {
		this.context = context;
	} 	
}
