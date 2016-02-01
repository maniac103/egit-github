/*******************************************************************************
 *  Copyright (c) 2011 Christian Trutz
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Christian Trutz - initial API and implementation
 *******************************************************************************/
package org.eclipse.egit.github.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.User;
import org.junit.Test;

/**
 * Unit tests of {@link Issue}
 */
public class IssueTest {

	/**
	 * Test default state of issue
	 */
	@Test
	public void defaultState() {
		Issue issue = new Issue();
		assertNull(issue.getAssignee());
		assertNull(issue.getBody());
		assertNull(issue.getBodyHtml());
		assertNull(issue.getBodyText());
		assertNull(issue.getClosedAt());
		assertEquals(0, issue.getComments());
		assertNull(issue.getCreatedAt());
		assertNull(issue.getHtmlUrl());
		assertNull(issue.getLabels());
		assertNull(issue.getMilestone());
		assertEquals(0, issue.getNumber());
		assertNull(issue.getPullRequest());
		assertNull(issue.getState());
		assertNull(issue.getTitle());
		assertNull(issue.getUpdatedAt());
		assertNull(issue.getUrl());
		assertNull(issue.getUser());
		assertNotNull(issue.toString());
		assertEquals(0, issue.getId());
		assertNull(issue.getClosedBy());
	}

	/**
	 * Test updating issue fields
	 */
	@Test
	public void updateFields() {
		Issue issue = new Issue();
		User assignee = new User().setLogin("assignee");
		assertEquals(assignee, issue.setAssignee(assignee).getAssignee());
		assertEquals("body", issue.setBody("body").getBody());
		assertEquals("<body>", issue.setBodyHtml("<body>").getBodyHtml());
		assertEquals("text", issue.setBodyText("text").getBodyText());
		assertEquals(new Date(1000), issue.setClosedAt(new Date(1000))
				.getClosedAt());
		assertEquals(5, issue.setComments(5).getComments());
		assertEquals(new Date(2000), issue.setCreatedAt(new Date(2000))
				.getCreatedAt());
		assertEquals("html", issue.setHtmlUrl("html").getHtmlUrl());
		assertEquals(new ArrayList<Label>(),
				issue.setLabels(new ArrayList<Label>()).getLabels());
		Milestone milestone = new Milestone().setNumber(50);
		assertEquals(milestone, issue.setMilestone(milestone).getMilestone());
		assertEquals(123, issue.setNumber(123).getNumber());
		PullRequest pr = new PullRequest().setNumber(500);
		assertEquals(pr, issue.setPullRequest(pr).getPullRequest());
		assertEquals("state", issue.setState("state").getState());
		assertEquals("title", issue.setTitle("title").getTitle());
		assertEquals(new Date(3000), issue.setUpdatedAt(new Date(3000))
				.getUpdatedAt());
		assertEquals("url", issue.setUrl("url").getUrl());
		User user = new User().setLogin("user");
		assertEquals(user, issue.setUser(user).getUser());
		assertNull(issue.setLabels(null).getLabels());
		assertEquals(39, issue.setId(39).getId());
		assertEquals("user", issue.setClosedBy(user).getClosedBy().getLogin());
	}

	/**
	 * Test non-mutable created at date
	 */
	@Test
	public void getCreatedAtReferenceMutableObject() {
		Issue issue = new Issue();
		issue.setCreatedAt(new Date(55555555));
		issue.getCreatedAt().setTime(0);
		assertTrue(issue.getCreatedAt().getTime() != 0);
	}

	/**
	 * Test non-mutable updated at date
	 */
	@Test
	public void getUpdatedAtReferenceMutableObject() {
		Issue issue = new Issue();
		issue.setUpdatedAt(new Date(44444444));
		issue.getUpdatedAt().setTime(0);
		assertTrue(issue.getUpdatedAt().getTime() != 0);
	}

	/**
	 * Test non-mutable closed at date
	 */
	@Test
	public void getClosedAtReferenceMutableObject() {
		Issue issue = new Issue();
		issue.setClosedAt(new Date(99999999));
		issue.getClosedAt().setTime(0);
		assertTrue(issue.getClosedAt().getTime() != 0);
	}
}
