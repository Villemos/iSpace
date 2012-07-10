package com.logica.oam.ktree.enricher;

import java.util.List;
import java.util.Map;

import org.apache.camel.Body;

import com.villemos.ispace.ktree.folder.Item;

public class DomainDetector {
	public void process(@Body Map<String, List<Object>> documents) {

		for (Object object : documents.get("documents")) {
			Item item = (Item) object;
			
			/** Get first element in path. */
			String[] elements = item.inPath.split("/");
			item.metadata.put("domain", elements[0]);
		}
	}
}
