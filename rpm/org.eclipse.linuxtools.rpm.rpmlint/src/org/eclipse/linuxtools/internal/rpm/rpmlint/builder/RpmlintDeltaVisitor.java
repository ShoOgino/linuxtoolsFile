/*******************************************************************************
 * Copyright (c) 2007, 2018 Alphonse Van Assche and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *    Red Hat Inc. - ongoing maintenance
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.rpmlint.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.linuxtools.internal.rpm.rpmlint.Activator;
import org.eclipse.linuxtools.internal.rpm.rpmlint.parser.RpmlintParser;

/**
 * Visitor that checks whether the resource is a .spec or .rpm file and whether
 * it's ADDED or CHANGED. If both conditions are true it's stored for later
 * usage.
 *
 */
public class RpmlintDeltaVisitor implements IResourceDeltaVisitor {

	private List<String> paths = new ArrayList<>();

	@Override
	public boolean visit(IResourceDelta delta) {
		IResource resource = delta.getResource();
		if (Activator.SPECFILE_EXTENSION.equals(resource.getFileExtension())
				|| Activator.RPMFILE_EXTENSION.equals(resource.getFileExtension())) {
			switch (delta.getKind()) {
			// we first visiting resources to be able to run the rpmlint command
			// only once. That improve drastically the performance.
			case IResourceDelta.ADDED:
				paths.add(resource.getLocation().toOSString());
				break;
			case IResourceDelta.CHANGED:
				RpmlintParser.deleteMarkers(resource);
				paths.add(resource.getLocation().toOSString());
				break;
			}
		}
		return true;
	}

	/**
	 * Returns the visited and marked paths.
	 * 
	 * @return The marked paths.
	 */
	public List<String> getVisitedPaths() {
		return paths;
	}

}
