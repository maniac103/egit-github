/******************************************************************************
 *  Copyright (c) 2011, 2015 GitHub Inc. and others
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Kevin Sawicki (GitHub Inc.) - initial API and implementation
 *    Michael Mathews (Arizona Board of Regents) - (Bug: 447419)
 *    			 Team Membership API implementation
 *****************************************************************************/
package org.eclipse.egit.github.core.tests;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Collections;

import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.Team;
import org.eclipse.egit.github.core.TeamMembership;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.eclipse.egit.github.core.service.TeamService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests of {@link TeamService}
 */
@RunWith(MockitoJUnitRunner.class)
public class TeamServiceTest {

	@Mock
	private GitHubClient client;

	@Mock
	private GitHubResponse response;

	private TeamService service;

	private RepositoryId repo;

	/**
	 * Test case set up
	 *
	 * @throws IOException
	 */
	@Before
	public void before() throws IOException {
		doReturn(response).when(client).get(any(GitHubRequest.class));
		service = new TeamService(client);
		repo = new RepositoryId("o", "n");
	}

	/**
	 * Create service using default constructor
	 */
	@Test
	public void constructor() {
		assertNotNull(new TeamService().getClient());
	}

	/**
	 * Get team
	 *
	 * @throws IOException
	 */
	@Test
	public void getTeam() throws IOException {
		service.getTeam(3);
		GitHubRequest request = new GitHubRequest();
		request.setUri("/teams/3");
		verify(client).get(request);
	}

	/**
	 * Get teams with null name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getTeamsNullName() throws IOException {
		service.getTeams((String) null);
	}

	/**
	 * Get teams with empty name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getTeamsEmptyName() throws IOException {
		service.getTeams("");
	}

	/**
	 * Get teams
	 *
	 * @throws IOException
	 */
	@Test
	public void getTeams() throws IOException {
		service.getTeams("group");
		GitHubRequest request = new GitHubRequest();
		request.setUri(Utils.page("/orgs/group/teams"));
		verify(client).get(request);
	}

	/**
	 * Create team with null organization
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void createTeamNullOrg() throws IOException {
		service.createTeam(null, new Team().setName("pullers"));
	}

	/**
	 * Create team with empty organization
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void createTeamEmptyOrg() throws IOException {
		service.createTeam("", new Team().setName("pullers"));
	}

	/**
	 * Create team with null team
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void createTeamNullTeam() throws IOException {
		service.createTeam("group", null);
	}

	/**
	 * Create team
	 *
	 * @throws IOException
	 */
	@Test
	public void createTeam() throws IOException {
		service.createTeam("group", new Team().setName("pullers"));
		verify(client).post(eq("/orgs/group/teams"), any(), eq(Team.class));
	}

	/**
	 * Create team with repositories
	 *
	 * @throws IOException
	 */
	@Test
	public void createTeamWithRepos() throws IOException {
		service.createTeam("group", new Team().setName("pullers"),
				Collections.singletonList("repo1"));
		verify(client).post(eq("/orgs/group/teams"), any(), eq(Team.class));
	}

	/**
	 * Edit team null team
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void editTeamNullTeam() throws IOException {
		service.editTeam(null);
	}

	/**
	 * Edit team
	 *
	 * @throws IOException
	 */
	@Test
	public void editTeam() throws IOException {
		Team team = new Team().setId(20);
		service.editTeam(team);
		verify(client).post("/teams/20", team, Team.class);
	}

	/**
	 * Delete team
	 *
	 * @throws IOException
	 */
	@Test
	public void deleteTeam() throws IOException {
		service.deleteTeam(50);
		verify(client).delete("/teams/50");
	}

	/**
	 * Get members
	 *
	 * @throws IOException
	 */
	@Test
	public void getMembers() throws IOException {
		service.getMembers(15);
		GitHubRequest request = new GitHubRequest();
		request.setUri(Utils.page("/teams/15/members"));
		verify(client).get(request);
	}

	/**
	 * Is member with null name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void isMemberNullName() throws IOException {
		service.isMember(2, null);
	}

	/**
	 * Is member with empty name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void isMemberEmptyName() throws IOException {
		service.isMember(2, "");
	}

	/**
	 * Is member
	 *
	 * @throws IOException
	 */
	@Test
	public void isMember() throws IOException {
		service.isMember(5, "abc");
		GitHubRequest request = new GitHubRequest();
		request.setUri("/teams/5/members/abc");
		verify(client).get(request);
	}

	/**
	 * Add member with null name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void addMemberNullName() throws IOException {
		service.addMember(2, null);
	}

	/**
	 * Add member with empty name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void addMemberEmptyName() throws IOException {
		service.addMember(2, "");
	}

	/**
	 * Add member
	 *
	 * @throws IOException
	 */
	@Test
	public void addMember() throws IOException {
		service.addMember(6, "tt");
		verify(client).put("/teams/6/members/tt");
	}

	/**
	 * Remove member with null name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void removeMemberNullName() throws IOException {
		service.removeMember(3, null);
	}

	/**
	 * Remove member with empty name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void removeMemberEmptyName() throws IOException {
		service.removeMember(3, "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void getMembershipNullName() throws IOException {
		service.getMembership(6, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getMembershipEmptyName() throws IOException {
		service.getMembership(6, "");
	}

	@Test
	public void getMembership() throws IOException {
		service.getMembership(6, "tt");
		GitHubRequest request = new GitHubRequest();
		request.setUri("/teams/6/memberships/tt");
		verify(client).get(request);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addMembershipNullName() throws IOException {
		service.addMembership(6, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addMembershipEmptyName() throws IOException {
		service.addMembership(6, "");
	}

	@Test
	public void addMembership() throws IOException {
		service.addMembership(6, "tt");
		verify(client).put("/teams/6/memberships/tt", null, TeamMembership.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void removeMembershipNullName() throws IOException {
		service.removeMembership(6, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void removeMembershipEmptyName() throws IOException {
		service.removeMembership(6, "");
	}

	@Test
	public void removeMembership() throws IOException {
		service.removeMembership(6, "tt");
		verify(client).delete("/teams/6/memberships/tt");
	}

	/**
	 * Remove member
	 *
	 * @throws IOException
	 */
	@Test
	public void removeMember() throws IOException {
		service.removeMember(6, "aa");
		verify(client).delete("/teams/6/members/aa");
	}

	/**
	 * Get repositories
	 *
	 * @throws IOException
	 */
	@Test
	public void getRepositories() throws IOException {
		service.getRepositories(7);
		GitHubRequest request = new GitHubRequest();
		request.setUri(Utils.page("/teams/7/repos"));
		verify(client).get(request);
	}

	/**
	 * Is repository
	 *
	 * @throws IOException
	 */
	@Test
	public void isRepository() throws IOException {
		service.isTeamRepository(8, repo);
		GitHubRequest request = new GitHubRequest();
		request.setUri("/teams/8/repos/o/n");
		verify(client).get(request);
	}

	/**
	 * Add repository
	 *
	 * @throws IOException
	 */
	@Test
	public void addRepository() throws IOException {
		service.addRepository(8, repo);
		verify(client).put("/teams/8/repos/o/n");
	}

	/**
	 * Remove repository
	 *
	 * @throws IOException
	 */
	@Test
	public void removeRepository() throws IOException {
		service.removeRepository(8, repo);
		verify(client).delete("/teams/8/repos/o/n");
	}

	/**
	 * Get teams for repository
	 *
	 * @throws IOException
	 */
	@Test
	public void getRepositoryTeams() throws IOException {
		service.getTeams(repo);
		GitHubRequest request = new GitHubRequest();
		request.setUri(Utils.page("/repos/o/n/teams"));
		verify(client).get(request);
	}
}
