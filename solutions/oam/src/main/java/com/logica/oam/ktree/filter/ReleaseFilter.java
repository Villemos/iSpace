package com.logica.oam.ktree.filter;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Body;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.logica.oam.ktree.types.AlphabeticSorter;
import com.villemos.ispace.ktree.folder.Item;

public class ReleaseFilter {

	/** The logger. */
	private static final Log LOG = LogFactory.getLog(ReleaseFilter.class);

	protected Comparator sorter = new AlphabeticSorter();

	public void process(@Body Map<String, List<Item>> data) {

		Map<String, Item> newDocumentMap = new HashMap<String, Item>();

		try {
			for (Object object : data.get("documents")) {
				Item doc = (Item) object;

				/** If the documet has a reference ID, then we only take the latest document with the ID.
				 * Else we take all documents.*/

				String referenceId = (String) doc.get("Reference ID");
				if (referenceId == null || referenceId.equals("")) {
					/** If there is no referenceID, then we store the document under a random UUID
					 * to ensure that the document is included even if other documents exist with the
					 * same title. */
					newDocumentMap.put(UUID.randomUUID().toString(), doc);
				}
				else {

					/** See if we know the document already. The identification is based on the 
					 * reference ID of the document. We keep only one document per reference ID. */
					if (newDocumentMap.containsKey(referenceId)) {

						/** See if this release if higher. */
						Item theEntry = newDocumentMap.get(referenceId);
						Integer[] oldId = parseReleaseString((String) theEntry.get("Version"));
						Integer[] newId = parseReleaseString((String) doc.get("Version"));

						if (isHigher(oldId, newId)) {
							/** If this is a later release, then overwrite. */
							LOG.info("Replacing document '" + theEntry.title + "' with reference ID '" + referenceId + "' of release '" + theEntry.get("Version") + "', replaced with release '" + doc.get("Version") + "'.");
							newDocumentMap.put(referenceId, doc);
						}
					}
					else {
						newDocumentMap.put(referenceId, doc);
					}
				}
			}

			List<Item> newEntries = new LinkedList<Item>();
			for (Item toAdd : newDocumentMap.values()) {
				newEntries.add(toAdd);
			}
			Collections.sort(newEntries, sorter);

			data.put("documents", newEntries);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	

	protected boolean isHigher(Integer[] oldId, Integer[] newId) {

		if (newId == null || oldId == null || newId[0] == null || oldId[0] == null) {
			LOG.error("null pointer ahead!");
		}

		if (newId[0] > oldId[0]) {
			return true;
		}
		else if (newId[0] < oldId[0]) {
			return false;
		}

		if (newId[1] > oldId[1]) {
			return true;
		}
		else if (newId[1] < oldId[1]) {
			return false;
		}

		if (newId[2] > oldId[2]) {
			return true;
		}
		else if (newId[2] < oldId[2]) {
			return false;
		}

		if (newId[3] > oldId[3]) {
			return true;
		}
		else if (newId[3] < oldId[3]) {
			return false;
		}

		return false;
	}

	/** a.	3.00.00 can be 3.00 or 3.0 or 3.00.0 
	 *  b.	4.7 can be 4.07.00 or 4.07 or 4.07.0 
	 *  c.	3.01.00 can be 3.01 or 3.01.0
	 *  d.	1.4.1 can be 1.04.01 or 1.04.1
	 * */
	protected Integer[] parseReleaseString(String releaseIdStr) {

		Integer[] releaseId = {0, 0, 0, 0};

		if (releaseIdStr.toLowerCase().equals("unknown") || releaseIdStr.equals("")) {
			/** All matches. */
		}
		else {
			Pattern releasePattern = Pattern.compile("(\\d+).(\\d+)(.(\\d+))*(.(\\d+))*");
			Matcher matcher = releasePattern.matcher(releaseIdStr);

			if (matcher.find()) {
				releaseId[0] = Integer.parseInt(matcher.group(1));
				releaseId[1] = Integer.parseInt(matcher.group(2));
				if (matcher.groupCount() > 3 && matcher.group(4) != null) {
					releaseId[2] = Integer.parseInt(matcher.group(4));
				}

				if (matcher.groupCount() > 5 && matcher.group(6) != null) {
					releaseId[3] = Integer.parseInt(matcher.group(6));
				}
			}
		}

		return releaseId;
	}
}
