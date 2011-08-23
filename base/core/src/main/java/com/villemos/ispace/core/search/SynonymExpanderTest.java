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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author villemosg
 *
 */
public class SynonymExpanderTest extends TestCase {

	/**
	 * Test method for {@link com.villemos.ispace.core.search.SynonymExpander#expand(java.util.List)}.
	 */
	public void testExpand() {
		SynonymExpander expander = new SynonymExpander();
		
		List<String> synonyms = new ArrayList<String>();
		synonyms.add("Operations");
		synonyms.add("Ops");
		
		Map<String, List<String>> synonymMap = new HashMap<String, List<String>>();
		synonymMap.put("OPS", synonyms);
		
		// expander.setSynonyms(synonymMap);
		
		List<String> tokens = new ArrayList<String>();
		tokens.add("OPS");
		assertTrue(expander.expand(tokens).size() == 3);
	}

}
