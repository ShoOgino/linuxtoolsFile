/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *    Alphonse Van Assche
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParseException;
import org.eclipse.ui.texteditor.MarkerUtilities;

public class SpecfileErrorHandler {
	
	public static final String SPECFILE_ERROR_MARKER_ID = Activator.PLUGIN_ID
	+ ".specfileerror";
	
	private IFile file;
	private IDocument document;
	
	public SpecfileErrorHandler(IFile file, IDocument document)
	{
		this.file = file;
		this.document = document;
	}
	
	public void handleError(SpecfileParseException e) {
		int lineNumber = e.getLineNumber();
		
		if (file == null) {	return;	}
		
		Map<String, Object> map = new HashMap<String, Object>();
		MarkerUtilities.setLineNumber(map, lineNumber);
		MarkerUtilities.setMessage(map, e.getMessage());
		map.put(IMarker.MESSAGE, e.getMessage());
		map.put(IMarker.LOCATION, file.getFullPath().toString());

		Integer charStart = getCharOffset(lineNumber, e.getStartColumn());
		if (charStart != null) {
			map.put(IMarker.CHAR_START, charStart);
		}
		Integer charEnd = getCharOffset(lineNumber, e.getEndColumn());
		if (charEnd != null) {
			map.put(IMarker.CHAR_END, charEnd);
		}
		
		// FIXME:  add severity level
		map.put(IMarker.SEVERITY, Integer.valueOf(e.getSeverity()));
		
		try {
			MarkerUtilities.createMarker(file, map, SPECFILE_ERROR_MARKER_ID);
		} catch (CoreException ee) {
			SpecfileLog.logError(ee);
		}
		return;
	}
	
	public void removeExistingMarkers()
	{
		if (file == null) {	return;	}
		
		try
		{
			file.deleteMarkers(SPECFILE_ERROR_MARKER_ID, true, IResource.DEPTH_ZERO);
		}
		catch (CoreException e1)
		{
			SpecfileLog.logError(e1);
		}
	}
	
	private Integer getCharOffset(int lineNumber, int columnNumber)
	{
		try
		{
			return Integer.valueOf(document.getLineOffset(lineNumber) + columnNumber);
		}
		catch (BadLocationException e)
		{
			SpecfileLog.logError(e);
			return null;
		}
	}
	
	public void setFile(IFile file) {
		this.file = file;
	}
	
	public void setDocument(IDocument document) {
		this.document = document;
	}

}
