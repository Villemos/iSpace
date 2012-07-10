package com.logica.oam.ktree.transformer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jxl.Sheet;
import jxl.Workbook;

import org.apache.camel.Body;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.logica.oam.ktree.types.ApplicationData;
import com.villemos.ispace.ktree.folder.Item;


/**
 * @author villemosg
 * 
 * Class for reordering a Map extracted from ktree, containing a entry keyed on
 * 'documents' and containing a list of all documents in Ktree.
 * 
 * The splitter reads in a list of Applications from another sheet, set through the
 * cidlPath property. It thereafter iterates through all documents and try to identify
 * which application the document belongs too, based on the documents path.
 * 
 * A new Map is created, with each entry keyed on the application name and containing 
 * a list of the documents assigned to this application. The entry 'NO APPLICATION' is
 * used to list all documents that could not be assigned to an application.
 * 
 * The new Map is set on the in.body of the exchange and parsed on in the route. 
 *
 */
public class OldApplicationSplitter {

	/** The logger. */
	private static final Log LOG = LogFactory.getLog(OldApplicationSplitter.class);

	/** The full path to the file containing a list of applications. */
	protected String cidlPath = "D:/Benutzer-Profile/villemosg/iSpace-ws/oam/NewDocBasSnapInputv2.xls";

	protected boolean collectNoApplication = false;

	/**
	 * Method to convert the Map listing all documents to a Map keyed on 
	 * application name and listing all documents assigned to the application.
	 * 
	 * @param exchange
	 */
	public void process(@Body Map<String, List<Object>> data) {

		/** The list of applications. */
		List<Object> applications = new ArrayList<Object>();
		List<Object> documents = data.get("documents");

		try {			
			
			/** Read in all applications listed in static list. */
			Workbook workbook = Workbook.getWorkbook(new File(cidlPath));			
			Sheet cidlData = workbook.getSheet("Versions and links");

			/** Find start row. */
			int row = 1;

			String name = "";
			
			/** Read in all applications listed in the CI list and record the release ID */
			while (row < cidlData.getRows()) {
								
				if (cidlData.getCell(0, row).getContents().equals("") == false && cidlData.getCell(3, row).getContents().equals("JA")) {
					name = cidlData.getCell(0, row).getContents();
					LOG.info("Importing APP " + name);
					applications.add(new ApplicationData(name, null, null, false, true));
					data.put(name, new ArrayList<Object>());
				}
								
				row++;	
			}			

			/** Iterate through all documents and insert in the data objects. */
			for (Object object : documents) {
				Item entry = (Item) object;

				/** If the document is part of the higher level documents, then insert it in the higher level tab.*/
				if (entry.inPath.startsWith("UNITs/PDGS SW Library/Documents/")) {
					if (data.containsKey("HigherLevel") == false) {
						data.put("HigherLevel", new ArrayList<Object>());
					}
					data.get("HigherLevel").add(entry);
					LOG.info("Added document '" + entry.inPath + "/" + entry.filename + "' as higher level.");
					continue;
				}

				/** Find out which application the document is part of. */
				boolean assigned = false;
				for (String knownAppName : data.keySet()) {
					
					if (entry.inPath.contains("/" + knownAppName + "/")) {
						assigned = true;
						data.get(knownAppName).add(entry);
						LOG.info("Assigned document '" + entry.inPath + "/" + entry.filename + "' with ID '" + entry.id + "' to application '" + knownAppName + "'.");
						break;						
					}
				}
				
				if (assigned == false) {
					LOG.warn("Failed to assigned document '" + entry.inPath + "/" + entry.filename + "' with ID '" + entry.id + "'.");

					if (collectNoApplication == true) {
						if (data.containsKey("NO APPLICATION") == false) {
							data.put("NO APPLICATION", new ArrayList<Object>());
						}
						data.get("NO APPLICATION").add(entry);
						LOG.info("Failed to locate application for document '" + entry.inPath + "/" + entry.filename + "' with ID '" + entry.id + "'.");
					}
				}
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}	

		data.remove("documents");
		data.remove("applications");
	}

	public String getCidlPath() {
		return cidlPath;
	}


	public void setCidlPath(String cidlPath) {
		this.cidlPath = cidlPath;
	}


}
