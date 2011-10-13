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
package com.villemos.ispace.enricher;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.apache.camel.Headers;

import com.villemos.ispace.api.InformationObject;
import com.villemos.ispace.core.utilities.IoFieldSetter;

public class PatternBasedEnricher extends RegularExpressionBuffer{

	protected String headerFieldName;

	protected String pattern;

	protected Map<Integer, String> groups = null;

	@Handler
	public void match(@Headers Map<String, Object> headers, @Body InformationObject io) {
		if (headers.containsKey(headerFieldName)) {
			Pattern thePattern = Pattern.compile(pattern);
			Matcher matcher = thePattern.matcher(headers.get(headerFieldName).toString());
			if (matcher.find()) {
				Iterator<Entry<Integer, String>> it = groups.entrySet().iterator();
				while (it.hasNext()) {
					Entry<Integer, String> entry = it.next();

					if (matcher.groupCount() >= entry.getKey()) {
						IoFieldSetter.setField(io, entry.getValue(), matcher.group(entry.getKey()));
					}
				}
			}
		}
	}

	public String getHeaderFieldName() {
		return headerFieldName;
	}

	public void setHeaderFieldName(String headerFieldName) {
		this.headerFieldName = headerFieldName;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public Map<Integer, String> getGroups() {
		return groups;
	}

	public void setGroups(Map<Integer, String> groups) {
		this.groups = groups;
	}
}
