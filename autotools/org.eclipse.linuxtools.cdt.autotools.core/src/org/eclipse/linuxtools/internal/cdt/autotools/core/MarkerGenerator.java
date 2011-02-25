/*******************************************************************************
 * Copyright (c) 2000, 2006, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.autotools.core;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;

public abstract class MarkerGenerator {

	static final int SEVERITY_INFO = IMarkerGenerator.SEVERITY_INFO;
	static final int SEVERITY_WARNING = IMarkerGenerator.SEVERITY_WARNING;
	static final int SEVERITY_ERROR_RESOURCE = IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
	static final int SEVERITY_ERROR_BUILD = IMarkerGenerator.SEVERITY_ERROR_BUILD;

	/**
	 * Constructor for MarkerGenerator
	 */
	public MarkerGenerator() {
		super();
	}

	/*
	 * callback from Output Parser
	 */
	public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {

		try {
			IMarker[] cur = file.findMarkers(IAutotoolsMarker.AUTOTOOLS_PROBLEM_MARKER, false, IResource.DEPTH_ONE);
			/*
			 * Try to find matching markers and don't put in duplicates
			 */
			if ((cur != null) && (cur.length > 0)) {
				for (int i = 0; i < cur.length; i++) {
					int line = ((Integer) cur[i].getAttribute(IMarker.LOCATION)).intValue();
					int sev = ((Integer) cur[i].getAttribute(IMarker.SEVERITY)).intValue();
					String mesg = (String) cur[i].getAttribute(IMarker.MESSAGE);
					if (line == lineNumber && sev == mapMarkerSeverity(severity) && mesg.equals(errorDesc)) {
						return;
					}
				}
			}

			IMarker marker = file.createMarker(IAutotoolsMarker.AUTOTOOLS_PROBLEM_MARKER);
			marker.setAttribute(IMarker.LOCATION, lineNumber);
			marker.setAttribute(IMarker.MESSAGE, errorDesc);
			marker.setAttribute(IMarker.SEVERITY, mapMarkerSeverity(severity));
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			marker.setAttribute(IMarker.CHAR_START, -1);
			marker.setAttribute(IMarker.CHAR_END, -1);
			if (errorVar != null) {
				marker.setAttribute(IAutotoolsMarker.MARKER_VARIABLE, errorVar);
			}
		}
		catch (CoreException e) {
			CCorePlugin.log(e.getStatus());
		}

	}
	
	public abstract IProject getProject();
	
	public boolean hasMarkers(IResource file) {
		IMarker[] markers;
		try {
			markers = file.findMarkers(IAutotoolsMarker.AUTOTOOLS_PROBLEM_MARKER, false, IResource.DEPTH_ONE);
		} catch (CoreException e) {
			return false;
		}
		return markers.length > 0;
	}

	/*
	 * callback from Output Parser
	 */
	public void addMarker(AutotoolsProblemMarkerInfo info) {
		ProblemMarkerInfo problemMarkerInfo = info.getProblemMarkerInfo();
//		ProblemMarkerInfo problemMarkerInfo = info;
		try {
			IResource markerResource = problemMarkerInfo.file ;
			if (markerResource==null)  {
				markerResource = getProject();
			}
			IMarker[] cur = markerResource.findMarkers(IAutotoolsMarker.AUTOTOOLS_PROBLEM_MARKER, true, IResource.DEPTH_ONE);
//			IMarker[] cur = markerResource.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_ONE);
			/*
			 * Try to find matching markers and don't put in duplicates
			 */
			if ((cur != null) && (cur.length > 0)) {
				for (int i = 0; i < cur.length; i++) {
					int line = ((Integer) cur[i].getAttribute(IMarker.LOCATION)).intValue();
					int sev = ((Integer) cur[i].getAttribute(IMarker.SEVERITY)).intValue();
					String mesg = (String) cur[i].getAttribute(IMarker.MESSAGE);
					if (line == problemMarkerInfo.lineNumber && sev == mapMarkerSeverity(problemMarkerInfo.severity) && mesg.equals(problemMarkerInfo.description)) {
						return;
					}
				}
			}

			IMarker marker = markerResource.createMarker(IAutotoolsMarker.AUTOTOOLS_PROBLEM_MARKER);
//			IMarker marker = markerResource.createMarker(ICModelMarker.C_MODEL_PROBLEM_MARKER);
			marker.setAttribute(IMarker.LOCATION, problemMarkerInfo.lineNumber);
			marker.setAttribute(IMarker.MESSAGE, problemMarkerInfo.description);
			marker.setAttribute(IMarker.SEVERITY, mapMarkerSeverity(problemMarkerInfo.severity));
			marker.setAttribute(IMarker.LINE_NUMBER, problemMarkerInfo.lineNumber);
			marker.setAttribute(IMarker.CHAR_START, -1);
			marker.setAttribute(IMarker.CHAR_END, -1);
			if (problemMarkerInfo.variableName != null) {
				marker.setAttribute(IAutotoolsMarker.MARKER_VARIABLE, problemMarkerInfo.variableName);
			}
			if (problemMarkerInfo.externalPath != null) {
				marker.setAttribute(IAutotoolsMarker.MARKER_EXTERNAL_LOCATION, problemMarkerInfo.externalPath.toOSString());
			}
			if (info.libraryInfo != null) {
				marker.setAttribute(IAutotoolsMarker.MARKER_LIBRARY_INFO, info.libraryInfo);
			}
		}
		catch (CoreException e) {
			CCorePlugin.log(e.getStatus());
		}
	}

	private int mapMarkerSeverity(int severity) {
		switch (severity) {
			case SEVERITY_ERROR_BUILD :
			case SEVERITY_ERROR_RESOURCE :
				return IMarker.SEVERITY_ERROR;
			case SEVERITY_INFO :
				return IMarker.SEVERITY_INFO;
			case SEVERITY_WARNING :
				return IMarker.SEVERITY_WARNING;
		}
		return IMarker.SEVERITY_ERROR;
	}
	
	/* (non-Javadoc)
	 * Removes the IMarkers for the project specified in the argument if the
	 * project exists, and is open. 
	 * 
	 * @param project
	 */
	public void removeAllMarkers(IProject project) {
		if (project == null || !project.isAccessible()) return;

		// Clear out the problem markers
		IWorkspace workspace = project.getWorkspace();
		IMarker[] markers;
		try {
			markers = project.findMarkers(IAutotoolsMarker.AUTOTOOLS_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
//			markers = project.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			// Handled just about every case in the sanity check
			return;
		}
		if (markers != null) {
			try {
				workspace.deleteMarkers(markers);
			} catch (CoreException e) {
				// The only situation that might cause this is some sort of resource change event
				return;
			}
		}
	}
}
