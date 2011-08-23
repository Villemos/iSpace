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

import java.util.List;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.springframework.beans.factory.annotation.Autowired;

import com.villemos.ispace.core.caches.IAcronymManager;


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
