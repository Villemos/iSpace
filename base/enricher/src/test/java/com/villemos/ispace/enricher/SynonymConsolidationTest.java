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
package com.villemos.ispace.enricher;

import java.util.Arrays;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class SynonymConsolidationTest extends CamelTestSupport {

	@EndpointInject(uri="mock:end")
	protected MockEndpoint mock = null;
	
    @Test
    public void testAsynchRetrieval() throws Exception {
    	
        context.stop();
        context.addRoutes(new RouteBuilder() {
            public void configure() {
            	
            	SynonymConsolidator bean = new SynonymConsolidator();

            	bean.setDiscreteFields(Arrays.asList(new String[] {"field1", "field3"}));

                from("direct:store")
                .to("mock:synonym");

                from("direct:start")
                .bean(bean)
                .to("mock:end");                
                
                from("direct:synonym")
                .bean(bean, "registerSynonym");
            }
        });
        context.start();

        /** Inject a few synonyms. */
        createSynonym("Synonym1", "root1", "accepted");
        createSynonym("Synonym2", "root1", "accepted");
        createSynonym("Synonym3", "root1", "remove");
        createSynonym("Synonym4", "root1", "ignore");
        createSynonym("Synonym5", "root2", "accepted");
        createSynonym("Synonym6", "root2", "accepted");
        
        /** See if they work. */
        
        /** Replacement of field 1 and 3. */
        createDocument("Synonym1", "Synonym2", "Synonym5");
        validateExpect("root1", "Synonym2", "root2");

        /** Replacement of field 1, ignore field 3. */
        createDocument("Synonym1", "Synonym3", "Synonym4");
        validateExpect("root1", "Synonym3", "Synonym4");

        /** Replacement of field 1, remove field 3. */
        createDocument("Synonym1", "Synonym2", "Synonym3");
        validateExpect("root1", "Synonym2", null);
        
        /** Accept a synonym. */
        createSynonym("Synonym4", "root1", "accepted");

        createDocument("Synonym1", "Synonym3", "Synonym4");
        validateExpect("root1", "Synonym3", "root1");
        
        /** Add a new synonym*/
        createSynonym("Synonym7", "root3", "accepted");

        createDocument("Synonym7", "Synonym3", "Synonym4");
        validateExpect("root3", "Synonym3", "root1");
        
        /** See if new synonyms are detected. */
        assertTrue(getMockEndpoint("mock:synonym").getExchanges().size() == 0);
        createDocument("Synonym8", "Synonym3", "Synonym4");
        validateExpect("Synonym8", "Synonym3", "root1");
        assertTrue(getMockEndpoint("mock:synonym").getExchanges().size() == 1);
        
        createDocument("Synonym8", "Synonym3", "Synonym4");
        validateExpect("Synonym8", "Synonym3", "root1");
        assertTrue(getMockEndpoint("mock:synonym").getExchanges().size() == 1);        
    }
    
    private void validateExpect(String field1, String field2, String field3) {
		Exchange exchange = getMockEndpoint("mock:end").getExchanges().get(0);
		assertTrue(exchange != null);
		assertTrue(exchange.getIn() != null);
		assertTrue(exchange.getIn().getHeader("ispace.field.field1").equals(field1));
		assertTrue(exchange.getIn().getHeader("ispace.field.field2").equals(field2));
		
		if (field3 != null) {
			assertTrue(exchange.getIn().getHeader("ispace.field.field3").equals(field3));
		}
		else {
			assertTrue(exchange.getIn().getHeader("ispace.field.field3") == null);
		}
		
		getMockEndpoint("mock:end").getExchanges().clear();
		
	}

	// "The filter bubble"
    // Apache Haupt (relevance engine)
    public void createSynonym(String synonym, String root, String state) {
    	Exchange exchange = new DefaultExchange(context);
        exchange.setProperty(Exchange.CHARSET_NAME, "UTF-8");
        Message in = exchange.getIn();
        
        in.setHeader("ispace.field.url", "ispace:/synonym/" + synonym);
        in.setHeader("ispace.field.title", "Synonym "+ synonym);
        in.setHeader("ispace.field.type", "synonym");
        in.setHeader("ispace.field.mime", "virtual");
        in.setHeader("ispace.field.text", synonym);
        in.setHeader("ispace.field.root", root);
        in.setHeader("ispace.field.state", state);

        template.send("direct:synonym", exchange);
    }
    
    public void createDocument(String field1, String field2, String field3) {
    	Exchange exchange = new DefaultExchange(context);
        exchange.setProperty(Exchange.CHARSET_NAME, "UTF-8");
        Message in = exchange.getIn();
        
        in.setHeader("ispace.field.url", "uri:/test");
        in.setHeader("ispace.field.title", "Test Title");
        in.setHeader("ispace.field.field1", field1);
        in.setHeader("ispace.field.field2", field2);
        in.setHeader("ispace.field.field3", field3);

        template.send("direct:start", exchange);
    }

}
