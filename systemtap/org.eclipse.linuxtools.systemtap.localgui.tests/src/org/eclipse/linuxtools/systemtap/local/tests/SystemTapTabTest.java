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
package org.eclipse.linuxtools.systemtap.local.tests;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.systemtap.local.launch.SystemTapLaunchShortcut;
import org.eclipse.linuxtools.systemtap.local.launch.SystemTapOptionsTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class SystemTapTabTest extends TestCase{

	
	public void testTabs(){
		Shell sh = new Shell();
		Composite cmp = new Composite(sh, SWT.NONE);
		
		SystemTapLaunchShortcut shortCut = new SystemTapLaunchShortcut();
		SystemTapOptionsTab stp = new SystemTapOptionsTab();
		stp.createControl(cmp);
		ILaunchConfiguration configuration;
		try {
			configuration = shortCut.outsideGetLaunchConfigType().
			newInstance(null, (DebugPlugin.getDefault().getLaunchManager()).
					generateUniqueLaunchConfigurationNameFrom("invalid"));
			ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
			stp.setDefaults(wc);
			stp.performApply(wc);
			wc.doSave();
			stp.initializeFrom(configuration);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		sh.open();
	}
}
