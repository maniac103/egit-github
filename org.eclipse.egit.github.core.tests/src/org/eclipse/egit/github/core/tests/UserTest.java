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
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.UserPlan;
import org.junit.Test;

/**
 * Unit tests of {@link User}
 */
public class UserTest {

	/**
	 * Test default state of user
	 */
	@Test
	public void defaultState() {
		User user = new User();
		assertNull(user.getAvatarUrl());
		assertNull(user.getBlog());
		assertEquals(0, user.getCollaborators());
		assertNull(user.getCompany());
		assertNull(user.getCreatedAt());
		assertEquals(0, user.getDiskUsage());
		assertNull(user.getEmail());
		assertEquals(0, user.getFollowers());
		assertEquals(0, user.getFollowing());
		assertNull(user.getGravatarId());
		assertNull(user.getHtmlUrl());
		assertEquals(0, user.getId());
		assertNull(user.getLocation());
		assertNull(user.getLogin());
		assertNull(user.getName());
		assertEquals(0, user.getOwnedPrivateRepos());
		assertNull(user.getPlan());
		assertEquals(0, user.getPrivateGists());
		assertEquals(0, user.getPublicRepos());
		assertEquals(0, user.getPublicGists());
		assertEquals(0, user.getTotalPrivateRepos());
		assertNull(user.getType());
		assertNull(user.getUrl());
		assertFalse(user.isHireable());
		assertNull(user.getBio());
	}

	/**
	 * Test updating user fields
	 */
	@Test
	public void updateFields() {
		User user = new User();
		assertEquals("avatar", user.setAvatarUrl("avatar").getAvatarUrl());
		assertEquals("blog", user.setBlog("blog").getBlog());
		assertEquals(50, user.setCollaborators(50).getCollaborators());
		assertEquals("inc.", user.setCompany("inc.").getCompany());
		assertEquals(new Date(6000), user.setCreatedAt(new Date(6000))
				.getCreatedAt());
		assertEquals(500, user.setDiskUsage(500).getDiskUsage());
		assertEquals("e@mai.l", user.setEmail("e@mai.l").getEmail());
		assertEquals(1, user.setFollowers(1).getFollowers());
		assertEquals(2, user.setFollowing(2).getFollowing());
		assertEquals("gid", user.setGravatarId("gid").getGravatarId());
		assertEquals("html", user.setHtmlUrl("html").getHtmlUrl());
		assertEquals(90, user.setId(90).getId());
		assertEquals("earth", user.setLocation("earth").getLocation());
		assertEquals("auser", user.setLogin("auser").getLogin());
		assertEquals("A User", user.setName("A User").getName());
		assertEquals(12, user.setOwnedPrivateRepos(12).getOwnedPrivateRepos());
		UserPlan plan = new UserPlan().setName("gold");
		assertEquals(plan, user.setPlan(plan).getPlan());
		assertEquals(3, user.setPrivateGists(3).getPrivateGists());
		assertEquals(4, user.setPublicRepos(4).getPublicRepos());
		assertEquals(77, user.setPublicGists(77).getPublicGists());
		assertEquals(80, user.setTotalPrivateRepos(80).getTotalPrivateRepos());
		assertEquals("reg", user.setType("reg").getType());
		assertEquals("url", user.setUrl("url").getUrl());
		assertTrue(user.setHireable(true).isHireable());
		assertEquals("bio", user.setBio("bio").getBio());
	}
}
