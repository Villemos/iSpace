package com.villemos.sdms.core.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.springframework.beans.factory.annotation.Autowired;

import com.villemos.sdms.core.caches.ISynonymManager;


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
