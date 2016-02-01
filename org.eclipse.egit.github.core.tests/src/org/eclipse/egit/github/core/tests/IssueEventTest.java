/******************************************************************************
 *  Copyright (c) 2011 GitHub Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Kevin Sawicki (GitHub Inc.) - initial API and implementation
 *****************************************************************************/
package org.eclipse.egit.github.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.IssueEvent;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Rename;
import org.eclipse.egit.github.core.User;
import org.junit.Test;

/**
 * Unit tests of {@link IssueEvent}
 */
public class IssueEventTest {

	/**
	 * Test default state of issue event
	 */
	@Test
	public void defaultState() {
		IssueEvent event = new IssueEvent();
		assertEquals(0, event.getId());
		assertNull(event.getUrl());
		assertNull(event.getActor());
		assertNull(event.getCommitId());
		assertNull(event.getEvent());
		assertNull(event.getCreatedAt());
		assertNull(event.getLabel());
		assertNull(event.getAssignee());
		assertNull(event.getMilestone());
		assertNull(event.getRename());
		assertNull(event.getIssue());
	}

	/**
	 * Test updating issue event fields
	 */
	@Test
	public void updateFields() {
		IssueEvent event = new IssueEvent();
		assertEquals(4356, event.setId(4356).getId());
		assertEquals("url://a", event.setUrl("url://a").getUrl());
		User actor = new User().setName("Act Tor");
		assertEquals(actor, event.setActor(actor).getActor());
		assertEquals("a12b", event.setCommitId("a12b").getCommitId());
		assertEquals(IssueEvent.TYPE_CLOSED, event.setEvent("closed").getEvent());
		assertEquals(new Date(60000), event.setCreatedAt(new Date(60000))
				.getCreatedAt());
		Label label = new Label().setName("Lab El").setColor("563d7c");
		assertEquals(label, event.setLabel(label).getLabel());
		User assignee = new User().setName("Assig Nee");
		assertEquals(assignee, event.setAssignee(assignee).getAssignee());
		Milestone milestone = new Milestone().setTitle("Milestone");
		assertEquals(milestone, event.setMilestone(milestone).getMilestone());
		Rename rename = new Rename().setFrom("from").setTo("to");
		assertEquals(rename, event.setRename(rename).getRename());
		Issue issue = new Issue().setNumber(30);
		assertEquals(issue, event.setIssue(issue).getIssue());
	}

	/**
	 * Test non-mutable created at date
	 */
	@Test
	public void nonMutableCreatedAt() {
		IssueEvent event = new IssueEvent();
		Date date = new Date(1000);
		event.setCreatedAt(date);
		date.setTime(0);
		assertFalse(date.equals(event.getCreatedAt()));
	}
}
