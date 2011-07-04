package com.villemos.sdms.doccrawler.enricher;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Handler;
import org.apache.camel.Headers;
import org.springframework.beans.factory.annotation.Autowired;

/** Class for adding a number of fixed fields to the document. The fixed fields
 * are configured as part of the system setup. Typically this should be used to
 * for example mark all documents as coming from a specific data source, or from
 * a specific company or project. */
public class ConstantFields {

	/** The fields to be set. */
	protected Map<String, String> constantFields = new HashMap<String, String>(); 
	
	/** Processor method of the bean. */
	@Handler
	public void addFields(@Headers Map<String, String> headerFields) {
		headerFields.putAll(constantFields);
	}

	public Map<String, String> getConstantFields() {
		return constantFields;
	}

	public void setConstantFields(Map<String, String> constantFields) {
		this.constantFields = constantFields;
	}	
	
	
}
