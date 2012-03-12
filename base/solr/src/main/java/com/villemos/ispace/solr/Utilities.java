package com.villemos.ispace.solr;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.villemos.ispace.api.Facet;
import com.villemos.ispace.api.InformationObject;
import com.villemos.ispace.api.ResultSet;

public class Utilities {

	private static final transient Logger LOG = LoggerFactory.getLogger(Utilities.class);
	
	public static synchronized ResultSet getResultSet(QueryResponse response, int rows, String query) {

		ResultSet set = new ResultSet();

		for (SolrDocument document : response.getResults()) {

			/** Use reflection to set the IO fields. */
			InformationObject io = new InformationObject();

			for (String field : document.getFieldNames()) {

				/** We ignore all fields used for analysis. */
				if (field.endsWith("__")) {
					continue;
				}

				/** If a dynamically (extension) field. */
				else if (field.endsWith("_s") || field.endsWith("_t")) {
					io.dynamic.put(field, document.getFieldValue(field));
				}
				else {
					try {
						/** Locate field. We do not use getField([name]) as it will throw an exception if
						 * it doesnt find the field. */
						Field ioField = InformationObject.class.getField(field);

						/** If a static defined field. */
						if (ioField != null) {					

							/** If this is a 'multivalued' field, insert as a list.  */ 
							if (ioField.getType() == List.class) {
								Method add = List.class.getDeclaredMethod("addAll",Collection.class);
								add.invoke(ioField.get(io), document.getFieldValues(field));
							}
							/** Else set only the field.*/
							else {
								if (ioField.getType() == String.class) {
									ioField.set(io, document.getFieldValue(field).toString());								
								}
								else if (ioField.getType() == long.class || ioField.getType() == Long.class) {
									ioField.set(io, Long.toString((Long) document.getFieldValue(field)));
								}
								else if (ioField.getType() == float.class || ioField.getType() == Float.class) {
									ioField.set(io, (Float) document.getFieldValue(field));
								}
								else if (ioField.getType() == int.class || ioField.getType() == Integer.class) {
									ioField.set(io, Integer.toString((Integer) document.getFieldValue(field)));
								}
								else if (ioField.getType() == double.class || ioField.getType() == Double.class) {
									ioField.set(io, Double.toString((Double) document.getFieldValue(field)));
								}
								else if (ioField.getType() == URL.class) {
									ioField.set(io, new URL((String) document.getFieldValue(field)));
								}
								else if (ioField.getType() == Date.class) {
									LOG.warn("Date type not supported.");
								}
								else {
									LOG.warn("Failed to assign value '" + field + "' to IO object. Type '" + ioField.getType().getName() + "' not supported.");
								}
							}						
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}

					/** If this is the unique key, then we can use it to get the highlighting. */
					if (field.equals("hasUri")) {
						if (response.getHighlighting() != null && response.getHighlighting().get(field) != null) {
							io.highlight = response.getHighlighting().get(field).get("withRawText");
						}
					}
				}
			}
			set.informationobjects.add(io);
		}

		set.facets = new ArrayList<Facet>();
		if (response.getFacetFields() != null) {
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
		}

		set.statistics.queryTime = response.getQTime();
		set.statistics.totalFound = response.getResults().getNumFound();
		set.statistics.totalRequested = rows;
		set.statistics.totalReturned = response.getResults().size();
		set.statistics.maxScore = response.getResults().getMaxScore() == null ? 0f : response.getResults().getMaxScore();

		/** Get all suggestions. */
		if (response.getSpellCheckResponse() != null) {
			for (org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion suggestion : response.getSpellCheckResponse().getSuggestions()) {
				set.suggestions.add(new com.villemos.ispace.api.Suggestion(query, suggestion.toString(), "Solr"));
			}
		}

		return set;
	}

}
