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

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jxl.Workbook;
import jxl.format.CellFormat;
import jxl.read.biff.BiffException;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class TemplateBasedWorkbookFormatter extends DefaultWorkbookFormatter {

	private static final transient Logger LOG = LoggerFactory.getLogger(TemplateBasedWorkbookFormatter.class);
	
	@Override
	protected void createSheetFormatter(ExcellEndpoint endpoint) {
		sheetFormatter = new TemplateBasedSheetFormatter();
	}

	/**
	 * Method to copy a template sheet to another sheet.
	 * 
	 * The same should be possible with the 'workbook.copySheet()' method, but it
	 * doesnt seem to work. This one does. 
	 * 
	 * @param sheet The sheet being copied from.
	 * @param newSheet The sheet to copy to.
	 * @throws RowsExceededException Not happy
	 * @throws WriteException Not happy
	 */
	protected void copySheet(WritableSheet sheet, WritableSheet newSheet) throws RowsExceededException, WriteException {

		for (int row = 0 ; row < sheet.getRows() ; row++) {
			
			/** Set the width of the row. Weird way of doing it, but thats the jxlapi way. */
			// newSheet.setRowView(row, sheet.getRowView(row).);
			
			for (int column = 0 ; column < sheet.getColumns() ; column++) {
				
				/** Set the width of the column. Weird way of doing it, but thats the jxlapi way. */
				newSheet.setColumnView(column, sheet.getColumnView(column).getDimension());
				
				WritableCell readCell = sheet.getWritableCell(column, row);
				if (readCell.getClass() != jxl.biff.EmptyCell.class) {
					WritableCell newCell = readCell.copyTo(column, row);
					CellFormat readFormat = readCell.getCellFormat();
					WritableCellFormat newFormat = new WritableCellFormat(readFormat);
					newCell.setCellFormat(newFormat);
					newSheet.addCell(newCell);
				}
			}
		}
	}

	@Override
	protected void createSheet(String sheetName, WritableWorkbook workbook, ExcellEndpoint endpoint) throws RowsExceededException, WriteException {

		/** See if we have a template defined for this sheet name to use. */
		sheet = workbook.getSheet(endpoint.getSheetTemplateNames().get(sheetName));
		
		/** If we didnt find a specific template for this sheet, then see if we have a "Template" sheet defined. */
		if (sheet == null) {
			sheet = workbook.getSheet(endpoint.getSheetTemplateNames().get("Template"));
		} 
		
		if (sheet != null) {
			WritableSheet newSheet = workbook.createSheet(sheetName, workbook.getNumberOfSheets());
			copySheet(sheet, newSheet);
			sheet = newSheet;
		}
		/** Else simply create an empty sheet.*/
		else {
			sheet = workbook.createSheet(sheetName, workbook.getNumberOfSheets());
		}
	}

	@Override
	protected void createWorkbook(File newFile, Exchange exchange, ExcellEndpoint endpoint) throws IOException, BiffException {
		/** Open the template workbook */
		String templateName = endpoint.getTemplate();
		Workbook workbookIn = Workbook.getWorkbook(new File(templateName));

		/** Copy it to the new workbook. */
		if (newFile.getParentFile() != null) {
			newFile.getParentFile().mkdirs();
		}
		workbook = Workbook.createWorkbook(newFile, workbookIn);
		
		LOG.info("Creating spread sheet '" + newFile.getAbsolutePath() + "'.");
	}

	@Override
	protected synchronized void flushWorkbook() throws IOException, WriteException {
		if (workbook != null) {

			/** Remove the template sheet. */
			workbook.removeSheet(0);
			
			/** Write and flush the workbook. */
			workbook.write();
			workbook.close();
			workbook = null;
		}
		sheet = null;
		sheetFormatter = null;
		LOG.info("Done");
	}
}
