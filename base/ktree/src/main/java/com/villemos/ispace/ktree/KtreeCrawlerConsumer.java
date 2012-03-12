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
package com.villemos.ispace.ktree;

import java.util.concurrent.ScheduledExecutorService;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.villemos.ispace.httpcrawler.HttpCrawlerConsumer;

// public class KtreeCrawlerConsumer extends ScheduledPollConsumer {
public class KtreeCrawlerConsumer extends HttpCrawlerConsumer {

	private static final Log Logger = LogFactory.getLog(KtreeCrawlerConsumer.class);
	
	protected KtreeAccessor crawler = null;
	
	public KtreeCrawlerConsumer(DefaultEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
		crawler = new KtreeAccessor(endpoint, null);
	}

	public KtreeCrawlerConsumer(Endpoint endpoint, Processor processor, ScheduledExecutorService executor) {
		super(endpoint, processor, executor);
		crawler = new KtreeAccessor(endpoint, this);
	}

	@Override
	protected int poll() throws Exception {
		crawler.doPoll();	
		
		Exchange exchange = getEndpoint().createExchange();
		exchange.getIn().setBody(crawler.getResults());
		
		getAsyncProcessor().process(exchange, new AsyncCallback() {
			public void done(boolean doneSync) {
				Logger.trace("Done processing URL");
			}
		});

		return 0;
	}
}
