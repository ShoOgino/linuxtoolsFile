package org.eclipse.linuxtools.tmf.ui.tests.trace;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Well formed XML
 * @author Matthew Khouzam
 *
 */
@RunWith(Parameterized.class)
public class CustomXmlTraceValidTest extends CustomXmlTraceTest {

    private final static String pathname = "tracesets/xml/valid";


    /**
     * This should create the parameters to launch the project
     *
     * @return the path of the parameters
     */
    @Parameters(name = "{index}: path {0}")
    public static Collection<Object[]> getFiles() {
        File[] validFiles = (new File(pathname)).listFiles();
        Collection<Object[]> params = new ArrayList<Object[]>();
        for (File f : validFiles) {
            Object[] arr = new Object[] { f.getAbsolutePath() };
            params.add(arr);
        }
        return params;
    }

    /**
     * Test all the invalid xml files
     */
    @Test
    public void testValid() {
        IStatus valid = getTrace().validate(null, getPath());
        if (IStatus.OK != valid.getSeverity()) {
            fail(valid.toString());
        }
    }

    /**
     * ctor
     *
     * @param filePath
     *            the path
     */
    public CustomXmlTraceValidTest(String filePath) {
        this.setPath(filePath);
    }

}
