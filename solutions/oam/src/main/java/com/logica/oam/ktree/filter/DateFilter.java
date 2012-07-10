package com.logica.oam.ktree.filter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.Headers;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.villemos.ispace.ktree.folder.Item;

public class DateFilter {

	/** The logger. */
	private static final Log LOG = LogFactory.getLog(DateFilter.class);
	
	protected Date from = new Date();
	
	protected Date to = new Date();

	protected String noChangeLabel = "Not Changed";
	protected String updateLabel = "Updated";
	protected String createLabel = "Created";
	
	
	public void process(@Body Map<String, List<Object>> documents, @Headers Map<String, Object> headers) {

		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DateFormat baseFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		/** See if the from / to has been set in the header. */
		if (headers.get("filterfromdate") != null) {
			try {
				from = baseFormat.parse((String) headers.get("filterfromdate"));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		if (headers.get("filtertodate") != null) {
			try {
				to = baseFormat.parse((String) headers.get("filtertodate"));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		for (Object object : documents.get("documents")) {
			Item item = (Item) object;
			
			try {
				Date creationDate = format.parse(item.created_date);
				Date updatedDate = format.parse(item.modified_date);
				
				boolean found = false;
				if (from.before(creationDate) && to.after(creationDate)) {
					item.metadata.put("last_change", createLabel);
					found = true;
				}
				if (from.before(updatedDate) && to.after(updatedDate) && creationDate.compareTo(updatedDate) != 0) {
					item.metadata.put("last_change", updateLabel);
					found = true;
				}
				
				if (found == false) {
					/** Neither changed nor updated. Remove. */
					item.metadata.put("last_change", noChangeLabel);
					LOG.info("Date filter REMOVED document " + item.absoluteFilename + " with creation date " + creationDate + " and updated date " + updatedDate);
				}
				else {
					LOG.info("Date filter KEPT document " + item.absoluteFilename + " with creation date " + creationDate + " and updated date " + updatedDate);
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
}
