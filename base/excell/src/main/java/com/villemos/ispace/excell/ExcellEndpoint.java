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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.ScheduledPollEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcellEndpoint extends ScheduledPollEndpoint {

	private static final transient Logger LOG = LoggerFactory.getLogger(ExcellEndpoint.class);


	protected IWorkbookFormatter workbookFormatter = null;

	/** If set to true, then the data read from the excell sheet will be streamed, i.e. each entry will
	 * be send in a separate exchange. */
	protected boolean stream = false;

	/** The name of the input file. May be dynamically changed by inserting the following
	 * keywords;
	 *   '${TIMESTAMP}'. Will be replaced with a timestamp in the format defined by filenameDateFormat.
	 *   '${ID}'. Will be replaced with the value of the '${ID}' attribute. 
	 * */
	protected String filename = null;

	/** Format to be used when the filename ${TIMETSAMP} keyword is set. */
	protected String filenameDateFormat = "yyyy-MM-dd-HH-mm-ss";

	/** An ID that can be used to dynamically change the filename. */
	protected String filenameId = "1";

	/** Map of the sheet objects to be used for printing sheets. */
	protected Map<String, ISheetFormatter> sheets = new HashMap<String, ISheetFormatter> ();

	/** Full path to the spreadsheet that is the input template. */
	protected String template = null;

	protected ExcellFileConsumer consumer = new ExcellFileConsumer(this);

	/** Name of sheet to be used as generic Template if no template has been set above. */
	protected Map<String, String> sheetTemplateNames = new HashMap<String, String>();
	{
		sheetTemplateNames.put("Template", "Template");
		sheetTemplateNames.put("Statistics", "Statistics");
	}

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
	/** Date formatter: "09:04:00 2011" */
	protected String dateFormat = "EEE MMM d HH:mm:ss z yyyy";

	/** The fully qualified name of the class to be created. Can be used if no column in the 
	 * spreadsheet contains the class name. */
	protected String className = null;

	/** The column holding the class definition. Default value is 0. If the className is set, then
	 * this value is ignored. */
	protected int classColumn = 0;

	protected String defaultEncoding = "string";

	/** Defines whether data will be appended to the end of the excell spreadsheet or a new sheet created. */
	protected boolean appendMode = false;

	protected boolean sendStatus = false;

	protected List<String> ignore = new ArrayList<String>();
	
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

		String filename = this.filename;
		
		if (filename.contains("TIMESTAMP")) {
			if (filenameDateFormat == null) {
				LOG.error("The excell 'filename' option contains the keyword TIMESTAMP, but the option 'filenameDateFormat' has not been set.");
			}
			else {
				try {
					DateFormat df = new SimpleDateFormat(filenameDateFormat, Locale.UK);
					filename = filename.replaceAll("TIMESTAMP", df.format(new Date()));
				}
				catch (Exception e) {
					LOG.error("The option 'filenameDateFormat' has been set to an illegal value. Trying to format date resulted in a '" + e.toString() + "' exception.");
				}
			}
		}

		if (filename.contains("FILENAMEID")) {
			if (filenameDateFormat == null) {
				LOG.error("The excell 'filename' option contains the keyword FILENAMEID, but the option 'filenameId' has not been set.");
			}
			else {			
				filename = filename.replaceAll("FILENAMEID", filenameId);
			}
		}		

		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;

		if (this.filename.endsWith(".xls") == false) {
			this.filename = this.filename + ".xls";
		}
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



	public Map<String, String> getSheetTemplateNames() {
		return sheetTemplateNames;
	}

	public void setSheetTemplateNames(Map<String, String> sheetTemplateNames) {
		this.sheetTemplateNames = sheetTemplateNames;
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

	public String getDefaultEncoding() {
		return defaultEncoding;
	}

	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	public ExcellFileConsumer getConsumer() {
		return consumer;
	}

	public void setConsumer(ExcellFileConsumer consumer) {
		this.consumer = consumer;
	}

	public boolean isSendStatus() {
		return sendStatus;
	}

	public void setSendStatus(boolean sendStatus) {
		this.sendStatus = sendStatus;
	}

	public String getFilenameId() {
		return filenameId;
	}

	public void setFilenameId(String filenameId) {
		this.filenameId = filenameId;
	}

	public String getFilenameDateFormat() {
		return filenameDateFormat;
	}

	public void setFilenameDateFormat(String filenameDateFormat) {
		this.filenameDateFormat = filenameDateFormat;
	}

	public List<String> getIgnore() {
		return ignore;
	}

	public void setIgnore(String ignore) {
		
		for (String element : ignore.split(":")) {
			this.ignore.add(element);
		}
	}


}
