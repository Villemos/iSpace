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
package com.villemos.ispace.testclient;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.component.direct.DirectEndpoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.villemos.ispace.core.search.AutoCompletionProxy;

/**
 * Represents a direct endpoint that synchronously invokes the consumers of the
 * endpoint when a producer sends a message to it.
 * 
 * @version 
 */
public class TestClientEndpoint extends DirectEndpoint {

	private static final Log LOG = LogFactory.getLog(TestClientEndpoint.class);

	boolean allowMultipleConsumers = true;

	protected DataPrinter printer = null;
	
	protected AutoCompletionProxy autoCompletionProxy = null;
	
	public TestClientEndpoint(String uri, TestClientComponent component) {
		super(uri, component);
	}

	public Producer createProducer() throws Exception {
		throw new UnsupportedOperationException("Producer not supported for HttpCrawler endpoint. Sorry!");
	}

	public Consumer createConsumer(Processor processor) throws Exception {
		return new TestClientConsumer(this, processor);
	}

	public boolean isAllowMultipleConsumers() {
		return allowMultipleConsumers;
	}

	public void setAllowMultipleConsumers(boolean allowMutlipleConsumers) {
		this.allowMultipleConsumers = allowMutlipleConsumers;
	}

	public boolean isSingleton() {
		return true;
	}

	public DataPrinter getPrinter() {
		return printer;
	}

	public void setPrinter(DataPrinter printer) {
		this.printer = printer;
	}

	public AutoCompletionProxy getAutoCompletionProxy() {
		return autoCompletionProxy;
	}

	public void setAutoCompletionProxy(AutoCompletionProxy autoCompletionProxy) {
		this.autoCompletionProxy = autoCompletionProxy;
	}
}
