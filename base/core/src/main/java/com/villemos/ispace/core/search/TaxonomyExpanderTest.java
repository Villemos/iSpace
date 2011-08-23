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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class TaxonomyExpanderTest extends TestCase {

	public void testExpand() {
		Map<String, List<String>> taxonomy = new HashMap<String, List<String>>();
		/*
		 *  	OPS
		 *  		OPS-G
		 *  			OPS-GI
		 *  			OPS-GM
		 *  		OPS-B
		 *      TEC
		 */
		taxonomy.put("OPS", Arrays.asList(new String[] {"OPS-G", "OPS-B"}));
		taxonomy.put("OPS-G", Arrays.asList(new String[] {"OPS-GI", "OPS-GM"}));

		TaxonomyExpander expander = new TaxonomyExpander();
		// expander.setTaxonomy(taxonomy);
		
		List<String> token = new ArrayList<String>();
		
		token.add("OPS-GI");
		assertTrue(expander.expand(token).size() == 1);
		
		token = new ArrayList<String>();
		token.add("OPS-G");
		assertTrue(expander.expand(token).size() == 3);
		
		token = new ArrayList<String>();
		token.add("OPS");		
		assertTrue(expander.expand(token).size() == 5);
	}
}
