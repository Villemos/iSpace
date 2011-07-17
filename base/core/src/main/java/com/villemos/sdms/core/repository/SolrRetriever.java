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
package com.villemos.sdms.core.repository;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

import com.villemos.sdms.core.accessors.RepositoryLink;
import com.villemos.sdms.core.io.InformationObject;

/**
 * @author villemosg
 *
 */
public class SolrRetriever extends SolrAccessor {

	@Handler
	public void retrieve(@Body List<String> tokens, Exchange exchange) {

		/** Build the search string. */
		String queue = "";
		for (String token : tokens) {
			queue += token + " ";
		}

		List<InformationObject> results = new ArrayList<InformationObject>();

		/** Configure the request. */
		SolrQuery query = new SolrQuery(queue.trim());
		query.setQueryType("extended");
		query.setRows(1000);

		try {
			QueryResponse response = RepositoryLink.getAccessor().getServer().query(query);

			// Step through all results found for the field
			for (SolrDocument document : response.getResults()) {
				results.add(fromSolr(document));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		exchange.getOut().setBody(results);
	}

	/** Method to get next batch of results. The method 'request' must have been used first, to do an initial
	 * search. Calling this method will provide the next batch of results.
	 * */
	public void getNext(Exchange exchange) {
		
	}
	
	/** Creates an information object from a retrieved Solr document. The type
	 * of the Java objects to be created is identified through the 'isOfType' field.
	 */
	public InformationObject fromSolr(SolrDocument document) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		InformationObject instance = (InformationObject) Class.forName((String) document.getFieldValue("isOfType")).newInstance();

		Class<?> clazz = instance.getClass();

		try {
			
			for (Field field : getAllFields(clazz)) {
				
				if (field.getType().getCanonicalName().equals(List.class.getName()) && document.getFieldValue(field.getName()) != null) {
					Method add = List.class.getDeclaredMethod("add", Object.class);					
					add.invoke(field.get(instance), document.getFieldValue(field.getName()));					
				}
				else {
					field.set(instance, document.getFieldValue(field.getName()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return instance;
	}
}
