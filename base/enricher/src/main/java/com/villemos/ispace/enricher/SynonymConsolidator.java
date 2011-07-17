package com.villemos.ispace.enricher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Handler;
import org.apache.camel.Headers;

public class SynonymConsolidator extends SynonymBuffer {

	/** List of fields which are discrete and should be consolidated. */
	protected List<String> discreteFields = new ArrayList<String>();

	@Handler
	public void process(@Headers Map<String, Object> headers, CamelContext context) {
		
		/** Iterate through all fields known to be synonym fields. */
		for (String field : discreteFields) {

			/** If field doesnt exist, then continue. */
			if (headers.containsKey("ispace.field." + field) == false) {
				continue;
			}
			
			/** Get the value. */
			Object value = headers.get("ispace.field." + field);
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
							// headers.put("ispace.field." + field, null);
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
						headers.put("ispace.field." + field, acceptedSynonyms.get(value));
					}
					else if (removeSynonyms.containsKey(value)) {
						/** Synonym known to be false. Remove. */
						headers.put("ispace.field." + field, null);
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
