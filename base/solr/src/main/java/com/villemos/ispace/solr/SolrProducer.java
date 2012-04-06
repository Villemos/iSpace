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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.villemos.ispace.api.Facet;
import com.villemos.ispace.api.ResultSet;
import com.villemos.ispace.api.SolrOptions;
import com.villemos.ispace.api.Statistics;
import com.villemos.ispace.api.Suggestion;
import com.villemos.ispace.core.utilities.EndpointConfigurer;

/**
 * The Solr producer.
 */
public class SolrProducer extends DefaultProducer {

	private static final transient Logger LOG = LoggerFactory.getLogger(SolrProducer.class);

	/** The Solr endpoint. All configuration options are set on the endpoint. */
	private SolrEndpoint endpoint;

	/** Time stamp of the last time a retrieval was performed. Can be used to do
	 * incremental retrievals. */
	protected Long lastRetrievalTime = 0l;

	protected String uriField = null;

	protected XStream xstream = new XStream();

	/**
	 * Constructor
	 * 
	 * @param endpoint The endpoint.
	 */
	public SolrProducer(SolrEndpoint endpoint) {
		super(endpoint);
		this.endpoint = endpoint;
	}

	@Override
	public void process(Exchange exchange) throws Exception {

		EndpointConfigurer.configure(exchange.getIn().getHeaders(), endpoint, "solr.option.");

		/** 
		 * The rules for recognizing what to do are
		 * 
		 * 1. If the header holds a 'delete' flag, then delete.
		 * 2. If the body holds a IO then insert. 
		 * 3. If the header holds a query then retrieve.
		 *  */
		if (exchange.getIn().getHeaders().containsKey(SolrOptions.delete)) {
			delete(exchange);
		}		
		else if (exchange.getIn().getBody() != null) {
			insert(exchange.getIn().getBody());
		}
		else if (exchange.getIn().getHeaders().containsKey(SolrOptions.query)) {
			retrieve(exchange);
		}

		if (endpoint.isCommit()) {
			commit();
		}
	}

	private void delete(Exchange exchange)  {

		String query = "";
		try {
			/** The delete can be defined;
			 * 1. As a query.
			 * 2. Based on a IO in the body. */

			String deleteField = (String) exchange.getIn().getHeaders().get(SolrOptions.delete);
			if (deleteField.equals("") == false) {
				query = (String) exchange.getIn().getHeaders().get(SolrOptions.delete);
				LOG.info("Deleting based on query '" + query + "'.");
				UpdateResponse response = endpoint.getServer().deleteByQuery(query);
				if (response.getStatus() == 500) {
					LOG.error("Failed to delete.");				
				}
			}
			else if (exchange.getIn().getBody() != null) {
				query = "hasUri:" + ((String) exchange.getIn().getBody());
				LOG.info("Deleting based on IO based query '" + query + "'.");			
				UpdateResponse response = endpoint.getServer().deleteByQuery(query);
				if (response.getStatus() == 500) {
					LOG.error("Failed to delete.");				
				}
			}
			else {
				LOG.warn("Failed to delete. Exchange header field '" + SolrOptions.delete + "' == '" + deleteField + "'. Body is '" + exchange.getIn().getBody().toString() + "'.");
			}
		}
		catch (Exception e) {
			LOG.error("Delete request '" + query + "'failed. Exception thrown.");
			e.printStackTrace();
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
	protected void insert(Object io) throws Exception {

		/** The document we will be storing. */
		SolrInputDocument document = new SolrInputDocument();

		/** Add all fields. */
		Map<String, Field> fields = new HashMap<String, Field>();
		Utilities.getAllFields(io.getClass(), fields);

		Iterator<Entry<String, Field>> it = fields.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Field> entry = it.next();

			Object value = entry.getValue().get(io);
			if (value instanceof Integer || value.getClass() == int.class) {
				document.addField(entry.getKey(), Integer.toString((Integer) value) + "_i");
			}
			if (value instanceof Double || value.getClass() == double.class) {
				document.addField(entry.getKey(), Double.toString((Double) value) + "_d");
			}
			if (value instanceof Float || value.getClass() == float.class) {
				document.addField(entry.getKey(), Float.toString((Float) value) + "_f");
			}
			if (value instanceof Long || value.getClass() == long.class) {
				document.addField(entry.getKey(), Long.toString((Long) value) + "_i");
			}
			if (value instanceof Boolean || value.getClass() == boolean.class) {
				document.addField(entry.getKey(), Boolean.toString((Boolean) value) + "_b");
			}
			if (value instanceof String || value.getClass() == char.class) {
				document.addField(entry.getKey(), value + "_s");
			}
			else {
				/** Dont encode. */
			}
		}

		/** Ensure that we have a unique key field. */
		String uri = "uuid:" + UUID.randomUUID().toString();
		if (uriField != null) {
			uri = (String) fields.get(uriField).get(io);
		}
		document.setField("hasUri", uri);

		/** Insert the type. */
		document.setField("ofClass", io.getClass().getName());

		/** Insert the serialization. */
		document.setField("serialization", "<![CDATA[" + xstream.toXML(io) + "]]>");

		/** Send the document to the SOLR server. */
		UpdateResponse response = endpoint.getServer().add(document);
		if (response.getStatus() == 500) {
			LOG.error("Failed to submit the document to the SOLR server. Server returned status code '" + response.getStatus() + "'.");
		}
	}

	protected void commit() throws SolrServerException, IOException {
		LOG.info("Forcing commit of changes.");
		UpdateResponse commitResponse = endpoint.getServer().commit();
		if (commitResponse.getStatus() == 500) {
			LOG.error("Failed to commit the document to the SOLR server. Server returned status code '" + commitResponse.getStatus() + "'.");
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
	 * @throws RemoteException 
	 */
	protected void retrieve(Exchange exchange) throws SolrServerException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, RemoteException {

		/** Configure the request. 
		 * 
		 * One keywords are supported 
		 *   FROMLAST. Will be replaced with the timestamp of the last retrieval (initial is 0). 
		 */	
		String queryString = endpoint.getQuery();
		queryString = queryString.replaceAll("FROMLAST", Long.toString(lastRetrievalTime));
		SolrQuery query = new SolrQuery(queryString);
		configureQuery(query);

		if (queryString.equals("*:*")) {
			query.setQueryType("basic");
		}

		/** If we are asked for facets, then add the facets. */
		if (endpoint.getFacets()) {
			query.setFacet(true);
			query.addFacetField(endpoint.getFacetField());
		}

		/** Search and set result set. Notice that this will return the results upto the 
		 * configured number of rows. More results may thus be in the repository. */

		/** Data is either returned as a batch contained in the body of the exchange, or as
		 * a stream send to the callback object in the body. The exchange header field 
		 * 'solr.stream' is used to indicate which delivery mode is used. */
		if (endpoint.isStream() == false) {

			QueryResponse response = endpoint.getServer().query(query);
			if (response.getStatus() != 0) {
				log.error("Failed to execute retrieval request. Failed with status '" + response.getStatus() + "'.");
			}

			exchange.getOut().getHeaders().put(SolrOptions.count, (int) response.getResults().getNumFound());
			if (endpoint.isCount() == false) {
				ResultSet results;
				try {
					results = Utilities.getResultSet(response, query.getRows(), queryString);
					exchange.getOut().setBody(results);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		else {
			/***/
			Statistics statistics = new Statistics();

			int maxNumberOfHits = query.getRows();

			/** When streaming, we retrieve in chunks. */
			int streamBatchSize = 100 > maxNumberOfHits ? maxNumberOfHits : 100;
			query.setRows(streamBatchSize);
			Integer index = query.getStart() == null ? 0 : query.getStart();

			QueryResponse response = endpoint.getServer().query(query);
			if (response.getStatus() != 0) {
				log.error("Failed to execute retrieval request. Failed with status '" + response.getStatus() + "'.");
			}

			int numberOfHits = (int) response.getResults().getNumFound();
			if (numberOfHits > maxNumberOfHits) {
				numberOfHits = maxNumberOfHits;
			}

			boolean deliverOnes = false;

			do {				
				ResultSet set;
				try {
					set = Utilities.getResultSet(response, maxNumberOfHits, queryString);

					/** Update the statistics. */
					statistics.maxScore = statistics.maxScore > set.statistics.maxScore ? statistics.maxScore : set.statistics.maxScore;
					statistics.totalFound = set.statistics.totalFound;
					statistics.totalRequested = set.statistics.totalRequested;
					statistics.queryTime += set.statistics.queryTime;
					statistics.totalReturned += set.statistics.totalReturned;

					/** Deliver latest statistics. */
					Exchange newExchange = new DefaultExchange(endpoint.getCamelContext());
					newExchange.getIn().setBody(statistics);
					endpoint.getCamelContext().createProducerTemplate().send("direct:results", newExchange);

					/** Deliver the data that is the same for each sequential query, i.e. facets and suggestions. */
					if (deliverOnes == false) {
						for(Facet facet : set.facets){
							newExchange = new DefaultExchange(endpoint.getCamelContext());
							newExchange.getIn().setBody(facet);
							endpoint.getCamelContext().createProducerTemplate().send("direct:results", newExchange);
						}
						for(Suggestion suggestion : set.suggestions){
							newExchange = new DefaultExchange(endpoint.getCamelContext());
							newExchange.getIn().setBody(suggestion);
							endpoint.getCamelContext().createProducerTemplate().send("direct:results", newExchange);
						}

						deliverOnes = true;
					}

					/** Deliver the found information objects. */
					for(Object document : set.informationobjects){
						newExchange = new DefaultExchange(endpoint.getCamelContext());
						newExchange.getIn().setBody(document);
						endpoint.getCamelContext().createProducerTemplate().send("direct:results", newExchange);
					}
					index += streamBatchSize;				

					if (numberOfHits > index && statistics.totalReturned < statistics.totalFound) {
						query.setStart(index);

						long numberMissing = numberOfHits - statistics.totalReturned; 
						if ( numberMissing < streamBatchSize ) {
							query.setRows((int) numberMissing);
						}

						response = endpoint.getServer().query(query);
					}
					else {
						break;
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

			} while (true);
		}
	}


	private void configureQuery(SolrQuery query) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		/** Set the default values. May be overridden by later settings. */
		query.setRows(endpoint.getRows());
		query.setSortField(endpoint.getSortField(), endpoint.getSortOrder());

		/** We per default always set highlighting. */
		query.setHighlight(true).setHighlightSnippets(1);
		query.setParam("hl.fl", "withRawText");

		/** Configure facets. */
		query.setFacet(endpoint.getFacets());
		if (endpoint.getFacets() == true) {
			query.setQuery(endpoint.getQuery());
			query.setFacetSort(endpoint.getFacetsort());
			query.setFacetLimit(endpoint.getFacetlimit());
			query.setFacetPrefix(endpoint.getFacetprefix());
			query.setFacetMinCount(endpoint.getMinCount());
			query.setFacetMissing(endpoint.isFacetMissing());
		}

		query.addFacetField(endpoint.getFacetField());
	}	
}