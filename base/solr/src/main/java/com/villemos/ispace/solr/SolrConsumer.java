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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.villemos.ispace.api.Facet;
import com.villemos.ispace.api.ResultSet;

public class SolrConsumer extends ScheduledPollConsumer {

	private static final Log LOG = LogFactory.getLog(SolrConsumer.class);
	/** Time stamp of the last time a retrieval was performed. Can be used to do
	 * incremental retrievals. */

	protected Date lastRetrievalTime = new Date(0);

	protected DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	public SolrConsumer(DefaultEndpoint endpoint, Processor processor) {
		super(endpoint, processor);		
	}

	public SolrConsumer(Endpoint endpoint, Processor processor,
			ScheduledExecutorService executor) {
		super(endpoint, processor, executor);
	}

	@Override
	protected int poll() throws Exception {

		String queryString = getSolrEndpoint().getQuery();

		queryString = queryString.replaceAll("FROMLAST", format.format(lastRetrievalTime));
		lastRetrievalTime = new Date();

		SolrQuery query = new SolrQuery(queryString);		

		if (getSolrEndpoint().getQueryHandler() != null) {
			query.setQueryType(getSolrEndpoint().getQueryHandler());
		}

		/** Search and set result set. Notice that this will return the results upto the 
		 * configured number of rows. More results may thus be in the repository. */
		QueryResponse response = getSolrEndpoint().getServer().query(query);
		if (response.getStatus() != 0) {
			log.error("Failed to execute retrieval request. Failed with status '" + response.getStatus() + "'.");
		}

		/** Get the result set. */
		ResultSet results = Utilities.getResultSet(response, (int) response.getResults().getNumFound(), queryString);

		/** Either deliver the complete result set as on batch, or as a stream. */
		if (getSolrEndpoint().getDeliveryMode().equals("batch")) {

			Exchange exchange = getEndpoint().createExchange();
			exchange.getIn().setBody(results);

			getAsyncProcessor().process(exchange, new AsyncCallback() {
				public void done(boolean doneSync) {
					LOG.trace("Done processing sending Batch.");
				}
			});
		}
		else {
			/** Iterate through the result set and inject the io objects. */			
			for (Object io : results.informationobjects) {
				Exchange exchange = getEndpoint().createExchange();
				exchange.getIn().setBody(io);

				getAsyncProcessor().process(exchange, new AsyncCallback() {
					public void done(boolean doneSync) {
						LOG.trace("Done processing streaming information objects.");
					}
				});
			}
			
			for (Facet facet : results.facets) {				
				Exchange exchange = getEndpoint().createExchange();
				exchange.getIn().setBody(facet);

				getAsyncProcessor().process(exchange, new AsyncCallback() {
					public void done(boolean doneSync) {
						LOG.trace("Done streaming facets.");
					}
				});
			}			
			
			/** TODO Should the suggestions and statistics also be injected? */
		}

		return 0;
	}

	protected SolrEndpoint getSolrEndpoint() {
		return (SolrEndpoint) getEndpoint();
	}
}
