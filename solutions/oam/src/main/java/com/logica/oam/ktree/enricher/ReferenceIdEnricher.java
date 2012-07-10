package com.logica.oam.ktree.enricher;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Body;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.villemos.ispace.ktree.folder.Item;

public class ReferenceIdEnricher extends VersionEnricher {

	/** The logger. */
	private static final Log LOG = LogFactory.getLog(ReferenceIdEnricher.class);

	// protected String pattern = "(\\p{Alpha}\\p{Alnum}{1,}-\\p{Alpha}{2,}-\\p{Alpha}{2,}-\\p{Alpha}{2,}-\\d{2,}(-\\d{2,})*)" +
	protected String pattern = "((\\p{Alpha}\\p{Alnum}{1,}-\\p{Alpha}{2,}-)*\\p{Alpha}{2,}-\\p{Alpha}{2,}-(\\d|X){2,}(-\\d{2,})*)";
	protected Integer group = 1;
	protected String name = "Reference ID (Title)";

	public void process(@Body Map<String, List<Object>> data) {

		Pattern thePattern = Pattern.compile(pattern);

		for (Object object : data.get("documents")) {
			Item item = (Item) object;
			Matcher matcher = thePattern.matcher(item.title);
			if (matcher.find()) {
				String value = matcher.group(group).trim().toUpperCase();
				setValue(item, value, name);
			}
		}	

		addStatistics(data, "Reference ID Enricher");
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public Integer getGroup() {
		return group;
	}

	public void setGroup(Integer group) {
		this.group = group;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}