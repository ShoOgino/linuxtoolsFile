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
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class LibHoverLoadJob extends Job {

    private static final String LOADING = "LibHover.Loading.msg"; //$NON-NLS-1$
    public LibHoverLoadJob(String title) {
        super(title);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        // Load all libhover docs now
        monitor.beginTask(LibHoverMessages.getString(LOADING),
                IProgressMonitor.UNKNOWN);
        monitor.worked(1);
        LibHover.getLibHoverDocs();
        Collection<LibHoverLibrary> c = LibHover.getLibraries();
        for (Iterator<LibHoverLibrary> i = c.iterator(); i.hasNext();) {
            LibHoverLibrary l = i.next();
            l.getHoverInfo();
        }
        monitor.done();
        return Status.OK_STATUS;
    }

}
