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

package com.villemos.ispace.excell;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.camel.Exchange;

import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.villemos.ispace.api.ResultSet;

public class DefaultWorkbookFormatter implements IWorkbookFormatter {

	/** The name of the output file. A timestamp will be appended and the 
	 * postfix '.xls' */
	protected String filename = "Output";

	protected String timestamp = "yyyy-MM-dd-HH-mm-ss";
	
	/** Map of the sheet objects to be used for printing sheets. */
	protected Map<String, ISheetFormatter> sheets = new HashMap<String, ISheetFormatter> ();

	protected WritableWorkbook workbook = null;

	protected ISheetFormatter sheetFormatter = null;

	protected WritableSheet sheet = null;
	
	@Override
	public synchronized void add(Exchange exchange, ExcellEndpoint endpoint) {
		Object object = exchange.getIn().getBody();
		try {
			/** In streaming mode the work book is closed at intervals. */
			if (workbook == null) {
				createWorkbook(exchange);
			}

			/** Insert the data. */
			if (object instanceof ResultSet) {
				/** TODO */
			}
			else if (object instanceof List) {
				insertData("data", workbook, (Collection) object);
			}
			else if (object instanceof Set) {
				insertData("data", workbook, (Collection) object);
			}
			else if (object instanceof Map) {
				Iterator<Entry<String, List>> it = ((Map<String, List>) object).entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, List> entry = it.next();
					sheet = null;
					sheetFormatter = null;
					insertData(entry.getKey(), workbook, entry.getValue());
				}
			}
			else {
				insertData("data", workbook, Arrays.asList(new Object[] {object}));
			}

			/** In stream mode someone else must call 'flushWorkbook' */
			if (endpoint.isStream() == false) {
				flushWorkbook();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void insertData(String sheetName, WritableWorkbook workbook, Collection objects) throws RowsExceededException, WriteException {
		if (sheetFormatter == null) {
			sheetFormatter = sheets.get(sheetName);
		}

		/** Use the template sheet formatter which has either been configured through 
		 * the route configuration or will be the default formatter. */
		if (sheetFormatter == null) {
			createSheetFormatter();
		}

		/** see if the sheet already exist. */
		if (sheet == null) {
			sheet = workbook.getSheet(sheetName);
		}
		/** If it doesnt exist, then...*/
		if (sheet == null) {
			createSheet(sheetName, workbook);
		}

		/** Write the data to the sheet, using the formatter. */
		sheetFormatter.add(objects, sheet);
	}

	protected void createSheetFormatter() {
		sheetFormatter = new DefaultSheetFormatter();
	}
	
	protected void createSheet(String sheetName, WritableWorkbook workbook) throws RowsExceededException, WriteException {
		 sheet = workbook.createSheet(sheetName, workbook.getNumberOfSheets());
	}

	protected void createWorkbook(Exchange exchange) throws BiffException, IOException {
		workbook = Workbook.createWorkbook(new File(buildFileName(exchange)));
	}	

	protected String buildFileName(Exchange exchange) {
		String newFileName = "";
		
		/** If the exchange holds a filename in the header, then use that. */
		if (exchange.getIn().getHeader("filename") != null) {
			newFileName = (String) exchange.getIn().getHeader("filename");
		}
		/** Else build a name using the timestamp. */
		else {
			SimpleDateFormat formatter = new SimpleDateFormat(timestamp);
			newFileName = filename + "_" + formatter.format(new Date()) + ".xls"; 
		}
		
		return newFileName;
	}
	
	protected synchronized void flushWorkbook() throws IOException, WriteException {
		if (workbook != null) {
			/** Write and flush the workbook. */
			workbook.write();
			workbook.close();
			workbook = null;
		}
		sheet = null;
		sheetFormatter = null;
	}
}
