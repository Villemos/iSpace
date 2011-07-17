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
package com.villemos.ispace.solr;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.junit.Test;

public class SolrComponentTest extends CamelTestSupport {

    @Test
    public void testAsynchRetrieval() throws Exception {
    	
        context.stop();
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("direct:start")
                .to("solr:storage?solrServerUrl=http://open.logicaspace.com:8080/apache-solr-1.4.0");
            }
        });
        context.start();
    	
    	Exchange exchange = new DefaultExchange(context);
        exchange.setProperty(Exchange.CHARSET_NAME, "UTF-8");
        Message in = exchange.getIn();
        in.setHeader("solr.query", "design");
        in.setHeader("solr.stream", "");
        in.setHeader("solr.option.rows", 10);
        in.setHeader("solr.option.highlight", true);
        in.setHeader("solr.option.includescore", true);
        in.setHeader("solr.option.sortfield", new Object[] {"score", ORDER.desc});
        
        DummyCallback callback = new DummyCallback();
        exchange.getIn().setBody(callback);
        template.send("direct:start", exchange);
        
        // assertTrue(exchange.getOut().getBody() != null);
        // assertTrue(((SolrDocumentList) exchange.getOut().getBody()).size()== 10);
        
        while (true) {
        	Thread.sleep(1000);
        }
    }
}
