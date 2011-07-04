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

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

import com.villemos.sdms.core.io.InformationObject;


public class GenericAccessor {

	public List<InformationObject> getTypes(String filter) {

		List<InformationObject> results = null;
		
		if (results == null) {
			/** Configure the request. */
			SolrQuery query = new SolrQuery(filter);
			query.setQueryType("basic");

			results = new ArrayList<InformationObject>();

			try {
				QueryResponse response = AbstractAccessor.getAccessor().getServer().query(query);

				// Step through all results found for the field
				for (SolrDocument document : response.getResults()) {
					InformationObject instance = (InformationObject) Class.forName((String) document.getFieldValue("Type")).newInstance();
					//instance.fromSolr(document);
					results.add(instance);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		return results;
	}
}
