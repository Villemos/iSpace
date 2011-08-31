package com.villemos.ispace.enricher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.Handler;

import com.villemos.ispace.api.RegularExpression;

public class RegularExpressionBuffer {

	protected Map<String, List<RegularExpression>> categoryToExpression = new HashMap<String, List<RegularExpression>>();
	
	@Handler
	public void registerRegx(@Body RegularExpression regx) {
		if (categoryToExpression.containsKey(regx.toDetect) == false) {
			categoryToExpression.put(regx.toDetect, new ArrayList<RegularExpression>());
		}
		categoryToExpression.get(regx.toDetect).add(regx);
	}	
}
