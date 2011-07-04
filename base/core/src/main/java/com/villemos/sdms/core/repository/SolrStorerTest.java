/**
 * villemos consulting [space^] (http://www.villemos.de) 
 * Probe. Send. Act. Emergent solution.
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
package com.villemos.sdms.core.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import junit.framework.TestCase;

import com.villemos.sdms.core.io.Acronym;
import com.villemos.sdms.core.io.Comment;
import com.villemos.sdms.core.io.InformationObject;
import com.villemos.sdms.core.io.Like;
import com.villemos.sdms.core.io.Organisation;
import com.villemos.sdms.core.io.Person;
import com.villemos.sdms.core.io.Synonym;


/**
 * @author villemosg
 *
 */
public class SolrStorerTest extends TestCase {

	@Test
	public void testStorage() {
		SolrStorer storer = new SolrStorer();
		
		List<InformationObject> objects = new ArrayList<InformationObject>();

		Acronym acronym = new Acronym();
		acronym.hasText = "European Space Architecture Workshop";
		acronym.hasLog = null;
		acronym.hasName = "ESAW";
		acronym.hasParent = null;
		acronym.hasUri = "acronym:ESAW/European Space Architecture Workshop";		
		objects.add(acronym);
		
		Comment comment = new Comment();
		comment.associatedTo = Arrays.asList(new String[] {"acronym:ESAW/European Space Architecture Workshop"});
		comment.hasLog = null;
		comment.hasName = "ESAW 2011";
		comment.hasParent = null;
		comment.hasText = "Was pretty boring.";
		comment.hasUri = UUID.randomUUID().toString();
		comment.raisedBy = "Person:Gert Villemos";
		objects.add(comment);
		
		Like like = new Like();
		like.associatedTo = Arrays.asList(new String[] {"acronym:ESAW/European Space Architecture Workshop"});
		like.hasLog = null;
		like.hasName = "ESAW 2011 is Great!";
		like.hasParent = null;
		like.hasText = "I really liked ESAW.";
		like.hasUri = UUID.randomUUID().toString();
		like.raisedBy = "Person:Gert Villemos";
		objects.add(like);
		
		Organisation organisation = new Organisation();
		organisation.hasLog = null;
		organisation.hasName = "Villemos Consulting";
		organisation.hasParent = null;
		organisation.hasText = "An excellent consulting firm.";
		organisation.hasUri = "Organisation:Villemos Consulting";
		objects.add(organisation);
		
		Person person = new Person();
		person.hasLog = null;
		person.hasName = "Gert Villemos";
		person.furtherInformation = Arrays.asList(new String[] {"http://www.villemos.de"});
		person.hasParent = null;
		person.hasText = "A senior architect.";
		person.hasUri = "Person:Gert Villemos";
		objects.add(person);
		
		Synonym synonym = new Synonym();
		synonym.hasLog = null;
		synonym.hasName = "Villemos, G";
		synonym.hasParent = null;
		synonym.hasText = "Gert Villemos";
		synonym.hasUri = "Synonym:Gert Villemos/Villemos, G";
		objects.add(synonym);
		
		storer.store(objects);
	}
}
