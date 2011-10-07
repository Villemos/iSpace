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
package com.villemos.ispace.api;

import java.util.HashMap;
import java.util.Map;


/**
 * Data structure holding the definition of a facet. A facet is a specific field
 * with all the values associated with the field. 
 *
 */
public class Facet {

	/** The field for which the facet values are applicable. */
	public String field;
	
	/** Map keyed on facet value, with the value being a count of how often it occurs. */
	public Map<String, Long> values = new HashMap<String, Long>();
}