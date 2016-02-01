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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.OrganizationService.RoleFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test of {@link OrganizationService}
 */
@RunWith(MockitoJUnitRunner.class)
public class OrganizationServiceTest {

	@Mock
	private GitHubClient client;

	@Mock
	private GitHubResponse response;

	private OrganizationService service;

	/**
	 * Test case set up
	 *
	 * @throws IOException
	 */
	@Before
	public void before() throws IOException {
		doReturn(response).when(client).get(any(GitHubRequest.class));
		service = new OrganizationService(client);
	}

	/**
	 * Create service using default constructor
	 */
	@Test
	public void constructor() {
		assertNotNull(new OrganizationService().getClient());
	}

	/**
	 * Get organizations
	 *
	 * @throws IOException
	 */
	@Test
	public void getCurrentUserOrganizations() throws IOException {
		service.getOrganizations();
		GitHubRequest request = new GitHubRequest();
		request.setUri(Utils.page("/user/orgs"));
		verify(client).get(request);
	}

	/**
	 * Get organizations with null user
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getOrganizationsNullUser() throws IOException {
		service.getOrganizations(null);
	}

	/**
	 * Get organization with empty user
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getOrganizationsEmptyUser() throws IOException {
		service.getOrganizations("");
	}

	/**
	 * Get organizations
	 *
	 * @throws IOException
	 */
	@Test
	public void getOrganizations() throws IOException {
		service.getOrganizations("auser");
		GitHubRequest request = new GitHubRequest();
		request.setUri(Utils.page("/users/auser/orgs"));
		verify(client).get(request);
	}

	/**
	 * Get organization with null name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getOrganizationNullName() throws IOException {
		service.getOrganization(null);
	}

	/**
	 * Get organization with empty name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getOrganizationEmptyName() throws IOException {
		service.getOrganization("");
	}

	/**
	 * Get organization
	 *
	 * @throws IOException
	 */
	@Test
	public void getOrganization() throws IOException {
		service.getOrganization("group");
		GitHubRequest request = new GitHubRequest();
		request.setUri("/orgs/group");
		verify(client).get(request);
	}

	/**
	 * Edit organization with null org.
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void editOrganizationNullOrg() throws IOException {
		service.editOrganization(null);
	}

	/**
	 * Edit organization with null org. name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void editOrganizationNullOrgName() throws IOException {
		service.editOrganization(new User().setLogin(null));
	}

	/**
	 * Edit organization with empty org. name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void editOrganizationEmptyOrgName() throws IOException {
		service.editOrganization(new User().setLogin(""));
	}

	/**
	 * Edit organization
	 *
	 * @throws IOException
	 */
	@Test
	public void editOrganization() throws IOException {
		User org = new User().setLogin("group");
		service.editOrganization(org);
		verify(client).post("/orgs/group", org, User.class);
	}

	/**
	 * Get members with null name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getMembersNullName() throws IOException {
		service.getMembers(null);
	}

	/**
	 * Get members with empty name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getMembersEmptyName() throws IOException {
		service.getMembers("");
	}

	/**
	 * Get members
	 *
	 * @throws IOException
	 */
	@Test
	public void getMembers() throws IOException {
		service.getMembers("group");
		GitHubRequest request = new GitHubRequest();
		request.setUri(Utils.page("/orgs/group/members"));
		verify(client).get(request);
	}

	/**
	 * Get members with role filter "all"
	 *
	 * @throws IOException
	 */
	@Test
	public void getMembersAll() throws IOException {
		testMembersByRole(RoleFilter.all);
	}

	/**
	 * Get members with role filter "all"
	 *
	 * @throws IOException
	 */
	@Test
	public void getMembersAdmin() throws IOException {
		testMembersByRole(RoleFilter.admin);
	}

	/**
	 * Get members with role filter "all"
	 *
	 * @throws IOException
	 */
	@Test
	public void getMembersMember() throws IOException {
		testMembersByRole(RoleFilter.member);
	}

	private void testMembersByRole(RoleFilter roleFilter) throws IOException {
		service.getMembers("group", roleFilter);
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("role", roleFilter.toString());
		GitHubRequest request = new GitHubRequest();
		request.setParams(params);
		request.setUri(Utils.page("/orgs/group/members?role=" + roleFilter.toString()));
		verify(client).get(request);
	}

	/**
	 * Get public members with null name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getPublicMembersNullName() throws IOException {
		service.getPublicMembers(null);
	}

	/**
	 * Get public members with empty name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getPublicMembersEmptyName() throws IOException {
		service.getPublicMembers("");
	}

	/**
	 * Get public members
	 *
	 * @throws IOException
	 */
	@Test
	public void getPublicMembers() throws IOException {
		service.getPublicMembers("group");
		GitHubRequest request = new GitHubRequest();
		request.setUri(Utils.page("/orgs/group/public_members"));
		verify(client).get(request);
	}

	/**
	 * Is member with null name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void isMemberNullName() throws IOException {
		service.isMember(null, "person");
	}

	/**
	 * Is member with empty name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void isMemberEmptyName() throws IOException {
		service.isMember("", "person");
	}

	/**
	 * Is member with null user
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void isMemberNullUser() throws IOException {
		service.isMember("group", null);
	}

	/**
	 * Is member with empty user
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void isMemberEmptyUser() throws IOException {
		service.isMember("group", "");
	}

	/**
	 * Is member
	 *
	 * @throws IOException
	 */
	@Test
	public void isMember() throws IOException {
		service.isMember("group", "person");
		GitHubRequest request = new GitHubRequest();
		request.setUri("/orgs/group/members/person");
		verify(client).get(request);
	}

	/**
	 * Is public member with null name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void isPublicMemberNullName() throws IOException {
		service.isPublicMember(null, "person");
	}

	/**
	 * Is public member with empty name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void isPublicMemberEmptyName() throws IOException {
		service.isPublicMember("", "person");
	}

	/**
	 * Is public member with null user
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void isPublicMemberNullUser() throws IOException {
		service.isPublicMember("group", null);
	}

	/**
	 * Is public member with empty user
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void isPublicMemberEmptyUser() throws IOException {
		service.isPublicMember("group", "");
	}

	/**
	 * Is public member
	 *
	 * @throws IOException
	 */
	@Test
	public void isPublicMember() throws IOException {
		service.isPublicMember("group", "person");
		GitHubRequest request = new GitHubRequest();
		request.setUri("/orgs/group/public_members/person");
		verify(client).get(request);
	}

	/**
	 * Show membership with null name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void showMembershipNullName() throws IOException {
		service.showMembership(null, "person");
	}

	/**
	 * Show member with empty name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void showMembershipEmptyName() throws IOException {
		service.showMembership("", "person");
	}

	/**
	 * Show membership with null user
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void showMembershipNullUser() throws IOException {
		service.showMembership("group", null);
	}

	/**
	 * Show membership with empty user
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void showMembershipEmptyUser() throws IOException {
		service.showMembership("group", "");
	}

	/**
	 * Show membership
	 *
	 * @throws IOException
	 */
	@Test
	public void showMembership() throws IOException {
		service.showMembership("group", "person");
		verify(client).put("/orgs/group/public_members/person");
	}

	/**
	 * Hide membership with null name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void hideMembershipNullName() throws IOException {
		service.hideMembership(null, "person");
	}

	/**
	 * Hide membership with empty name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void hideMembershipEmptyName() throws IOException {
		service.hideMembership("", "person");
	}

	/**
	 * Hide membership with null user
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void hideMembershipNullUser() throws IOException {
		service.hideMembership("group", null);
	}

	/**
	 * Hide membership with empty user
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void hideMembershipEmptyUser() throws IOException {
		service.hideMembership("group", "");
	}

	/**
	 * Hide membership
	 *
	 * @throws IOException
	 */
	@Test
	public void hideMembership() throws IOException {
		service.hideMembership("group", "person");
		verify(client).delete("/orgs/group/public_members/person");
	}

	/**
	 * Remove member with null name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void removeMemberNullName() throws IOException {
		service.removeMember(null, "person");
	}

	/**
	 * Remove member with empty name
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void removeMemberEmptyName() throws IOException {
		service.removeMember("", "person");
	}

	/**
	 * Remove member with null user
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void removeMemberNullUser() throws IOException {
		service.removeMember("group", null);
	}

	/**
	 * Remove member with empty user
	 *
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void removeMemberEmptyUser() throws IOException {
		service.removeMember("group", "");
	}

	/**
	 * Remove member
	 *
	 * @throws IOException
	 */
	@Test
	public void removeMember() throws IOException {
		service.removeMember("group", "person");
		verify(client).delete("/orgs/group/members/person");
	}
}
