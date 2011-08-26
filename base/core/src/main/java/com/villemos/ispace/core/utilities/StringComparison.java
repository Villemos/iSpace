package com.villemos.ispace.core.utilities;

import com.wcohen.ss.JaroWinklerTFIDF;

public class StringComparison {

    protected static JaroWinklerTFIDF matcher = new JaroWinklerTFIDF ();

    public static double match(String candidate, String value) { 
        return matcher.score(candidate, value);
    }
}
