/**
 * villemos consulting [space^] (http://www.villemos.com) 
 * Probe. Sense. Act. Emergent solution.
 * 
 * Copyright 2011 Gert Villemos
 * All Rights Reserved.
 * Released under proprietary license, i.e. not free. But we are friendly.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of villemos consulting, and its suppliers
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to villemos consulting
 * and its suppliers and may be covered by European and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * 
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from villemos consulting Incorporated.
 * 
 * And it wouldn't be nice either.
 * 
 */
package com.villemos.ispace.solr;

import java.net.MalformedURLException;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.ScheduledPollEndpoint;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.villemos.ispace.fields.Fields;

/**
 * The endpoint for production of solr producers. The endpoint does
 * not support consumers.
 * 
 * The endpoint manages connections to a underlying Solr {@link http://lucene.apache.org/solr/} 
 * repository. The repository must be preinstalled and running. The Solr repository will be
 * accessed through HTTP, using the SolrJ API {@link http://wiki.apache.org/solr/Solrj}}.
 * 
 * The preinstalled Solr server must have a Solr schema file, defining the fields allowed for
 * a document submitted for indexing. The endpoint assume per default that the following fields
 * are available
 *    'content'. The main field holding a large portion of text to be indexed.
 *    
 * The body of each insert exchanges are inserted in the content field.
 *    
 * In addition the exchange can contain any number of additional fields, set in the header. 
 * Each field must have the format 'solr.field.[name]'. It must as value contain a value object or 
 * a list of value objects. Setting the header field 'solr.field.url' with the value
 * 'file:c:/foo/baa.txt' will thus lead to the solr field 'url' being added to the solr document
 * prior to storage.
 * 
 */
public class SolrEndpoint extends ScheduledPollEndpoint {

	/** Da' logger! */
	private static final transient Logger LOG = LoggerFactory.getLogger(SolrEndpoint.class);

	/** The url of the SOLR server. The URL must have the format:
	 *    [protocol]://[host IP or DNS name]:[port]/[path to solr] 
	 *  Default is http://localhost:8080/apache-solr-1.4.1 */
	protected String solrServerUrl = "http://localhost:8080/apache-solr-3.3.0/";

	/** The solr name of the field holding the content. */
	protected String contentFieldName = Fields.withRawText;

	/** The solr name of the field holding the unique ID (required by Solr). */
	protected String uniqueidFieldName = Fields.hasUri;

	/** Flag indicating whether the endpoint should automatically assign a unique ID to the entry, if not already set. */
	protected boolean assignId = true;

	/** Flag indicating whether to force a commit upon each submission. */
	protected boolean forceCommit = true;

	/** The server. Will be initialized upon first access. */
	protected SolrServer server = null;	

	protected String query = "";

	protected String queryHandler = null;

	/** Default constructor. */
	public SolrEndpoint() {
	}

	public SolrEndpoint(String uri, SolrComponent component) {
		super(uri, component);
	}

	public SolrEndpoint(String endpointUri) {
		super(endpointUri);
	}


	/* (non-Javadoc)
	 * @see org.apache.camel.Endpoint#createProducer()
	 */
	public Producer createProducer() throws Exception {
		return new SolrProducer(this);
	}

	/* (non-Javadoc)
	 * @see org.apache.camel.Endpoint#createConsumer(org.apache.camel.Processor)
	 */
	public Consumer createConsumer(Processor processor) throws Exception {
		Consumer consumer = new SolrConsumer(this, processor);
		configureConsumer(consumer);

		return consumer; 
	}


	/* (non-Javadoc)
	 * @see org.apache.camel.IsSingleton#isSingleton()
	 */
	public boolean isSingleton() {
		return true;
	}

	/**
	 * Returns the server instance, providing a connection to the SOLR
	 * repository. The method will lazy load the connection if necessary.
	 * 
	 * @return A connection to the SolrServer or null upon connection failure.
	 */
	public synchronized SolrServer getServer() {

		if (server == null) {
			try {				
				server = new CommonsHttpSolrServer(solrServerUrl);
			} catch (MalformedURLException e) {
				LOG.error("Failed to contact the solr server on URL '" + solrServerUrl + "'. Is the server running?");
				e.printStackTrace();
			}		
		}

		return server;
	}


	/**
	 * Method to set the SolrServer object managing the Solr repository connection.
	 * 
	 * @param server The instance to be used. Setting 'null' will reinitialize the connection.
	 */
	public synchronized void setServer(SolrServer server) {
		this.server = server;
	}


	/**
	 * Sets the URL to the Solr server. Should be in the format 
	 * http://[host IP or DSN][:[port]]/[path]
	 * 
	 * @param solrServerUrl
	 */
	public void setSolrServerUrl(String solrServerUrl) {
		this.solrServerUrl = solrServerUrl;
	}


	/**
	 * Get the name of the Solr field that 
	 * 
	 * @return
	 */
	public String getContentFieldName() {
		return contentFieldName;
	}

	/**
	 * Sets the name of the field in the solr repository which contains
	 * the main text.
	 * 
	 * @param contentFieldName The name of the field, as configured in the solr 'schema.xml', containing
	 * the main text.
	 */
	public void setContentFieldName(String contentFieldName) {
		this.contentFieldName = contentFieldName;
	}

	public String getUniqueidFieldName() {
		return uniqueidFieldName;
	}

	public void setUniqueidFieldName(String uniqueidFieldName) {
		this.uniqueidFieldName = uniqueidFieldName;
	}

	public boolean getAssignId() {
		return assignId;
	}

	public void setAssignId(boolean assignId) {
		this.assignId = assignId;
	}

	public boolean getForceCommit() {
		return forceCommit;
	}

	public void setForceCommit(boolean forceCommit) {
		this.forceCommit = forceCommit;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getQueryHandler() {
		return queryHandler;
	}

	public void setQueryHandler(String queryHandler) {
		this.queryHandler = queryHandler;
	}
}
