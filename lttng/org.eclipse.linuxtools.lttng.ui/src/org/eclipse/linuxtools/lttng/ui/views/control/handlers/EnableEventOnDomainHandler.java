/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.control.handlers;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.lttng.ui.views.control.ControlView;
import org.eclipse.linuxtools.lttng.ui.views.control.model.LogLevelType;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TraceLogLevel;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TraceSessionState;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceDomainComponent;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceSessionComponent;
import org.eclipse.ui.IWorkbenchPage;

/**
 * <b><u>EnableEventOnDomainHandler</u></b>
 * <p>
 * Command handler implementation to enable events for a known domain and default channel 'channel0'
 * (which will be created if doesn't exist).
 * </p>
 */
public class EnableEventOnDomainHandler extends BaseEnableEventHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The domain component the command is to be executed on. 
     */
    private TraceDomainComponent fDomain = null;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.handlers.BaseEnableEventHandler#enableEvents(java.util.List, boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void enableEvents(List<String> eventNames, boolean isKernel, IProgressMonitor monitor) throws ExecutionException {
        fDomain.enableEvents(eventNames, monitor);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.handlers.BaseEnableEventHandler#enableSyscalls(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void enableSyscalls(IProgressMonitor monitor) throws ExecutionException {
        fDomain.enableSyscalls(monitor);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.handlers.BaseEnableEventHandler#enableProbe(java.lang.String, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void enableProbe(String eventName, String probe, IProgressMonitor monitor) throws ExecutionException {
        fDomain.enableProbe(eventName, probe, monitor);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.handlers.BaseEnableEventHandler#enableFunctionProbe(java.lang.String, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void enableFunctionProbe(String eventName, String probe, IProgressMonitor monitor) throws ExecutionException {
        fDomain.enableFunctionProbe(eventName, probe, monitor);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.handlers.BaseEnableEventHandler#enableLogLevel(java.lang.String, org.eclipse.linuxtools.lttng.ui.views.control.model.LogLevelType, org.eclipse.linuxtools.lttng.ui.views.control.model.TraceLogLevel, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void enableLogLevel(String eventName, LogLevelType logLevelType, TraceLogLevel level, IProgressMonitor monitor) throws ExecutionException {
        fDomain.enableLogLevel(eventName, logLevelType, level, monitor);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.handlers.BaseEnableEventHandler#getDomain()
     */
    @Override
    public TraceDomainComponent getDomain() {
        return fDomain;
    }
    /*
     * (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        // Get workbench page for the Control View
        IWorkbenchPage page = getWorkbenchPage();
        if (page == null) {
            return false;
        }

        fDomain = null;
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            StructuredSelection structered = ((StructuredSelection) selection);
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = (Object) iterator.next();
                if (element instanceof TraceDomainComponent) {
                    // Add only if corresponding TraceSessionComponents is inactive and not destroyed
                    TraceDomainComponent domain = (TraceDomainComponent) element; 
                    TraceSessionComponent session = domain.getSession();
                    if(session.getSessionState() == TraceSessionState.INACTIVE && !session.isDestroyed()) {
                        fDomain = domain;
                        fSession = session;
                    }
                }
            }
        }
        return fDomain != null;
    }
}
