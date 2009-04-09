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

package org.eclipse.linuxtools.systemtapgui.ide.views;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.linuxtools.systemtapgui.ide.actions.hidden.ProbeAliasAction;
import org.eclipse.linuxtools.systemtapgui.ide.structures.TapsetLibrary;
import org.eclipse.linuxtools.systemtapgui.logging.LogManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;


/**
 * The Probe Alias Browser module of the SystemTap GUI. This class provides a list of all probe aliases
 * defined in the tapset (both the standard, and user-specified tapsets), and allows the user to insert
 * template probes into an editor.
 * @author Henry Hughes
 * @author Ryan Morse
 */
public class ProbeAliasBrowserView extends BrowserView {
	public ProbeAliasBrowserView() {
		super();
		LogManager.logInfo("Initializing", this);
	}
	
	/**
	 * Creates the UI on the given <code>Composite</code>
	 */
	public void createPartControl(Composite parent) {
		LogManager.logDebug("Start createPartControl: parent-" + parent, this);
		super.createPartControl(parent);
		TapsetLibrary.init();
		TapsetLibrary.addListener(new ViewUpdater());
		refresh();
		makeActions();
		LogManager.logDebug("End createPartControl:", this);
	}
	
	/**
	 * Refreshes the list of probe aliases in the viewer.
	 */
	public void refresh() {
		LogManager.logDebug("Start refresh:", this);
		super.viewer.setInput(TapsetLibrary.getProbes());
		LogManager.logDebug("End refresh:", this);
	}
	
	/**
	 * Wires up all of the actions for this browser, such as double and right click handlers.
	 */
	private void makeActions() {
		LogManager.logDebug("Start makeActions:", this);
		doubleClickAction = new ProbeAliasAction(getSite().getWorkbenchWindow(), this);
		dblClickListener = new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				LogManager.logDebug("doubleClick fired", this);
				doubleClickAction.run();
			}
		};
		viewer.addDoubleClickListener(dblClickListener);
		Control control = this.viewer.getControl();
		MenuManager manager = new MenuManager("probePopup");

		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		Menu menu = manager.createContextMenu(control);
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(manager, viewer);
		LogManager.logDebug("End makeActions:", this);
	}
	
	public void dispose() {
		LogManager.logInfo("Disposing", this);
		super.dispose();
		if(null != doubleClickAction)
			doubleClickAction.dispose();
		doubleClickAction = null;
		if(null != viewer)
			viewer.removeDoubleClickListener(dblClickListener);
		dblClickListener = null;
		if(null != menu)
			menu.dispose();
		menu = null;
	}

	public static final String ID = "org.eclipse.linuxtools.systemtapgui.ide.views.ProbeAliasBrowserView";
	private ProbeAliasAction doubleClickAction;
	private IDoubleClickListener dblClickListener;
	private Menu menu;
}
