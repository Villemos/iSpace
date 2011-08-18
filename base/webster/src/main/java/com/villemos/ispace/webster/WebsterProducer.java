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
package com.villemos.ispace.webster;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.villemos.ispace.httpcrawler.HttpClientConfigurer;

/**
 * The HelloWorld producer.
 */
public class WebsterProducer extends DefaultProducer {

	private static final transient Logger LOG = LoggerFactory.getLogger(WebsterProducer.class);

	private WebsterEndpoint endpoint;

	public WebsterProducer(WebsterEndpoint endpoint) {
		super(endpoint);
		this.endpoint = endpoint;
	}

	public void process(Exchange exchange) throws Exception {

		/** Create a client. */
		HttpClient client = HttpClientConfigurer.setupClient(true, "www.merriam-webster.com", 80, endpoint.getProxyHost(), endpoint.getProxyPort(), null, null, null);
		
		/** Iterate through each query element and see if we find it in webster. */
		
	}
}
