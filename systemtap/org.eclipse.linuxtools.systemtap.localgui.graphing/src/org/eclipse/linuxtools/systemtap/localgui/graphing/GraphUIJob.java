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

package org.eclipse.linuxtools.systemtap.localgui.graphing;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.progress.UIJob;

/**
 * Initializes and runs a StapGraph and TreeViewer within the SystemTap View
 * 
 * @author chwang
 *
 */
public class GraphUIJob extends UIJob{
	private StapGraph g;
	private StapGraphParser parser;
	private static int treeSize = 200;


	public StapGraph getGraph() {
		return g;
	}
	
	public GraphUIJob(String name, StapGraphParser parser) {
		super(name);
		//CREATE THE SHELL
		this.parser = parser;
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		
		//-------------Initialize shell, menu
		treeSize = 200;

		
		//OPEN UP THE SYSTEMTAPVIEW IF IT IS NOT ALREADY OPEN
		SystemTapView.forceDisplay();
		
		Composite treeComp = SystemTapView.makeTreeComp(treeSize);
		Composite graphComp = SystemTapView.makeGraphComp();
		
		g = new StapGraph(graphComp, SWT.NONE, treeComp);

		
		//-------------Load graph data
		g.loadData(SWT.NONE, 0, StapGraph.CONSTANT_TOP_NODE_NAME, parser.timeMap.get(0), 1, -1, false, ""); //$NON-NLS-1$
		boolean marked = false;
		String msg;
		
		
	    for (int id_parent : parser.serialMap.keySet()) {
	    	
			for (int id_child : parser.outNeighbours.get(id_parent)) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				marked = false;
				msg = ""; //$NON-NLS-1$
				if (id_child != -1) {
					
//					if (parser.markedMap.keySet() != null) {
//						if (parser.markedMap.keySet().contains(id_child)) {
//							msg = parser.markedMap.get(id_child);							
//							parser.markedMap.keySet().remove((Integer)id_child);
//							marked = true;
//						}
//					}
					
					if (parser.timeMap.get(id_child) == null){						
						g.loadData(SWT.NONE, id_child, parser.serialMap
								.get(id_child), parser.timeMap.get(0),
								1, id_parent, marked,msg);
					}else{
						g.loadData(SWT.NONE, id_child, parser.serialMap
								.get(id_child), parser.timeMap.get(id_child),
								1, id_parent, marked,msg);
					}
				}
			}
		}

	    
	    g.aggregateCount = parser.countMap;
	    g.aggregateTime = parser.cumulativeTimeMap;

	    //Set total time (automatically seeks highest useful node)
	    g.setTotalTime();
	    
	    //-------------Finish initializations
	    //Generate data for collapsed nodes
	    g.recursivelyCollapseAllChildrenOfNode(g.getTopNode());
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
	    g.initializeTree();
	    

	    g.draw(StapGraph.CONSTANT_DRAWMODE_RADIAL, StapGraph.CONSTANT_ANIMATION_SLOW,
	    		g.getFirstUsefulNode());
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
	    g.setFocus();
	    
	    SystemTapView.setValues(graphComp, treeComp, g, parser);
	    SystemTapView.createPartControl();
	    
		return Status.OK_STATUS;
	}
	

	/**
	 * Returns number of StapData objects created 
	 * @return
	 */
	public int getNumberOfDataNodes() {
		return g.getDataMapSize();
	}
	
	
	/**
	 * For easier JUnit testing only. Allows public access to run method without scheduling an extra job.
	 *  
	 * @param m
	 * @return
	 */
	public IStatus testRun(IProgressMonitor m) {
		return runInUIThread(m);
	}
	
	
}
