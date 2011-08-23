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

import com.villemos.ispace.Fields;

public class SynonymConsolidator extends SynonymBuffer {

	/** List of fields which are discrete and should be consolidated. */
	protected List<String> discreteFields = new ArrayList<String>();

	@Handler
	public void process(@Headers Map<String, Object> headers, CamelContext context) {
		
		/** Iterate through all fields known to be synonym fields. */
		for (String field : discreteFields) {

			/** If field doesnt exist, then continue. */
			if (headers.containsKey(Fields.prefix + field) == false) {
				continue;
			}
			
			/** Get the value. */
			Object value = headers.get(Fields.prefix + field);
			if (value == null) {
				continue;
			}

			if (value instanceof List) {

				for (Object element : (List) value) {					
					if (element instanceof String) {
						if (acceptedSynonyms.containsKey(element)) {
							/** Known synonym. Replace. */
							element = acceptedSynonyms.get(element);
						}
						else if (removeSynonyms.containsKey(element)) {
							/** Synonym known to be false. Remove. */
							// headers.put(Fields.prefix + field, null);
						}
						else {
							/** New synonym. Register. */
							registerNewSynonym(element, context);
						}
					}
				}
			}
			else {
				if (value instanceof String) {
					if (acceptedSynonyms.containsKey(value)) {
						/** Known synonym. Replace. */
						headers.put(Fields.prefix + field, acceptedSynonyms.get(value));
					}
					else if (removeSynonyms.containsKey(value)) {
						/** Synonym known to be false. Remove. */
						headers.put(Fields.prefix + field, null);
					}
					else if (knownSynonyms.containsKey(value) == false) {
						/** New synonym. Register. */
						registerNewSynonym(value, context);
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
