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

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.ScheduledPollEndpoint;

/**
 * Represents a HelloWorld endpoint.
 */
public class ExcellEndpoint extends ScheduledPollEndpoint {
	
	protected IWorkbookFormatter workbookFormatter = null;
	
	/** If set to true, then the data read from the excell sheet will be streamed, i.e. each entry will
	 * be send in a separate exchange. */
	protected boolean stream = false;
	
	/** The name of the input file. */
	protected String filename = null;

	/** Timestamp to be appended to filename. */
	protected String timestamp = "'excell-output'-yyyy-MM-dd-HH-mm-ss'.xls'";
	
	/** Map of the sheet objects to be used for printing sheets. */
	protected Map<String, ISheetFormatter> sheets = new HashMap<String, ISheetFormatter> ();

	/** Full path to the spreadsheet that is the input template. */
	protected String template = null;

	/** Name of sheet to be used as generic Template if no template has been set above. */
	protected String sheetTemplateName = "Template";

	/** The sheet formatter to be used if no other is specified. */
	protected ISheetFormatter sheetTemplateFormatter = new DefaultSheetFormatter();

	/** Map keyed on Class object field names and with the value to be applied when creating
	 * the spreadsheet. As an example the Class object field 'documentName' can be 
	 * converted to 'Document Title' in the spreadsheet, by inserting the key-value
	 * pair 'documentName'-'Document Title'. */
	protected Map<String, String> fieldNames = null;

	/** The first row to be used to insert a row. If set to -1, then the first row is the first
	 * free row. */
	protected int startRow = -1;
	
	/** The last row to be processed. If -1, then all rows are processed. Else the row set
	 * by this value is the last to be processed. */
	protected int endRow = -1;
	
	/** Format to be used when dates are written / read in as strings. */
	protected String dateFormat = null;
	
	/** The fully qualified name of the class to be created. Can be used if no column in the 
	 * spreadsheet contains the class name. */
	protected String className = null;
	
	/** The column holding the class definition. Default value is 0. If the className is set, then
	 * this value is ignored. */
	protected int classColumn = 0;
	
	/** Defines whether data will be appended to the end of the excell spreadsheet or a new sheet created. */
	protected boolean appendMode = false;
	
	public ExcellEndpoint() {
    }

    public ExcellEndpoint(String uri, ExcellComponent component) {
        super(uri, component);
    }

    public ExcellEndpoint(String endpointUri) {
        super(endpointUri);
    }

    public Producer createProducer() throws Exception {
        return new ExcellProducer(this);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        return new ExcellConsumer(this, processor);
    }

    public boolean isSingleton() {
        return true;
    }

	public boolean isStream() {
		return stream;
	}

	public void setStream(boolean stream) {
		this.stream = stream;
	}

	public IWorkbookFormatter getWorkbookFormatter() {
		if (workbookFormatter == null) {
			if (template != null) {
				workbookFormatter = new TemplateBasedWorkbookFormatter();
			}
			else {
				workbookFormatter = new DefaultWorkbookFormatter();
			}
		}
		
		return workbookFormatter;
	}

	public void setWorkbookFormatter(IWorkbookFormatter workbookFormatter) {
		this.workbookFormatter = workbookFormatter;
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

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
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

	public Map<String, String> getFieldNames() {
		return fieldNames;
	}

	public void setFieldNames(Map<String, String> fieldNames) {
		this.fieldNames = fieldNames;
	}

	public int getStartRow() {
		return startRow;
	}

	public void setStartRow(int startRow) {
		this.startRow = startRow;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public int getClassColumn() {
		return classColumn;
	}

	public void setClassColumn(int classColumn) {
		this.classColumn = classColumn;
	}

	public int getEndRow() {
		return endRow;
	}

	public void setEndRow(int endRow) {
		this.endRow = endRow;
	}

	public boolean isAppendMode() {
		return appendMode;
	}

	public void setAppendMode(boolean appendMode) {
		this.appendMode = appendMode;
	}
}
