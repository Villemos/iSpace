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
package com.villemos.ispace.directoryassembler;

import org.apache.camel.Endpoint;

import com.villemos.ispace.ktree.KtreeCrawlerComponent;

import java.util.Map;

/**
 * Represents the component that manages {@link DocumentAssemblerEndpoint}. It holds the
 * list of named direct endpoints.
 *
 * @version
 */
public class DocumentAssemblerComponent extends KtreeCrawlerComponent {

    protected Endpoint createEndpoint(String uri, String remaining, Map parameters) throws Exception {
        Endpoint endpoint = new DocumentAssemblerEndpoint(uri, this);
        setProperties(endpoint, parameters);
        return endpoint;
    }
}
