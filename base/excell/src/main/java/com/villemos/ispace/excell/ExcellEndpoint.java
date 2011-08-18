/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
	protected String file = "C:/Users/Public/export.xls";
	
	/** The default names of the header and body sheet in the exported sheet. */
	protected String bodySheet = "body";
	protected String headerSheet = "header";

	/** If set to true, then the data read from the excell sheet will be streamed, i.e. each entry will
	 * be send in a separate exchange. */
	protected boolean stream = false;
	
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
	
	
}
