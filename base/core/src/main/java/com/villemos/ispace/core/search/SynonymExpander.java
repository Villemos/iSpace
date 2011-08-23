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
package com.villemos.ispace.core.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.springframework.beans.factory.annotation.Autowired;

import com.villemos.ispace.core.caches.ISynonymManager;


public class SynonymExpander {

	/** List of synonyms. The map is keyed on the synonym root and the value is a list
	 * of related synonyms. */	
	@Autowired
	protected ISynonymManager synonymManager = null;
	
	@Handler
	public List<String> expand(@Body List<String> tokens) {
	
		/** For each token, add all synonyms. */
		List<String> newTokens = new ArrayList<String>();
		for (String token : tokens) {
			List<String> elements = synonymManager.getSynonyms().get(token); 
			if (elements != null) {
				newTokens.addAll(elements);
				newTokens.add(token);
			}
		}
		
		return newTokens;
	}

	public void setSynonymManager(ISynonymManager synonymManager) {
		this.synonymManager = synonymManager;
	}
}
