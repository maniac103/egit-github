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

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.junit.Test;

/**
 * Unit tests of {@link Repository}
 */
public class RepositoryTest {

	/**
	 * Test default state of repository
	 */
	@Test
	public void defaultState() {
		Repository repo = new Repository();
		assertNull(repo.getCloneUrl());
		assertNull(repo.getCreatedAt());
		assertNull(repo.getDescription());
		assertEquals(0, repo.getForks());
		assertNull(repo.getGitUrl());
		assertNull(repo.getHomepage());
		assertNull(repo.getHtmlUrl());
		assertNull(repo.getLanguage());
		assertNull(repo.getDefaultBranch());
		assertNull(repo.getMirrorUrl());
		assertNull(repo.getName());
		assertEquals(0, repo.getOpenIssues());
		assertNull(repo.getOwner());
		assertNull(repo.getParent());
		assertNull(repo.getPushedAt());
		assertEquals(0, repo.getSize());
		assertNull(repo.getSource());
		assertNull(repo.getSshUrl());
		assertNull(repo.getSvnUrl());
		assertNull(repo.getUrl());
		assertEquals(0, repo.getWatchers());
		assertFalse(repo.isFork());
		assertFalse(repo.isHasDownloads());
		assertFalse(repo.isHasIssues());
		assertFalse(repo.isHasWiki());
		assertFalse(repo.isPrivate());
		assertEquals(0, repo.getId());
		assertNull(repo.getUpdatedAt());
	}

	/**
	 * Test updating fields of repository
	 */
	@Test
	public void updateFields() {
		Repository repo = new Repository();
		assertEquals("clone://", repo.setCloneUrl("clone://").getCloneUrl());
		assertEquals(new Date(2500), repo.setCreatedAt(new Date(2500))
				.getCreatedAt());
		assertEquals("a repo", repo.setDescription("a repo").getDescription());
		assertEquals(10, repo.setForks(10).getForks());
		assertEquals("git://", repo.setGitUrl("git://").getGitUrl());
		assertEquals("home", repo.setHomepage("home").getHomepage());
		assertEquals("html", repo.setHtmlUrl("html").getHtmlUrl());
		assertEquals("java", repo.setLanguage("java").getLanguage());
		assertEquals("master", repo.setDefaultBranch("master").getDefaultBranch());
		assertEquals("project", repo.setName("project").getName());
		assertEquals(20, repo.setOpenIssues(20).getOpenIssues());
		User owner = new User().setLogin("owner");
		assertEquals(owner, repo.setOwner(owner).getOwner());
		Repository parent = new Repository().setName("parent");
		assertEquals(parent, repo.setParent(parent).getParent());
		assertEquals(new Date(3000), repo.setPushedAt(new Date(3000))
				.getPushedAt());
		assertEquals(100, repo.setSize(100).getSize());
		Repository source = new Repository().setName("source");
		assertEquals(source, repo.setSource(source).getSource());
		assertEquals("ssh://", repo.setSshUrl("ssh://").getSshUrl());
		assertEquals("svn://", repo.setSvnUrl("svn://").getSvnUrl());
		assertEquals("url://", repo.setUrl("url://").getUrl());
		assertEquals(200, repo.setWatchers(200).getWatchers());
		assertTrue(repo.setFork(true).isFork());
		assertTrue(repo.setHasDownloads(true).isHasDownloads());
		assertTrue(repo.setHasIssues(true).isHasIssues());
		assertTrue(repo.setHasWiki(true).isHasWiki());
		assertTrue(repo.setPrivate(true).isPrivate());
		assertEquals("mirror", repo.setMirrorUrl("mirror").getMirrorUrl());
		assertEquals(14, repo.setId(14).getId());
		assertEquals(new Date(50000), repo.setUpdatedAt(new Date(50000))
				.getUpdatedAt());
	}

	/**
	 * Generate id with null name
	 */
	@Test
	public void generateIdNullName() {
		Repository repo = new Repository();
		repo.setName(null);
		repo.setOwner(new User().setLogin("tuser"));
		assertNull(repo.generateId());
	}

	/**
	 * Generate id with empty name
	 */
	@Test
	public void generateIdEmptyName() {
		Repository repo = new Repository();
		repo.setName("");
		repo.setOwner(new User().setLogin("tuser"));
		assertNull(repo.generateId());
	}

	/**
	 * Generate id with null owner
	 */
	@Test
	public void generateIdNullOwner() {
		Repository repo = new Repository();
		repo.setName("myproject");
		assertNull(repo.generateId());
	}

	/**
	 * Generate id with null owner login
	 */
	@Test
	public void generateIdNullOwnerLogin() {
		Repository repo = new Repository();
		repo.setName("myproject");
		repo.setOwner(new User().setLogin(null));
		assertNull(repo.generateId());
	}

	/**
	 * Generate id with empty owner login
	 */
	@Test
	public void generateIdEmptyOwnerLogin() {
		Repository repo = new Repository();
		repo.setName("myproject");
		repo.setOwner(new User().setLogin(""));
		assertNull(repo.generateId());
	}

	/**
	 * Generate id for repository
	 */
	@Test
	public void generateId() {
		Repository repo = new Repository();
		repo.setName("myproject");
		repo.setOwner(new User().setLogin("tuser"));
		assertEquals("tuser/myproject", repo.generateId());
	}
}
