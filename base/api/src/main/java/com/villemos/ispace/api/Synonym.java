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

public class Synonym extends InformationObject {

	public Synonym(String value, String category, String root, String extractedFrom) {
		this.hasTitle = "Synonym: " + value;
		this.hasUri = "ispace:synonym/" + category + "/"+ root + "/" + value;
		this.ofMimeType = "virtual";
		this.ofEntityType = "Synonym";
		this.hasState = "candidate";
		this.withRawText = value;
		this.hasRootValue = root;
		this.ofCategory = category;
		this.withAttachedLog.add("Candidate synonym detected and extracted from source '" + extractedFrom + "'.");
	}

	public String hasRootValue;
	public String hasState;
	public String ofCategory;
}
