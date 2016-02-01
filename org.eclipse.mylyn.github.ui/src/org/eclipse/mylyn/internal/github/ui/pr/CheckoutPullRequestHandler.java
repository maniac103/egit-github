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
package org.eclipse.mylyn.internal.github.ui.pr;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.egit.core.op.CreateLocalBranchOperation;
import org.eclipse.egit.core.op.FetchOperation;
import org.eclipse.egit.core.op.MergeOperation;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.ui.Activator;
import org.eclipse.egit.ui.UIPreferences;
import org.eclipse.egit.ui.internal.branch.BranchOperationUI;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.mylyn.internal.github.core.pr.PullRequestComposite;
import org.eclipse.mylyn.internal.github.core.pr.PullRequestConnector;
import org.eclipse.mylyn.internal.github.core.pr.PullRequestUtils;
import org.eclipse.mylyn.internal.github.ui.GitHubUi;
import org.eclipse.mylyn.internal.github.ui.TaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskData;

/**
 * Checkout pull request handler
 */
public class CheckoutPullRequestHandler extends TaskDataHandler {

	/**
	 * ID
	 */
	public static final String ID = "org.eclipse.mylyn.github.ui.command.checkoutPullRequest"; //$NON-NLS-1$

	/**
	 * Create checkout pull request handler
	 */
	public CheckoutPullRequestHandler() {
	}

	private RevCommit getBase(Repository repo, PullRequest request)
			throws IOException {
		try (RevWalk walk = new RevWalk(repo)) {
			return walk.parseCommit(repo.resolve(request.getBase().getSha()));
		}
	}

	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final TaskData data = getTaskData(event);
		if (data == null)
			return null;

		Job job = new Job(MessageFormat.format(
				Messages.CheckoutPullRequestHandler_JobName, data.getTaskId())) {

			protected IStatus run(IProgressMonitor monitor) {
				SubProgressMonitor sub;
				try {
					PullRequestComposite prComp = PullRequestConnector
							.getPullRequest(data);
					if (prComp == null)
						return Status.CANCEL_STATUS;
					PullRequest request = prComp.getRequest();
					Repository repo = PullRequestUtils.getRepository(request);
					if (repo == null)
						return Status.CANCEL_STATUS;

					String branchName = PullRequestUtils.getBranchName(request);
					Ref branchRef = repo.getRef(branchName);
					RemoteConfig remote = null;
					String headBranch = null;

					monitor.beginTask("", 5); //$NON-NLS-1$

					// Add remote
					if (!PullRequestUtils.isFromSameRepository(request)) {
						monitor.subTask(MessageFormat
								.format(Messages.CheckoutPullRequestHandler_TaskAddRemote,
										request.getHead().getRepo().getOwner()
												.getLogin()));
						remote = PullRequestUtils.addRemote(repo, request);
						headBranch = PullRequestUtils.getHeadBranch(request);
					} else {
						remote = PullRequestUtils.getRemoteConfig(repo,
								Constants.DEFAULT_REMOTE_NAME);
						headBranch = request.getHead().getRef();
					}
					monitor.worked(1);

					// Create topic branch starting at SHA-1 of base
					if (branchRef == null) {
						sub = new SubProgressMonitor(monitor, 1);
						sub.subTask(MessageFormat
								.format(Messages.CheckoutPullRequestHandler_TaskCreateBranch,
										branchName));
						PullRequestUtils.configureTopicBranch(repo, request);
						new CreateLocalBranchOperation(repo, branchName,
								getBase(repo, request)).execute(sub);
						sub.done();
					} else
						monitor.worked(1);

					// Checkout topic branch
					if (!PullRequestUtils.isCurrentBranch(branchName, repo)) {
						sub = new SubProgressMonitor(monitor, 1);
						sub.subTask(MessageFormat
								.format(Messages.CheckoutPullRequestHandler_TaskCheckoutBranch,
										branchName));
						BranchOperationUI.checkout(repo, branchName).run(sub);
						sub.done();
					} else
						monitor.worked(1);

					// Fetch from remote
					sub = new SubProgressMonitor(monitor, 1);
					sub.subTask(MessageFormat.format(
							Messages.CheckoutPullRequestHandler_TaskFetching,
							remote.getName()));
					new FetchOperation(repo, remote, Activator.getDefault()
							.getPreferenceStore()
							.getInt(UIPreferences.REMOTE_CONNECTION_TIMEOUT),
							false).run(sub);
					sub.done();

					// Merge head onto base
					sub = new SubProgressMonitor(monitor, 1);
					sub.subTask(MessageFormat.format(
							Messages.CheckoutPullRequestHandler_TaskMerging,
							headBranch));
					new MergeOperation(repo, headBranch).execute(sub);
					sub.done();

					monitor.done();
					executeCallback(event);
				} catch (IOException e) {
					GitHubUi.logError(e);
				} catch (CoreException e) {
					GitHubUi.logError(e);
				} catch (URISyntaxException e) {
					GitHubUi.logError(e);
				} catch (InvocationTargetException e) {
					GitHubUi.logError(e);
				}
				return Status.OK_STATUS;
			}
		};
		schedule(job, event);
		return null;
	}
}
