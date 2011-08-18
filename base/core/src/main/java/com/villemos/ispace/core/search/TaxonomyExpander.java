/**
 * villemos consulting [space^] (http://www.villemos.de) 
 * Probe. Send. Act. Emergent solution.
 * 
 * Copyright 2011 Gert Villemos
 * All Rights Reserved.
 * Released under proprietary license, i.e. not free. But we are friendly.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of villemos consulting, and its suppliers
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to villemos consulting
 * and its suppliers and may be covered by European and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * 
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from villemos consulting Incorporated.
 * 
 * And it wouldn't be nice either.
 * 
 */
package com.villemos.ispace.core.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.springframework.beans.factory.annotation.Autowired;

import com.villemos.ispace.core.caches.ITaxonomyManager;


/** The taxonomy expander expands the elements of a search queue based on a taxonomy, i.e.
 *  based on a hierarchical classification. 
 *  
 *  Consider the following taxonomy
 *  	OPS
 *  		OPS-G
 *  			OPS-GI
 *  			OPS-GM
 *  		OPS-B
 *      TEC
 *  
 *  When searching for OPS, any element that has OPS-G, OPS-GI, OPS-GM or OPS-B is also 
 *  taxonomically interesting. This expander will create the string
 *  
 *    "OPS"   => "OPS OR OPS-G OR OPS-GI OR OPS-GM OR OPS-B"
 *    "OPS-G" => "OPS-G OR OPS-GI OR OPS-GM"
 *  
 */
public class TaxonomyExpander {

	@Autowired
	protected ITaxonomyManager taxonomyManager = null;

	@Handler
	public List<String> expand(@Body List<String> tokens) {
		
		/** Break the body into tokens. Then replace each token with all synonym values. */
		List<String> newTokens = new ArrayList<String>();
		for (String token : tokens) {
			newTokens.addAll(recursiveExpand(token));
			newTokens.add(token);
		}
		
		return newTokens;
	}
	
	protected List<String> recursiveExpand(String token) {

		List<String> newTokens = new ArrayList<String> ();
		
		List<String> elements = taxonomyManager.getTaxonomy().get(token);
		if (elements != null) {
			for (String nextToken : elements) {
				newTokens.addAll(recursiveExpand(nextToken));
				newTokens.add(nextToken);
			}
		}
		
		return newTokens;
	}

	public void setTaxonomyManager(ITaxonomyManager taxonomyManager) {
		this.taxonomyManager = taxonomyManager;
	}
}
