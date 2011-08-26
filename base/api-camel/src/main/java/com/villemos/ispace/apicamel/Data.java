package com.villemos.ispace.apicamel;

import java.rmi.RemoteException;
import java.util.UUID;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.impl.DefaultExchange;

import com.villemos.ispace.api.Fields;
import com.villemos.ispace.api.ICallback;
import com.villemos.ispace.api.IData;
import com.villemos.ispace.api.ResultSet;

public class Data implements IData {

	/** The Camel context used to send the request.*/
	protected CamelContext context = null;

	/** The name of the route used to issue search requests. */
	protected String searchRouteName = "direct:search";
	
	/** The name of the route used to issue storage requests. */
	protected String storeRouteName = "direct:store";
	
	/** The default minimum number of entries to be returned.  This is a 
	 * desired number, not a guaranteed number. The actually returned data may
	 * - Hold less. This is the case when the number of matches is lower than the configured number of rows.
	 * - Hold more. This is the case when multiple providers services the request (configured in the Camel route). Each may return upto 'rows' entries.  
	 */
	protected int rows = 100;

	public Data(CamelContext context) {
		this.context = context;
	}	

	@Override
	public ResultSet search(String search) {
		return doSearch(search, true, null, 0);
	}

	@Override	
	public void search(String search, ICallback callback) {		
		doSearch(search, true, callback, 0);	
	} 	
	@Override
	public ResultSet search(String search, boolean facets) {
		return doSearch(search, facets, null, 0);
	}

	@Override
	public void search(String search, boolean facets, ICallback callback) {
		doSearch(search, facets, callback, 0);
	}

	@Override
	public ResultSet search(String search, int offset) {
		return doSearch(search, true, null, offset);
	}

	@Override
	public void search(String search, ICallback callback, int offset) {
		doSearch(search, true, callback, offset);	
	} 	

	@Override
	public ResultSet search(String search, boolean facets, int offset) {
		return doSearch(search, facets, null, offset);
	}

	@Override
	public void search(String search, boolean facets, ICallback callback, int offset) {
		doSearch(search, facets, callback, offset);
	}


	/**
	 * The actual method to perform the retrieval. Used by the public search methods.
	 * This is the central point to update when reconfiguring search behaviour.
	 * 
	 * @param search The keyword(s) of the search
	 * @param facets The consumer to be called for each found document / facet.
	 * @param callback The consumer to be called for each found document / facet.
	 * @param offset The offset of the first entry to be retrieved. Can be used to 'continue' a search.
	 * @return A result set holding all documents (including highlighting and score) and facets.
	 */
	protected ResultSet doSearch(String search, boolean facets, ICallback callback, int offset) {

		Exchange exchange = new DefaultExchange(context, ExchangePattern.InOut);
		exchange.getIn().setHeader(Fields.query, search);
		exchange.getIn().setHeader(Fields.stream, callback);
		exchange.getIn().setHeader(Fields.facetquery, facets);
		exchange.getIn().setHeader(Fields.offset, offset);
		exchange.getIn().setHeader(Fields.rows, rows);
		context.createProducerTemplate().send(searchRouteName, exchange);

		return (ResultSet) exchange.getOut().getBody();
	}


	/**
	 * Sets the context of the API. Is needed to inject requests into the Camel route.
	 * 
	 * @param context The Camel context of the route.
	 */
	public void setContext(CamelContext context) {
		this.context = context;
	}

	/**
	 * Gets the number of 'rows' to be retrieved, i.e. the number of entries. This is a 
	 * desired number, not a guaranteed number. The actually returned data may
	 * - Hold less. This is the case when the number of matches is lower than the configured number of rows.
	 * - Hold more. This is the case when multiple providers services the request (configured in the Camel route). Each may return upto 'rows' entries. 
	 * 
	 * @return Number of entries to be retrieved.
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * Sets the number of 'rows' to be retrieved, i.e. the number of entries. This is a 
	 * desired number, not a guaranteed number. The actually returned data may
	 * - Hold less. This is the case when the number of matches is lower than the configured number of rows.
	 * - Hold more. This is the case when multiple providers services the request (configured in the Camel route). Each may return upto 'rows' entries. 
	 * 
	 * @return Number of entries to be retrieved.
	 */
	public void setRows(int rows) {
		this.rows = rows;
	}

	@Override
	public boolean storeComment(String uriOfParent, String comment) throws RemoteException {
		Exchange exchange = new DefaultExchange(context, ExchangePattern.InOut);
		exchange.getIn().setHeader(Fields.hasUri, "ispace:comment/" + uriOfParent + "/" + UUID.randomUUID().toString());
		exchange.getIn().setHeader(Fields.hasTitle, "Comment to " + uriOfParent);
		exchange.getIn().setHeader(Fields.isAttachedTo, uriOfParent);
		exchange.getIn().setHeader(Fields.ofEntityType, "Comment");
		exchange.getIn().setHeader(Fields.ofMimeType, rows);
		exchange.getIn().setHeader(Fields.withRawText, comment);
		
		context.createProducerTemplate().send(storeRouteName, exchange);

		return exchange.getException() == null;
	}
}
