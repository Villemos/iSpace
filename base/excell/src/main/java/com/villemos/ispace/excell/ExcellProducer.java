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
package com.villemos.ispace.excell;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcellProducer extends DefaultProducer {

	private static final transient Logger LOG = LoggerFactory.getLogger(ExcellProducer.class);

	/** The endpoint which this producer has been created from. */
	private ExcellEndpoint endpoint;

	public ExcellProducer(ExcellEndpoint endpoint) {
		super(endpoint);
		this.endpoint = endpoint;
	}

	@Override
	@Handler
	public void process(Exchange exchange) throws Exception {

		/** Extract the Excell header configuration fields and set them on the
		 * endpoint. This can be used to reconfigure any endpoint fields 
		 * dynamically. */
		Iterator<Entry<String, Object>> it = exchange.getIn().getHeaders().entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Object> entry = it.next();
			String headerEntryName = entry.getKey();

			/** If the field has the format 'excell.[field name]', then set the endpoint
			 * field accordingly. */
			if (headerEntryName.startsWith("excell.")) {
				String fieldName = headerEntryName.replaceAll("excell.", "");
				try {
					Field field = endpoint.getClass().getDeclaredField(fieldName);
					field.setAccessible(true);
					field.set(endpoint, entry.getValue());
				}
				catch (Exception e) {
					LOG.error("The exchange contains the header field '" + entry.getKey() + "'. The field '" + fieldName + "' is not a valid configuration field of the Excell Component.");
				}
			}
		}

		endpoint.getWorkbookFormatter().add(exchange, (ExcellEndpoint) getEndpoint());
	}
}
