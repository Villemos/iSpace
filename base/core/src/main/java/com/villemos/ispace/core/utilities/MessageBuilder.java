package com.villemos.ispace.core.utilities;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;

import com.villemos.ispace.api.Fields;
import com.villemos.ispace.api.InformationObject;

public class MessageBuilder {

	
	public static synchronized Message createIoMessage(String hasUri, String hasTitle, String ofMimeType, String fromSource, String withRawText) {
		Message message = new DefaultMessage();
		
		message.setHeader(Fields.hasUri, hasUri);
		message.setHeader(Fields.hasTitle, hasTitle);
		message.setHeader(Fields.ofMimeType, ofMimeType);
		message.setHeader(Fields.fromSource, fromSource);
		message.setHeader(Fields.withRawText, withRawText);
				
		return message;
	}

	public static Message createAcronymMessage(String acronym, String definition, String extractedFrom) {
		Message message = new DefaultMessage();		
		
		InformationObject io = new InformationObject();
		
		
		io.hasUri = "ispace:/acronym/" + acronym + "/" + definition;
		io.hasTitle = definition + " (" + acronym + ")";
		io.ofMimeType = "Virtual";
		io.ofEntityType = "Acronym";
		io.withRawText = definition;
		io.withAttachedLog.add("Acronym extracted from '" + extractedFrom + "'.");

		message.setHeader("ispace.boostfactor" + 0,1L);
		message.setBody(io);

		return message;
	}

	public static Exchange buildExchange(InformationObject io, CamelContext context) {
		
		Exchange exchange = new DefaultExchange(context);
		
		/** TODO Set all fields. */
		
		exchange.getIn().setBody(io);
		
		return exchange;
	}
}
