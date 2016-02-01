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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.internal.github.core.pr.PullRequestComposite;
import org.eclipse.mylyn.internal.github.core.pr.PullRequestConnector;
import org.eclipse.mylyn.internal.github.core.pr.PullRequestUtils;
import org.eclipse.mylyn.internal.github.ui.GitHubUi;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.mylyn.resources.ui.ResourcesUi;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskActivationAdapter;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;

/**
 * Adds the files from a pull request's commits to the context when it is
 * activated.
 */
public class PullRequestContextSynchronizer extends TaskActivationAdapter {

	public void taskActivated(ITask task) {
		if (task == null)
			return;
		if (!PullRequestConnector.KIND.equals(task.getConnectorKind()))
			return;
		IInteractionContext context = ContextCore.getContextManager()
				.getActiveContext();
		if (context == null)
			return;

		try {
			TaskData data = TasksUi.getTaskDataManager().getTaskData(task);
			PullRequestComposite prComp = PullRequestConnector
					.getPullRequest(data);
			if (prComp == null)
				return;
			PullRequest request = prComp.getRequest();
			Repository repository = PullRequestUtils.getRepository(request);
			if (repository == null)
				return;
			try (RevWalk walk = new RevWalk(repository);
					TreeWalk diffs = new TreeWalk(walk.getObjectReader())) {
				diffs.setFilter(TreeFilter.ANY_DIFF);
				diffs.setRecursive(true);
				diffs.addTree(walk.parseCommit(
						ObjectId.fromString(request.getHead().getSha())).getTree());
				diffs.addTree(walk.parseCommit(
						ObjectId.fromString(request.getBase().getSha())).getTree());
				Set<IResource> resources = new HashSet<IResource>();
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				String base = repository.getWorkTree().getAbsolutePath() + "/"; //$NON-NLS-1$
				while (diffs.next()) {
					IFile file = root.getFileForLocation(Path.fromOSString(base
							+ diffs.getPathString()));
					if (file != null)
						resources.add(file);
				}
				if (!resources.isEmpty())
					ResourcesUi.addResourceToContext(resources,
							InteractionEvent.Kind.SELECTION);
			}
		} catch (MissingObjectException ignored) {
			// Ignored
		} catch (IOException e) {
			GitHubUi.logError(e);
		} catch (CoreException e) {
			GitHubUi.logError(e);
		}
	}
}
