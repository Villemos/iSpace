package com.logica.oam.ktree.types;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.villemos.ispace.ktree.folder.Item;

public class ReleaseComperator implements Comparator {

	public int compare(Object o1, Object o2) {
		Item lhs = (Item) o1;
		Item rhs = (Item) o2;

		String newReleaseId = (String) lhs.metadata.get("Release ID");

		String oldReleaseId = (String) rhs.metadata.get("Release ID");
		
		Integer[] newId = parseReleaseString(newReleaseId);
		Integer[] oldId = parseReleaseString(oldReleaseId);
		
		if (newId[0] > oldId[0]) {
			return -1;
		}
		else if (newId[0] < oldId[0]) {
			return 1;
		}

		if (newId[1] > oldId[1]) {
			return -1;
		}
		else if (newId[1] < oldId[1]) {
			return 1;
		}

		if (newId[2] > oldId[2]) {
			return -1;
		}
		else if (newId[2] < oldId[2]) {
			return 1;
		}

		if (newId[3] > oldId[3]) {
			return -1;
		}
		else if (newId[3] < oldId[3]) {
			return 1;
		}

		return 0;
	}
	
	/** a.	3.00.00 can be 3.00 or 3.0 or 3.00.0 
	 *  b.	4.7 can be 4.07.00 or 4.07 or 4.07.0 
	 *  c.	3.01.00 can be 3.01 or 3.01.0
	 *  d.	1.4.1 can be 1.04.01 or 1.04.1
	 * */
	protected Integer[] parseReleaseString(String releaseIdStr) {

		Integer[] releaseId = {0, 0, 0, 0};

		if (releaseIdStr == null || releaseIdStr.toLowerCase().equals("unknown") || releaseIdStr.equals("")) {
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
