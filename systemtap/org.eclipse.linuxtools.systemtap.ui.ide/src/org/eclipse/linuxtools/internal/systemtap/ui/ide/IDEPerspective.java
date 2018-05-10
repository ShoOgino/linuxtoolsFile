/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide;

import org.eclipse.linuxtools.internal.systemtap.ui.ide.views.FunctionBrowserView;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.views.KernelBrowserView;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.views.ProbeAliasBrowserView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

/**
 * The <code>IDEPerspective</code> class defines the layout of the IDE perspective
 * in the application.
 * @see org.eclipse.ui.IPerspectiveFactory
 * @author Ryan Morse
 */
public class IDEPerspective implements IPerspectiveFactory {
    public static final String ID = "org.eclipse.linuxtools.systemtap.ui.ide.IDEPerspective"; //$NON-NLS-1$

    @Override
    public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible(true);

        IFolderLayout browsers = layout.createFolder("browsers", IPageLayout.LEFT, 0.25f, editorArea); //$NON-NLS-1$
        browsers.addPlaceholder(ProbeAliasBrowserView.ID + ":*"); //$NON-NLS-1$

        browsers.addView(ProbeAliasBrowserView.ID);
        browsers.addView(FunctionBrowserView.ID);
        browsers.addView(KernelBrowserView.ID);
        browsers.addView(IPageLayout.ID_PROJECT_EXPLORER);


        layout.getViewLayout(ProbeAliasBrowserView.ID).setCloseable(false);
        layout.getViewLayout(FunctionBrowserView.ID).setCloseable(false);
        layout.getViewLayout(KernelBrowserView.ID).setCloseable(false);

        IFolderLayout output = layout.createFolder("output", IPageLayout.BOTTOM, 0.75f, editorArea); //$NON-NLS-1$
        output.addView(IConsoleConstants.ID_CONSOLE_VIEW);

        layout.getViewLayout(IConsoleConstants.ID_CONSOLE_VIEW).setCloseable(false);

        layout.addShowViewShortcut(ProbeAliasBrowserView.ID);
        layout.addShowViewShortcut(FunctionBrowserView.ID);
        layout.addShowViewShortcut(KernelBrowserView.ID);
        layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);

        layout.addPerspectiveShortcut(ID);
    }
}
