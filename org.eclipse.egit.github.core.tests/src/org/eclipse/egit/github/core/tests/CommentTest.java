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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.User;
import org.junit.Test;

/**
 * Unit tests of {@link Comment}
 */
public class CommentTest {

	/**
	 * Test default state of comment
	 */
	@Test
	public void defaultState() {
		Comment comment = new Comment();
		assertNull(comment.getBody());
		assertNull(comment.getBodyHtml());
		assertNull(comment.getBodyText());
		assertNull(comment.getCreatedAt());
		assertEquals(0, comment.getId());
		assertNull(comment.getUpdatedAt());
		assertNull(comment.getUrl());
		assertNull(comment.getHtmlUrl());
		assertNull(comment.getUser());
	}

	/**
	 * Test updating comment fields
	 */
	@Test
	public void updateFields() {
		Comment comment = new Comment();
		assertEquals("body", comment.setBody("body").getBody());
		assertEquals("<body>", comment.setBodyHtml("<body>").getBodyHtml());
		assertEquals("text", comment.setBodyText("text").getBodyText());
		assertEquals(new Date(1234), comment.setCreatedAt(new Date(1234))
				.getCreatedAt());
		assertEquals(100, comment.setId(100).getId());
		assertEquals(new Date(2345), comment.setUpdatedAt(new Date(2345))
				.getUpdatedAt());
		assertEquals("http", comment.setUrl("http").getUrl());
		assertEquals("http", comment.setHtmlUrl("http").getHtmlUrl());
		User user = new User().setLogin("auser");
		assertEquals(user, comment.setUser(user).getUser());
	}

	/**
	 * Test non-mutable created at date
	 */
	@Test
	public void getCreatedAtReferenceMutableObject() {
		Comment comment = new Comment();
		comment.setCreatedAt(new Date(12345));
		comment.getCreatedAt().setTime(0);
		assertTrue(comment.getCreatedAt().getTime() != 0);
	}

	/**
	 * Test non-mutable updated at date
	 */
	@Test
	public void getUpdatedAtReferenceMutableObject() {
		Comment comment = new Comment();
		comment.setUpdatedAt(new Date(54321));
		comment.getUpdatedAt().setTime(0);
		assertTrue(comment.getUpdatedAt().getTime() != 0);
	}
}
