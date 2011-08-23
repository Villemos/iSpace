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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultExchange;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.villemos.ispace.Fields;

public class ReferenceBooster {

	protected double boost = 2.0d;
	
	public void boost(CamelContext context) {
		
		ProducerTemplate retriever = context.createProducerTemplate();
		
		/** Get all reference IDs. */
		Exchange exchange = new DefaultExchange(context);
		exchange.getIn().setHeader("ispace.query", Fields.withReferenceId + ":*");
		retriever.send("direct:storage", exchange);

		if (exchange.getOut().getBody() != null) {
			
			Map<Integer, SolrDocument> documents = new HashMap<Integer, SolrDocument>();
			int max = 1;
			
			/** Iterate through facet results. */
			for (SolrDocument document : (SolrDocumentList) exchange.getOut().getBody()) {
				
				/** Retrieve the number of documents referencing this document. */
				Exchange countExchange = new DefaultExchange(context);
				countExchange.getIn().setHeader("ispace.countquery", document.getFieldValue(Fields.withReferenceId));
				retriever.send("direct:storage", countExchange);
				int count = (Integer) countExchange.getIn().getHeaders().get("ispace.count");
				documents.put(count, document);
				max = count > max ? count : max;
			}

			Iterator<Entry<Integer, SolrDocument>> it = documents.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Integer, SolrDocument> entry = it.next();
				
				Exchange restoreExchange = new DefaultExchange(context);
				
				/** Calculate corresponding relevance. */
				restoreExchange.getIn().getHeaders().put("ispace.documentboost", boost * (entry.getKey()/max));
				
				/** Configure the document. */
				configureExchange(restoreExchange, entry.getValue());
				restoreExchange.getIn().getHeaders().remove("ispace.count");
				
				/** Resubmit the document with new boost. */
				retriever.send("direct:storage", restoreExchange);
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
