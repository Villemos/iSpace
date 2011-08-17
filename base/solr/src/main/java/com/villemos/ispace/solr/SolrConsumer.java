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
import org.apache.solr.common.SolrDocument;

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

		/** Iterate through the found documents and inject them. */
		for (SolrDocument doc : response.getResults()) {
			
			Exchange exchange = getEndpoint().createExchange();
	
			/** Set all Solr document fields as headers. */
			for (String name : doc.getFieldNames()) {
				exchange.getIn().setHeader("Fields.prefix." + name, doc.getFirstValue(name));
			}
			exchange.getIn().setBody(doc);
				
			getAsyncProcessor().process(exchange, new AsyncCallback() {
				public void done(boolean doneSync) {
					LOG.trace("Done processing URL");
				}
			});
		}
		
		return 0;
	}

	protected SolrEndpoint getSolrEndpoint() {
		return (SolrEndpoint) getEndpoint();
	}
}
