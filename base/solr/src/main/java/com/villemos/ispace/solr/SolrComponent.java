/**
 * villemos consulting [space^] (http://www.villemos.com) 
 * Probe. Sense. Act. Emergent solution.
 * 
 * Copyright 2011 Gert Villemos
 * All Rights Reserved.
 * Released under proprietary license, i.e. not free. But we are friendly.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of villemos consulting, and its suppliers
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to villemos consulting
 * and its suppliers and may be covered by European and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * 
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from villemos consulting Incorporated.
 * 
 * And it wouldn't be nice either.
 * 
 */
package com.villemos.ispace.solr;

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

/**
 * Represents the component that manages {@link SolrEndpoint}.
 */
public class SolrComponent extends DefaultComponent {

	
    /* (non-Javadoc)
     * @see org.apache.camel.impl.DefaultComponent#createEndpoint(java.lang.String, java.lang.String, java.util.Map)
     */
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        Endpoint endpoint = new SolrEndpoint(uri, this);
        setProperties(endpoint, parameters);
        return endpoint;
    }
}
