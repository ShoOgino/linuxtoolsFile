/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * Copyright (c) 2011 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *    Alexander Kurtakov - cleanups and simplification
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.rpmlint.builder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.linuxtools.rpm.rpmlint.Activator;
import org.eclipse.linuxtools.rpm.rpmlint.RpmlintLog;
import org.eclipse.linuxtools.rpm.rpmlint.parser.RpmlintItem;
import org.eclipse.linuxtools.rpm.rpmlint.parser.RpmlintParser;
import org.eclipse.linuxtools.rpm.ui.editor.markers.SpecfileErrorHandler;

/**
 * Visitor that generates markers for rpmlint found warnings and errors.
 *
 */
public class RpmlintMarkerVisitor implements IResourceVisitor {

	private List<RpmlintItem> rpmlintItems;
	
	private RpmlintBuilder builder;
	
	private boolean firstWarningInResource;
	
	/**
	 * Creates a visitor for handling .rpm and .spec files and adding markers for rpmlint warnings/errors. 
	 * @param builder The rpmlint project builder.
	 * @param rpmlintItems The rpmlint identified warnings and errors.
	 */
	public RpmlintMarkerVisitor(RpmlintBuilder builder, List<RpmlintItem> rpmlintItems) {
		this.rpmlintItems = rpmlintItems;
		this.builder = builder;
	}

	/**
	 * @see org.eclipse.core.resources.IResourceVisitor#visit(org.eclipse.core.resources.IResource)
	 */
	public boolean visit(IResource resource) throws CoreException {
		if (Activator.SPECFILE_EXTENSION.equals(resource.getFileExtension())) {
			firstWarningInResource = true;
			for (RpmlintItem item : rpmlintItems) {
				if (item.getFileName().equals(resource.getLocation().toOSString())) {
					IFile currentFile = ((IFile)resource);
					if (firstWarningInResource) {
						RpmlintParser.getInstance().deleteMarkers(resource);
						// remove internal marks on the current resource
						currentFile.deleteMarkers(SpecfileErrorHandler.SPECFILE_ERROR_MARKER_ID, false, IResource.DEPTH_ZERO);
						firstWarningInResource = false;
					}

					String specContent = fileToString(currentFile);
					int lineNumber;
					// FIXME: workaround the wrong line number with configure-without-libdir-spec
					if (item.getId().equals("configure-without-libdir-spec")) { //$NON-NLS-1$
						item.setLineNbr(-1);
						lineNumber = RpmlintParser.getInstance().getRealLineNbr(specContent, "./configure"); //$NON-NLS-1$
						if (lineNumber == -1)
							lineNumber = RpmlintParser.getInstance().getRealLineNbr(specContent, "%configure"); //$NON-NLS-1$
						item.setLineNbr(lineNumber);
					}
					
					lineNumber = item.getLineNbr();
					if (lineNumber == -1) {
						lineNumber = RpmlintParser.getInstance().getRealLineNbr(specContent, item.getRefferedContent());
						if (lineNumber == -1) {
							lineNumber = 1;
						}
					}
					lineNumber -= 1;
					// end workaround
					
					// BTW we mark specfile with the internal marker.
					builder.getSpecfileParser().setErrorHandler(builder.getSpecfileErrorHandler(currentFile, specContent));
					builder.getSpecfileParser().setTaskHandler(builder.getSpecfileTaskHandler(currentFile, specContent));
					builder.getSpecfileParser().parse(specContent);

					IDocument document = new Document(specContent);
					int charStart = getLineOffset(document, lineNumber);
					int charEnd = charStart + getLineLength(document, lineNumber);
					RpmlintParser.getInstance().addMarker(currentFile, item.getId() + ": " //$NON-NLS-1$
							+ item.getMessage(), lineNumber, charStart, charEnd,
							item.getSeverity(), item.getId(),
							item.getRefferedContent());
				}
			}
		} else if (Activator.RPMFILE_EXTENSION.equals(resource
				.getFileExtension())) {
			firstWarningInResource = true;
			for (RpmlintItem item : rpmlintItems) {
					IFile currentFile = ((IFile) resource);
					if (firstWarningInResource) {
						RpmlintParser.getInstance().deleteMarkers(resource);
						// remove internal marks on the current resource
						currentFile.deleteMarkers(
								SpecfileErrorHandler.SPECFILE_ERROR_MARKER_ID,
								false, IResource.DEPTH_ZERO);
						firstWarningInResource = false;
					}

					RpmlintParser.getInstance().addMarker(currentFile,
							item.getId() + ": " //$NON-NLS-1$
									+ item.getMessage(), item.getSeverity(),
							item.getId(), item.getRefferedContent());
			}
		}
		return true;
	}
	
	private static int getLineOffset(IDocument document, int lineNumber) {
		try {
			return document.getLineOffset(lineNumber);
		} catch (BadLocationException e) {
			RpmlintLog.logError(e);
			return 1;
		}
	}
	
	private static int getLineLength(IDocument document, int lineNumber) {
		try {
			return document.getLineLength(lineNumber);
		} catch (BadLocationException e) {
			RpmlintLog.logError(e);
			return 1;
		}
	}
	
	private String fileToString(IFile file) {
		String ret = new String(); 
		try {
			InputStream in = file.getContents();
			int nbrOfByte = in.available();
			byte[] bytes = new byte[nbrOfByte];
			in.read(bytes);
			ret = new String(bytes);
			in.close();
		} catch (CoreException e) {
			RpmlintLog.logError(e);
		} catch (IOException e) {
			RpmlintLog.logError(e);
		}
		return ret;
	}
	
}
