package com.villemos.ispace.api;

import java.util.Date;

public class Statistics {

	/** The time at which the request was issued. */
	public Date timestamp = new Date();
	
	/** Total number of entries in the repository matching the query. The number returned may
	 * be less, depending on the number of 'rows' the retrieval request was issued with. */
	public long totalFound = 0;

	/** Total number of entries requested, i.e. the 'rows'. */
	public long totalRequested = 0;
	
	/** The total number of results returned. The value will be the lowest of 'rows' or the 
	 * 'totalfound'. */
	public long totalReturned = 0;
	
	/** Time in milliseconds it took to retrieve the results. This delta time does not include the 
	 * time spend in the iSpace system. */
	public long queryTime = 0;

	/** The maximum score returned. */
	public Float maxScore = 0f;
}
