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

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.ScheduledPollEndpoint;

/**
 * Represents a HelloWorld endpoint.
 */
public class ExcellEndpoint extends ScheduledPollEndpoint {
	
	/** The file to be written to / read from. */
	protected String file = "C:/Users/Public/export";
	
	/** The default names of the header and body sheet in the exported sheet. */
	protected String bodySheet = "body";
	protected String headerSheet = "metadata";

	protected IWorkbookFormatter workbookFormatter = new DefaultWorkbookFormatter();
	
	/** If set to true, then the data read from the excell sheet will be streamed, i.e. each entry will
	 * be send in a separate exchange. */
	protected boolean stream = false;
	
	protected String timestamp = "yyyy-MM-dd_HH-MM-ss";
	
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

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getBodySheet() {
		return bodySheet;
	}

	public void setBodySheet(String bodySheet) {
		this.bodySheet = bodySheet;
	}

	public String getHeaderSheet() {
		return headerSheet;
	}

	public void setHeaderSheet(String headerSheet) {
		this.headerSheet = headerSheet;
	}

	public boolean isStream() {
		return stream;
	}

	public void setStream(boolean stream) {
		this.stream = stream;
	}

	public IWorkbookFormatter getWorkbookFormatter() {
		return workbookFormatter;
	}

	public void setWorkbookFormatter(IWorkbookFormatter workbookFormatter) {
		this.workbookFormatter = workbookFormatter;
	}
	
	
}
