package com.villemos.ispace.enricher;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;

import com.villemos.ispace.api.Acronym;
import com.villemos.ispace.api.InformationObject;

public class AcronymDetector {

	class AcronymFields {
		int acronym = 0;
		int acronymDefinition = 0;
		public AcronymFields(int acronym, int acronymDefinition) {
			super();
			this.acronym = acronym;
			this.acronymDefinition = acronymDefinition;
		}
	}

	protected Map<Pattern, AcronymFields> acronymPatterns = new HashMap<Pattern, AcronymFields>();
	{
		/** Matches Test For Acronym (TFA). Group 5 = TFA, Group 1 = Test For Acronym. */
		acronymPatterns.put(Pattern.compile("((\\p{Upper})\\w+\\s(\\p{Upper})\\w+\\s(\\p{Upper})\\w+)\\s\\((\\2\\3\\4)\\)"), new AcronymFields(5, 1));

		/** Matches TFA (Test For Acronym). Group 5 = TFA, Group 1 = Test For Acronym. */
		acronymPatterns.put(Pattern.compile("((\\p{Upper})\\w+\\s(\\p{Upper})\\w+\\s(\\p{Upper})\\w+)\\s(\\2\\3\\4)"), new AcronymFields(5, 1));

		/** Matches TFA (Test For Acronym). Group 1 = TFA, Group 5 = Test For Acronym. */
		acronymPatterns.put(Pattern.compile("((\\p{Upper})(\\p{Upper})(\\p{Upper}))\\s\\(((\\2)\\w+\\s(\\3)\\w+\\s(\\4)\\w+)\\)"), new AcronymFields(1, 5));
	}

	public void process(@Body InformationObject io, CamelContext context) {
		if (io.withRawText != null && io.withRawText.equals("") == false) {
			Iterator<Entry<Pattern, AcronymFields>> it = acronymPatterns.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Pattern, AcronymFields> entry = it.next();
				Matcher matcher = entry.getKey().matcher(io.withRawText);
				if (matcher.find()) {
					Exchange exchange = new DefaultExchange(context);
					exchange.getIn().setBody(new Acronym(matcher.group(entry.getValue().acronym), matcher.group(entry.getValue().acronymDefinition), io.hasUri));
					context.createProducerTemplate().send("direct:inject", exchange);
				}			
			}
		}
	}	
}
