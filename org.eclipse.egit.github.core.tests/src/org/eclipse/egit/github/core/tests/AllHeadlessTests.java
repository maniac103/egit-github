/*******************************************************************************
 * Copyright (c) 2011 Red Hat and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green <david.green@tasktop.com> - initial contribution
 *     Christian Trutz <christian.trutz@gmail.com> - initial contribution
 *     Chris Aniszczyk <caniszczyk@gmail.com> - initial contribution
 *******************************************************************************/
package org.eclipse.egit.github.core.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * All headless unit tests of GitHub Java API
 */
@RunWith(Suite.class)
@SuiteClasses({ ApplicationTest.class, //
		AuthorizationTest.class, //
		BlobTest.class, //
		CollaboratorServiceTest.class, //
		CommentTest.class, //
		CommitCommentPayloadTest.class, //
		CommitCommentTest.class, //
		CommitFileTest.class, //
		CommitServiceTest.class, //
		CommitStatsTest.class, //
		CommitStatusTest.class, //
		CommitTest.class, //
		CommitUserTest.class, //
		ContentsServiceTest.class, //
		ContributorTest.class, //
		CreatePayloadTest.class, //
		DataServiceTest.class, //
		DateFormatterTest.class, //
		DateUtilTest.class, //
		DeletePayloadTest.class, //
		DeployKeyServiceTest.class, //
		DownloadPayloadTest.class, //
		DownloadResourceTest.class, //
		DownloadServiceTest.class, //
		DownloadTest.class, //
		EncodingUtilsTest.class, //
		EventFormatterTest.class, //
		EventPayloadTest.class, //
		EventRepositoryTest.class, //
		EventServiceTest.class, //
		EventTest.class, //
		FieldErrorTest.class, //
		FollowPayloadTest.class, //
		ForkApplyPayloadTest.class, //
		ForkPayloadTest.class, //
		GistChangeStatusTest.class, //
		GistFileTest.class, //
		GistPayloadTest.class, //
		GistRevisionTest.class, //
		GistServiceTest.class, //
		GistTest.class, //
		GitHubClientTest.class, //
		GollumPageTest.class, //
		GollumPayloadTest.class, //
		GsonUtilsTest.class, //
		IdTest.class, //
		IssueCommentPayloadTest.class, //
		IssueEventTest.class, //
		IssueServiceTest.class, //
		IssueTest.class, //
		IssuesPayloadTest.class, //
		KeyTest.class, //
		LabelComparatorTest.class, //
		LabelServiceTest.class, //
		LabelTest.class, //
		LanguageTest.class, //
		MarkdownServiceTest.class, //
		MemberPayloadTest.class, //
		MergeStatusTest.class, //
		MilestoneComparatorTest.class, //
		MilestoneServiceTest.class, //
		MilestoneTest.class, //
		OAuthServiceTest.class, //
		OrganizationServiceTest.class, //
		PagedRequestTest.class, //
		PullRequestMarkerTest.class, //
		PullRequestPayloadTest.class, //
		PullRequestReviewCommentPayloadTest.class, //
		PullRequestServiceTest.class, //
		PullRequestTest.class, //
		PushPayloadTest.class, //
		ReferenceTest.class, //
		RepositoryBranchTest.class, //
		RepositoryCommitCompareTest.class, //
		RepositoryCommitTest.class, //
		RepositoryContentsTest.class, //
		RepositoryHookResponseTest.class, //
		RepositoryHookTest.class, //
		RepositoryIdTest.class, //
		RepositoryIssueTest.class, //
		RepositoryServiceTest.class, //
		RepositoryTagTest.class, //
		RepositoryTest.class, //
		RequestErrorTest.class, //
		RequestExceptionTest.class, //
		SearchIssueTest.class, //
		SearchRepositoryTest.class, //
		ShaResourceTest.class, //
		StargazerServiceTest.class, //
		TagTest.class, //
		TeamAddPayloadTest.class, //
		TeamServiceTest.class, //
		TeamTest.class, //
		TreeEntryTest.class, //
		TreeTest.class, //
		TypedResourceTest.class, //
		UrlUtilsTest.class, //
		UserPlanTest.class, //
		UserServiceTest.class, //
		UserTest.class, //
		WatchPayloadTest.class, //
		WatcherServiceTest.class //
})
public class AllHeadlessTests {

}
