/*******************************************************************************
 *  Copyright (c) 2011 GitHub Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Kevin Sawicki (GitHub Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.internal.github.ui.gist;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.egit.core.RepositoryUtil;
import org.eclipse.egit.core.op.CloneOperation;
import org.eclipse.egit.core.op.CloneOperation.PostCloneTask;
import org.eclipse.egit.core.op.ConnectProviderOperation;
import org.eclipse.egit.ui.Activator;
import org.eclipse.egit.ui.UIPreferences;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.mylyn.internal.github.core.gist.GistAttribute;
import org.eclipse.mylyn.internal.github.ui.TaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Clone Gist handler class.
 */
public class CloneGistHandler extends TaskDataHandler {

	/**
	 * Get gist name for task data used to create projects and Git repositories
	 *
	 * @param data
	 * @return name
	 */
	public static String getGistName(TaskData data) {
		return "gist-" + data.getTaskId(); //$NON-NLS-1$
	}

	private static IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	private static RepositoryUtil getRepoUtil() {
		return org.eclipse.egit.core.Activator.getDefault().getRepositoryUtil();
	}

	/**
	 * ID
	 */
	public static final String ID = "org.eclipse.mylyn.github.ui.command.cloneGist"; //$NON-NLS-1$

	@Override
	public boolean isEnabled(TaskData data) {
		String id = getGistName(data);
		return !getWorkspaceRoot().getProject(id).exists()
				&& !getRepoUtil().getConfiguredRepositories().contains(id);
	}

	private File getParentDirectory() {
		String destinationDir = RepositoryUtil.getDefaultRepositoryDir();
		File parentDir = new File(destinationDir);
		if (!parentDir.exists() || !parentDir.isDirectory())
			parentDir = ResourcesPlugin.getWorkspace().getRoot()
					.getRawLocation().toFile();
		return parentDir;
	}

	private void createProject(final File workDir, final String name,
			final Repository repository, IProgressMonitor monitor)
			throws CoreException {
		IProjectDescription description = null;
		String projectName = null;
		File projectFile = new File(workDir, ".project"); //$NON-NLS-1$
		if (projectFile.exists()) {
			description = ResourcesPlugin.getWorkspace()
					.loadProjectDescription(
							Path.fromOSString(projectFile.getAbsolutePath()));
			projectName = description.getName();
		} else {
			description = ResourcesPlugin.getWorkspace().newProjectDescription(
					name);
			description
					.setLocation(Path.fromOSString(workDir.getAbsolutePath()));
			projectName = name;
		}

		monitor.setTaskName(Messages.CloneGistHandler_TaskCreatingProject);
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(projectName);
		project.create(description, monitor);
		project.open(IResource.BACKGROUND_REFRESH, monitor);

		monitor.setTaskName(Messages.CloneGistHandler_TaskConnectingProject);
		new ConnectProviderOperation(project, repository.getDirectory())
				.execute(monitor);
	}

	private CloneOperation createCloneOperation(TaskData data, String name)
			throws IOException, URISyntaxException {
		String pullUrl = data.getRoot()
				.getAttribute(GistAttribute.CLONE_URL.getMetadata().getId())
				.getValue();
		URIish uri = new URIish(pullUrl);
		int timeout = Activator.getDefault().getPreferenceStore()
				.getInt(UIPreferences.REMOTE_CONNECTION_TIMEOUT);
		final File workDir = new File(getParentDirectory(), name);

		if (getRepoUtil().getConfiguredRepositories().contains(
				new File(workDir, Constants.DOT_GIT).getAbsolutePath()))
			throw new IOException(MessageFormat.format(
					Messages.CloneGistHandler_ErrorRepoExists, name));

		return new CloneOperation(uri, true, null, workDir, Constants.R_HEADS
				+ Constants.MASTER, Constants.DEFAULT_REMOTE_NAME, timeout);
	}

	private Job createCloneJob(final ExecutionEvent event, final TaskData data) {
		Job job = new Job(Messages.CloneGistHandler_TaskCloning) {

			protected IStatus run(IProgressMonitor monitor) {
				try {
					final String name = getGistName(data);

					CloneOperation operation = createCloneOperation(data, name);

					operation.addPostCloneTask(new PostCloneTask() {

						public void execute(Repository repository,
								IProgressMonitor monitor) throws CoreException {
							if (monitor.isCanceled())
								return;
							monitor.setTaskName(Messages.CloneGistHandler_TaskRegisteringRepository);
							getRepoUtil().addConfiguredRepository(
									repository.getDirectory());
						}
					});

					operation.addPostCloneTask(new PostCloneTask() {

						public void execute(final Repository repository,
								IProgressMonitor monitor) throws CoreException {
							IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

								public void run(IProgressMonitor monitor)
										throws CoreException {
									if (monitor.isCanceled())
										return;
									createProject(repository.getDirectory()
											.getParentFile(), name, repository,
											monitor);
								}
							};
							ResourcesPlugin.getWorkspace().run(runnable,
									monitor);
						}
					});

					operation.run(monitor);
				} catch (Exception e) {
					displayError(event, e);
					Activator.logError("Error cloning gist", e); //$NON-NLS-1$
				} finally {
					fireHandlerChanged(new HandlerEvent(CloneGistHandler.this,
							true, false));
				}
				return Status.OK_STATUS;
			}
		};
		return job;
	}

	private void displayError(final ExecutionEvent event, Exception exception) {
		final Throwable cause = exception.getCause() != null ? exception
				.getCause() : exception;
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			public void run() {
				ErrorDialog.openError(HandlerUtil.getActiveShell(event),
						Messages.CloneGistHandler_ErrorTitle,
						Messages.CloneGistHandler_ErrorMessage, Activator
								.createErrorStatus(cause.getLocalizedMessage(),
										cause));
			}
		});
	}

	/**
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		TaskData data = getTaskData(event);
		if (data != null)
			schedule(createCloneJob(event, data), event);
		return null;
	}
}
