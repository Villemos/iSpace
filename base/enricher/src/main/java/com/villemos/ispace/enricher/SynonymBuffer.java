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

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Headers;
import org.apache.camel.impl.DefaultExchange;

import com.villemos.ispace.api.Fields;
import com.villemos.ispace.core.utilities.StringComparison;

public class SynonymBuffer {

	protected Map<String, String> fieldsToEntityType = null;

	protected Map<String, Map<String, String>> acceptedSynonyms = new HashMap<String, Map<String, String>>();
	protected Map<String, Map<String, String>> removeSynonyms = new HashMap<String, Map<String, String>>();
	protected Map<String, Map<String, String>> knownSynonyms = new HashMap<String, Map<String, String>>();

	protected static double threshold = 0.9;

	// Spyder crawler (open source)
	// Elastic (store)
	// Geotagger
	// 
	public void registerNewSynonym(String element, String category, String uri, CamelContext context) {
		Exchange exchange = new DefaultExchange(context);

		double bestScore = 0D;		
		String root = element;

		/** Try to detect a similar synonym. */
		for (String possibleRoot : knownSynonyms.keySet()) {
			double score = StringComparison.match(possibleRoot, element);

			if (score > bestScore && score > threshold) {
				root = possibleRoot;
				bestScore = score;
			}
		}

		exchange.getIn().setHeader(Fields.hasTitle, "Synonym: " + element);
		exchange.getIn().setHeader(Fields.hasUri, "ispace:synonym/" + category + "/"+ root + "/" + element);
		exchange.getIn().setHeader(Fields.ofMimeType, "virtual");
		exchange.getIn().setHeader(Fields.ofEntityType, "Synonym");
		exchange.getIn().setHeader(Fields.hasState, "candidate");
		exchange.getIn().setHeader(Fields.withRawText, element);
		exchange.getIn().setHeader(Fields.hasRootValue, root);
		exchange.getIn().setHeader(Fields.ofCategory, category);
		exchange.getIn().setHeader(Fields.withAttachedLog, "Candidate synonym detected and extracted from source '" + uri + "'.");
		exchange.getIn().setHeader("ispace.boostfactor" + 0,1L);

		registerSynonym(exchange.getIn().getHeaders());

		context.createProducerTemplate().send("direct:store", exchange);
	}

	public synchronized void registerSynonym(@Headers Map<String, Object> headers) {
		String synonymCategory = (String) headers.get(Fields.ofCategory);

		if (headers.get(Fields.hasState).equals("accepted")) {
			if (acceptedSynonyms.containsKey(synonymCategory) == false) {
				acceptedSynonyms.put(synonymCategory, new HashMap<String, String>());
			}
			acceptedSynonyms.get(synonymCategory).put((String) headers.get(Fields.withRawText), (String) headers.get(Fields.hasRootValue));
		}
		else if (headers.get(Fields.hasState).equals("remove")) {
			if (removeSynonyms.containsKey(synonymCategory) == false) {
				removeSynonyms.put(synonymCategory, new HashMap<String, String>());
			}
			removeSynonyms.get(synonymCategory).put((String) headers.get(Fields.withRawText), (String) headers.get(Fields.hasRootValue));
		}

		if (knownSynonyms.containsKey(synonymCategory) == false) {
			knownSynonyms.put(synonymCategory, new HashMap<String, String>());
		}
		knownSynonyms.get(synonymCategory).put((String) headers.get(Fields.withRawText), (String) headers.get(Fields.hasRootValue));
	}

	public Map<String, String> getFieldsToEntityType() {
		return fieldsToEntityType;
	}

	public void setFieldsToEntityType(Map<String, String> fieldsToEntityType) {
		this.fieldsToEntityType = fieldsToEntityType;
	}
}
