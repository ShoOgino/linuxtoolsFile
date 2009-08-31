/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * <b><u>PerspectiveFactory</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class PerspectiveFactory implements IPerspectiveFactory {

    // LTTng views
    private static final String PROJECT_VIEW_ID      = Labels.ProjectView_ID;
    private static final String CONTROL_VIEW_ID      = Labels.ControlView_ID;
    private static final String EVENTS_VIEW_ID       = Labels.EventsView_ID;
    private static final String TIME_FRAME_VIEW_ID   = Labels.TimeFrameView_ID;
    private static final String CONTROL_FLOW_VIEW_ID = Labels.ControlFlowView_ID;
    private static final String RESOURCES_VIEW_ID    = Labels.ResourcesView_ID;
    private static final String STATISTICS_VIEW_ID   = Labels.StatisticsView_ID;
    private static final String HISTOGRAM_VIEW_ID    = Labels.HistogramView_ID;

    // Standard Eclipse views
    private static final String PROPERTIES_VIEW_ID   = IPageLayout.ID_PROP_SHEET;
    private static final String PROBLEM_VIEW_ID      = IPageLayout.ID_PROBLEM_VIEW;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	public void createInitialLayout(IPageLayout layout) {

        layout.setEditorAreaVisible(false);

        addFastViews(layout);
        addViewShortcuts(layout);
        addPerspectiveShortcuts(layout);

        // Create the top left folder
        IFolderLayout topLeftFolder = layout.createFolder("topLeftFolder", IPageLayout.LEFT, 0.15f, IPageLayout.ID_EDITOR_AREA);
        topLeftFolder.addView(PROJECT_VIEW_ID);
        topLeftFolder.addView(CONTROL_VIEW_ID);

        // Create the bottom left folder
        IFolderLayout bottomLeftFolder = layout.createFolder("bottomLeftFolder", IPageLayout.BOTTOM, 0.50f, "topLeftFolder");
        bottomLeftFolder.addView(PROPERTIES_VIEW_ID);
   
        // Create the middle right folder
        IFolderLayout topRightFolder = layout.createFolder("topRightFolder", IPageLayout.TOP, 0.50f, IPageLayout.ID_EDITOR_AREA);
        topRightFolder.addView(CONTROL_FLOW_VIEW_ID);
        topRightFolder.addView(RESOURCES_VIEW_ID);
        topRightFolder.addView(STATISTICS_VIEW_ID);

        // Create the middle right folder
        IFolderLayout middleRightFolder = layout.createFolder("middleRightFolder", IPageLayout.BOTTOM, 0.50f, "topRightFolder");
        middleRightFolder.addView(EVENTS_VIEW_ID);

        // Create the bottom right folder
        IFolderLayout bottomRightFolder = layout.createFolder("bottomRightFolder", IPageLayout.BOTTOM, 0.65f, "middleRightFolder");
        bottomRightFolder.addView(TIME_FRAME_VIEW_ID);
        bottomRightFolder.addView(HISTOGRAM_VIEW_ID);
        bottomRightFolder.addView(PROBLEM_VIEW_ID);

	}

    /**
     * Add fast views to the perspective
     * 
     * @param layout
     */
    private void addFastViews(IPageLayout layout) {
        // TODO Auto-generated method stub
    }

    /**
     * Add view shortcuts to the perspective
     * 
     * @param layout
     */
    private void addViewShortcuts(IPageLayout layout) {
        // TODO Auto-generated method stub
    }

    /**
     * Add perspective shortcuts to the perspective
     * 
     * @param layout
     */
    private void addPerspectiveShortcuts(IPageLayout layout) {
        // TODO Auto-generated method stub
    }

}
