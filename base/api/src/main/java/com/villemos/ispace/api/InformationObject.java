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
package com.villemos.ispace.api;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;

/**
 * Class holding an InformationObject, being a entry in a repository.
 * 
 * @author villemosg
 *
 */
public class InformationObject {


	public InformationObject() {};

	public float boost = 1f;

	public InformationObject(String uri, String hasTitle, String ofMimeType, String fromSource, String withRawText) {		
		this.hasUri = uri;
		this.hasTitle = hasTitle;
		this.ofMimeType = ofMimeType;
		this.fromSource = fromSource;
		this.withRawText = withRawText;
	}

	public InformationObject(String ofEntityType, String uri, String hasTitle, String ofMimeType, String fromSource, String withRawText) {
		this.ofEntityType = ofEntityType;
		this.hasUri = uri;
		this.hasTitle = hasTitle;
		this.ofMimeType = ofMimeType;
		this.fromSource = fromSource;
		this.withRawText = withRawText;
	}

	/** Standard fields of all IOs. */
	public String hasUri;
	public String wasStoredAt;
	public List<String> isAttachedTo = new ArrayList<String>();   
	public String fromSource;
	public String ofMimeType;
	public String ofEntityType = "io";
	public String hasTitle;
	public String isPartOf;   
	public List<String> hasPart = new ArrayList<String>();
	public String withRawText;
	public List<String> withAttachedLog = new ArrayList<String>();

	public float score;

	public Object get(String valueName) {

		try {
			Field field = InformationObject.class.getField(valueName);
			return field.get(this);
		}
		catch (Exception e) {
			/** Wasnt a meta-data field. Try the dynamic values. */
		}

		return dynamic.get(valueName);
	}

	
	public String getAsString(String valueName) {
		Object value = get(valueName);
		if (value != null) {
			return value.toString();
		}
		
		return "";
	}

	public Integer getAsInteger(String valueName) {
		Object value = get(valueName);
		if (value != null) {
			return (Integer) value;
		}
		
		return null;
	}
	
	public void set(String valueName, Object value) {
		try {
			Field field = InformationObject.class.getField(valueName);
			field.set(this, value);
		}
		catch (Exception e) {
			/** Wasnt a meta-data field. Try the dynamic values. */
			dynamic.put(valueName, value);
		}		
	}
	
	/** Fields for specific systems. Are not used by the core part of the system. */
	public MultiMap dynamic = new MultiHashMap();

	/** Extract(s) from the main content field, in which the search criterion occurs. Will be
	 * ignored in all storage requests. */
	public List<String> highlight = new ArrayList<String>();

	/** A list of IO objects which are comments to this IO. */
	public List<InformationObject> comments = new ArrayList<InformationObject>();

	public void addField(String name, Object value) {
		dynamic.put(name, value);
	};

	public void setContent(String content) {
		this.withRawText = content;
	}
}
