/*******************************************************************************
 * Copyright (c) 2011, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;

/**
 * Visitor keeping a list of spec files only.
 *
 */
public class SpecfileVisitor implements IResourceVisitor {

    private List<IResource> paths = new ArrayList<>();

    @Override
    public boolean visit(IResource resource) {
		if (resource.getType() == IResource.FILE && resource.getFileExtension() != null
				&& resource.getFileExtension().equals("spec")) { //$NON-NLS-1$
			paths.add(resource);
		}
        return true;
    }

    /**
     * Returns a list of all spec files found.
     *
     * @return The found spec files.
     */
    public List<IResource> getSpecFiles() {
        return paths;
    }
}
