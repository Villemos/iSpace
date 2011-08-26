package com.villemos.ispace.solr;

import java.util.ArrayList;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.SolrDocument;

import com.villemos.ispace.api.Facet;
import com.villemos.ispace.api.Fields;
import com.villemos.ispace.api.InformationObject;
import com.villemos.ispace.api.ResultSet;

public class Utilities {

	public static synchronized ResultSet getResultSet(QueryResponse response) {
		ResultSet set = new ResultSet();
		set.informationobjects = new ArrayList<InformationObject>();
		for (SolrDocument document : response.getResults()) {
			InformationObject io = new InformationObject();
			for (String field : document.getFieldNames()) {
				io.values.put(field, document.getFieldValues(field));

				/** If this is the unique key, then we can use it to get the highlighting. */
				if (field.equals(Fields.hasUri)) {
					if (response.getHighlighting().get(field) != null) {
						io.highlight = response.getHighlighting().get(field).get(Fields.withRawText);
					}
				}
			}
			set.informationobjects.add(io);
		}

		set.facets = new ArrayList<Facet>();
		for (FacetField facetfield :  response.getFacetFields()) {
			Facet facet = new Facet();
			facet.field = facetfield.getName();

			if (facetfield.getValues() != null) {
				for (Count count : facetfield.getValues()) {
					facet.values.put(count.getName(), count.getCount());
				}
			}
			set.facets.add(facet);
		}

		return set;
	}
	
}
