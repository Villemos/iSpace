package com.logica.oam.ktree.transformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.camel.Body;

import com.logica.oam.ktree.types.ReleaseComperator;
import com.villemos.ispace.ktree.folder.Item;

public class DuplicateMerger {

	public void process(@Body Map<String, List<Object>> documents) {

		Map<String, List<Item>> newMap = new TreeMap<String, List<Item>>();

		for (Object object : documents.get("documents")) {
			Item item = (Item) object;

			/** See if item is already known. */
			List<Item> existing = newMap.get(item.title.trim()+item.filesize.trim());
			if (existing == null) {
				existing = new ArrayList<Item>();
				existing.add(item);
				newMap.put(item.title.trim()+item.filesize.trim(), existing);
			}
			else {
				existing.add(item);
			}
		}

		List<Object> newDocuments = new ArrayList<Object>();

		/** Iterate through each list in the map */
		Iterator<Entry<String, List<Item>>> it = newMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<Item>> entry = it.next();

			/** Sort the list. */
			Collections.sort(entry.getValue(), new ReleaseComperator());

			/** The first entry is the one we are after, the other we mark as 'duplicates'. */
			Item uniqueItem = entry.getValue().get(0);
			String duplicates = "{";
			String separator = "";
			for (int index = 1; index < entry.getValue().size() ; index++) {
				duplicates += entry.getValue().get(index).metadata.get("Release ID") + " (doc#" + entry.getValue().get(1).id + ")" +separator;
				separator = ", ";
			}
			duplicates += "}";

			uniqueItem.metadata.put("duplicates", duplicates);

			newDocuments.add(uniqueItem);
		}

		documents.put("documents", newDocuments);
	}
}
