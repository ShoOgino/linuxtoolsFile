package org.eclipse.linuxtools.tmf.core.tests.util;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.linuxtools.tmf.core.TmfCorePlugin;

@SuppressWarnings("nls")
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test suite for " + TmfCorePlugin.PLUGIN_ID + ".util"); //$NON-NLS-1$);
		//$JUnit-BEGIN$
		suite.addTestSuite(TmfFixedArrayTest.class);
		//$JUnit-END$
		return suite;
	}
}