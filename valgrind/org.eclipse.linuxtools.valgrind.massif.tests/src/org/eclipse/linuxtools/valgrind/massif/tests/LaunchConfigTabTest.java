/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.massif.tests;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.linuxtools.valgrind.launch.ValgrindOptionsTab;
import org.eclipse.linuxtools.valgrind.massif.MassifPlugin;
import org.eclipse.linuxtools.valgrind.massif.MassifToolPage;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class LaunchConfigTabTest extends AbstractMassifTest {
	
	protected ValgrindOptionsTab tab;
	protected MassifToolPage dynamicTab;
	protected ILaunchConfiguration config;
	protected Shell testShell;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("alloctest"); //$NON-NLS-1$

		config = createConfiguration(proj.getProject());

		testShell = new Shell(Display.getDefault());
		testShell.setLayout(new GridLayout());
		tab = new ValgrindOptionsTab();
	}

	@Override
	protected void tearDown() throws Exception {
		tab.dispose();
		testShell.dispose();
		deleteProject(proj);
		super.tearDown();
	}

	private ILaunchConfigurationWorkingCopy initConfig() throws CoreException {
		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		tab.setDefaults(wc);
		tab.createControl(testShell);
		tab.initializeFrom(config);
		int ix = Arrays.asList(tab.getTools()).indexOf(MassifPlugin.TOOL_ID);
		tab.getToolsCombo().select(ix);
		ILaunchConfigurationTab dynamicTab = tab.getDynamicTab();
		this.dynamicTab = (MassifToolPage) dynamicTab;
		return wc;
	}
	
	private ILaunch saveAndLaunch(ILaunchConfigurationWorkingCopy wc, String testName)
	throws Exception {
		tab.performApply(wc);
		config = wc.doSave();

		ILaunch launch = doLaunch(config, testName);
		return launch;
	}
	
	public void testDefaults() throws Exception {		
		ILaunchConfigurationWorkingCopy wc = initConfig();
		ILaunch launch = saveAndLaunch(wc, "testDefaults"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--tool=massif")); //$NON-NLS-1$
			assertFalse(cmd.contains("--xml=yes")); //$NON-NLS-1$
			assertTrue(cmd.contains("-q")); //$NON-NLS-1$
			assertTrue(cmd.contains("--trace-children=no")); //$NON-NLS-1$
			assertTrue(cmd.contains("--child-silent-after-fork=yes")); //$NON-NLS-1$
			assertTrue(cmd.contains("--demangle=yes")); //$NON-NLS-1$
			assertTrue(cmd.contains("--num-callers=12")); //$NON-NLS-1$
			assertTrue(cmd.contains("--error-limit=yes")); //$NON-NLS-1$
			assertTrue(cmd.contains("--show-below-main=no")); //$NON-NLS-1$
			assertFalse(cmd.contains("--suppressions")); //$NON-NLS-1$
			assertTrue(cmd.contains("--max-stackframe=2000000")); //$NON-NLS-1$
			assertTrue(cmd.contains("--run-libc-freeres=yes")); //$NON-NLS-1$

			assertTrue(cmd.contains("--heap=yes")); //$NON-NLS-1$
			assertTrue(cmd.contains("--heap-admin=8")); //$NON-NLS-1$
			assertTrue(cmd.contains("--stacks=no")); //$NON-NLS-1$
			assertFalse(cmd.contains("--alloc-fn")); //$NON-NLS-1$
			assertTrue(cmd.contains("--threshold=1.0")); //$NON-NLS-1$
			assertTrue(cmd.contains("--peak-inaccuracy=1.0")); //$NON-NLS-1$
			assertTrue(cmd.contains("--time-unit=i")); //$NON-NLS-1$
			assertTrue(cmd.contains("--detailed-freq=10")); //$NON-NLS-1$
			assertTrue(cmd.contains("--max-snapshots=100")); //$NON-NLS-1$
			assertTrue(cmd.contains("--alignment=8")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}
	
	public void testHeap() throws Exception {		
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getHeapButton().setSelection(false);
		ILaunch launch = saveAndLaunch(wc, "testHeap"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--heap=no")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}
	
	public void testHeapAdmin() throws Exception {		
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getHeapAdminSpinner().setSelection(30);
		ILaunch launch = saveAndLaunch(wc, "testHeapAdmin"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--heap-admin=30")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}
	
	public void testStacks() throws Exception {		
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getStacksButton().setSelection(true);
		ILaunch launch = saveAndLaunch(wc, "testStacks"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--stacks=yes")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}
	
	public void testDepth() throws Exception {		
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getDepthSpinner().setSelection(50);
		ILaunch launch = saveAndLaunch(wc, "testDepth"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--depth=50")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}
	
	public void testAllocFn() throws Exception {		
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getAllocFnList().add("foo"); //$NON-NLS-1$
		ILaunch launch = saveAndLaunch(wc, "testAllocFn"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--alloc-fn=foo")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}
	
	public void testAllocFnMultiple() throws Exception {		
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getAllocFnList().add("foo"); //$NON-NLS-1$
		dynamicTab.getAllocFnList().add("bar"); //$NON-NLS-1$
		ILaunch launch = saveAndLaunch(wc, "testAllocFnMultiple"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--alloc-fn=foo")); //$NON-NLS-1$
			assertTrue(cmd.contains("--alloc-fn=bar")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}
	
	public void testAllocFnSpace() throws Exception {		
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getAllocFnList().add("operator new(unsigned)"); //$NON-NLS-1$
		ILaunch launch = saveAndLaunch(wc, "testAllocFnSpace"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--alloc-fn=operator new(unsigned)")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}
	
	public void testThreshold() throws Exception {		
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getThresholdSpinner().setSelection(20);
		ILaunch launch = saveAndLaunch(wc, "testThreshold"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--threshold=2.0")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}
	
	public void testPeakInaccuracy() throws Exception {		
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getPeakInaccuracySpinner().setSelection(0);
		ILaunch launch = saveAndLaunch(wc, "testPeakInaccuracy"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--peak-inaccuracy=0.0")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}
	
	public void testTimeUnitBytes() throws Exception {		
		ILaunchConfigurationWorkingCopy wc = initConfig();
		String[] items = dynamicTab.getTimeUnitCombo().getItems();
		int ix = -1;
		for (int i = 0; i < items.length; i++) {
			if (items[i].equals(MassifToolPage.TIME_B_STRING)) {
				ix = i;
			}
		}
		dynamicTab.getTimeUnitCombo().select(ix);
		ILaunch launch = saveAndLaunch(wc, "testTimeUnitBytes"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--time-unit=B")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}
	
	public void testTimeUnitMilliseconds() throws Exception {		
		ILaunchConfigurationWorkingCopy wc = initConfig();
		String[] items = dynamicTab.getTimeUnitCombo().getItems();
		int ix = -1;
		for (int i = 0; i < items.length; i++) {
			if (items[i].equals(MassifToolPage.TIME_MS_STRING)) {
				ix = i;
			}
		}
		dynamicTab.getTimeUnitCombo().select(ix);
		ILaunch launch = saveAndLaunch(wc, "testTimeUnitMilliseconds"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--time-unit=ms")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}
	
	public void testDetailedFreq() throws Exception {		
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getDetailedFreqSpinner().setSelection(1);
		ILaunch launch = saveAndLaunch(wc, "testDetailedFreq"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--detailed-freq=1")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}
	
	public void testMaxSnapshots() throws Exception {		
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getMaxSnapshotsSpinner().setSelection(200);
		ILaunch launch = saveAndLaunch(wc, "testMaxSpapshots"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--max-snapshots=200")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}
	
	public void testAlignment() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getAlignmentSpinner().setSelection(512);
		tab.performApply(wc);
		config = wc.doSave();

		assertTrue(tab.isValid(config));

		ILaunch launch = doLaunch(config, "testAlignment"); //$NON-NLS-1$
		IProcess[] p = launch.getProcesses();
		if (p.length > 0) {
			String cmd = p[0].getAttribute(IProcess.ATTR_CMDLINE);
			assertEquals(0, p[0].getExitValue());
			assertTrue(cmd.contains("--alignment=512")); //$NON-NLS-1$
		}
		else {
			fail();
		}
	}

	public void testAlignmentBad() throws Exception {
		ILaunchConfigurationWorkingCopy wc = initConfig();
		dynamicTab.getAlignmentSpinner().setSelection(63);
		tab.performApply(wc);
		config = wc.doSave();

		assertFalse(tab.isValid(config));
	}
}
