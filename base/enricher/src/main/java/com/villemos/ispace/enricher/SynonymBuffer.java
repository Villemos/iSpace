package com.villemos.ispace.enricher;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Headers;
import org.apache.camel.impl.DefaultExchange;

import com.villemos.ispace.fields.Fields;

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
		
		exchange.getIn().setHeader(Fields.prefix + Fields.hasTitle, "Synonym: " + element);
		exchange.getIn().setHeader(Fields.prefix + Fields.hasUri, "virtual:/synomym/" + element + "/" + element);
		exchange.getIn().setHeader(Fields.prefix + Fields.ofMimeType, "virtual");
		exchange.getIn().setHeader(Fields.prefix + Fields.ofDocumentType, "synonym");
		exchange.getIn().setHeader(Fields.prefix + Fields.hasState, "candidate");
		exchange.getIn().setHeader(Fields.prefix + Fields.withRawText, element);
		exchange.getIn().setHeader(Fields.prefix + Fields.hasRootValue, element);
		exchange.getIn().setHeader(Fields.prefix + Fields.withAttachedLog, "Candidate synonym detected.");
		exchange.getIn().setHeader("ispace.boostfactor" + 0,1L);
		
		registerSynonym(exchange.getIn().getHeaders());
		
		context.createProducerTemplate().send("direct:store", exchange);
	}
	
	public synchronized void registerSynonym(@Headers Map<String, Object> headers) {
		if (headers.get(Fields.prefix + Fields.hasState).equals("accepted")) {
			acceptedSynonyms.put((String) headers.get(Fields.prefix + Fields.withRawText), (String) headers.get(Fields.prefix + Fields.hasRootValue));
		}
		else if (headers.get(Fields.prefix + Fields.hasState).equals("remove")) {
			removeSynonyms.put((String) headers.get(Fields.prefix + Fields.withRawText), (String) headers.get(Fields.prefix + Fields.hasRootValue));
		}
		else {
			knownSynonyms.put((String) headers.get(Fields.prefix + Fields.withRawText), (String) headers.get(Fields.prefix + Fields.hasRootValue));
		}
	}
}
