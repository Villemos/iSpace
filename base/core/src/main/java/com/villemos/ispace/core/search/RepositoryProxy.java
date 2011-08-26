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
package com.villemos.ispace.core.search;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.impl.DefaultExchange;

import com.villemos.ispace.api.Fields;
import com.villemos.ispace.api.ResultSet;


/**
 * API for issuing retrieval requests.
 * 
 * IMPORTANT: To use this API the Camel configuration must contain a route 
 *   <route>
 *      <from uri="direct:search"/>
 *      ...
 *   </route>
 * 
 * A request is send through a Camel route. This allows the request to go through a 
 * configurable number of steps, where each step can transform the request in some 
 * way. Examples are the expansion of the query to include synonyms of the same word.
 * 
 * It also allows each request to be distributed to a configurable set of providers,
 * for example a solr repository holding documents + a dynamic webster consumer providing
 * term definitions.
 *
 * Results are returned either as a batch (ResultSet) or as a stream (continuous delivery of 
 * single InformationObject or Facet). When using a stream, the user must provide a class
 * handling the callback. The class must implement the 'ICallback' interface.
 * 
 * Streaming is ideal for consumers which support asynchonious processing, such as GUIs that
 * can update single entries in a list. Total delivery time will be the same, but the first
 * entry will arrive much faster, thus improving the responsiveness of the system.
 *
 */
public class RepositoryProxy {

	/** The Camel context used to send the request.*/
	protected CamelContext context = null;

	/** The default minimum number of entries to be returned.  This is a 
	 * desired number, not a guaranteed number. The actually returned data may
	 * - Hold less. This is the case when the number of matches is lower than the configured number of rows.
	 * - Hold more. This is the case when multiple providers services the request (configured in the Camel route). Each may return upto 'rows' entries.  
	 */
	protected int rows = 100;
	
	public RepositoryProxy(CamelContext context) {
		this.context = context;
	}	
	
	/**
	 * A simple keyword search, using mainly default values and no streaming. 
	 * 
	 * The search will retrieve the first 100 (can be changed by setting 'rows' 
	 * on the API) entries plus the related facets.  
	 * 
	 * @param search The keyword(s) of the search
	 * @return A result set holding all documents (including highlighting and score) and facets.
	 */
	public ResultSet search(String search) {
		return doSearch(search, true, null, 0);
	}

	/**
	 * A simple keyword search, using mainly default values and streaming.
	 * 
	 * The search will retrieve the first 100 (can be changed by setting 'rows' 
	 * on the API) entries plus the related facets.  
	 * 
	 * @param search The keyword(s) of the search
	 * @param callback The consumer to be called for each found document / facet.
	 */
	public void search(String search, ICallback callback) {		
		doSearch(search, true, callback, 0);	
	} 	

	/**
	 * A simple keyword search, using mainly default values and no streaming, with the
	 * option to configure whether facets are returned. Disabling facet retrieval provides
	 * a (small) performance boost.
	 * 
	 * The search will retrieve the first 100 (can be changed by setting 'rows' 
	 * on the API) entries plus the related facets.  
	 * 
	 * @param search The keyword(s) of the search.
	 * @param facets Flag setting whether the facets should be retrieved.
	 * @return A result set holding all documents (including highlighting and score) and facets (optional).
	 */
	public ResultSet search(String search, boolean facets) {
		return doSearch(search, facets, null, 0);
	}

	/**
	 * A simple keyword search, using mainly default values and streaming, with the
	 * option to configure whether facets are returned. Disabling facet retrieval provides
	 * a (small) performance boost.
	 * 
	 * The search will retrieve the first 100 (can be changed by setting 'rows' 
	 * on the API) entries plus the related facets.  
	 * 
	 * @param search The keyword(s) of the search.
	 * @param facets Flag setting whether the facets should be retrieved.
	 */
	public void search(String search, boolean facets, ICallback callback) {
		doSearch(search, facets, callback, 0);
	}

	/**
	 * A simple keyword search, starting from an offset and no streaming. 
	 * 
	 * The search will retrieve the first 100 (can be changed by setting 'rows' 
	 * on the API) entries starting from the offset plus the related facets. If
	 * for example the repository holds 200 matches, then setting offset=50 and 
	 * using the default rows=100 will retrieve matches 50 to 150.  
	 * 
	 * @param search The keyword(s) of the search
	 * @param offset The offset of the first entry to be retrieved. Can be used to 'continue' a search.
	 * @return A result set holding all documents (including highlighting and score) and facets.
	 */
	public ResultSet search(String search, int offset) {
		return doSearch(search, true, null, offset);
	}

	/**
	 * A simple keyword search, starting from an offset and streaming. 
	 * 
	 * The search will retrieve the first 100 (can be changed by setting 'rows' 
	 * on the API) entries starting from the offset plus the related facets. If
	 * for example the repository holds 200 matches, then setting offset=50 and 
	 * using the default rows=100 will retrieve matches 50 to 150.  
	 * 
	 * @param search The keyword(s) of the search
	 * @param callback The consumer to be called for each found document / facet.
	 * @param offset The offset of the first entry to be retrieved. Can be used to 'continue' a search.
	 * @return A result set holding all documents (including highlighting and score) and facets.
	 */
	public void search(String search, ICallback callback, int offset) {
		doSearch(search, true, callback, offset);	
	} 	

	/**
	 * A simple keyword search, using mainly default values and no streaming, with the
	 * option to configure whether facets are returned. Disabling facet retrieval provides
	 * a (small) performance boost.
	 * 
	 * The search will retrieve the first 100 (can be changed by setting 'rows' 
	 * on the API) entries starting from the offset plus the related facets. If
	 * for example the repository holds 200 matches, then setting offset=50 and 
	 * using the default rows=100 will retrieve matches 50 to 150.  
	 * 
	 * @param search The keyword(s) of the search
	 * @param facets The consumer to be called for each found document / facet.
	 * @param offset The offset of the first entry to be retrieved. Can be used to 'continue' a search.
	 * @return A result set holding all documents (including highlighting and score) and facets.
	 */
	public ResultSet search(String search, boolean facets, int offset) {
		return doSearch(search, facets, null, offset);
	}

	/**
	 * A simple keyword search, using mainly default values and streaming, with the
	 * option to configure whether facets are returned. Disabling facet retrieval provides
	 * a (small) performance boost.
	 * 
	 * The search will retrieve the first 100 (can be changed by setting 'rows' 
	 * on the API) entries starting from the offset plus the related facets. If
	 * for example the repository holds 200 matches, then setting offset=50 and 
	 * using the default rows=100 will retrieve matches 50 to 150.  
	 * 
	 * @param search The keyword(s) of the search
	 * @param facets The consumer to be called for each found document / facet.
	 * @param callback The consumer to be called for each found document / facet.
	 * @param offset The offset of the first entry to be retrieved. Can be used to 'continue' a search.
	 * @return A result set holding all documents (including highlighting and score) and facets.
	 */
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
		context.createProducerTemplate().send("direct:search", exchange);
		
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
}
