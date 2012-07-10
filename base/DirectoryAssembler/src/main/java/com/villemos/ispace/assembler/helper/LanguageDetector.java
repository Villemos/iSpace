package com.villemos.ispace.assembler.helper;

import java.util.ArrayList;

import org.apache.camel.Body;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;
import com.villemos.ispace.aperture.InformationObject;

public class LanguageDetector {

	/** The logger. */
	private static final Log LOG = LogFactory.getLog(LanguageDetector.class);

	protected Double threshold = 0.9d;

	protected Double allowance = 0.9d;

	protected String profiles = "D:\\Benutzer-Profile\\villemosg\\iSpace-ws\\oam\\src\\main\\resources\\profiles";

	{
		try {
			DetectorFactory.loadProfile(profiles);
		} catch (LangDetectException e) {
			e.printStackTrace();
		}		
	}

	public void process(@Body InformationObject io) {

		try {
			if (io.withRawText == null || io.withRawText.equals("")) {
				LOG.warn("Empty text in IO '" + io.hasTitle + "'.");
				io.metadata.put("Language", "");
				io.metadata.put("Language Probability", 0.);												
			}
			else if (count(io.withRawText) > io.withRawText.length()*allowance) {
				LOG.warn("Text of file '" + io.hasTitle + "' contains more than " + allowance + " percent of non-characters.");
				io.metadata.put("Language", "");
				io.metadata.put("Language Probability", 0.);								
			}
			else {

				Detector detector = DetectorFactory.create();
				detector.append(io.withRawText);

				ArrayList<Language> langlist = detector.getProbabilities();			

				if (langlist.size() == 0) {
					LOG.warn("Failed to find language for file '" + io.hasTitle + "'.");
					io.metadata.put("Language", "");
					io.metadata.put("Language Probability", 0.);				
				}
				else if (langlist.get(0).prob < threshold) {
					LOG.warn("Failed to confidently find language for file '" + io.hasTitle + "'. Probability only of being '" + langlist.get(0).lang + "' is only '" + langlist.get(0).prob + "'.");
					io.metadata.put("Language", langlist.get(0).lang);
					io.metadata.put("Language Probability", langlist.get(0).prob);
				}
				else {
					LOG.info("Found language '" + langlist.get(0).lang + "' with propability '" + langlist.get(0).prob + "'.");
					io.metadata.put("Language", langlist.get(0).lang);
					io.metadata.put("Language Probability", langlist.get(0).prob);
				}
			}
		} catch (LangDetectException e) {
			LOG.error("Failed to detect language. Received exception " + e);
			io.metadata.put("Language", "");
			io.metadata.put("Language Probability", 0.);								
		}
	}

	protected static int count(final String s) {
		final char[] chars = s.toCharArray();
		int count = 0;
		for(int i=0; i < chars.length; i++) {
			if (Character.isLetter(chars[i]) == false) {
				count++;
			}
		}
		return count;
	}
}
