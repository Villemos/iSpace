/**
 * villemos consulting [space^] (http://www.villemos.de) 
 * Probe. Send. Act. Emergent solution.
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
package com.villemos.sdms.core.accessors;

import java.net.MalformedURLException;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

/**
 * Singleton class for accessing a SOLR repository.
 */
public class RepositoryLink {

	/** The instance. */
	protected static RepositoryLink accessor = null;
	
	/** The url of the SOLR server. The URL must have the format:
	 *    [protocol]://[host IP or DNS name]:[port]/[path to solr] 
	 *  Default is http://localhost:8080/apache-solr-1.4.1 */
	// protected String solrServerUrl = "http://localhost:8080/apache-solr-1.4.1/";
	protected String solrServerUrl = "http://open.logicaspace.com:8080/apache-solr-1.4.0/";

	/** The server. Will be initialized upon first access. */
	protected SolrServer server = null;
	
	/** Instance accessor. Will create an instance upon first access. */
	public static synchronized RepositoryLink getAccessor() {
		if (accessor == null) {
			accessor = new RepositoryLink();
		}
		
		return accessor;
	}
	
	/**
	 * Returns the server instance, providing a connection to the SOLR
	 * repository. The method will lazy load the connection if necessary.
	 * 
	 * @return A connection to the SolrServer or null upon failure.
	 */
	public synchronized SolrServer getServer() {
		if (server == null) {
			try {				
				server = new CommonsHttpSolrServer(solrServerUrl);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}		
		}
		
		return server;
	}
}
