/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.statistics;

import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.statistics.TmfEventsStatistics;
import org.junit.BeforeClass;

/**
 * Unit tests for the {@link TmfEventsStatistics}
 *
 * @author Alexandre Montplaisir
 */
public class TmfEventsStatisticsTest extends TmfStatisticsTest {

    /**
     * Set up the fixture once for all tests.
     */
    @BeforeClass
    public static void setUpClass() {
        try {
            backend = new TmfEventsStatistics(TestParams.createTrace());
        } catch (TmfTraceException e) {
            e.printStackTrace();
        }
    }
}
