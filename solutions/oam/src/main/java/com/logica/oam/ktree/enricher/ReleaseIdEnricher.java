package com.logica.oam.ktree.enricher;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Body;

import com.villemos.ispace.ktree.folder.Item;

public class ReleaseIdEnricher {

	public void process(@Body Map<String, List<Object>> documents) {

		for (Object object : documents.get("documents")) {
			Item item = (Item) object;
			Pattern releasePattern = Pattern.compile("release ((\\d+).(\\d+)(.(\\d+))*(.(\\d+))*)");
			Matcher matcher = releasePattern.matcher(item.inPath.toLowerCase());
			if (matcher.find()) {
				item.metadata.put("Release ID", matcher.group(1));
			}
		}
	}
}
