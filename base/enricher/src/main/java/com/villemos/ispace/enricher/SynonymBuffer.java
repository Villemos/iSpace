package com.villemos.ispace.enricher;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Headers;
import org.apache.camel.impl.DefaultExchange;

public class SynonymBuffer {
	
	protected Map<String, String> acceptedSynonyms = new HashMap<String, String>();
	protected Map<String, String> removeSynonyms = new HashMap<String, String>();
	protected Map<String, String> knownSynonyms = new HashMap<String, String>();
	

	// Spyder crawler (open source)
	// Elastic (store)
	// Geotagger
	// 
	public void registerNewSynonym(Object element, CamelContext context) {
		Exchange exchange = new DefaultExchange(context);
		
		exchange.getIn().setHeader("ispace.field.title", "Synonym: " + element);
		exchange.getIn().setHeader("ispace.field.uri", "virtual:/synomym/" + element + "/" + element);
		exchange.getIn().setHeader("ispace.field.mime", "virtual");
		exchange.getIn().setHeader("ispace.field.type", "synonym");
		exchange.getIn().setHeader("ispace.field.state", "candidate");
		exchange.getIn().setHeader("ispace.field.text", element);
		exchange.getIn().setHeader("ispace.field.root", element);
		exchange.getIn().setHeader("ispace.field.log", "Candidate synonym detected.");
		
		registerSynonym(exchange.getIn().getHeaders());
		
		context.createProducerTemplate().send("direct:store", exchange);
	}
	
	public synchronized void registerSynonym(@Headers Map<String, Object> headers) {
		if (headers.get("ispace.field.state").equals("accepted")) {
			acceptedSynonyms.put((String) headers.get("ispace.field.text"), (String) headers.get("ispace.field.root"));
		}
		else if (headers.get("ispace.field.state").equals("remove")) {
			removeSynonyms.put((String) headers.get("ispace.field.text"), (String) headers.get("ispace.field.root"));
		}
		else {
			knownSynonyms.put((String) headers.get("ispace.field.text"), (String) headers.get("ispace.field.root"));
		}
	}
}
