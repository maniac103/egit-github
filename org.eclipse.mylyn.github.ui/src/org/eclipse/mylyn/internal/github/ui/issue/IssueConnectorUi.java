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
package org.eclipse.mylyn.internal.github.ui.issue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.mylyn.internal.github.core.GitHub;
import org.eclipse.mylyn.internal.github.core.issue.IssueConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TaskHyperlink;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskSearchPage;
import org.eclipse.mylyn.tasks.ui.wizards.NewTaskWizard;
import org.eclipse.mylyn.tasks.ui.wizards.RepositoryQueryWizard;

/**
 * GitHub connector specific UI extensions.
 */
public class IssueConnectorUi extends AbstractRepositoryConnectorUi {

	private final Pattern issuePattern = Pattern
			.compile("(?:([a-zA-Z0-9_\\.-]+)(?:/([a-zA-Z0-9_\\.-]+))?)?\\#(\\d+)"); //$NON-NLS-1$

	/**
	 * Get core repository connector
	 * 
	 * @return connector
	 */
	public static IssueConnector getCoreConnector() {
		return (IssueConnector) TasksUi
				.getRepositoryConnector(GitHub.CONNECTOR_KIND);
	}

	/**
	 * 
	 * 
	 * @return the unique type of the repository: "github"
	 */
	@Override
	public String getConnectorKind() {
		return GitHub.CONNECTOR_KIND;
	}

	/**
	 * 
	 * 
	 * @return {@link AbstractRepositorySettingsPage} with GitHub specific
	 *         parameter like user name, password, ...
	 */
	@Override
	public ITaskRepositoryPage getSettingsPage(
			final TaskRepository taskRepository) {
		return new IssueRepositorySettingsPage(taskRepository);
	}

	/**
	 * 
	 * 
	 * @return {@link NewTaskWizard} with GitHub specific tab
	 */
	@Override
	public IWizard getNewTaskWizard(final TaskRepository taskRepository,
			final ITaskMapping taskSelection) {
		return new NewTaskWizard(taskRepository, taskSelection);
	}

	/**
	 * This {@link AbstractRepositoryConnectorUi} has search page.
	 * 
	 * @return {@code true}
	 */
	@Override
	public boolean hasSearchPage() {
		return true;
	}

	/**
	 * Returns {@link IWizard} used in Mylyn for creating new queries. This
	 * {@link IWizard} has a wizard page for creating GitHub specific task
	 * queries.
	 * 
	 * @return {@link RepositoryQueryWizard} with GitHub specific query page
	 */
	@Override
	public IWizard getQueryWizard(final TaskRepository taskRepository,
			final IRepositoryQuery queryToEdit) {
		RepositoryQueryWizard wizard = new RepositoryQueryWizard(taskRepository);
		IssueRepositoryQueryPage queryPage = new IssueRepositoryQueryPage(
				taskRepository, queryToEdit);
		wizard.addPage(queryPage);
		return wizard;
	}

	public IHyperlink[] findHyperlinks(TaskRepository repository, String text,
			int index, int textOffset) {
		List<IHyperlink> hyperlinks = new ArrayList<IHyperlink>();

		Matcher matcher = issuePattern.matcher(text);
		while (matcher.find()) {
			if (index == -1
					|| (index >= matcher.start() && index <= matcher.end())) {
				String user = matcher.group(1);
				String project = matcher.group(2);
				String taskId = matcher.group(3);

				if (project == null && user != null) {
					// same project name, different user
					String url = repository.getUrl();
					RepositoryId repo = GitHub.getRepository(url);
					if (repo != null)
						project = repo.getName();
				}

				TaskRepository taskRepository = null;
				if (user == null && project == null) {
					taskRepository = repository;
				} else if (user != null && project != null) {
					String repositoryUrl = GitHub
							.createGitHubUrl(user, project);
					taskRepository = TasksUi
							.getRepositoryManager()
							.getRepository(GitHub.CONNECTOR_KIND, repositoryUrl);
					if (taskRepository == null) {
						repositoryUrl = GitHub.createGitHubUrlAlternate(user,
								project);
						taskRepository = TasksUi.getRepositoryManager()
								.getRepository(GitHub.CONNECTOR_KIND,
										repositoryUrl);
					}
				}
				if (taskRepository != null) {
					Region region = createRegion(textOffset, matcher);
					hyperlinks
							.add(new TaskHyperlink(region, repository, taskId));
				} else if (user != null && project != null) {
					Region region = createRegion(textOffset, matcher);
					String url = GitHub.createGitHubUrl(user, project)
							+ "/issues/" + taskId;
					hyperlinks.add(new URLHyperlink(region, url));
				}
			}
		}
		return hyperlinks.toArray(new IHyperlink[hyperlinks.size()]);
	}

	private Region createRegion(int textOffset, Matcher matcher) {
		return new Region(matcher.start() + textOffset, matcher.end()
				- matcher.start());
	}

	/**
	 * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#getSearchPage(org.eclipse.mylyn.tasks.core.TaskRepository,
	 *      org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public ITaskSearchPage getSearchPage(TaskRepository repository,
			IStructuredSelection selection) {
		return new IssueRepositoryQueryPage(repository, null);
	}

	@Override
	public String getTaskKindLabel(ITask task) {
		return Messages.IssueConnectorUi_LabelIssueKind;
	}
}
