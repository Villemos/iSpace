package com.villemos.ispace.solr;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.villemos.ispace.api.Facet;
import com.villemos.ispace.api.ResultSet;

public class Utilities {

	private static final transient Logger LOG = LoggerFactory.getLogger(Utilities.class);

	protected static XStream xstream = new XStream();
	
	public static synchronized ResultSet getResultSet(QueryResponse response, int rows, String query) throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException {

		ResultSet set = new ResultSet();

		for (SolrDocument document : response.getResults()) {

			/** Use reflection to set the IO fields. */
			Object io = null;
			io = Class.forName((String) document.get("ofClass"));

			for (String field : document.getFieldNames()) {

				/** We ignore all fields used for analysis. */
				if (field.endsWith("__")) {
					continue;
				}

				/** Locate field. We do not use getField([name]) as it will throw an exception if
				 * it doesnt find the field. */
				Map<String, Field> fields = new HashMap<String, Field>();
				Utilities.getAllFields(io.getClass(), fields);
				Field ioField = fields.get(field);

				Object value = document.get(field);
				if (Utilities.isPrimitive(value)) {
					ioField.set(io, value);
				}
				else {
					/** Everything else we encode with XStream. */
					ioField.set(io, xstream.fromXML((String) value));
				}

				/** If this is the unique key, then we can use it to get the highlighting. */
				if (field.equals("hasUri")) {
					if (response.getHighlighting() != null && response.getHighlighting().get(field) != null) {
						set.highlights.put(io, response.getHighlighting().get(field).get("withRawText"));
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

	protected static Map<String, Field> getAllFields(Class clazz, Map<String, Field> fields) {
		/** See if there is a super class. */
		if (clazz.getSuperclass() != null) {
			getAllFields(clazz.getSuperclass(), fields);
		}

		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			fields.put(field.getName(), field);
		}

		return fields;
	}

	public static boolean isPrimitive(Object value) {
		return value instanceof String || 
		value instanceof Boolean ||
		value instanceof Long ||
		value instanceof Double ||
		value.getClass().isPrimitive();
	}
}
