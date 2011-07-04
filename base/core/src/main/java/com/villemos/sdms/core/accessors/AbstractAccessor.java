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

public class AbstractAccessor {

	protected static AbstractAccessor accessor = null;
	
	protected SolrServer server = null;
	
	public static AbstractAccessor getAccessor() {
		if (accessor == null) {
			accessor = new AbstractAccessor();
		}
		
		return accessor;
	}
	
	public SolrServer getServer() {
		if (server == null) {
			try {				
				// server = new CommonsHttpSolrServer("http://open.logicaspace.com:8080/apache-solr-1.4.0/");
				server = new CommonsHttpSolrServer("http://localhost:8080/apache-solr-1.4.1/");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}		
		}
		
		return server;
	}
}
