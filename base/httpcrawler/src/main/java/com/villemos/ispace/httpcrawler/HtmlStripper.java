/* ----------------------------------------------------------------------------
 * © Copyright European Space Agency, 2009
 *             European Space Operations Centre
 *             Darmstadt Germany
 * The copyright of this document is vested in the European Space Agency. This 
 * document may only be reproduced in whole or in part,stored in a retrieval 
 * system, transmitted in any form, or by any means e.g. electronically, 
 * mechanically or by photocopying, or otherwise, with the prior permission of
 * the Agency.
 * ----------------------------------------------------------------------------
 * System       : Huginn
 * Component    : webcrawlerframework
 * Classname    : org.esa.huginn.webcrawlerframework.HtmlStripper.java 
 * Author       : Logica Huginn team (gert.villemos@logica.com)
 * Created on   : 20.04.2010 21:55:14 
 * ----------------------------------------------------------------------------
 */
package com.villemos.ispace.httpcrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.CharReader;
import org.apache.solr.analysis.HTMLStripCharFilter;

/**
 * TODO write here a description of the class
 * 
 * @author     huginn team
 * @version    $Revision: $
 * @date       $Date: $
 */
public class HtmlStripper
{
    public String stripHTMLX(String value) {
        StringBuilder out = new StringBuilder();
        StringReader strReader = new StringReader(value);
        try {
            HTMLStripCharFilter html = new HTMLStripCharFilter(CharReader.get(strReader.markSupported() ? strReader : new BufferedReader(strReader)));
            char[] cbuf = new char[1024 * 10];
            while (true) {
                int count = html.read(cbuf);
                if (count == -1)
                    break; // end of stream mark is -1
                if (count > 0)
                    out.append(cbuf, 0, count);
            }
            html.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
            //  "Failed stripping HTML for column: " + column, e);
        }
        return out.toString();
    }
}
