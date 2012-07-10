package com.logica.oam.ktree.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.villemos.ispace.ktree.folder.Item;

public class ElementFilter {

	/** The logger. */
	private static final Log LOG = LogFactory.getLog(ElementFilter.class);

	public static boolean keep(Item entry) {
		if (entry.document_type.equals("Installation Kit") || 
				entry.document_type.equals("Source Kit") ||
				entry.document_type.equals("Change Proposal") ||
				entry.document_type.equals("Change Request") ||
				entry.document_type.equals("Waiver") ||
				entry.document_type.equals("Software Problem Report") ||
				entry.document_type.equals("Deviation") ||
				entry.filename.toLowerCase().endsWith(".rar") ||
				entry.filename.toLowerCase().endsWith(".war") ||
				entry.filename.toLowerCase().endsWith(".jar") ||
				entry.filename.toLowerCase().endsWith(".exe") ||
				entry.filename.toLowerCase().endsWith(".zip") ||
				entry.filename.toLowerCase().endsWith(".gz") ||
				entry.filename.toLowerCase().endsWith(".tgz") ||
				entry.filename.toLowerCase().endsWith(".bin") ||
				entry.filename.toLowerCase().endsWith(".tar") ||
				entry.filename.contains("InstallationKit") ||
				entry.filename.contains("Installation Kit") ||
				entry.filename.contains("testdata") ||
				entry.filename.contains("SMR") ||
				entry.filename.toLowerCase().contains("software maintenance record") ||
				entry.filename.contains(".dmp.gz") ||
				// entry.inPath.contains("Installation Kit") ||
			   (entry.filename.toLowerCase().endsWith(".doc") == false && entry.filename.toLowerCase().endsWith(".docx") == false &&entry.filename.toLowerCase().endsWith(".pdf") == false && entry.filename.toLowerCase().endsWith(".txt") == false)	) {
			return false;
		}
		
		return true;
	}
	
	public void process(Exchange exchange) {
		/** Iterate through all documents and insert in the data objects. */
		List<Item> documents = ((Map<String, List<Item>>) exchange.getIn().getBody()).get("documents");
		
		List<Item> newList = new ArrayList<Item>(documents);
		
		for (Item entry : documents) {

			/** If the document is an installation kit or a source kit, then we remove it. */
			if (keep(entry) == false) {
				LOG.info("Removing entry '" + entry.filename+ "'.");
				newList.remove(entry);
			}
		}
		
		((Map<String, List<Item>>) exchange.getIn().getBody()).put("documents", newList);
	}
}
