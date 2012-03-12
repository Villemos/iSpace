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
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Map.Entry;

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

import com.villemos.ispace.api.Facet;
import com.villemos.ispace.api.InformationObject;
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
		else if (exchange.getIn().getBody() != null && exchange.getIn().getBody() instanceof InformationObject) {
			insert((InformationObject) exchange.getIn().getBody());
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
			else if (exchange.getIn().getBody() != null && exchange.getIn().getBody() instanceof InformationObject) {
				query = "hasUri:" + ((InformationObject) exchange.getIn().getBody()).hasUri;
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
	protected void insert(InformationObject io) throws Exception {

		/** The document we will be storing. */
		SolrInputDocument document = new SolrInputDocument();

		/** Add the static fields. */
		document.setField("hasUri", io.hasUri);
		document.setField("wasStoredAt", io.wasStoredAt);
		document.setField("isAttachedTo", io.isAttachedTo);   
		document.setField("fromSource", io.fromSource);
		document.setField("ofMimeType", io.ofMimeType);
		document.setField("ofEntityType", io.ofEntityType);
		document.setField("hasTitle", io.hasTitle);
		document.setField("withReferenceId", io.withReferenceId);
		document.setField("withIssue", io.withIssue);
		document.setField("withRevision", io.withRevision);
		document.setField("isPartOf", io.isPartOf);
		document.setField("hasPart", io.hasPart);
		document.setField("withRawText", io.withRawText);
		document.setField("withAttachedLog", io.withAttachedLog);

		/** Add the dynamic fields. */
		Iterator<Entry<String, Object>> it = io.dynamic.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Object> entry = it.next();
			document.setField(entry.getKey() + "_s", entry.getValue());
		}

		document.setDocumentBoost(io.boost);

		LOG.info("Storing document '" + io.hasUri + "'");

		/** Send the document to the SOLR server. */
		UpdateResponse response = endpoint.getServer().add(document);
		if (response.getStatus() == 500) {
			LOG.error("Failed to submit the document to the SOLR server. Server returned status code '" + response.getStatus() + "'.");
		}

		/** Store the comments. */
		for (InformationObject comment : io.comments) {
			insert(comment);
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
				ResultSet results = Utilities.getResultSet(response, query.getRows(), queryString);

				/** Get attached comments. */	
				if (endpoint.isComments()) {
					for (InformationObject io : results.informationobjects) {
						SolrQuery commentQuery = new SolrQuery("ofEntityType:\"Comment\" AND isAttachedTo:\"" + io.hasUri + "\"");
						commentQuery.setQueryType("basic");
						commentQuery.setRows(1000);
						QueryResponse commentsResponse = endpoint.getServer().query(commentQuery);

						ResultSet comments = Utilities.getResultSet(commentsResponse, 0, queryString);
						for (InformationObject comment : comments.informationobjects) {
							io.comments.add(comment);
						}
					}
				}

				exchange.getOut().setBody(results);
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
				ResultSet set = Utilities.getResultSet(response, maxNumberOfHits, queryString);

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
				for(InformationObject document : set.informationobjects){

					/** Get attached comments. */
					if (exchange.getIn().getHeaders().containsKey(SolrOptions.comments)) {					
						SolrQuery commentQuery = new SolrQuery("ofEntityType:\"Comment\" AND isAttachedTo:\"" + document.hasUri + "\"");
						commentQuery.setQueryType("basic");
						commentQuery.setRows(1000);
						QueryResponse commentsResponse = endpoint.getServer().query(commentQuery);

						ResultSet comments = Utilities.getResultSet(commentsResponse, 0, queryString);
						for (InformationObject comment : comments.informationobjects) {
							document.comments.add(comment);
						}
					}

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