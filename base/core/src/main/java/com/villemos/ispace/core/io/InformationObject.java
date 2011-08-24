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
package com.villemos.ispace.core.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class holding an InformationObject, being a entry in a repository.
 * 
 * @author villemosg
 *
 */
public class InformationObject {

	/** The field values of the found document. The value is a list, with 0 or more entries. Each
	 * entry is a value assigned to this document. */
	public Map<String, Collection<Object>> values = new HashMap<String, Collection<Object>>();
	
	/** Extract(s) from the main content field, in which the search criterion occurs. */
	public List<String> highlight = new ArrayList<String>();
}
