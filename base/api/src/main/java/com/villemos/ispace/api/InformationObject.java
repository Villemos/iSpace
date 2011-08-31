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

	/** Standard fields of all IOs. */
	public String hasUri;
	public String wasStoredAt;
	public List<String> isAttachedTo = new ArrayList<String>();   
	public String fromSource;
	public String ofMimeType;
	public String ofEntityType;
	public String hasTitle;
	public String withReferenceId;
	public String withIssue;
	public String withRevision;
	public String isPartOf;   
	public List<String> hasPart = new ArrayList<String>();
	public String withRawText;
	public List<String> withAttachedLog = new ArrayList<String>();

	public float score;
	
	/** Fields for specific systems. Are not used by the core part of the system. */
	public MultiMap dynamic = new MultiHashMap();
	
	/** Extract(s) from the main content field, in which the search criterion occurs. Will be
	 * ignored in all storage requests. */
	public List<String> highlight = new ArrayList<String>();
	
	/** A list of IO objects which are comments to this IO. */
	public List<InformationObject> comments = new ArrayList<InformationObject>();
}
