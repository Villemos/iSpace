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
package com.villemos.sdms.core.detector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultExchange;
import org.springframework.beans.factory.annotation.Autowired;

public class AcronymDetector {

	@Autowired
	protected CamelContext context = null;
	
	@Autowired
	protected ProducerTemplate producer = null;
	
	/** Map of patterns. The key is the pattern itself. It will have a number of groups, of which one is the
	 * acronym itself, the other the definition of the acronym. These groups are identified in the integer list, i.e.
	 * - first entry is the group ID of the definition 'This Is Test'. 
	 * - second entry is the group ID of the acronym itself 'TIT'*/
	protected Map<Pattern, List<Integer>> patterns = new HashMap<Pattern, List<Integer>>();
	{
		/** Matches the form 'TIT This Is Test' and '(TIT) This Is Test'*/
		patterns.put(Pattern.compile("\\(*(\\p{Upper})(\\p{Upper})(\\p{Upper})\\)*\\s+(\\1\\w+\\s+.*?\\3\\w+)"), Arrays.asList(new Integer[] {5, 1}));
		
		/** Matches the form 'This Is Test TIT' and 'This Is Test (TIT)'*/
		patterns.put(Pattern.compile("((\\p{Upper})\\w+\\s+(\\p{Upper})\\w+\\s+(\\p{Upper})\\w+)\\s+\\(*(\\2\\3\\4)\\)*"), Arrays.asList(new Integer[] {1, 5}));
		
		/** Matches the form 'TIT (This Is Test)'*/
		patterns.put(Pattern.compile("(\\p{Upper})(\\p{Upper})(\\p{Upper})\\s+\\(*(\\1\\w+\\s+.*?\\3\\w+)\\)*"), Arrays.asList(new Integer[] {1, 5}));
	}
	
	public void detectAcronyms(@Body String text) {
		Iterator<Entry<Pattern, List<Integer>>> it = patterns.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Pattern, List<Integer>> entry = it.next();
			Matcher matcher = entry.getKey().matcher(text) ;
			while (matcher.find()) {
				String definition = matcher.group(entry.getValue().get(0));
				String acronym = matcher.group(entry.getValue().get(1));
				
				/** Check if acronym exist already. */
				Exchange exchange = new DefaultExchange(context, ExchangePattern.InOut);
				exchange.getIn().setBody("uri:\"" + UriUtility.createAcronymUri(acronym, definition) + "\"");
				Exchange reply = producer.send(exchange);
				
				if (reply.getOut() == null) {
					
					/** Create acronym entry. */
					
				}
			}
		}
	}	
}
