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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.villemos.ispace.api.ResultSet;

public class DefaultWorkbookFormatter implements IWorkbookFormatter {

	private static final transient Logger LOG = LoggerFactory.getLogger(DefaultWorkbookFormatter.class);
	
	protected WritableWorkbook workbook = null;

	protected ISheetFormatter sheetFormatter = null;

	protected WritableSheet sheet = null;
	
	@Override
	public synchronized void add(Exchange exchange, ExcellEndpoint endpoint) {
		Object object = exchange.getIn().getBody();
		try {
			/** In streaming mode the work book is closed at intervals. */
			if (workbook == null) {
				createWorkbook(exchange, endpoint);
			}

			/** Insert the data. */
			if (object instanceof ResultSet) {
				/** TODO */
			}
			else if (object instanceof List) {
				insertData("data", workbook, (Collection) object, endpoint);
			}
			else if (object instanceof Set) {
				insertData("data", workbook, (Collection) object, endpoint);
			}
			else if (object instanceof Map) {
				Iterator<Entry<String, List>> it = ((Map<String, List>) object).entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, List> entry = it.next();
					sheet = null;
					sheetFormatter = null;
					insertData(entry.getKey(), workbook, entry.getValue(), endpoint);
				}
			}
			else {
				insertData("data", workbook, Arrays.asList(new Object[] {object}), endpoint);
			}

			/** In stream mode someone else must call 'flushWorkbook' */
			if (endpoint.isStream() == false) {
				flushWorkbook();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void insertData(String sheetName, WritableWorkbook workbook, Collection objects, ExcellEndpoint endpoint) throws RowsExceededException, WriteException {
		if (sheetFormatter == null) {
			sheetFormatter = endpoint.getSheets().get(sheetName);
		}

		/** Use the template sheet formatter which has either been configured through 
		 * the route configuration or will be the default formatter. */
		if (sheetFormatter == null) {
			createSheetFormatter(endpoint);
		}

		/** see if the sheet already exist. */
		if (sheet == null) {
			sheet = workbook.getSheet(sheetName);
		}
		/** If it doesnt exist, then...*/
		if (sheet == null) {
			createSheet(sheetName, workbook, endpoint);
		}

		/** Write the data to the sheet, using the formatter. */
		sheetFormatter.add(objects, sheet, endpoint);
		
		/** Delete the template sheet from the workbook. */
		/** TODO */
	}

	protected void createSheetFormatter(ExcellEndpoint endpoint) {
		sheetFormatter = new DefaultSheetFormatter();
	}
	
	protected void createSheet(String sheetName, WritableWorkbook workbook, ExcellEndpoint endpoint) throws RowsExceededException, WriteException {
		 sheet = workbook.createSheet(sheetName, workbook.getNumberOfSheets());
	}

	protected void createWorkbook(Exchange exchange, ExcellEndpoint endpoint) throws BiffException, IOException {
		String newFileName = buildFileName(exchange, endpoint);
		File newFile = new File(newFileName);
		workbook = Workbook.createWorkbook(newFile);
		LOG.info("Creating spread sheet '" + newFile.getAbsolutePath() + "'.");		
	}	

	protected String buildFileName(Exchange exchange, ExcellEndpoint endpoint) {
		String newFileName = endpoint.getFilename();
		
		/** If the name has not been configured, then use the timestamp format to create a name. */
		if (newFileName == null) {
			SimpleDateFormat formatter = new SimpleDateFormat(endpoint.getTimestamp());
			newFileName = formatter.format(new Date()); 
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