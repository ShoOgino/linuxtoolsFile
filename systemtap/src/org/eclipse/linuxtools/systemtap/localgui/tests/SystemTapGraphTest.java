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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.systemtap.localgui.core.SystemTapUIErrorMessages;
import org.eclipse.linuxtools.systemtap.localgui.graphing.StapGraphParser;
import org.eclipse.linuxtools.systemtap.localgui.graphing.SystemTapView;
import org.eclipse.linuxtools.systemtap.localgui.launch.SystemTapLaunchConfigurationDelegate;
import org.eclipse.linuxtools.systemtap.localgui.launch.SystemTapLaunchShortcut;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class SystemTapGraphTest extends TestCase {	
	
	
/*	public void testLaunch() {
		System.out.println("\n\nLaunching SystemTapGraphTest");

		LaunchStapGraph launch = new LaunchStapGraph();
		launch.launch(bin, mode);
		checkScript(launch);
	}*/
	private  ArrayList<Button> list = new ArrayList<Button>();
	
	private class ButtonSelectionListener implements SelectionListener {
		private Action action;

		public ButtonSelectionListener(Action action) {
			this.action = action;
		}
		
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			action.run();
		}
		
	}
	
	public void testGraphLoading() throws InterruptedException {
		System.out.println("Testing graph loading");

		StapGraphParser parse = new StapGraphParser("Test StapParser", Activator.PLUGIN_LOCATION+"graph_data_output.graph");
		parse.testRun(new NullProgressMonitor());
		
		SystemTapView.forceDisplay();
		 
		
		//TODO: Figure out how to make the graph display at the same time as the dialog
		SystemTapUIErrorMessages testRadial = new SystemTapUIErrorMessages("Test graph", "Opening graph", 
				"Testing Graph. Press OK, then go through the list of tasks.");
		testRadial.schedule();

		
		
		
		

		ArrayList<String> tasks = new ArrayList<String>();
		

		tasks.add("(Manually) Maximize SystemTapView");
		tasks.add("Refresh");
		tasks.add("Tree View");
		tasks.add("Aggregate View");
		tasks.add("Box View");
		tasks.add("Collapse");
		tasks.add("Uncollapse.");
		tasks.add("Animation->Fast");
		tasks.add("Radial View");
		tasks.add("Collapse.");
		tasks.add("(Manually) Double-click node with no children in TreeViewer");
		tasks.add("(Manually) Expand an arrow in the TreeViewer");
		tasks.add("(Manually) Collapse an arrow in the TreeViewer");
		tasks.add("Save file");
		tasks.add("Reload file");
		
		
		final Shell sh = new Shell(SWT.SHELL_TRIM);
		sh.setSize(450,tasks.size()*38);
		sh.setText("Tasklist - press Finished when finished.");
		sh.setLayout(new GridLayout(1, false));
		sh.setAlpha(150);
		
		ScrolledComposite testComp = new ScrolledComposite(sh, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
	
		
		Composite buttons = new Composite(testComp, SWT.NONE);
		testComp.setContent(buttons);
		buttons.setLayout(new GridLayout(1, false));
	    testComp.setExpandHorizontal(true);
	    testComp.setExpandVertical(true);

	    int taskNumber = 0;
		for (String task : tasks) {
			taskNumber++;

			
			Button checkBox = new Button(buttons, SWT.CHECK);
			list.add(checkBox);
			checkBox.setText(task);
			Action act = null;
			switch (taskNumber) {
			case 1:
				break;
			case 2:
				act = SystemTapView.getView_refresh();
				break;
			case 3:
				act = SystemTapView.getView_treeview();
				break;
				
//				tasks.add("(Manually) Maximize SystemTapView");
//				tasks.add("Refresh");
//				tasks.add("Tree View");
//				tasks.add("Aggregate View");
//				tasks.add("Box View");
//				tasks.add("Collapse");
//				tasks.add("Uncollapse.");
//				tasks.add("Animation->Fast");
//				tasks.add("Radial View");
//				tasks.add("Collapse.");
//				tasks.add("(Manually) Double-click node with no children in TreeViewer");
//				tasks.add("(Manually) Expand an arrow in the TreeViewer");
//				tasks.add("(Manually) Collapse an arrow in the TreeViewer");
//				tasks.add("Save file");
//				tasks.add("Reload file");
			case 4:
				act = SystemTapView.getView_aggregateview();
				break;
			case 5:
				act = SystemTapView.getView_boxview();
				break;
			case 6:
			case 7:
				act = SystemTapView.getMode_collapsednodes();
				break;
			case 8:
				act = SystemTapView.getAnimation_fast();
				break;
			case 9:
				act = SystemTapView.getView_radialview();
				break;
			case 10:
				act = SystemTapView.getMode_collapsednodes();
				break;
			case 14:
				act = SystemTapView.getSave_callgraph();
				break;
			case 15:
				act = SystemTapView.getOpen_callgraph();
				break;
			default:
				break;
			}
			if (act != null) {
				ButtonSelectionListener bl = new ButtonSelectionListener(act);
				checkBox.addSelectionListener(bl);
			}
			
			
		}
		
//		Button finish = new Button(buttons, SWT.PUSH);
//		finish.setText("Finish");
//		finish.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				sh.dispose();
//			}
//		});
//		
		sh.open();
		
		
		boolean doneTasks =MessageDialog.openConfirm(new Shell(SWT.ON_TOP), "Check Graph", 
							"Press OK if all "+ tasks.size() + " boxes in the checklist have been checked.\n Hit Cancel if any test fails."); 
		assertEquals(true, doneTasks);

		for (Button b : list) {
			System.out.println("Testing button " + b.getText());
			assertEquals(true,b.getSelection());
		}
		
		
		
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
