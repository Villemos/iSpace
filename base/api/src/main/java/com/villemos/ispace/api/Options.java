package com.villemos.ispace.api;

public class Options {

	/** Header field indicating that this query is a 'normal' query. */
	public static final String query = "ispace.query";
	
	public static final String facets = "ispace.facet";
	
	public static final String delete = "ispace.delete";
	
	public static final String insert = "ispace.insert";

	public static final String comments = "ispace.comments";
	
	public static final String commit = "ispace.commit";
	
	/** Flag indicating that only the count should be returned. */
	public static final String count = "ispace.count";

	/** Header field indicating that the results should be streamed back. The field value must be a ICallback object. */
	public static final String stream = "ispace.stream";
		
	/** The offset of the retrieval. Can be used to retrieve 'pages' of data, i.e. first 10 results (offset=0), then 10 more (offset=10), then 10 more (offset=20), etc. */
	public static final String offset = "ispace.option.start";
	public static final String facetsort = "ispace.option.facetsort";
	public static final String facetlimit = "ispace.option.facetlimit";
	public static final String facetprefix = "ispace.option.facetprefix";
	public static final String facetfield = "ispace.option.facetfield";	
	public static final String rows = "ispace.option.rows";

}
