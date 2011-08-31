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
import java.util.Collection;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.Handler;
import org.apache.camel.Headers;

import com.villemos.ispace.api.InformationObject;

public class SynonymConsolidator {

	/** List of keys in the 'dynamic fields' which are discrete and should be consolidated. */
	protected List<String> discreteFields = new ArrayList<String>();

	protected SynonymBuffer synonymBuffer = null;

	@Handler
	public void process(@Headers InformationObject io, CamelContext context) {

		/** Iterate through all fields known to be synonym fields. */
		for (String field : discreteFields) {

			String category = synonymBuffer.fieldsToEntityType.get(field);

			/** Get the value. */
			if (io.dynamic.get(field) != null) {
				Collection value = (Collection) io.dynamic.get(field);
				for (Object element : value) {					
					if (element.equals("") == false) {
						if (synonymBuffer.acceptedSynonyms.containsKey(category) && synonymBuffer.acceptedSynonyms.get(category).contains(element)) {
							/** Known synonym. Replace. */
							int index = synonymBuffer.acceptedSynonyms.get(category).indexOf(element);
							element = synonymBuffer.acceptedSynonyms.get(category).get(index);
						}
						else if (synonymBuffer.removeSynonyms.containsKey(category) && synonymBuffer.removeSynonyms.get(category).contains(element)) {
							/** Synonym known to be false. Remove. */
							value.remove(element);
						}
						else if (synonymBuffer.knownSynonyms.containsKey(category) && synonymBuffer.knownSynonyms.get(category).contains(element)) {
							/** Synonym known. */
							continue;
						}
						else {
							/** New synonym. Register. */
							synonymBuffer.registerNewSynonym((String) element, category, io.hasUri, context);
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
