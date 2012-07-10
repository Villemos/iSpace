package com.logica.oam.ktree.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.villemos.ispace.ktree.folder.Item;

public class StatisticsCollector {

	/** The logger. */
	private static final Log LOG = LogFactory.getLog(StatisticsCollector.class);

	public void collect(Exchange exchange) {

		
		Map<String, List<Object>> documents = (Map<String, List<Object>>) exchange.getIn().getBody(); 
		
		List statistics = new ArrayList<Object>();

		Map<String, Integer> totalDiversity = new HashMap<String, Integer>();

		int totalDocuments = 0;
		int totalApplication = 0;

		int totalDocumentsAssigned = 0;
		int totalDocumentsNotAssigned = 0;

		int totalDocumentsLackingReferenceAndAssigned = 0;
		
		int totalInstallationKits = 0;
		
		long totalSizeOfLargeFiles = 0;
		
		long totalSizeInByte = 0;
		
		/** Map of sub statistics on documents lacking reference ID. Keyed on document type and holding a counter as value. */
		Map<String, Integer> statisticsOnDocumentsLackingReference = new HashMap<String, Integer>();

		/** Map of sub statistics on documents lacking reference ID. Keyed on document type and holding a counter as value. */
		Map<String, Integer> statisticsOnSpecificDocumentTypes = new HashMap<String, Integer>();

		Iterator<Entry<String, List<Object>>> it = documents.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<Object>> entry = it.next();

			if (entry.getKey().equals("Statistics")) {
				continue;
			}
			
			if (entry.getValue() != null) {				
				/** See if this is the list of not assigned documents. */
				if (entry.getKey().equals("NO APPLICATION")) {
					
					for (Object object : entry.getValue()) {
						Item doc = (Item) object;

						/** We ignore Installation Kits, except for total documents. */
						if (doc.document_type.toLowerCase().contains("installation kit")) {
							continue;
						}

						totalDocumentsNotAssigned++;
					}
				}
				else {
					totalApplication++;

					/** Collect application specific statistics. */
					int localDocumentsLackingReference = 0;
					Map<String, Integer> localDiversity = new HashMap<String, Integer>();

					for (Object object : entry.getValue()) {
						Item doc = (Item) object;

						/** We ignore Installation Kits, except for total documents. */
						if (doc.document_type.toLowerCase().contains("installation kit")) {
							continue;
						}
						
						totalDocumentsAssigned ++;
						
						/** Statistics on reference ID. */
						if (doc.get("Reference ID").equals("")) {
							localDocumentsLackingReference++;
							totalDocumentsLackingReferenceAndAssigned++;
						}

						/** Statistics on diversity. */
						if (localDiversity.get(doc.document_type) == null) {
							localDiversity.put(doc.document_type, 1);
						}
						else {
							Integer value = localDiversity.get(doc.document_type) + 1;
							localDiversity.put(doc.document_type, value);
						}
					}


					String strValue = "";
					String separator = "";
					Iterator<Entry<String, Integer>> localDiversityIterator = localDiversity.entrySet().iterator();
					while (localDiversityIterator.hasNext()) {
						Entry<String, Integer> localDiversityEntry = localDiversityIterator.next();
						strValue += separator + localDiversityEntry.getKey() + " (" + localDiversityEntry.getValue() + ")";
						separator = ", ";
					}
					
					// statistics.add(new Statistic(entry.getKey() + " count: doc# (-refID#)", entry.getValue().size() + " (" + localDocumentsLackingReference + ")"));
					// statistics.add(new Statistic(entry.getKey() + " diversity: doc type (doc#)", strValue));
				}
				
				/** No matter what... */
				for (Object object : entry.getValue()) {
					Item doc = (Item) object;

					totalSizeInByte += Integer.parseInt(doc.filesize);
					
					if (Integer.parseInt(doc.filesize) > 10000000) {
						LOG.warn("LARGE FILE " + doc.filesize + ", " + doc.filename);
						totalSizeOfLargeFiles += Integer.parseInt(doc.filesize);
					}
					
					/** ... except if installation kit */
					if (doc.document_type.toLowerCase().contains("installation kit")) {
						totalInstallationKits++;
						continue;
					}

					totalDocuments ++;

					if (doc.get("Reference ID") == null || doc.get("Reference ID").equals("")) {

						if (statisticsOnDocumentsLackingReference.get(doc.document_type) == null) {
							statisticsOnDocumentsLackingReference.put(doc.document_type, 0);
						}
						else {
							Integer value = statisticsOnDocumentsLackingReference.get(doc.document_type) + 1;
							statisticsOnDocumentsLackingReference.put(doc.document_type, value);
						}
					}

					if (doc.document_type.toLowerCase().contains("change proposal") ||
							doc.document_type.toLowerCase().contains("change request") ||
							doc.document_type.toLowerCase().contains("waiver") ||
							doc.document_type.toLowerCase().contains("sw release note") ||
							doc.document_type.toLowerCase().contains("problem report")) {
						if (statisticsOnSpecificDocumentTypes.get(doc.document_type) == null) {
							statisticsOnSpecificDocumentTypes.put(doc.document_type, 1);
						}
						else {
							Integer value = statisticsOnSpecificDocumentTypes.get(doc.document_type) + 1;
							statisticsOnSpecificDocumentTypes.put(doc.document_type, value);								
						}
					}

					if (totalDiversity.get(doc.document_type) == null) {
						totalDiversity.put(doc.document_type, 1);
					}
					else {
						Integer value = totalDiversity.get(doc.document_type) + 1;
						totalDiversity.put(doc.document_type, value);
					}
				}
			}
		}


		String strValue = "";
		String separator = "";
		Iterator<Entry<String, Integer>> totalDiversityIterator = totalDiversity.entrySet().iterator();
		while (totalDiversityIterator.hasNext()) {
			Entry<String, Integer> localDiversityEntry = totalDiversityIterator.next();
			strValue += separator + localDiversityEntry.getKey() + " (" + localDiversityEntry.getValue() + ")";
			separator = ", ";
		}
		statistics.add(new Statistic("Total diversity: doc type (doc#)", strValue));
		
		/** Map of sub statistics on documents lacking reference ID. Keyed on document type and holding a counter as value. */
		strValue = "";
		separator = "";
		Iterator<Entry<String, Integer>> statisticsOnDocumentsLackingReferenceIterator = statisticsOnDocumentsLackingReference.entrySet().iterator();
		while (statisticsOnDocumentsLackingReferenceIterator.hasNext()) {
			Entry<String, Integer> statisticsOnDocumentsLackingReferenceEntry = statisticsOnDocumentsLackingReferenceIterator.next();
			strValue += separator + statisticsOnDocumentsLackingReferenceEntry.getKey() + " (" + statisticsOnDocumentsLackingReferenceEntry.getValue() + ")";
			separator = ", ";
		}
		statistics.add(new Statistic("Total diversity of documents lacking reference: doc type (doc#)", strValue));		
		
		/** Map of sub statistics on documents lacking reference ID. Keyed on document type and holding a counter as value. */
		strValue = "";
		separator = "";
		Iterator<Entry<String, Integer>> statisticsOnSpecificDocumentTypesIterator = statisticsOnSpecificDocumentTypes.entrySet().iterator();
		while (statisticsOnSpecificDocumentTypesIterator.hasNext()) {
			Entry<String, Integer> statisticsOnSpecificDocumentTypesIteratorEntry = statisticsOnSpecificDocumentTypesIterator.next();
			strValue += separator + statisticsOnSpecificDocumentTypesIteratorEntry.getKey() + " (" + statisticsOnSpecificDocumentTypesIteratorEntry.getValue() + ")";
			separator = ", ";
		}
		// statistics.add(new Statistic("Total diversity of specific documents: doc type (doc#)", strValue));		

		statistics.add(new Statistic("Total number of documents: (doc#)", totalDocuments));
		statistics.add(new Statistic("Total number of applications: (app#)", totalApplication));
		statistics.add(new Statistic("Total number of documents assigned (doc#)", totalDocumentsAssigned));
		statistics.add(new Statistic("Total number of documents not assigned (doc#)", totalDocumentsNotAssigned));
		statistics.add(new Statistic("Total number of assigned documents lacking reference ID: (doc#)", totalDocumentsLackingReferenceAndAssigned));
		statistics.add(new Statistic("Total size: (bytes)", totalSizeInByte));
		statistics.add(new Statistic("Total size of Large Files: (bytes)", totalSizeOfLargeFiles));
		
		
		// statistics.add(new Statistic("Total number of installation kits: (doc#)", totalInstallationKits));

		if (documents.containsKey("statistics")) {
			documents.get("Statistics").addAll(statistics);
		}
		else {
			documents.put("Statistics", statistics);
		}
		// exchange.getIn().setBody(documents);
	}
}
