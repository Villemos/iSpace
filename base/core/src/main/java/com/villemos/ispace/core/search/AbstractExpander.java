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
import java.util.Map;

public abstract class AbstractExpander {


	/**
	 * Function for tokenizing a search string. The rules are that
	 * 
	 * - Anything in "" is left untouched.
	 * - Anything else is split based on white space.
	 * 
	 * The tokenizer will keep the order, i.e. if the queue string is reassembled by adding the
	 * tokens together, then the same string results.
	 * 
	 * @param queue The queue string to be tokenized.
	 * @return A list of tokens. Tokens with "" will contains the "". All tokens are trimmed, i.e. no trailing or leading white soaces.
	 */
	protected List<String> tokenize(String queue) {

		List<String> tokens = new ArrayList<String>();

		queue = queue.trim();
		int offset = 0;
		while (offset < queue.length() - 1) {
			String token = "";
			
			/** A complicated set of rules. */
			
			/** If the token is '"', then this is a string that should be tokenized in its total. */
			if ( queue.charAt(offset) == '"') {
				int nextIndex = queue.indexOf('"', offset + 2);
				if (nextIndex == -1) {
					nextIndex = queue.length() - 1;
				}				
				token = queue.substring(offset, nextIndex + 1).trim();
				offset = nextIndex + 1;
			}
			else {
				int nextIndex = queue.indexOf(' ', offset + 1);
				if (nextIndex == -1) {
					nextIndex = queue.length() - 1;
				}				
				token = queue.substring(offset, nextIndex + 1).trim();
				offset = nextIndex + 1;
			}
			
			tokens.add(token);
		}

		return tokens;
	}

	protected Map<String, List<String>> getSynonyms() {
		
		return null;
	}
	
	protected Map<String, List<String>> getTaxonomy() {
		
		return null;
	}
 
	protected Map<String, String> getAcronyms() {
		
		return null;
	}	
}
