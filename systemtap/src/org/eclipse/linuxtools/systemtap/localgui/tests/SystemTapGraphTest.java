/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.systemtap.localgui.tests;

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.linuxtools.systemtap.localgui.core.SystemTapUIErrorMessages;
import org.eclipse.linuxtools.systemtap.localgui.graphing.StapGraphParser;
import org.eclipse.linuxtools.systemtap.localgui.graphing.SystemTapView;
import org.eclipse.linuxtools.systemtap.localgui.launch.SystemTapLaunchConfigurationDelegate;
import org.eclipse.linuxtools.systemtap.localgui.launch.SystemTapLaunchShortcut;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;

public class SystemTapGraphTest extends TestCase {	
	
	
/*	public void testLaunch() {
		System.out.println("\n\nLaunching SystemTapGraphTest");

		LaunchStapGraph launch = new LaunchStapGraph();
		launch.launch(bin, mode);
		checkScript(launch);
	}*/
	
	public void testGraphLoading() {
		System.out.println("Testing graph loading");

		StapGraphParser parse = new StapGraphParser("Test StapParser", Activator.PLUGIN_LOCATION+"graph_data_output.graph");
		parse.testRun(new NullProgressMonitor());
		
		SystemTapView.forceDisplay();
		 
		
		//TODO: Figure out how to make the graph display at the same time as the dialog
		SystemTapUIErrorMessages testRadial = new SystemTapUIErrorMessages("Test graph", "Opening graph", 
				"Now checking graph for correctness. Please press OK, then maximize the SystemTap view.");
		testRadial.schedule();

		
		
		
		
		ArrayList<String> tasks = new ArrayList<String>();
		
		tasks.add("Maximize the window and click refresh icon.");
		tasks.add("Select Tree View icon");
		tasks.add("Select Aggregate View icon");
		tasks.add("Select Box View from icon.");
		tasks.add("Select Collapse icon.");
		tasks.add("Select Animation->Fast from the drop-down menu");
		tasks.add("Go to Radial View");
		tasks.add("Select Collapse icon again.");
		tasks.add("Double-click a node with no children in the TreeViewer");
		tasks.add("Double-click a node with children in the TreeViewer");
		tasks.add("Expand an arrow in the TreeViewer");
		tasks.add("Collapse an arrow in the TreeViewer");
		
		
		Shell sh = new Shell(SWT.ON_TOP | SWT.BORDER );
		sh.setSize(500,800);
		sh.setLayout(new FillLayout());
		
		ScrolledComposite testComp = new ScrolledComposite(sh, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		
		Composite buttons = new Composite(testComp, SWT.NONE);
		testComp.setContent(buttons);
		buttons.setLayout(new GridLayout(2, false));
	    testComp.setExpandHorizontal(true);
	    testComp.setExpandVertical(true);
	    


		for (String task : tasks) {
			Button checkBox = new Button(buttons, SWT.CHECK);
			checkBox.setText(task);
		}
		sh.open();
		
		assertEquals(true, MessageDialog.openConfirm(new Shell(), "Check Graph", 
				"Press OK when tasks are completed."));

		
		
		
		
		 //* To test:
		 //*
		 //* All transitions from (drawMode A, animMode A, collapseMode A, zoom A) to (drawMode B, animMode B, collapseMode B, zoom B) 
		 
		//Transition: (dRadial, aSlow, cTrue, zDefault)-->(dTree, same) 
//		SystemTapUIErrorMessages testTree = new SystemTapUIErrorMessages("Test graph", "Opening graph", 
//			"Now checking Tree View. Please press OK, then , press OK if correct.");
//		testTree.schedule();
//		
//		assertEquals(true, MessageDialog.openConfirm(new Shell(), "Check Graph", 
//		"Select Tree View from the drop-down menu. Does the Tree View look correct? Press OK to continue."));
//		
//		//Transition: (dTree, aSlow, cTrue, zDefault)-->(dAgg, same)
//		SystemTapUIErrorMessages testAggregate = new SystemTapUIErrorMessages("Test graph", "Opening graph", 
//		"Now checking Aggregate View. Please press OK, then select Aggregate View from the drop-down menu, press OK to continue.");
//		testAggregate.schedule();
//	
//		assertEquals(true, MessageDialog.openConfirm(new Shell(), "Check Graph", 
//		"Press OK, then Select Aggregate View from the drop-down menu. Does the Aggregate View look correct? Press OK to continue."));
//		
//		//Transition: (dAgg, aSlow, cTrue, zDefault)-->(dBox, same)
//		SystemTapUIErrorMessages testBox = new SystemTapUIErrorMessages("Test graph", "Opening graph", 
//		"Now checking Box View. Please press OK, then select Box View from the drop-down menu, press OK to continue.");
//		testBox.schedule();
//	
//		assertEquals(true, MessageDialog.openConfirm(new Shell(), "Check Graph", 
//		"Press OK, then Select Box View from the drop-down menu. Does the Box View look correct? Press OK to continue."));
	}
	
	
	public void checkScript(SystemTapLaunchShortcut launch) {
		//Check that script is set properly
		File f = new File (launch.getScriptPath());
		if (!f.exists())
			fail();
	}
	
	public void checkLaunchConfiguration(String checkString, SystemTapLaunchShortcut launch) {
		//Check that the configuration was properly set
		ILaunchConfiguration config = launch.getConfig();
		SystemTapLaunchConfigurationDelegate del = new SystemTapLaunchConfigurationDelegate();
		try {
			del.launch(config, "profile", null, null);
			assertEquals(del.getCommand(), checkString);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	
}
