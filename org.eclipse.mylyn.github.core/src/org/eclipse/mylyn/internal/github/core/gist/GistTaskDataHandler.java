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
package org.eclipse.mylyn.internal.github.core.gist;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.github.core.GitHub;
import org.eclipse.mylyn.internal.github.core.GitHubTaskDataHandler;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.RepositoryResponse.ResponseKind;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;

/**
 * Gist task data handler class.
 */
public class GistTaskDataHandler extends GitHubTaskDataHandler {

	/**
	 * SUMMARY_LENGTH
	 */
	public static final int SUMMARY_LENGTH = 80;

	/**
	 * Fill task data with comments
	 *
	 * @param repository
	 * @param data
	 * @param comments
	 * @return specified task data
	 */
	public TaskData fillComments(TaskRepository repository, TaskData data,
			List<Comment> comments) {
		addComments(data.getRoot(), comments, repository);
		return data;
	}

	/**
	 * Get gist url
	 *
	 * @param repositoryUrl
	 * @param gist
	 * @return url
	 */
	public String getGistUrl(String repositoryUrl, Gist gist) {
		return repositoryUrl + '/' + gist.getId();
	}

	/**
	 * Fill task data with data from gist
	 *
	 * @param repository
	 * @param data
	 * @param gist
	 * @return specified task data
	 */
	public TaskData fillTaskData(TaskRepository repository, TaskData data,
			Gist gist) {
		boolean isOwner = isOwner(repository, gist.getOwner());
		TaskAttributeMapper mapper = data.getAttributeMapper();

		TaskAttribute key = GistAttribute.KEY.getMetadata().create(data);
		mapper.setValue(key, gist.getId());

		TaskAttribute description = GistAttribute.DESCRIPTION.getMetadata()
				.create(data);
		description.getMetaData().setReadOnly(!isOwner);
		String gistDescription = gist.getDescription();
		if (gistDescription != null)
			mapper.setValue(description, gistDescription);

		TaskAttribute created = GistAttribute.CREATED.getMetadata()
				.create(data);
		mapper.setDateValue(created, gist.getCreatedAt());

		TaskAttribute updated = GistAttribute.UPDATED.getMetadata()
				.create(data);
		mapper.setDateValue(updated, gist.getUpdatedAt());

		TaskAttribute url = GistAttribute.URL.getMetadata().create(data);
		url.setValue(gist.getHtmlUrl());

		TaskAttribute cloneUrl = GistAttribute.CLONE_URL.getMetadata().create(
				data);
		if (isOwner)
			cloneUrl.setValue(gist.getGitPushUrl());
		else
			cloneUrl.setValue(gist.getGitPullUrl());

		IRepositoryPerson reporterPerson = null;
		User owner = gist.getOwner();
		if (owner != null) {
			TaskAttribute reporter = GistAttribute.AUTHOR.getMetadata().create(
					data);
			reporterPerson = createPerson(owner, repository);
			mapper.setRepositoryPerson(reporter, reporterPerson);

			TaskAttribute gravatar = GistAttribute.AUTHOR_GRAVATAR
					.getMetadata().create(data);
			mapper.setValue(gravatar, owner.getAvatarUrl());
		}

		Map<String, GistFile> files = gist.getFiles();
		int fileCount = 0;
		long sizeCount = 0;
		if (files != null && !files.isEmpty()) {
			int count = 1;
			for (GistFile file : files.values()) {
				fileCount++;
				sizeCount += file.getSize();
				TaskAttachmentMapper attachmentMapper = new TaskAttachmentMapper();
				attachmentMapper.setFileName(file.getFilename());
				attachmentMapper.setReplaceExisting(true);
				attachmentMapper.setLength((long) file.getSize());
				attachmentMapper.setPatch(false);
				attachmentMapper.setAuthor(reporterPerson);
				attachmentMapper.setAttachmentId(file.getFilename());
				TaskAttribute attribute = data.getRoot().createAttribute(
						TaskAttribute.PREFIX_ATTACHMENT + count);
				attachmentMapper.applyTo(attribute);

				GistAttribute.RAW_FILE_URL.getMetadata().create(attribute)
						.setValue(file.getRawUrl());

				count++;
			}
		}

		GistAttribute.COMMENT_NEW.getMetadata().create(data);

		TaskAttribute summary = GistAttribute.SUMMARY.getMetadata()
				.create(data);
		mapper.setValue(summary,
				generateSummary(fileCount, sizeCount, gist.getDescription()));

		return data;
	}

	private String generateSummary(int files, long size, String description) {
		StringBuilder summaryText = new StringBuilder();
		if (description != null && description.length() > 0) {
			description = description.trim();
			int firstLine = description.indexOf('\n');
			if (firstLine != -1)
				description = description.substring(0, firstLine).trim();
			if (description.length() > SUMMARY_LENGTH) {
				// Break on last whitespace if maximum length is in the middle
				// of a word
				if (!Character.isWhitespace(description.charAt(SUMMARY_LENGTH))
						&& !Character.isWhitespace(description
								.charAt(SUMMARY_LENGTH - 1))) {
					int lastWhitespace = description.lastIndexOf(' ');
					if (lastWhitespace > 0)
						description = description.substring(0, lastWhitespace);
					else
						description = description.substring(0, SUMMARY_LENGTH);
				} else
					description = description.substring(0, SUMMARY_LENGTH);
				description = description.trim();
			}
			if (description.length() > 0)
				summaryText.append(description).append(' ');
		}
		if (files != 1)
			summaryText.append(MessageFormat.format(
					Messages.GistTaskDataHandler_FilesMultiple, files));
		else
			summaryText.append(Messages.GistTaskDataHandler_FilesSingle);
		summaryText.append(',').append(' ').append(formatSize(size));
		return summaryText.toString();
	}

	private String formatSize(long size) {
		if (size == 1)
			return Messages.GistTaskDataHandler_SizeByte;
		else if (size < 1024)
			return new DecimalFormat(Messages.GistTaskDataHandler_SizeBytes)
					.format(size);
		else if (size >= 1024 && size <= 1048575)
			return new DecimalFormat(Messages.GistTaskDataHandler_SizeKilobytes)
					.format(size / 1024.0);
		else if (size >= 1048576 && size <= 1073741823)
			return new DecimalFormat(Messages.GistTaskDataHandler_SizeMegabytes)
					.format(size / 1048576.0);
		else
			return new DecimalFormat(Messages.GistTaskDataHandler_SizeGigabytes)
					.format(size / 1073741824.0);
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler#postTaskData(org.eclipse.mylyn.tasks.core.TaskRepository,
	 *      org.eclipse.mylyn.tasks.core.data.TaskData, java.util.Set,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public RepositoryResponse postTaskData(TaskRepository repository,
			TaskData taskData, Set<TaskAttribute> oldAttributes,
			IProgressMonitor monitor) throws CoreException {
		RepositoryResponse response = null;

		Gist gist = new Gist();
		GitHubClient client = GistConnector.createClient(repository);
		AuthenticationCredentials credentials = repository
				.getCredentials(AuthenticationType.REPOSITORY);
		if (credentials != null) {
			client.setCredentials(credentials.getUserName(),
					credentials.getPassword());
			gist.setUser(new User().setLogin(credentials.getUserName()));
		}

		GistService service = new GistService(client);
		TaskAttribute root = taskData.getRoot();
		gist.setId(taskData.getTaskId());
		gist.setDescription(root.getAttribute(
				GistAttribute.DESCRIPTION.getMetadata().getId()).getValue());

		if (taskData.isNew()) {
			try {
				gist = service.createGist(gist);
			} catch (IOException e) {
				throw new CoreException(GitHub.createWrappedStatus(e));
			}
			response = new RepositoryResponse(ResponseKind.TASK_CREATED,
					gist.getId());
		} else {
			try {
				String newComment = root.getAttribute(
						GistAttribute.COMMENT_NEW.getMetadata().getId())
						.getValue();
				if (newComment.length() > 0)
					service.createComment(taskData.getTaskId(), newComment);
				String author = GistAttribute.AUTHOR.getMetadata().getValue(
						taskData);
				if (isOwner(repository, author))
					service.updateGist(gist);
			} catch (IOException e) {
				throw new CoreException(GitHub.createWrappedStatus(e));
			}
			response = new RepositoryResponse(ResponseKind.TASK_UPDATED,
					taskData.getTaskId());
		}
		return response;
	}

	/**
	 * @see org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler#initializeTaskData(org.eclipse.mylyn.tasks.core.TaskRepository,
	 *      org.eclipse.mylyn.tasks.core.data.TaskData,
	 *      org.eclipse.mylyn.tasks.core.ITaskMapping,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean initializeTaskData(TaskRepository repository, TaskData data,
			ITaskMapping initializationData, IProgressMonitor monitor)
			throws CoreException {
		TaskAttributeMapper mapper = data.getAttributeMapper();

		TaskAttribute summary = GistAttribute.SUMMARY.getMetadata()
				.create(data);
		mapper.setValue(summary, Messages.GistTaskDataHandler_SummaryNewGist);
		GistAttribute.DESCRIPTION.getMetadata().create(data);

		return true;
	}

	/**
	 * Is the given Gist author the same user as configured via the task
	 * repository's credentials?
	 *
	 * @param repository
	 * @param author
	 * @return true if owner, false otherwise
	 */
	protected boolean isOwner(TaskRepository repository, User author) {
		if (author == null)
			return false;
		return isOwner(repository, author.getLogin());
	}

	/**
	 * Is the given Gist author the same user as configured via the task
	 * repository's credentials?
	 *
	 * @param repository
	 * @param author
	 * @return true if owner, false otherwise
	 */
	protected boolean isOwner(TaskRepository repository, String author) {
		AuthenticationCredentials creds = repository
				.getCredentials(AuthenticationType.REPOSITORY);
		if (creds == null)
			return false;
		return author != null && author.length() > 0
				&& author.equals(creds.getUserName());
	}
}
