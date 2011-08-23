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
package com.villemos.ispace;

public class Fields {

	/** Header field indicating that this query is a 'normal' query. */
	public static final String query = "ispace.query";

	/** Header field indicating that this query is a 'facet' query. */
	public static final String facetquery = "ispace.facetquery";

	/** Header field indicating that the results shouild be streamed back. The field value must be a ICallback object. */
	public static final String stream = "ispace.stream";
	
	/** Flag indicating that only the count should be returned. */
	public static final String count = "ispace.count";
	
	/** The offset of the retrieval. Can be used to retrieve 'pages' of data, i.e. first 10 results (offset=0), then 10 more (offset=10), then 10 more (offset=20), etc. */
	public static final String offset = "ispace.option.start";
	
	public static final String rows = "ispace.option.rows";
	
	public static final String prefix = "ispace.field.";
	
	public static final String hasUri = "hasUri";
	public static final String wasStoredAt = "wasStoredAt";
	public static final String hasTitle = "hasTitle";
	public static final String hasTitleText = "hasTitleText";
	public static final String fromSource = "fromSource";
	public static final String ofMimeType = "ofMimeType";
	public static final String ofDocumentType = "ofDocumentType";
	public static final String withRawText = "withRawText";
	public static final String withAttachedLog = "withAttachedLog";
	public static final String withReferenceId = "withReferenceId";
	public static final String withIssue = "withIssue";
	public static final String withRevision = "withRevision";
	public static final String hasRootValue = "hasRootValue";
	public static final String hasState = "hasState";
	public static final String isApplicableToField = "isApplicableToField";   
	
	/** Field used to define taxonomy. */
	public static final String partOf = "partOf";
}
