package com.villemos.ispace.core.search;


import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.impl.DefaultExchange;

import com.villemos.ispace.Fields;
import com.villemos.ispace.core.io.ResultSet;


public class RepositoryProxy {

	protected CamelContext context = null;

	protected int rows = 100;
	
	public ResultSet search(String search) {
		return doSearch(search, true, null, 0);
	}

	public void search(String search, ICallback callback) {
		doSearch(search, true, callback, 0);	
	} 	

	public ResultSet search(String search, boolean facets) {
		return doSearch(search, facets, null, 0);
	}

	/**
	 * Method to search the repository for a (set of) keywords, optionally retrieve facets, return the data as a stream
	 * to the object implementing the ICallback interface and starting from the offset (0).
	 * 
	 * @param search The (set of) keywords to search on. 
	 * @param callback A callback interface to receive the data as a stream.
	 */
	public void search(String search, boolean facets, ICallback callback) {
		doSearch(search, facets, callback, 0);
	}

	/**
	 * Method to search the repository for a (set of) keywords, also retrieve facets, return the data as a stream
	 * to the object implementing the ICallback interface and starting from the defined offset.
	 * 
	 * @param search The (set of) keywords to search on.
	 * @param callback A callback interface to receive the data as a stream.
	 */
	public ResultSet search(String search, int offset) {
		return doSearch(search, true, null, offset);
	}

	/**
	 * Method to search the repository for a (set of) keywords, return the data as a stream
	 * to the object implementing the ICallback interface and starting from the offset.
	 * 
	 * @param search The (set of) keywords to search on.
	 * @param callback A callback interface to receive the data as a stream.
	 * @param offset The offset. If 23 results exist and the offset is set to 5 and the rows to 10, then result {6 to 15} will be returned.
	 */
	public void search(String search, ICallback callback, int offset) {
		doSearch(search, true, callback, offset);	
	} 	

	public ResultSet search(String search, boolean facets, int offset) {
		return doSearch(search, facets, null, offset);
	}

	public void search(String search, boolean facets, ICallback callback, int offset) {
		doSearch(search, facets, callback, offset);
	}

	
	protected ResultSet doSearch(String search, boolean facets, ICallback callback, int offset) {
		
		Exchange exchange = new DefaultExchange(context, ExchangePattern.InOut);
		exchange.getIn().setHeader(Fields.query, search);
		exchange.getIn().setHeader(Fields.stream, callback);
		exchange.getIn().setHeader(Fields.facetquery, facets);
		exchange.getIn().setHeader(Fields.offset, offset);
		exchange.getIn().setHeader(Fields.rows, rows);
		context.createProducerTemplate().send("direct:search", exchange);
		
		return (ResultSet) exchange.getOut().getBody();
	}

	
	public CamelContext getContext() {
		return context;
	}

	public void setContext(CamelContext context) {
		this.context = context;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}
	
	
}
