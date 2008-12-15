package org.eclipse.linuxtools.valgrind.memcheck.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Tests for org.eclipse.linuxtools.valgrind.memcheck"); //$NON-NLS-1$
		// $JUnit-BEGIN$
		suite.addTestSuite(BasicMemcheckTest.class);
		suite.addTestSuite(DoubleClickTest.class);
		suite.addTestSuite(MarkerTest.class);
		// $JUnit-END$
		return suite;
	}

}
