package com.logica.oam.ktree.transformer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Body;

import com.villemos.ispace.ktree.folder.Item;

public class DuplicateSimplifier {

	protected List<String> ignore = new ArrayList<String>();
	{
		ignore.add("Statistics");
		ignore.add("folders");
		ignore.add("applications");
	}
	
	public void process(@Body Map<String, List<Object>> documents) {
		
		Iterator<Entry<String, List<Object>>> it = documents.entrySet().iterator();
		
		while (it.hasNext()) {
			Entry<String, List<Object>> entry = it.next();
			
			if (ignore.contains(entry.getKey())) {
				continue;
			}
			
			/** Iterate through all items in the list and simplify the metadata field "duplications". */
			for (Object object : entry.getValue()) {
				Item item = (Item) object;
				if (item.metadata.get("duplicates").equals("{}")) {
					item.metadata.put("duplicates", "No");
				}
				else {
					item.metadata.put("duplicates", "Yes");
				}
			}
		}		
	}	
}
