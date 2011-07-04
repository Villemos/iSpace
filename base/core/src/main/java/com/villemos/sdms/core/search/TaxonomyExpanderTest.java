package com.villemos.sdms.core.search;

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
		expander.setTaxonomy(taxonomy);
		
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
