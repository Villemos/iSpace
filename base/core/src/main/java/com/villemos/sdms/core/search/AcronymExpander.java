package com.villemos.sdms.core.search;

import java.util.List;
import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.springframework.beans.factory.annotation.Autowired;

import com.villemos.sdms.core.caches.IAcronymManager;


/**
 * Class expanding an acronym to its definition, grouped using '"'.
 * 
 */
public class AcronymExpander {

	@Autowired
	protected IAcronymManager acronymManager = null;

	@Handler
	public List<String> expand(@Body List<String> tokens) {

		/** Break the body into tokens. Then replace each token with all synonym values. */
		for (String token : tokens) {
			if (token.charAt(0) != '"') {
				String acronymDefinition = acronymManager.getAcronyms().get(token);
				if (acronymDefinition != null) {
					tokens.add('"' + acronymDefinition + '"');
				}
			}
		}
		
		return tokens;
	}

	public void setAcronymManager(IAcronymManager acronymManager) {
		this.acronymManager = acronymManager;
	}
}
