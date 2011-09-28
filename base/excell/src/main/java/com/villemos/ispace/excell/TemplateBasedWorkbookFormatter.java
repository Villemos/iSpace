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
import java.util.Map.Entry;
import java.util.Set;

import org.apache.camel.Exchange;

import com.villemos.ispace.api.ResultSet;

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

	/** Full path to the spreadsheet that is the input template. */
	protected String template = null;

	/** Name of sheet to be used as generic Template if no template has been set above. */
	protected String sheetTemplateName = "Template";

	/** The sheet formatter to be used if no other is specified. */
	protected ISheetFormatter sheetTemplateFormatter = new DefaultSheetFormatter();

	protected void createSheetFormatter() {
		sheetFormatter = sheetTemplateFormatter;
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
			for (int column = 0 ; column < sheet.getColumns() ; column++) {
				WritableCell readCell = sheet.getWritableCell(column, row);
				WritableCell newCell = readCell.copyTo(column, row);
				CellFormat readFormat = readCell.getCellFormat();
				WritableCellFormat newFormat = new WritableCellFormat(readFormat);
				newCell.setCellFormat(newFormat);
				newSheet.addCell(newCell);
			}
		}
	}

	protected void createSheet(String sheetName, WritableWorkbook workbook) throws RowsExceededException, WriteException {
		/** See if we have a default template which we should use. */
		sheet = workbook.getSheet(sheetTemplateName);
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
	
	protected void createWorkbook(Exchange exchange) throws BiffException, IOException {
		/** Open the template workbook */
		String templateName = template;
		if (exchange.getIn().getHeader("template") != null) {
			templateName = (String) exchange.getIn().getHeader("template");
		}
		Workbook workbookIn = Workbook.getWorkbook(new File(templateName));

		/** Build file name name. */
		String newFileName = buildFileName(exchange);
				
		/** Copy it to the new workbook. */
		workbook = Workbook.createWorkbook(new File(newFileName), workbookIn);
	}
	
	protected synchronized void flushWorkbook() throws IOException, WriteException {
		if (workbook != null) {
			
			/** Write and flush the workbook. */
			workbook.write();
			workbook.close();
			workbook = null;
		}
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public Map<String, ISheetFormatter> getSheets() {
		return sheets;
	}

	public void setSheets(Map<String, ISheetFormatter> sheets) {
		this.sheets = sheets;
	}

	public String getSheetTemplateName() {
		return sheetTemplateName;
	}

	public void setSheetTemplateName(String sheetTemplateName) {
		this.sheetTemplateName = sheetTemplateName;
	}

	public ISheetFormatter getSheetTemplateFormatter() {
		return sheetTemplateFormatter;
	}

	public void setSheetTemplateFormatter(ISheetFormatter sheetTemplateFormatter) {
		this.sheetTemplateFormatter = sheetTemplateFormatter;
	}
}
