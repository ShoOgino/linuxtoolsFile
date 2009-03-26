package org.eclipse.linuxtools.valgrind.cachegrind.tests;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.valgrind.cachegrind.CachegrindViewPart;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindFile;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindOutput;
import org.eclipse.linuxtools.valgrind.core.LaunchConfigurationConstants;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;

public class MultiProcessTest extends AbstractCachegrindTest {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createProjectAndBuild("cpptest"); //$NON-NLS-1$
		proj = createProjectAndBuild("multiProcTest"); //$NON-NLS-1$
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		deleteProject(proj);
	}
	
	public void testNoExec() throws Exception {
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfiguration config = createConfiguration(bin);
		doLaunch(config, "testNoExec"); //$NON-NLS-1$
		
		CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		assertEquals(1, view.getOutputs().length);
	}
	
	public void testNumPids() throws Exception {
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfigurationWorkingCopy config = createConfiguration(bin).getWorkingCopy();
		config.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_TRACECHILD, true);
		config.doSave();
		doLaunch(config, "testExec"); //$NON-NLS-1$
		
		CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		assertEquals(2, view.getOutputs().length);
	}
	
	public void testFileNames() throws Exception {
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfigurationWorkingCopy config = createConfiguration(bin).getWorkingCopy();
		config.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_TRACECHILD, true);
		config.doSave();
		doLaunch(config, "testExec"); //$NON-NLS-1$
		
		CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		
		int pidIx = 0;
		CachegrindOutput output = view.getOutputs()[pidIx];
		CachegrindFile file = getFileByName(output, "cpptest.cpp"); //$NON-NLS-1$
		if (file == null) {
			pidIx = 1;
			output = view.getOutputs()[pidIx];
			file = getFileByName(output, "cpptest.cpp"); //$NON-NLS-1$
		}
		assertNotNull(file);
		file = getFileByName(output, "cpptest.h"); //$NON-NLS-1$
		assertNotNull(file);
		
		// test other pid
		pidIx = (pidIx + 1) % 2;
		output = view.getOutputs()[pidIx];
		file = getFileByName(output, "parent.cpp"); //$NON-NLS-1$
		assertNotNull(file);
	}
	
	public void testNumFunctions() throws Exception {
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfigurationWorkingCopy config = createConfiguration(bin).getWorkingCopy();
		config.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_TRACECHILD, true);
		config.doSave();;
		doLaunch(config, "testExec"); //$NON-NLS-1$
		
		CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		
		int pidIx = 0;
		CachegrindOutput output = view.getOutputs()[pidIx];
		CachegrindFile file = getFileByName(output, "cpptest.cpp"); //$NON-NLS-1$
		if (file == null) {
			pidIx = 1;
			output = view.getOutputs()[pidIx];
			file = getFileByName(output, "cpptest.cpp"); //$NON-NLS-1$
		}
		assertNotNull(file);
		assertEquals(8, file.getFunctions().length);
		
		// test other pid
		pidIx = (pidIx + 1) % 2;
		output = view.getOutputs()[pidIx];
		file = getFileByName(output, "parent.cpp"); //$NON-NLS-1$
		assertNotNull(file);
		assertEquals(6, file.getFunctions().length);
	}
}
