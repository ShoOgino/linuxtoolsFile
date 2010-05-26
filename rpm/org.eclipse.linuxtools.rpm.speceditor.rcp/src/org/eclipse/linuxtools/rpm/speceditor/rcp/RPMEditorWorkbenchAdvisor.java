/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.speceditor.rcp;

import org.eclipse.linuxtools.rpm.speceditor.rcp.actions.RPMEditorActionBarAdvisor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;


public class RPMEditorWorkbenchAdvisor extends WorkbenchAdvisor {
	private DelayedEventsProcessor processor;
	
	public RPMEditorWorkbenchAdvisor(DelayedEventsProcessor processor) {
		this.processor = processor;
	}

    public String getInitialWindowPerspectiveId() {
        return "org.eclipse.linuxtools.rpm.speceditor.rcp.perspective"; //$NON-NLS-1$
    }
    
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
    	return new WorkbenchWindowAdvisor(configurer) {
			public void preWindowOpen() {
				super.preWindowOpen();
		        getWindowConfigurer().setInitialSize(new Point(600, 450));
		        getWindowConfigurer().setShowCoolBar(true);
		        getWindowConfigurer().setShowStatusLine(true);
		        getWindowConfigurer().setTitle("RPM Specfile Editor"); //$NON-NLS-1$
			}
			
			public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer abConfigurer) {
				return new RPMEditorActionBarAdvisor(abConfigurer);
			}
		};
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor#eventLoopIdle(org.eclipse.swt.widgets.Display)
     */
    public void eventLoopIdle(Display display) {
    	if (processor != null)
    		processor.catchUp(display);
    	super.eventLoopIdle(display);
    }

}
