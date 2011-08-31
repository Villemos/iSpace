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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.impl.DefaultExchange;

import com.villemos.ispace.api.Synonym;
import com.villemos.ispace.core.utilities.StringComparison;

public class SynonymBuffer {

	protected Map<String, String> fieldsToEntityType = null;

	protected Map<String, List<String>> acceptedSynonyms = new HashMap<String, List<String>>();
	protected Map<String, List<String>> removeSynonyms = new HashMap<String, List<String>>();
	protected Map<String, List<String>> knownSynonyms = new HashMap<String, List<String>>();

	protected static double threshold = 0.9;

	// Spyder crawler (open source)
	// Elastic (store)
	// Geotagger
	// 
	public void registerNewSynonym(String element, String category, String fromUri, CamelContext context) {
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

		/** Create new synonym. */
		Synonym synonym = new Synonym(element, category, root, fromUri);				
		
		/** Register locally. */
		registerSynonym(synonym);
		
		/** Send into system for storage. */
		Exchange exchange = new DefaultExchange(context);
		exchange.getIn().setHeader("ispace.boostfactor" + 0,1L);
		exchange.getIn().setBody(synonym);

		context.createProducerTemplate().send("direct:store", exchange);
	}

	@Handler
	public synchronized void registerSynonym(@Body Synonym synonym) {
		if (synonym.hasState.equals("accepted")) {
			if (acceptedSynonyms.containsKey(synonym.ofCategory) == false) {
				acceptedSynonyms.put(synonym.ofCategory, new ArrayList<String>());
			}
			acceptedSynonyms.get(synonym.ofCategory).add(synonym.withRawText);
		}
		else if (synonym.hasState.equals("remove")) {
			if (removeSynonyms.containsKey(synonym.ofCategory) == false) {
				removeSynonyms.put(synonym.ofCategory, new ArrayList<String>());
			}
			removeSynonyms.get(synonym.ofCategory).add(synonym.withRawText);
		}

		if (knownSynonyms.containsKey(synonym.ofCategory) == false) {
			knownSynonyms.put(synonym.ofCategory, new ArrayList<String>());
		}
		knownSynonyms.get(synonym.ofCategory).add(synonym.withRawText);
	}

	public Map<String, String> getFieldsToEntityType() {
		return fieldsToEntityType;
	}

	public void setFieldsToEntityType(Map<String, String> fieldsToEntityType) {
		this.fieldsToEntityType = fieldsToEntityType;
	}
}
