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
		this.hasUri = "ispace:synonym://" + category + "/"+ root + "/" + value;
		this.ofMimeType = "ispace/virtual";
		this.ofEntityType = "Synonym";
		
		dynamic.put("hasState", "candidate");
		dynamic.put("withRawText", value);
		dynamic.put("hasRootValue", root);
		dynamic.put("ofCategory", category);
		
		this.withAttachedLog.add("Candidate synonym detected and extracted from source '" + extractedFrom + "'.");
	}
	
	public boolean isAccepted() {
		if (dynamic.containsKey("hasState")) {
			return dynamic.get("hasState").equals("accepted");
		}
		
		return false;
	}
	
	public boolean isCandidate() {
		if (dynamic.containsKey("hasState")) {
			return dynamic.get("hasState").equals("candidate");
		}
		
		return false;
	}

	public boolean isRemove() {
		if (dynamic.containsKey("hasState")) {
			return dynamic.get("hasState").equals("remove");
		}
		
		return false;
	}

	public String getCategory() {
		return (String) dynamic.get("ofCategory");
	}
}
