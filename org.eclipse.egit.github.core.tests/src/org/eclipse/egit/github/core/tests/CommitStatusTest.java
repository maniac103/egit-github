/******************************************************************************
 *  Copyright (c) 2012, 2015 GitHub Inc. and others.
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
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.eclipse.egit.github.core.CommitStatus;
import org.eclipse.egit.github.core.User;
import org.junit.Test;

/**
 * Unit tests of {@link CommitStatus}
 */
public class CommitStatusTest {

	/**
	 * Test default state of commit status
	 */
	@Test
	public void defaultState() {
		CommitStatus status = new CommitStatus();
		assertNull(status.getCreatedAt());
		assertNull(status.getCreator());
		assertNull(status.getContext());
		assertNull(status.getDescription());
		assertEquals(0, status.getId());
		assertNull(status.getState());
		assertNull(status.getTargetUrl());
		assertNull(status.getUpdatedAt());
		assertNull(status.getUrl());
		assertNull(status.getContext());
	}

	/**
	 * Test updating commit status fields
	 */
	@Test
	public void updateFields() {
		CommitStatus status = new CommitStatus();
		assertEquals(new Date(1234), status.setCreatedAt(new Date(1234))
				.getCreatedAt());
		User creator = new User().setId(1);
		assertEquals(creator, status.setCreator(creator).getCreator());
		assertEquals("con/text", status.setContext("con/text").getContext());
		assertEquals("desc", status.setDescription("desc").getDescription());
		assertEquals(40, status.setId(40).getId());
		assertEquals("success", status.setState("success").getState());
		assertEquals("targetUrl", status.setTargetUrl("targetUrl")
				.getTargetUrl());
		assertEquals(new Date(5678), status.setUpdatedAt(new Date(5678))
				.getUpdatedAt());
		assertEquals("url", status.setUrl("url").getUrl());
		assertEquals("context", status.setContext("context").getContext());
	}
}
