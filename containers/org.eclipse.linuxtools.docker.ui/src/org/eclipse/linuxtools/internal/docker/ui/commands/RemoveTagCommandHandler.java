/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.commands;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.views.DVMessages;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRemoveTag;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class RemoveTagCommandHandler extends AbstractHandler {

	private final static String REMOVE_TAG_JOB_TITLE = "ImageRemoveTagTitle.msg"; //$NON-NLS-1$
	private final static String REMOVE_TAG_MSG = "ImageRemoveTag.msg"; //$NON-NLS-1$
	private static final String ERROR_REMOVING_TAG_IMAGE = "ImageRemoveTagError.msg"; //$NON-NLS-1$
	
	@Override
	public Object execute(final ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		final List<IDockerImage> selectedImages = CommandUtils
				.getSelectedImages(activePart);
		final IDockerConnection connection = CommandUtils
				.getCurrentConnection(activePart);
		if (selectedImages.size() != 1 || connection == null) {
			Activator.log(new DockerException(CommandMessages
					.getString("Command.missing.selection.failure"))); //$NON-NLS-1$
			return null;
		}
		final IDockerImage image = selectedImages.get(0);
		final ImageRemoveTag wizard = new ImageRemoveTag(image);
		final boolean removeTag = CommandUtils.openWizard(wizard,
				HandlerUtil.getActiveShell(event));
		if (removeTag) {
			performRemoveTagImage(connection, wizard.getTag());
		}
		return null;
	}
	
	private void performRemoveTagImage(final IDockerConnection connection,
			final String tag) {
		final Job removeTagImageJob = new Job(
				DVMessages.getString(REMOVE_TAG_JOB_TITLE)) {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask(DVMessages.getString(REMOVE_TAG_MSG), 2);
				try {
					((DockerConnection) connection).removeTag(tag);
					monitor.worked(1);
					((DockerConnection) connection).getImages(true);
					monitor.worked(1);
				} catch (final DockerException e) {
					Display.getDefault().syncExec(() -> MessageDialog.openError(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow()
									.getShell(),
							DVMessages.getFormattedString(
									ERROR_REMOVING_TAG_IMAGE, tag),
							e.getMessage()));
					// for now
				} catch (InterruptedException e) {
					// do nothing
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}

		};

		removeTagImageJob.schedule();

	}

}
