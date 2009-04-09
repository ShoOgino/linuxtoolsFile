/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtapgui.graphing.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.linuxtools.systemtapgui.graphing.internal.GraphingPlugin;
import org.eclipse.linuxtools.systemtapgui.graphing.internal.Localization;
import org.eclipse.linuxtools.systemtapgui.logging.LogManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;



public class GraphingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public GraphingPreferencePage() {
		super(GRID);
		LogManager.logDebug("Start GraphingPreferencePage:", this);
		setPreferenceStore(GraphingPlugin.getDefault().getPreferenceStore());
		setDescription(Localization.getString("GraphingPreferencePage.GraphDisplayPreferences"));
		LogManager.logDebug("End GraphingPreferencePage:", this);
	}
	
	public void createFieldEditors() {
		LogManager.logDebug("Start createFieldEditors:", this);

		addField(
				new IntegerFieldEditor(
				GraphingPreferenceConstants.P_GRAPH_UPDATE_DELAY,
				Localization.getString("GraphingPreferencePage.RefreshDelay"),
				getFieldEditorParent()));
		
		LogManager.logDebug("End createFieldEditors:", this);
	}

	public void init(IWorkbench workbench) {
		LogManager.logDebug("Start init:", this);
		LogManager.logInfo("Initializing", this);
		LogManager.logDebug("End init:", this);
	}
	
	public void dispose() {
		LogManager.logDebug("Start dispose:", this);
		LogManager.logInfo("Disposing", this);
		super.dispose();
		LogManager.logDebug("End dispose:", this);
	}
}
