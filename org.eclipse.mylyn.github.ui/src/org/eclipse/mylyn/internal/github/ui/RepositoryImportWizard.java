/*******************************************************************************
 *  Copyright (c) 2011 Christian Trutz
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Christian Trutz - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.internal.github.ui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.egit.core.Activator;
import org.eclipse.egit.core.RepositoryUtil;
import org.eclipse.egit.core.op.CloneOperation;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.SearchRepository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.ui.UIPreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.mylyn.internal.github.core.GitHub;
import org.eclipse.mylyn.internal.github.core.GitHubException;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

/**
 * {@link IImportWizard} for cloning GitHub repositories.
 */
public class RepositoryImportWizard extends Wizard implements IImportWizard {

	private final RepositorySearchWizardPage repositorySearchWizardPage = new RepositorySearchWizardPage();

	/**
	 * Create repository import wizard
	 */
	public RepositoryImportWizard() {
		setNeedsProgressMonitor(true);
		setDefaultPageImageDescriptor(WorkbenchImages
				.getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_IMPORT_WIZ));
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {
		addPage(repositorySearchWizardPage);
	}

	private CloneOperation createCloneOperation(SearchRepository repo,
			RepositoryService service) throws IOException, URISyntaxException {
		Repository fullRepo = service.getRepository(repo);
		URIish uri = new URIish(fullRepo.getCloneUrl());

		IPreferenceStore store = org.eclipse.egit.ui.Activator.getDefault()
				.getPreferenceStore();

		String defaultRepoDir = RepositoryUtil.getDefaultRepositoryDir();
		File directory = new File(new File(defaultRepoDir, repo.getOwner()),
				repo.getName());

		int timeout = store.getInt(UIPreferences.REMOTE_CONNECTION_TIMEOUT);

		return new CloneOperation(uri, true, null, directory, Constants.R_HEADS
				+ Constants.MASTER, Constants.DEFAULT_REMOTE_NAME, timeout);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		final SearchRepository[] repositories = repositorySearchWizardPage
				.getRepositories();
		String name = repositories.length != 1 ? MessageFormat.format(
				Messages.RepositoryImportWizard_CloningRepositories,
				repositories.length)
				: Messages.RepositoryImportWizard_CloningRepository;
		Job job = new Job(name) {
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(
						Messages.RepositoryImportWizard_CloningRepository,
						repositories.length * 3);
				GitHubClient client = GitHub
						.configureClient(new GitHubClient());
				RepositoryUtil repositoryUtil = Activator.getDefault()
						.getRepositoryUtil();
				RepositoryService service = new RepositoryService(client);
				for (SearchRepository repo : repositories)
					try {
						final String id = repo.getId();
						monitor.setTaskName(MessageFormat
								.format(Messages.RepositoryImportWizard_CreatingOperation,
										id));
						CloneOperation op = createCloneOperation(repo, service);
						monitor.worked(1);

						monitor.setTaskName(MessageFormat.format(
								Messages.RepositoryImportWizard_Cloning, id));
						SubProgressMonitor sub = new SubProgressMonitor(
								monitor, 1);
						op.run(sub);
						sub.done();

						monitor.setTaskName(MessageFormat
								.format(Messages.RepositoryImportWizard_Registering,
										id));
						repositoryUtil.addConfiguredRepository(op.getGitDir());
						monitor.worked(1);
					} catch (InvocationTargetException e) {
						GitHubUi.logError(e);
					} catch (InterruptedException e) {
						GitHubUi.logError(e);
					} catch (IOException e) {
						GitHubUi.logError(GitHubException.wrap(e));
					} catch (URISyntaxException e) {
						GitHubUi.logError(e);
					}
				monitor.done();

				return Status.OK_STATUS;
			}
		};
		IWorkbenchSiteProgressService progress = (IWorkbenchSiteProgressService) PlatformUI
				.getWorkbench().getService(IWorkbenchSiteProgressService.class);
		if (progress != null)
			progress.schedule(job);
		else
			job.schedule();
		return true;
	}
}
