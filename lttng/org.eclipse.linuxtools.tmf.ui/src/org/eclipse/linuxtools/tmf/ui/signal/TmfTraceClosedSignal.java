/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.signal;

import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Signal indicating a trace is now closed
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfTraceClosedSignal extends TmfSignal {

    private final ITmfTrace<?> fTrace;

    /**
     * Constructor for a new signal
     *
     * @param source
     *            The object sending this signal
     * @param trace
     *            The trace being closed
     */
    public TmfTraceClosedSignal(Object source, ITmfTrace<?> trace) {
        super(source);
        fTrace = trace;
    }

    /**
     * Get a reference to the trace being closed
     *
     * @return The trace object
     */
    public ITmfTrace<?> getTrace() {
        return fTrace;
    }

    @Override
    public String toString() {
        return "[TmfTraceClosedSignal (" + fTrace.getName() + ")]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
