package com.villemos.sdms.core.search;

import junit.framework.TestCase;

public class AbstractExpanderTest extends TestCase {

	public void testTokenize() {
		ZombieExpander expander = new ZombieExpander();
		
		String queue = "test a string";
		assertTrue(expander.tokenize(queue).size() == 3);
		
		queue = "test a \"string\"";
		assertTrue(expander.tokenize(queue).size() == 3);
		
		queue = "test \"a string\"";
		assertTrue(expander.tokenize(queue).size() == 2);
		
		queue = "\"test a\" string";
		assertTrue(expander.tokenize(queue).size() == 2);

		queue = "\"test a string\"";
		assertTrue(expander.tokenize(queue).size() == 1);
	}

}
