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
package com.villemos.ispace.core.repository;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Handler;
import org.apache.camel.Headers;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import com.villemos.ispace.core.accessors.RepositoryLink;



public class SolrStorer extends SolrAccessor {

	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SolrStorer.class);

	@Handler
	public void store(@Headers Map<String, Object> headers) {
		try {
			SolrInputDocument document = toSolr(headers);

			UpdateResponse response = RepositoryLink.getAccessor().getServer().add(document);
			if (response.getStatus() == 500) {
				logger.error("");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			RepositoryLink.getAccessor().getServer().commit();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected SolrInputDocument toSolr(Map<String, Object>  headers) {
		SolrInputDocument document = new SolrInputDocument();

		document.addField("hdTimestamp", (new Date()).getTime());

		Iterator<Entry<String, Object>> it = headers.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Object> entry = it.next();
			
			if (entry.getKey().startsWith("iSpace")) {
				String key = entry.getKey().replaceAll("iSpace.", "");
				if (entry.getValue() instanceof List) {
					for (Object value : (List<Object>) entry.getValue()) {
						document.addField(key, value);
					}
				}
				else {
					document.addField(key, entry.getValue());					
				}
			}
		}		

		return document;
	}
}
