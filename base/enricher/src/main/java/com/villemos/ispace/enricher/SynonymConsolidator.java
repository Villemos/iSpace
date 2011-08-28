/**
 * villemos solutions [space^] (http://www.villemos.com) 
 * Probe. Send. Act. Emergent solution. 
 * Copyright 2011 Gert Villemos
 * All Rights Reserved.
 * 
 * Released under the Apache license, version 2.0 (do what ever
 * you want, just dont claim ownership).
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of villemos solutions, and its suppliers
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to villemos solutions
 * and its suppliers and may be covered by European and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * 
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from villemos solutions.
 * 
 * And it wouldn't be nice either.
 * 
 */
package com.villemos.ispace.enricher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Handler;
import org.apache.camel.Headers;

import com.villemos.ispace.api.Fields;

public class SynonymConsolidator extends SynonymBuffer {

	/** List of fields which are discrete and should be consolidated. */
	protected List<String> discreteFields = new ArrayList<String>();

	@Handler
	public void process(@Headers Map<String, Object> headers, CamelContext context) {

		String uri = (String) headers.get(Fields.hasUri);

		/** Iterate through all fields known to be synonym fields. */
		for (String field : discreteFields) {

			/** If field doesnt exist, then continue. */
			if (headers.containsKey(field) == false) {
				continue;
			}

			String category = fieldsToEntityType.get(field);

			/** Get the value. */
			Object value = headers.get(field);
			if (value == null) {
				continue;
			}

			if (value instanceof List) {

				for (Object element : (List) value) {					
					if (element instanceof String) {
						if (element.equals("") == false) {
							if (acceptedSynonyms.containsKey(category) && acceptedSynonyms.get(category).contains(element)) {
								/** Known synonym. Replace. */
								int index = acceptedSynonyms.get(category).indexOf(element);
								element = acceptedSynonyms.get(category).get(index);
							}
							else if (removeSynonyms.containsKey(category) && removeSynonyms.get(category).contains(element)) {
								/** Synonym known to be false. Remove. */
								headers.put(field, null);
							}
							else if (knownSynonyms.containsKey(category) && knownSynonyms.get(category).contains(element)) {
								/** Synonym known. */
								continue;
							}
							else {
								/** New synonym. Register. */
								registerNewSynonym((String) element, category, uri, context);
							}
						}
					}
				}
			}
			else {
				if (value instanceof String) {
					if (value.equals("") == false) {
						if (acceptedSynonyms.containsKey(category) && acceptedSynonyms.get(category).contains(value)) {
							/** Known synonym. Replace. */
							headers.put(field, acceptedSynonyms.get(value));
						}
						else if (removeSynonyms.containsKey(category) && removeSynonyms.get(category).contains(value)) {
							/** Synonym known to be false. Remove. */
							headers.put(field, null);
						}
						else if (knownSynonyms.containsKey(category) && knownSynonyms.get(category).contains(value)) {
							/** Synonym known. */
							continue;
						}
						else {
							/** New synonym. Register. */
							registerNewSynonym((String) value, category, uri, context);
						}
					}
				}
			}					
		}
	}



	public List<String> getDiscreteFields() {
		return discreteFields;
	}


	public void setDiscreteFields(List<String> discreteFields) {
		this.discreteFields = discreteFields;
	}


}
