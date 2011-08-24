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
package com.villemos.ispace.solr;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.villemos.ispace.Fields;
import com.villemos.ispace.core.io.Facet;
import com.villemos.ispace.core.io.InformationObject;
import com.villemos.ispace.core.io.ResultSet;
import com.villemos.ispace.core.search.ICallback;

/**
 * The Solr producer.
 */
public class SolrProducer extends DefaultProducer {

	private static final transient Logger LOG = LoggerFactory.getLogger(SolrProducer.class);

	private SolrEndpoint endpoint;

	/** Time stamp of the last time a retrieval was performed. Can be used to do
	 * incremental retrievals. */
	protected Long lastRetrievalTime = 0l;

	public SolrProducer(SolrEndpoint endpoint) {
		super(endpoint);
		this.endpoint = endpoint;
	}

	@Override
	public void process(Exchange exchange) throws Exception {

		/** 
		 * Either the request holds a queue header field in which case it is 
		 * interpreted as a request for data, or it contains a facetquery 
		 * header field in which case it is interpreted as a facet retrieval
		 * request, or (fall through) it is interpreted as a insert request. */
		if (exchange.getIn().getHeaders().containsKey(Fields.query) && exchange.getIn().getHeaders().get(Fields.query) != null) {
			retrieveEntries(exchange);
		}
		else {
			insert(exchange);
		}
	}

	/**
	 * Method to process an exchange interpreted as a insert request.
	 * 
	 * The body of the exchange IN message is expected to hold the main part
	 * of the text to be indexed. In addition other fields may be used to 
	 * categorizing the data according to different categories, such as 
	 * 'person' or 'organisation'.  
	 * 
	 * @param exchange
	 * @throws Exception
	 */
	protected void insert(Exchange exchange) throws Exception {

		/** The document we will be storing. */
		SolrInputDocument document = new SolrInputDocument();
		Map<String, Object> headers = exchange.getIn().getHeaders();

		/** 
		 * Iterate through all headers. Each field with a name 'ispace.field.[name]' will
		 * be extracted and set on the Solr document, i.e. stored in the repository. */
		List<String> filteredHeader = filterHeaders(headers, Fields.prefix);

		for (String field : filteredHeader) {
			setFieldValue(document, field, headers.get(Fields.prefix + field));
		} 

		/** Make sure the document holds an ID. */
		if (endpoint.getAssignId() == true) {
			if (filteredHeader.contains(endpoint.getUniqueidFieldName()) == false) {
				document.addField(endpoint.getContentFieldName(), UUID.randomUUID().toString());
			}
		}

		/** Set the boost factor of the document, if specified in the header. */
		if (headers.containsKey("ispace.boostfactor")) {
			document.setDocumentBoost((Float) headers.get("ispace.boostfactor"));
		}

		/** Set the body as a field. The body is expected to be a String. */
		document.setField(endpoint.getContentFieldName(), exchange.getIn().getBody());

		/** Send the document to the SOLR server. */
		UpdateResponse response = endpoint.getServer().add(document);
		if (response.getStatus() == 500) {
			LOG.error("Failed to submit the document to the SOLR server. Server returned status code '" + response.getStatus() + "'.");
		}

		/** Force commit if the end point has been configured to force commits, or if
		 * the exchange specifies that it should be forced. */
		if (endpoint.getForceCommit() == true || headers.containsKey("forcecommit")) {
			endpoint.getServer().commit();
		}
	}



	/**
	 * Helper method to filter the JMS message header, based on a prefix.
	 * 
	 * @param headers The headers to be filtered.
	 * @param prefix The prefix that a header field name must have to be included.
	 * @return A list of keys matching the filter. 
	 */
	protected List<String> filterHeaders(Map<String, Object> headers, String prefix) {

		List<String> filteredHeaderFields = new ArrayList<String>();
		/** 
		 * Iterate through all headers. Each field with a name 'ispace.field.[solr field name]' will
		 * be extracted and set on the Solr document, i.e. stored in the repository. */
		Iterator<Entry<String, Object>> it = headers.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Object> entry = it.next();

			if (entry.getKey().startsWith(prefix)) {
				String key = entry.getKey().replaceAll(prefix, "");
				filteredHeaderFields.add(key);
			}
		}		

		return filteredHeaderFields;
	}


	protected void setFieldValue(SolrInputDocument document, String field, Object value) {
		if (value instanceof List) {
			for (Object element : (List<Object>) value) {
				document.addField(field, element);
			}
		}
		else {
			document.setField(field, value);					
		}
	}

	/**
	 * Retrieves a number of entries from the repository, based on the configured
	 * query. 
	 * 
	 * @param exchange
	 * @throws SolrServerException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	protected void retrieveEntries(Exchange exchange) throws SolrServerException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		/** Configure the request. 
		 * 
		 * Two keywords are supported
		 *   NOW. Will be replaced with a long value of the current time. 
		 *   FROMLAST. Will be replaced with the timestamp of the last retrieval (initial is 0). 
		 */	
		String queryString = ((String)exchange.getIn().getHeaders().get(Fields.query)).trim();
		queryString = queryString.replaceAll("NOW", Long.toString((new Date()).getTime())).replaceAll("FROMLAST", Long.toString(lastRetrievalTime));
		SolrQuery query = new SolrQuery(queryString);
		configureQuery(query, exchange);

		/** If we are asked for facets, then add the facets. */
		if (exchange.getIn().getHeader(Fields.facetquery) != null) {
			query.setFacet(true);
			query.addFacetField(Fields.ofMimeType);
			query.addFacetField(Fields.fromSource);
			query.addFacetField(Fields.ofDocumentType);
			query.addFacetField(Fields.withReferenceId);
			query.addFacetField(Fields.withIssue);
			query.addFacetField(Fields.withRevision);
		}

		/** Search and set result set. Notice that this will return the results upto the 
		 * configured number of rows. More results may thus be in the repository. */

		/** Data is either returned as a batch contained in the body of the exchange, or as
		 * a stream send to the callback object in the body. The exchange header field 
		 * 'ispace.stream' is used to indicate which delivery mode is used. */
		if (exchange.getIn().getHeaders().containsKey(Fields.stream) == false || exchange.getIn().getHeaders().get(Fields.stream) == null) {

			QueryResponse response = endpoint.getServer().query(query);
			if (response.getStatus() != 0) {
				log.error("Failed to execute retrieval request. Failed with status '" + response.getStatus() + "'.");
			}

			if (exchange.getIn().getHeaders().containsKey(Fields.count)) {
				exchange.getOut().setBody((int) response.getResults().getNumFound());
			} 
			else {
				exchange.getOut().setBody(getResultSet(response));
			}
		}
		else {
			/** Stream. */
			ICallback callback = (ICallback) exchange.getIn().getHeader(Fields.stream);

			int maxNumberOfHits = query.getRows();

			/** When streaming, we retrieve in chunks. */
			int streamBatchSize = 10;
			query.setRows(streamBatchSize);
			int index = query.getStart();

			QueryResponse response = endpoint.getServer().query(query);
			if (response.getStatus() != 0) {
				log.error("Failed to execute retrieval request. Failed with status '" + response.getStatus() + "'.");
			}

			int numberOfHits = (int) response.getResults().getNumFound();
			if (numberOfHits > maxNumberOfHits) {
				numberOfHits = maxNumberOfHits;
			}

			boolean hasDeliveredFacets = false;

			do {				
				ResultSet set = getResultSet(response);

				for(InformationObject document : set.informationobjects){
					callback.receive(document);
				}
				if (hasDeliveredFacets == false) {
					for(Facet facet : set.facets){
						callback.receive(facet);
					}
					hasDeliveredFacets = true;
				}
				index += streamBatchSize;				

				if (numberOfHits > index) {
					query.setStart(index);
					response = endpoint.getServer().query(query);
				}
				else {
					break;
				}
			} while (true);
		}
	}


	private void configureQuery(SolrQuery query, Exchange exchange) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		query.setRows(1000);

		/** We per default always set highlighting. */
		query.setHighlight(true).setHighlightSnippets(1);
		query.setParam("hl.fl", Fields.withRawText);

		/** The header of the exchange can be used to set query options. */
		Map<String, Object> headers = exchange.getIn().getHeaders();
		for (String key : filterHeaders(headers, "ispace.option.")) {
			Object value = headers.get("ispace.option." + key); 

			if (value instanceof List) {

				/** Find the method we can use to 'add' the values. */
				for (Method method : query.getClass().getMethods() ) {
					if (method.getName().equalsIgnoreCase("add" + key)) {

						/** Use the method to set all values. */
						for (Object element : (List<Object>) value) {
							if (method.getParameterTypes().length > 1) {
								method.invoke(query, (Object[])value);
							}
							else {
								method.invoke(query, value);
							}
						}
					}
					break;
				}
			}
			else {
				/** Find the method we can use to 'set' the value. */
				for (Method method : query.getClass().getMethods() ) {
					if (method.getName().equalsIgnoreCase("set" + key) || method.getName().equalsIgnoreCase("add" + key)) {
						if (method.getParameterTypes().length > 1) {
							method.invoke(query, (Object[])value);
						}
						else {
							method.invoke(query, value);
						}
						break;
					}
				}					
			}
		}
	}

	protected ResultSet getResultSet(QueryResponse response) {
		ResultSet set = new ResultSet();
		set.informationobjects = new ArrayList<InformationObject>();
		for (SolrDocument document : response.getResults()) {
			InformationObject io = new InformationObject();
			for (String field : document.getFieldNames()) {
				io.values.put(field, document.getFieldValues(field));
			}
			set.informationobjects.add(io);

			String uniqueId = (String) io.values.get(Fields.hasUri);
			if (response.getHighlighting().get(uniqueId) != null) {
				io.highlight = response.getHighlighting().get(uniqueId).get(Fields.withRawText);
			}
		}

		set.facets = new ArrayList<Facet>();
		for (FacetField facetfield :  response.getFacetFields()) {
			Facet facet = new Facet();
			facet.field = facetfield.getName();

			if (facetfield.getValues() != null) {
				for (Count count : facetfield.getValues()) {
					facet.values.put(count.getName(), count.getCount());
				}
			}
			set.facets.add(facet);
		}

		return set;
	}
}