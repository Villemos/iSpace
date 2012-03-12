package com.villemos.ispace.api;

public class SolrOptions {

	/** Header field indicating that this query is a 'normal' query. */
	public static final String query = "solr.option.query";
	
	public static final String delete = "solr.delete";
	
	public static final String facets = "solr.option.facets";
	
	public static final String insert = "solr.option.insert";

	public static final String comments = "solr.option.comments";
	
	public static final String commit = "solr.option.commit";
	
	/** Flag indicating that only the count should be returned. */
	public static final String count = "solr.option.count";

	/** Header field indicating that the results should be streamed back. The field value must be a ICallback object. */
	public static final String stream = "solr.option.stream";
		
	/** The offset of the retrieval. Can be used to retrieve 'pages' of data, i.e. first 10 results (offset=0), then 10 more (offset=10), then 10 more (offset=20), etc. */
	public static final String offset = "solr.option.start";
	public static final String facetsort = "solr.option.facetsort";
	public static final String facetlimit = "solr.option.facetlimit";
	public static final String facetprefix = "solr.option.facetprefix";
	public static final String facetfield = "solr.option.facetfield";	
	public static final String rows = "solr.option.rows";

}
