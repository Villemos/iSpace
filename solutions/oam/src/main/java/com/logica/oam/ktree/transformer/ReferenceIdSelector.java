package com.logica.oam.ktree.transformer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Body;

import com.logica.oam.ktree.statistics.Statistic;
import com.villemos.ispace.ktree.folder.Item;

public class ReferenceIdSelector {

	public void process(@Body Map<String, List<Object>> data) {

		long fromMetadata = 0;
		long fromHeader = 0;
		long fromTitle = 0;
		long fromCount = 0;
		long fromProperties = 0;

		Iterator<Entry<String, List<Object>>> it = data.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<Object>> entry = it.next();

			if (entry.getKey().equals("Statistics")) {
				continue;
			}

			for (Object object : entry.getValue()) {
				Item item = (Item) object;

				/** COpy the Ktree meta data */
				if (item.get("Reference ID").equals("") == false) {
					item.metadata.put("Reference ID (metadata)", item.metadata.get("Reference ID"));
					item.metadata.put("Reference ID", item.metadata.get(""));
				}

				/** If metadata set, then fine. */
				if (isProperReferenceId((String) item.get("Reference ID (metadata)"))) {
					fromMetadata++;
					item.metadata.put("Reference ID", item.metadata.get("Reference ID (metadata)"));
				}
				else if (isProperReferenceId((String) item.get("Reference ID (body doc ref)"))) {
					fromHeader++;
					item.metadata.put("Reference ID", item.metadata.get("Reference ID (body doc ref)"));
				}
				else if (isProperReferenceId((String) item.get("Reference ID (body misc 1)"))) {
					fromHeader++;
					item.metadata.put("Reference ID", item.metadata.get("Reference ID (body misc 1)"));
				}
				else if (isProperReferenceId((String) item.get("Reference ID (Title)"))) {
					fromTitle++;
					item.metadata.put("Reference ID", item.metadata.get("Reference ID (Title)"));
				}			
				else if (isProperReferenceId((String) item.get("Reference ID (body hits)"))) {
					fromCount++;
					item.metadata.put("Reference ID", item.metadata.get("Reference ID (body hits)"));
				}			
				else if (isProperReferenceId((String) item.get("Reference ID (property doc ref)"))) {
					fromProperties++;
					item.metadata.put("Reference ID", item.metadata.get("Reference ID (property doc ref)"));
				}			
			}
		}

		data.get("Statistics").add(new Statistic("Reference ID from metadata: (doc#)", fromMetadata));
		data.get("Statistics").add(new Statistic("Reference ID from body header: (doc#)", fromHeader));
		data.get("Statistics").add(new Statistic("Reference ID from title: (doc#)", fromTitle));
		data.get("Statistics").add(new Statistic("Reference ID from body count: (doc#)", fromCount));
		data.get("Statistics").add(new Statistic("Reference ID from doc properties: (doc#)", fromProperties));
	}
	
	protected boolean isProperReferenceId(String referenceId) {
		
		if (referenceId.equals("") || referenceId.equals("-") || referenceId.toLowerCase().equals("na")) {
			return false;
		}
		
		return true;
	}
}
