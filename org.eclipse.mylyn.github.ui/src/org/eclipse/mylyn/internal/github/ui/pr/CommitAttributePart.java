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
import java.text.MessageFormat;

import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.ui.UIUtils;
import org.eclipse.egit.ui.internal.UIIcons;
import org.eclipse.egit.ui.internal.commit.CommitEditor;
import org.eclipse.egit.ui.internal.commit.RepositoryCommit;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.mylyn.internal.github.core.GitHub;
import org.eclipse.mylyn.internal.github.core.issue.IssueConnector;
import org.eclipse.mylyn.internal.github.core.pr.PullRequestComposite;
import org.eclipse.mylyn.internal.github.core.pr.PullRequestUtils;
import org.eclipse.mylyn.internal.github.ui.GitHubUi;
import org.eclipse.mylyn.internal.github.ui.TaskDataHandler;
import org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorSection;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Task editor section to view commits attached to a pull request
 */
public class CommitAttributePart extends AbstractTaskEditorSection {

	private CommandContributionItem fetchCommits;

	private CommandContributionItem checkoutPr;

	private CommandContributionItem mergePr;

	private CommandContributionItem rebasePr;

	private PullRequestComposite request;

	/**
	 * Create commit attribute part
	 *
	 * @param request
	 */
	public CommitAttributePart(PullRequestComposite request) {
		setPartName(Messages.CommitAttributePart_PartName);
		this.request = request;
	}

	protected Control createContent(FormToolkit toolkit, Composite parent) {
		Composite displayArea = toolkit.createComposite(parent);
		GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(displayArea);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(displayArea);

		if (request != null) {
			Composite refArea = toolkit.createComposite(displayArea);
			GridLayoutFactory.fillDefaults().spacing(0, 0).numColumns(4)
					.applyTo(refArea);
			GridDataFactory.swtDefaults().grab(true, false).applyTo(refArea);
			Image branchIcon = UIIcons.BRANCH.createImage();
			UIUtils.hookDisposal(refArea, branchIcon);
			CLabel sourceLabel = new CLabel(refArea, SWT.NONE);
			toolkit.adapt(sourceLabel, false, false);
			sourceLabel.setText(Messages.CommitAttributePart_LabelSource);
			sourceLabel.setImage(branchIcon);
			sourceLabel.setForeground(toolkit.getColors().getColor(
					IFormColors.TITLE));
			toolkit.createText(refArea, request.getRequest().getHead()
					.getLabel(), SWT.READ_ONLY);
			CLabel destLabel = new CLabel(refArea, SWT.NONE);
			toolkit.adapt(destLabel, false, false);
			destLabel.setText(Messages.CommitAttributePart_LabelDestination);
			destLabel.setImage(branchIcon);
			destLabel.setForeground(toolkit.getColors().getColor(
					IFormColors.TITLE));
			GridDataFactory.swtDefaults().indent(15, 0).applyTo(destLabel);
			toolkit.createText(refArea, request.getRequest().getBase()
					.getLabel(), SWT.READ_ONLY);
		}

		Composite treeArea = toolkit.createComposite(displayArea);
		toolkit.paintBordersFor(treeArea);
		GridLayoutFactory.fillDefaults().spacing(0, 0)
				.extendedMargins(2, 2, 2, 7).applyTo(treeArea);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(treeArea);

		TreeViewer commitViewer = new TreeViewer(toolkit.createTree(treeArea,
				SWT.V_SCROLL | SWT.H_SCROLL | toolkit.getBorderStyle()));
		commitViewer.setContentProvider(new WorkbenchContentProvider());
		commitViewer.setLabelProvider(new DelegatingStyledCellLabelProvider(
				new WorkbenchLabelProvider()));
		GridDataFactory.fillDefaults().grab(true, true)
				.applyTo(commitViewer.getControl());
		commitViewer.getControl().setData(FormToolkit.KEY_DRAW_BORDER,
				FormToolkit.TREE_BORDER);
		commitViewer.addOpenListener(new IOpenListener() {

			public void open(final OpenEvent event) {
				PullRequest pr = request.getRequest();
				Repository repo = PullRequestUtils.getRepository(pr);
				if (repo != null)
					openCommits(repo, ((IStructuredSelection) event
							.getSelection()).toArray());
				else
					PullRequestConnectorUi.showNoRepositoryDialog(pr);
			}
		});

		int size = 0;
		if (request != null) {
			PullRequestAdapter root = new PullRequestAdapter(request);
			size = root.getChildren(root).length;
			commitViewer.setInput(root);
			Point treeSize = commitViewer.getControl().computeSize(SWT.DEFAULT,
					SWT.DEFAULT);
			((GridData) commitViewer.getControl().getLayoutData()).heightHint = Math
					.min(treeSize.y, 200);
		}
		getSection().setText(
				MessageFormat.format(
						Messages.CommitAttributePart_SectionCommits, size));
		return displayArea;
	}

	private void openCommits(final Repository repository,
			final Object[] elements) {
		if (elements.length == 0)
			return;
		if (repository == null)
			return;
		try (RevWalk walk = new RevWalk(repository)) {
			for (Object element : elements) {
				String id = ((PullRequestCommitAdapter) element).getCommit()
						.getSha();
				try {
					CommitEditor.openQuiet(new RepositoryCommit(repository,
							walk.parseCommit(ObjectId.fromString(id))));
				} catch (MissingObjectException ignored) {
					boolean fetch = MessageDialog.openQuestion(getControl()
							.getShell(),
							Messages.CommitAttributePart_TitleFetch,
							Messages.CommitAttributePart_MessageFetch);
					if (fetch) {
						fetchCommits(new Runnable() {

							public void run() {
								PlatformUI.getWorkbench().getDisplay()
										.asyncExec(new Runnable() {

											public void run() {
												openCommits(repository,
														elements);
											}
										});
							}
						});
					}

				}
			}
		} catch (IOException e) {
			GitHubUi.logError(e);
		}
	}

	private CommandContributionItem createCommandContributionItem(
			String commandId) {
		CommandContributionItemParameter parameter = new CommandContributionItemParameter(
				getTaskEditorPage().getEditorSite(), commandId, commandId,
				CommandContributionItem.STYLE_PUSH);
		return new CommandContributionItem(parameter);
	}

	protected void fillToolBar(ToolBarManager toolBarManager) {
		if (TasksUiUtil.isOutgoingNewTask(getTaskEditorPage().getTask(),
				IssueConnector.KIND))
			return;
		if (request == null)
			return;

		checkoutPr = createCommandContributionItem(CheckoutPullRequestHandler.ID);
		fetchCommits = createCommandContributionItem(FetchPullRequestHandler.ID);
		mergePr = createCommandContributionItem(MergePullRequestHandler.ID);
		rebasePr = createCommandContributionItem(RebasePullRequestHandler.ID);

		// Disable actions for now
		// toolBarManager.add(checkoutPr);
		toolBarManager.add(fetchCommits);
		// toolBarManager.add(mergePr);
		// toolBarManager.add(rebasePr);
	}

	@Override
	protected String getInfoOverlayText() {
		return request != null ? request.getRequest().getHead().getLabel()
				: null;
	}

	protected boolean shouldExpandOnCreate() {
		return true;
	}

	private void fetchCommits(final Runnable postHandler) {
		IHandlerService handlerService = (IHandlerService) getTaskEditorPage()
				.getEditorSite().getService(IHandlerService.class);
		try {
			IEvaluationContext context = TaskDataHandler.createContext(
					new StructuredSelection(getTaskData()), handlerService);
			if (postHandler != null)
				context.addVariable(TaskDataHandler.POST_HANDLER_CALLBACK,
						postHandler);
			handlerService.executeCommandInContext(fetchCommits.getCommand(),
					new Event(), context);
		} catch (CommandException e) {
			GitHub.logError(e);
		}
	}
}
