package com.villemos.ispace.enricher;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Handler;
import org.apache.camel.Headers;

public class PatternBasedEnricher {

	protected String headerFieldName;
	
	protected String pattern;
	
	protected Map<Integer, String> groups = null;
	
	@Handler
	public void match(@Headers Map<String, Object> headers) {
		if (headers.containsKey(headerFieldName)) {
			Pattern thePattern = Pattern.compile(pattern);
			Matcher matcher = thePattern.matcher(headers.get(headerFieldName).toString());
			if (matcher.find()) {
				Iterator<Entry<Integer, String>> it = groups.entrySet().iterator();
				while (it.hasNext()) {
					Entry<Integer, String> entry = it.next();
					
					if (matcher.groupCount() >= entry.getKey()) {
						headers.put("ispace.field." + entry.getValue(), matcher.group(entry.getKey()));
					}
				}
			}
		}
	}

	public String getHeaderFieldName() {
		return headerFieldName;
	}

	public void setHeaderFieldName(String headerFieldName) {
		this.headerFieldName = "ispace.field." + headerFieldName;
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
