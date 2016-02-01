/******************************************************************************
 *  Copyright (c) 2012 GitHub Inc.
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

import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.event.PullRequestReviewCommentPayload;
import org.junit.Test;

/**
 * Unit tests of {@link PullRequestReviewCommentPayload}
 */
public class PullRequestReviewCommentPayloadTest {

	/**
	 * Test default state of {@link PullRequestReviewCommentPayload}
	 */
	@Test
	public void defaultState() {
		PullRequestReviewCommentPayload payload = new PullRequestReviewCommentPayload();
		assertNull(payload.getAction());
		assertNull(payload.getComment());
		assertNull(payload.getPullRequest());
	}

	/**
	 * Test updating {@link PullRequestReviewCommentPayload} fields
	 */
	@Test
	public void updateFields() {
		PullRequestReviewCommentPayload payload = new PullRequestReviewCommentPayload();
		CommitComment comment = new CommitComment();
		PullRequest pullRequest = new PullRequest().setTitle("pull");
		assertEquals("created", payload.setAction("created").getAction());
		assertEquals(pullRequest, payload.setPullRequest(pullRequest).getPullRequest());
		assertEquals(comment, payload.setComment(comment).getComment());
	}
}
