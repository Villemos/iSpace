package com.logica.oam.ktree.enricher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Body;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.logica.oam.ktree.statistics.Statistic;
import com.villemos.ispace.ktree.folder.Item;

public class VersionEnricher {

	/** The logger. */
	private static final Log LOG = LogFactory.getLog(VersionEnricher.class);

	protected long updated = 0;
	protected long incon = 0;
	
	protected String name = "Version (Title)";	
	protected List<Integer> groups = new ArrayList<Integer>();
		
	protected Pattern thePattern = null;
	
	public void process(@Body Map<String, List<Object>> data) {

		for (Object object : data.get("documents")) {

			Item item = (Item) object;
			Matcher matcher = thePattern.matcher(item.title);
			if (matcher.find()) {
				
				/** Build the value*/
				String value = null;
				for (Integer group : groups) {
					String element = matcher.group(group).replaceAll("_", "").replaceAll("-", "").replaceAll("\\s", "");
					value = value == null ? element : value + "." + element;
				}				
				
				setValue(item, value, name);
			}
		}	

		addStatistics(data, "Version Enricher Type 1");
	}
	
	protected void setValue(Item item, String value, String name) {
		item.metadata.put(name, value);
		LOG.info("Enriching item ('" + item.title + "'), setting '" + name + "' = '" + value + "'.");
		
		if (item.log == null) {
			item.log = "Enriched: " + name + "=" + value;
		}
		else {
			item.log += "; " + "Enriched: " + name + "=" + value;
		}
		
		updated++;
	}
	
	protected void reportInconsistency(Item item, String value, String name) {
		LOG.error("Inconsistency in ('" + item.title + "')' " + name + "': (In Title) '" + value + "' != '" + item.metadata.get(name) + "' (in metadata).");
		incon++;
	}

	protected void addStatistics(Map<String, List<Object>> data, String identifier) {
		if (data.get("Statistics") == null) {
			data.put("Statistics", new ArrayList<Object>());
		}
		
		data.get("Statistics").add(new Statistic(identifier + " (updated)", updated));
		data.get("Statistics").add(new Statistic(identifier + " (inconsistencies)", incon));
	}

	public String getPattern() {
		return thePattern.toString();
	}

	public void setPattern(String pattern) {
		thePattern = Pattern.compile(pattern);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Integer> getGroups() {
		return groups;
	}

	public void setGroups(List<Integer> groups) {
		this.groups = groups;
	}
	
}
