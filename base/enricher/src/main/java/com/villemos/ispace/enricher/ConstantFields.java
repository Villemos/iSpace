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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.apache.camel.Headers;

import com.villemos.ispace.api.InformationObject;

/** Class for adding a number of fixed fields to the document. The fixed fields
 * are configured as part of the system setup. Typically this should be used to
 * for example mark all documents as coming from a specific data source, or from
 * a specific company or project. */
public class ConstantFields {

	/** The fields to be set. */
	protected Map<String, String> constantFields = new HashMap<String, String>(); 
	
	/** Processor method of the bean. */
	@Handler
	public void addFields(@Body InformationObject io) {
		Iterator<Entry<String, String>> it = constantFields.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			
			try {
				io.getClass().getField(entry.getKey());
			}
			catch (Exception e) {
				io.dynamic.put(entry.getKey(), entry.getValue());
			}
		}
	}

	public Map<String, String> getConstantFields() {
		return constantFields;
	}

	public void setConstantFields(Map<String, String> constantFields) {
		this.constantFields = constantFields;
	}	
}
