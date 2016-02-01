/******************************************************************************
 *  Copyright (c) 2015 Jon Ander Peñalba
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Jon Ander Peñalba - initial API and implementation
 *****************************************************************************/
package org.eclipse.egit.github.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.StargazerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests of {@link StargazerService}
 */
@RunWith(MockitoJUnitRunner.class)
public class StargazerServiceTest {

	@Mock
	private GitHubClient client;

	@Mock
	private GitHubResponse response;

	private StargazerService service;

	private RepositoryId repo;

	/**
	 * Test case set up
	 *
	 * @throws IOException
	 */
	@Before
	public void before() throws IOException {
		doReturn(response).when(client).get(any(GitHubRequest.class));
		service = new StargazerService(client);
		repo = new RepositoryId("o", "n");
	}

	/**
	 * Create service using default constructor
	 */
	@Test
	public void constructor() {
		assertNotNull(new StargazerService().getClient());
	}

	/**
	 * Get stargazers
	 *
	 * @throws IOException
	 */
	@Test
	public void getStargazers() throws IOException {
		service.getStargazers(repo);
		GitHubRequest request = new GitHubRequest();
		request.setUri(Utils.page("/repos/o/n/stargazers"));
		verify(client).get(request);
	}

	/**
	 * Get starred
	 *
	 * @throws IOException
	 */
	@Test
	public void getCurrentStarred() throws IOException {
		service.getStarred();
		GitHubRequest request = new GitHubRequest();
		request.setUri(Utils.page("/user/starred"));
		verify(client).get(request);
	}

	/**
	 * Get starred with null name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getStarredNullName() throws IOException {
		service.getStarred(null);
	}

	/**
	 * Get starred with empty name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getStarredEmptyName() throws IOException {
		service.getStarred("");
	}

	/**
	 * Get starred
	 *
	 * @throws IOException
	 */
	@Test
	public void getStarred() throws IOException {
		service.getStarred("auser");
		GitHubRequest request = new GitHubRequest();
		request.setUri(Utils.page("/users/auser/starred"));
		verify(client).get(request);
	}

	/**
	 * Is starring
	 *
	 * @throws IOException
	 */
	@Test
	public void isStarring() throws IOException {
		service.isStarring(repo);
		GitHubRequest request = new GitHubRequest();
		request.setUri("/user/starred/o/n");
		verify(client).get(request);
	}

	/**
	 * Star repository
	 *
	 * @throws IOException
	 */
	@Test
	public void star() throws IOException {
		service.star(repo);
		verify(client).put("/user/starred/o/n");
	}

	/**
	 * Unstar repository
	 *
	 * @throws IOException
	 */
	@Test
	public void unstar() throws IOException {
		service.unstar(repo);
		verify(client).delete("/user/starred/o/n");
	}

	/**
	 * Page stargazes
	 *
	 * @throws IOException
	 */
	@Test
	public void pageStargazers() throws IOException {
		PageIterator<User> iter = service.pageStargazers(repo);
		assertNotNull(iter);
		assertTrue(iter.hasNext());
		assertEquals(Utils.page("/repos/o/n/stargazers"), iter.getRequest()
				.generateUri());
	}

	/**
	 * Page starred
	 *
	 * @throws IOException
	 */
	@Test
	public void pageCurrentStarred() throws IOException {
		PageIterator<Repository> iter = service.pageStarred();
		assertNotNull(iter);
		assertTrue(iter.hasNext());
		assertEquals(Utils.page("/user/starred"), iter.getRequest()
				.generateUri());
	}

	/**
	 * Page starred
	 *
	 * @throws IOException
	 */
	@Test
	public void pageStarred() throws IOException {
		PageIterator<Repository> iter = service.pageStarred("auser");
		assertNotNull(iter);
		assertTrue(iter.hasNext());
		assertEquals(Utils.page("/users/auser/starred"), iter.getRequest()
				.generateUri());
	}
}
