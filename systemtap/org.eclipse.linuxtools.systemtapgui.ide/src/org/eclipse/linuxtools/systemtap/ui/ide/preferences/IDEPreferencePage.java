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

package org.eclipse.linuxtools.systemtap.ui.ide.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.linuxtools.systemtap.ui.ide.internal.IDEPlugin;
import org.eclipse.linuxtools.systemtap.ui.ide.internal.Localization;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;


public class IDEPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public IDEPreferencePage() {
		super(GRID);
		LogManager.logDebug("Start IDEPreferencePage:", this);
		setPreferenceStore(IDEPlugin.getDefault().getPreferenceStore());
		setDescription("Preferences when loading the IDE perspective");
		LogManager.logDebug("End IDEPreferencePage:", this);
	}
	
	public void createFieldEditors() {
		LogManager.logDebug("Start createFieldEditors:", this);
		addField(new BooleanFieldEditor(
				IDEPreferenceConstants.P_STORED_TREE, 
				Localization.getString("IDEPreferencePage.UseStoredTapsetTree"),
				getFieldEditorParent()));
				LogManager.logDebug("End createFieldEditors:", this);
	}

	public void init(IWorkbench workbench) {
		LogManager.logInfo("Initializing", this);
	}

	public void dispose() {
		LogManager.logInfo("Disposing", this);
		super.dispose();
	}
}