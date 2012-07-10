/**
 * villemos solutions [space^] (http://www.villemos.com) 
 * Probe. Send. Act. Emergent solution. 
 * Copyright 2011 Gert Villemos
 * All Rights Reserved.
 * 
 * Released under the Apache license, version 2.0 (do what ever
 * you want, just dont claim ownership).
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of villemos solutions, and its suppliers
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to villemos solutions
 * and its suppliers and may be covered by European and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * 
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from villemos solutions.
 * 
 * And it wouldn't be nice either.
 * 
 */
package com.logica.oam.ktree.types;

import java.util.Comparator;

public class AlphabeticSorter implements Comparator {

	protected String fieldName = "title";

	public int compare(Object o1, Object o2) {

		int result = 0;

		try {

			String leftHandSide = (String) o1.getClass().getField(fieldName).get(o1);
			String rightHandSide = (String) o2.getClass().getField(fieldName).get(o2);

			if (leftHandSide.equals("HigherLevel")) {
				result = -1;
			}
			else if (rightHandSide.equals("HigherLevel")) {
				result = 1;
			}
			else {
				result = leftHandSide.compareToIgnoreCase(rightHandSide);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
}
