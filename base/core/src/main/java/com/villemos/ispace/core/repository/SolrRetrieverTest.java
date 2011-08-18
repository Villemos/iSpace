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
package com.villemos.ispace.core.repository;

import java.util.Arrays;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Test;

/**
 * @author villemosg
 *
 */
public class SolrRetrieverTest {

	/**
	 * Test method for {@link com.villemos.ispace.core.repository.SolrRetriever#retrieve(java.util.List, org.apache.camel.Exchange)}.
	 */
	@Test
	public void testRetrieve() {
		SolrRetriever retriever = new SolrRetriever();
		
		CamelContext context = null;
		Exchange exchange = new DefaultExchange(context);
		retriever.retrieve(Arrays.asList(new String[] {"ESAW"}), exchange);
	}

}
