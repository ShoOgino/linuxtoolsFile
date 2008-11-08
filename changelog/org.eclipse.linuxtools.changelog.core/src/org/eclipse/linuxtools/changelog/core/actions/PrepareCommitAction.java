/*******************************************************************************
 * Copyright (c) 2006, 2007 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.core.actions;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.linuxtools.changelog.core.ChangelogPlugin;
import org.eclipse.linuxtools.changelog.core.LineComparator;
import org.eclipse.linuxtools.changelog.core.Messages;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.mapping.IResourceDiff;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.ui.IContributorResourceAdapter;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ide.IContributorResourceAdapter2;


/**
 * 
 * @author klee
 *
 */
public class PrepareCommitAction extends ChangeLogAction {

	
	protected void doRun() {
		

		IRunnableWithProgress code = new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
			//	monitor.beginTask("Loading Clipboard", 1000);
				loadClipboard(monitor);
				//monitor.done();
			}
		};

		ProgressMonitorDialog pd = new ProgressMonitorDialog(getWorkbench()
				.getActiveWorkbenchWindow().getShell());

		try {
			pd.run(false /* fork */, false /* cancelable */, code);
		} catch (InvocationTargetException e) {
			ChangelogPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, ChangelogPlugin.PLUGIN_ID, IStatus.ERROR, e
							.getMessage(), e));
			return;
		} catch (InterruptedException e) {
			ChangelogPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, ChangelogPlugin.PLUGIN_ID, IStatus.ERROR, e
							.getMessage(), e));
			return;
		}
		
	//	loadClipboard();
		
	}
	
	
	private ResourceMapping getResourceMapping(Object o) {
		if (o instanceof ResourceMapping) {
			return (ResourceMapping) o;
		}
		if (o instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) o;
			Object adapted = adaptable.getAdapter(ResourceMapping.class);
			if (adapted instanceof ResourceMapping) {
				return (ResourceMapping) adapted;
			}
			adapted = adaptable.getAdapter(IContributorResourceAdapter.class);
			if (adapted instanceof IContributorResourceAdapter2) {
				IContributorResourceAdapter2 cra = (IContributorResourceAdapter2) adapted;
				return cra.getAdaptedResourceMapping(adaptable);
			}
		} else {
			Object adapted = Platform.getAdapterManager().getAdapter(o,
					ResourceMapping.class);
			if (adapted instanceof ResourceMapping) {
				return (ResourceMapping) adapted;
			}
		}
		
	
		return null;
	}
	
	private void loadClipboard(IProgressMonitor monitor) {
		
		IEditorPart currentEditor;
		
		try {
			currentEditor = getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().getActiveEditor();
		} catch (Exception e) {
			// no editor is active now so do nothing
			
			return;
		}
		
		if (currentEditor == null)
			return;
			
		
		
	//	System.out.println(currentEditor.getTitle());
		String diffResult = "";
		IEditorInput input = currentEditor.getEditorInput();
		ResourceMapping mapping = getResourceMapping(input);
		IProject project = null;
		IResource[] resources = new IResource[1];

		if (mapping != null) {
			project = mapping.getProjects()[0];
			resources[0] = (IResource)mapping.getModelObject();
		} else if (input instanceof IFileEditorInput) {
			IFileEditorInput f = (IFileEditorInput)input;
			project = f.getFile().getProject();
			resources[0] = f.getFile();
		} else {
			return; // can't get what we need
		}
		
		RepositoryProvider r = RepositoryProvider.getProvider(project);
		SyncInfoSet set = new SyncInfoSet();
		Subscriber s = r.getSubscriber();
		s.collectOutOfSync(resources, IResource.DEPTH_ZERO, set, monitor);
		SyncInfo[] infos = set.getSyncInfos();

		if (infos.length == 1) {
			int kind = SyncInfo.getChange(infos[0].getKind());
			if (kind == SyncInfo.CHANGE) {
				try {
					IDiff d = s.getDiff(infos[0].getLocal());
					if (d instanceof IThreeWayDiff
							&& ((IThreeWayDiff)d).getDirection() == IThreeWayDiff.OUTGOING) {
						IThreeWayDiff diff = (IThreeWayDiff)d;
						monitor.beginTask(null, 100);
						IResourceDiff localDiff = (IResourceDiff)diff.getLocalChange();
						IFile file = (IFile)localDiff.getResource();
						monitor.subTask(Messages.getString("ChangeLog.MergingDiffs")); // $NON-NLS-1$
						String osEncoding = file.getCharset();
						IFileRevision ancestorState = localDiff.getBeforeState();
						IStorage ancestorStorage;
						if (ancestorState != null)
							ancestorStorage = ancestorState.getStorage(monitor);
						else 
							ancestorStorage = null;

						RangeDifference[] rd = null;

						try {
							LineComparator left = new LineComparator(ancestorStorage.getContents(), osEncoding);
							LineComparator right = new LineComparator(file.getContents(), osEncoding);
							rd = RangeDifferencer.findDifferences(left, right);
							for (int j = 0; j < rd.length; ++j) {
								RangeDifference tmp = rd[j];
								if (tmp.kind() == RangeDifference.CHANGE) {
									LineNumberReader l = new LineNumberReader(new InputStreamReader(file.getContents()));
									int rightLength = tmp.rightLength() > 0 ? tmp.rightLength() : tmp.rightLength() + 1;
									for (int i = 0; i < tmp.rightStart(); ++i) {
										try {
											l.readLine(); 
										} catch (IOException e) {
											break;
										}
									}
									for (int i = 0; i < rightLength; ++i) {
										try {
											diffResult += l.readLine() + "\n"; // $NON-NLS-1$
										} catch (IOException e) {
											// do nothing
										}
									}
								}
							}
						} catch (UnsupportedEncodingException e) {
							// do nothing for now
						}
						monitor.done();
					}
				} catch (CoreException e) {
					// do nothing
				}
			}
		}
		
		if (!diffResult.equals(""))
			populateClipboardBuffer(diffResult);
	}
	
	private void populateClipboardBuffer(String input) {
		
		TextTransfer plainTextTransfer = TextTransfer.getInstance();
		Clipboard clipboard = new Clipboard(getWorkbench().getDisplay());		
		clipboard.setContents(
			new String[]{input}, 
			new Transfer[]{plainTextTransfer});	
		clipboard.dispose();
	}
	
	
	
}
