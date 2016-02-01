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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Date;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.GistRevision;
import org.eclipse.egit.github.core.User;
import org.junit.Test;

/**
 * Unit tests of {@link Gist}
 */
public class GistTest {

	/**
	 * Test default state of gist
	 */
	@Test
	public void defaultState() {
		Gist gist = new Gist();
		assertEquals(0, gist.getComments());
		assertNull(gist.getCreatedAt());
		assertNull(gist.getDescription());
		assertNull(gist.getFiles());
		assertNull(gist.getGitPullUrl());
		assertNull(gist.getGitPushUrl());
		assertNull(gist.getHistory());
		assertNull(gist.getHtmlUrl());
		assertNull(gist.getId());
		assertNull(gist.getUpdatedAt());
		assertNull(gist.getUrl());
		assertNull(gist.getUser());
		assertNull(gist.getOwner());
		assertFalse(gist.isPublic());
	}

	/**
	 * Test updating gist fields
	 */
	@Test
	public void updateFields() {
		Gist gist = new Gist();
		assertEquals(3, gist.setComments(3).getComments());
		assertEquals(new Date(5000), gist.setCreatedAt(new Date(5000))
				.getCreatedAt());
		assertEquals("desc", gist.setDescription("desc").getDescription());
		assertEquals(Collections.emptyMap(),
				gist.setFiles(Collections.<String, GistFile> emptyMap())
						.getFiles());
		assertEquals("pull", gist.setGitPullUrl("pull").getGitPullUrl());
		assertEquals("push", gist.setGitPushUrl("push").getGitPushUrl());
		assertEquals(Collections.emptyList(),
				gist.setHistory(Collections.<GistRevision> emptyList())
						.getHistory());
		assertEquals("html", gist.setHtmlUrl("html").getHtmlUrl());
		assertEquals("id", gist.setId("id").getId());
		assertEquals(new Date(1000), gist.setUpdatedAt(new Date(1000))
				.getUpdatedAt());
		assertEquals("url", gist.setUrl("url").getUrl());
		User user = new User().setLogin("use");
		assertEquals(user, gist.setUser(user).getUser());
		assertTrue(gist.setPublic(true).isPublic());
	}

	/**
	 * Test non-mutable created at date
	 */
	@Test
	public void getCreatedAtReferenceMutableObject() {
		Gist gist = new Gist();
		gist.setCreatedAt(new Date(11111));
		gist.getCreatedAt().setTime(0);
		assertTrue(gist.getCreatedAt().getTime() != 0);
	}

	/**
	 * Test non-mutable updated at date
	 */
	@Test
	public void getUpdatedAtReferenceMutableObject() {
		Gist gist = new Gist();
		gist.setUpdatedAt(new Date(22222));
		gist.getUpdatedAt().setTime(0);
		assertTrue(gist.getUpdatedAt().getTime() != 0);
	}
}
