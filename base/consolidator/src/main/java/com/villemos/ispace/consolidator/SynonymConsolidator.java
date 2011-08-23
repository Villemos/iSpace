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
package com.villemos.ispace.consolidator;

import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultExchange;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.villemos.ispace.enricher.SynonymBuffer;

/**
 * The consolidator will consolidate all documents to the current set of synonyms.
 * 
 * The consolidator receives synonym through a route. It can be triggered through a timer 
 * route and will then perform the consolidation.
 */
public class SynonymConsolidator extends SynonymBuffer {

	/** Fields to be consolidated. */
	protected List<String> discreteFields = null;

	public void consolidate(CamelContext context) {

		ProducerTemplate retriever = context.createProducerTemplate();

		/** Iterate through all synonyms to REMOVE. */
		for (String synonym : removeSynonyms.keySet()) {
			for (String field : discreteFields) {

				/** Retrieve all documents with this synonym in one of the discrete fields. */
				Exchange exchange = new DefaultExchange(context);
				exchange.getIn().setHeader("ispace.query", field + ":" + synonym);
				retriever.send("direct:storage", exchange);

				if (exchange.getOut().getBody() != null) {
					/** Iterate through results and replace */
					for (SolrDocument document : (SolrDocumentList) exchange.getOut().getBody()) {
						document.setField(field, "");					

						Exchange newExchange = new DefaultExchange(context);
						configureExchange(newExchange, document);
						retriever.send("direct:storage", newExchange);
					}
				}
			}
		}
		
		/** Iterate through all synonyms to REMOVE. */
		for (String synonym : acceptedSynonyms.keySet()) {
			for (String field : discreteFields) {

				/** Retrieve all documents with this synonym in one of the discrete fields. */
				Exchange exchange = new DefaultExchange(context);
				exchange.getIn().setHeader("ispace.query", field + ":" + synonym);
				retriever.send("direct:storage", exchange);

				if (exchange.getOut().getBody() != null) {
					/** Iterate through results and replace */
					for (SolrDocument document : (SolrDocumentList) exchange.getOut().getBody()) {
						document.setField(field, acceptedSynonyms.get(synonym));					

						Exchange newExchange = new DefaultExchange(context);
						configureExchange(newExchange, document);
						retriever.send("direct:storage", newExchange);
					}
				}
			}
		}

	}

	protected void configureExchange(Exchange exchange, SolrDocument document) {
		/** Iterate through all solr fields and set them in the header. */
		for (String key : document.getFieldValuesMap().keySet()) {
			exchange.getIn().setHeader(key, document.getFieldValue(key));
		}		
	}
}
