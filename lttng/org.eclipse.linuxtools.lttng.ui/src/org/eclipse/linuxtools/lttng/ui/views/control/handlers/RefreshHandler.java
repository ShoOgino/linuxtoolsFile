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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.lttng.ui.views.control.ControlView;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TargetNodeState;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TargetNodeComponent;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceControlComponent;
import org.eclipse.ui.IWorkbenchPage;

/**
 * <b><u>RefreshHandler</u></b>
 * <p>
 * Command handler implementation to refresh node configuration.
 * </p>
 */
public class RefreshHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The node component reference.
     */
    private TargetNodeComponent fNode;
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        fNode.refresh();
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        fNode = null;

        // Get workbench page for the Control View
        IWorkbenchPage page = getWorkbenchPage();
        if (page == null) {
            return false;
        }

        // Check if one or more session are selected
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            
            StructuredSelection structered = ((StructuredSelection) selection);
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = (Object) iterator.next();
                if (element instanceof TraceControlComponent) {
                    TraceControlComponent component = (TraceControlComponent) element;
                    boolean isConnected = component.getTargetNodeState() == TargetNodeState.CONNECTED;
                    if (isConnected) {
                        while ((component != null) && component.getClass() != TargetNodeComponent.class) {
                            component = (TraceControlComponent) component.getParent();
                        }
                        if (component != null) {
                            fNode = (TargetNodeComponent) component;
                        }
                    }
                }
            }
        }
        return fNode != null;
    }
}
