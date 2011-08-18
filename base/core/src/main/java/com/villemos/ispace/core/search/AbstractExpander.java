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
