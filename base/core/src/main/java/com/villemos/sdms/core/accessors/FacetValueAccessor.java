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

import java.util.Map;
import java.util.TreeMap;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;

public class FacetValueAccessor {

	public Map<Long, String> getFacetValues(String field, String value, String facet) {

		/** Configure the request. */
		SolrQuery query = new SolrQuery(field + ":" + value);
		query.setQueryType("basic");
		query.addFacetField(facet);          
		query.setFacet(true);
		query.setFacetLimit(-1);
		query.setRows(0);
		query.setFacetSort("count");

		Map<Long, String> values = new TreeMap<Long, String>();

		try
		{
			QueryResponse response = AbstractAccessor.getAccessor().getServer().query(query);
			
			// Step through all FACETS found for the field
			for (FacetField specificFacet : response.getFacetFields()) {

				// If no facets were found...
				if (specificFacet != null && specificFacet.getValues() != null) {

					// Step through all VALUES of the facet            
					for (Count facetValue : specificFacet.getValues()) {
						values.put(facetValue.getCount(), facetValue.getName());
					}
				}
			}
		}
		catch (SolrServerException e)
		{
			e.printStackTrace();
		}

		return values;
	}
}
