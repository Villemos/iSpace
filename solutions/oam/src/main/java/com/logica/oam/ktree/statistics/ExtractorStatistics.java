package com.logica.oam.ktree.statistics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Body;

import com.villemos.ispace.ktree.folder.Item;

public class ExtractorStatistics {

	public void process(@Body Map<String, List<Object>> data) {

		/** Calculate the statistics; 
		 * 
		 *    - Total number of documents.
		 *    
		 *    - Total number of documents.
		 *    
		 *    - Documents lacking reference ID.
		 *    - Documents with reference ID (somewhere).
		 *    x Documents with reference ID (header or body).    
		 *    x Documents which is unreadable.
		 *     
		 *    - Documents where reference ID was in metadata
		 *    x Documents where reference ID was found in body (body header).
		 *    x Documents where reference ID was found in body (body properties).
		 *    x Documents where reference ID was found in body (count).
		 *    - Documents where reference ID was found in title.
		 *    - Documents where the reference ID is inconsistent.
		 *    
		 *    - Documents where Version was in metadata
		 *    - Documents where Version was found in body.
		 *    - Documents where Version was found in title.
		 *    - Documents where Version is inconsistent.
		 * 
		 * */

		List statistics = new ArrayList<Object>();

		int totalDocuments = 0;

		int lackingRefId = 0;
		int withRefId = 0;

		int unreadable = 0;

		int refIdMetadata = 0;

		int refIdBodyHeader = 0;
		int refIdBodyProperties = 0;
		int refIdBodyCount = 0;

		int refIdTitle = 0;
		int refIdInconsistent = 0;

		int lackingVersion = 0;
		int withVersion = 0;

		int versionMetadata = 0;
		int versionBody = 0;
		int versionTitle = 0;
		int versionInconsistent = 0;

		Iterator<Entry<String, List<Object>>> it = data.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<Object>> entry = it.next();

			if (entry.getKey().equals("Statistics")) {
				continue;
			}

			for (Object object : entry.getValue()) {
				Item item = (Item) object;

				totalDocuments++;

				if (item.get("Language").equals("en") == false || (Double) item.get("Language Probability") < 0.9d) {
					unreadable++;
				}

				if (	item.get("Reference ID").equals("") == true && 
						item.get("Reference ID (body doc ref)").equals("") == true && 
						item.get("Reference ID (body misc 1)").equals("") == true &&
						item.get("Reference ID (body hits)").equals("") == true &&
						item.get("Reference ID (property doc ref)").equals("") == true &&
						item.get("Reference ID (Title)").equals("") == true) {
					lackingRefId++;
				} 
				else {
					withRefId++;
				}

				if (item.get("Reference ID").equals("") == false) {refIdMetadata++;};

				if (item.get("Reference ID (body doc ref)").equals("") == false || item.get("Reference ID (body misc 1)").equals("") == false) {refIdBodyHeader++;};
				if (item.get("Reference ID (body hits)").equals("") == false) {refIdBodyCount++;};
				if (item.get("Reference ID (property doc ref)").equals("") == false) {refIdBodyProperties++;};

				if (item.get("Reference ID (Title)").equals("") == false) {refIdTitle++;};

				if (compare((String) item.get("Reference ID (Metadata)"), (String) item.get("Reference ID (Body)"), (String) item.get("Reference ID (Title)")) == false) {
					refIdInconsistent++;
				};

				if (item.get("Version (Metadata)").equals("") == true && item.get("Version (Body)").equals("") == true && item.get("Version (Title)").equals("") == true) {
					lackingVersion++;
				} 
				else {
					withVersion++;
				}

				if (item.get("Version (Metadata)").equals("") == false) {versionMetadata++;};
				if (item.get("Version (Body)").equals("") == false) {versionBody++;};
				if (item.get("Version (Title)").equals("") == false) {versionTitle++;};

				if (compare((String) item.get("Version (Metadata)"), (String) item.get("Version (Body)"), (String) item.get("Version (Title)")) == false) {
					versionInconsistent++;
				};
			}
		}

		statistics.add(new Statistic("Total number of documents: (doc#)", totalDocuments));

		statistics.add(new Statistic("Unreadable: (doc#)", unreadable));

		statistics.add(new Statistic("Reference ID found in body (body ref): (doc#)", refIdBodyHeader));
		statistics.add(new Statistic("Reference ID found in body (body properties): (doc#)", refIdBodyProperties));
		statistics.add(new Statistic("Reference ID found in body (body count): (doc#)", refIdBodyCount));

		statistics.add(new Statistic("Reference ID found in metadata: (doc#)", refIdMetadata));
		statistics.add(new Statistic("Reference ID found in title: (doc#)", refIdTitle));
		//statistics.add(new Statistic("Reference ID inconsistent (doc#)", refIdInconsistent));

		statistics.add(new Statistic("Lacking Reference ID: (doc#)", lackingRefId));
		statistics.add(new Statistic("With Reference ID: (doc#)", withRefId));

		//		statistics.add(new Statistic("Version found in body: (doc#)", versionBody));
		//		statistics.add(new Statistic("Version found in metadata: (doc#)", versionMetadata));
		//		statistics.add(new Statistic("Version found in title: (doc#)", versionTitle));
		//		statistics.add(new Statistic("Version inconsistent (doc#)", versionInconsistent));
		//		
		//		statistics.add(new Statistic("Lacking Version: (doc#)", lackingVersion));
		//		statistics.add(new Statistic("With Version: (doc#)", withVersion));

		data.put("Statistics", statistics);
	}

	protected boolean compare(String meta, String body, String title) {
		if (meta.equals("") == false && body.equals("") == false && meta.equals(body) == false) {
			return false;
		}

		if (meta.equals("") == false  && title.equals("") == false  && meta.equals(title) == false) {
			return false;
		}

		if (title.equals("") == false  && body.equals("") == false  && title.equals(body) == false) {
			return false;
		}

		return true;
	}
}
