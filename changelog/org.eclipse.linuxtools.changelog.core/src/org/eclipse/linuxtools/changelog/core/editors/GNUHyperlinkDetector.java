/*******************************************************************************
 * Copyright (c) 2006 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *    Remy Chi Jian Suen <remy.suen@gmail.com> - clean up internal API references (bug #179389)
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.core.editors;

import org.eclipse.team.ui.synchronize.SyncInfoCompareInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.compare.CompareEditorInput;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.Token;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * 
 * @author klee (Kyu Lee)
 */
public class GNUHyperlinkDetector implements IHyperlinkDetector {

	private IPath documentLocation;

	/**
	 * Creates a new URL hyperlink detector for GNU Format changelogs.
	 * 
	 * NOTE: It assumes that the path this ChangeLog is in, is root
	 * directory of path names in this ChangeLog.
	 * 
	 * ex) ChangeLog is in /some/project and in ChangeLog, path names are like
	 * abc/file.java ghi/file2.java
	 * 
	 * then absolute path of file.java and file2.java are
	 * /some/project/abc/file.java and /some/project/ghi/file2.java
	 * 
	 * @param textViewer The text viewer in which to detect the hyperlink.
	 */
	public GNUHyperlinkDetector(ITextViewer textViewer, TextEditor editor) {
		Assert.isNotNull(textViewer);

		documentLocation = getDocumentLocation(editor);

	}

	/**
	 * Detector using RuleBasedScanner.
	 */
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {

		IDocument thisDoc = textViewer.getDocument();

		GNUElementScanner scanner = new GNUElementScanner();

		scanner.setDefaultReturnToken(new Token("default"));

		IRegion lineInfo = null;

		try {
			lineInfo = thisDoc.getLineInformationOfOffset(region.getOffset());
		} catch (org.eclipse.jface.text.BadLocationException e1) {
			e1.printStackTrace();
			return null;
		}

		if (lineInfo == null)
			return null;

		scanner.setRange(thisDoc, lineInfo.getOffset(), lineInfo.getLength());

		Token tmpToken = (Token) scanner.nextToken();

		String tokenStr = (String) tmpToken.getData();

		if (tokenStr == null) {
			return null;
		}

		// try to find non-default token if fail, return null.
		while (tokenStr.equals("default")) {
			tmpToken = (Token) scanner.nextToken();
			tokenStr = (String) tmpToken.getData();
			if (tokenStr == null)
				return null;

		}

		Region tokenRegion = new Region(scanner.getTokenOffset(), scanner
				.getTokenLength());

		String line = "";
		try {
			line = thisDoc
					.get(tokenRegion.getOffset(), tokenRegion.getLength());
		} catch (org.eclipse.jface.text.BadLocationException e1) {
			e1.printStackTrace();
			return null;
		}

		// process file link
		if (tokenStr.equals(GNUElementScanner.FILE_NAME)) {

			Region pathRegion = null;

			// cut "* "
			line = line.substring(2);
			int trailingWhiteSpace;
			if (((trailingWhiteSpace = line.indexOf(":")) > 0)
					|| ((trailingWhiteSpace = line.indexOf(" ")) > 0)) {

				line = line.substring(0, trailingWhiteSpace);
				pathRegion = new Region(tokenRegion.getOffset() + 2,
						trailingWhiteSpace);
			} else {
				pathRegion = new Region(tokenRegion.getOffset() + 2, line
						.length());
			}
			
			
			if (documentLocation == null)
				return null;

			IPath filePath = documentLocation.append(line);

			return new IHyperlink[] { new FileHyperlink(pathRegion,
					ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(
							filePath)) };

		}

		return null;
	}

	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/**
	 * Get current directory that ChangeLog is in.
	 * 
	 * @param currentEditor
	 * @return path that this ChangeLog is in
	 */
	private IPath getDocumentLocation(IEditorPart currentEditor) {
		IWorkspaceRoot myWorkspaceRoot = getWorkspaceRoot();
		String WorkspaceRoot = myWorkspaceRoot.getLocation().toOSString();
		IEditorInput cc = currentEditor.getEditorInput();

		if (cc instanceof IFileEditorInput) {
			IFileEditorInput test = (IFileEditorInput) cc;
			IFile loc = test.getFile();

			IPath docLoc = new Path(WorkspaceRoot
					+ loc.getFullPath().toOSString());
			docLoc = docLoc.removeLastSegments(1);
			return docLoc;

		}

		if ((cc instanceof SyncInfoCompareInput)
				|| (cc instanceof CompareEditorInput)) {

			CompareEditorInput test = (CompareEditorInput) cc;
			if (test.getCompareResult() == null)
				return null;

			IPath docLoc = new Path(WorkspaceRoot
					+ test.getCompareResult().toString());
			docLoc = docLoc.removeLastSegments(1);
			return docLoc;

		}

		return null;
	}

}
