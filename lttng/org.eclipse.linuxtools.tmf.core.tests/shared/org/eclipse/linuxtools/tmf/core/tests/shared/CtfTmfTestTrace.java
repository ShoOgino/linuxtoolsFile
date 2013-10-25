/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.shared;

import org.eclipse.linuxtools.ctf.core.tests.shared.CtfTestTrace;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.tests.stubs.ctf.CtfTmfTraceStub;

/**
 * Available CTF TMF test traces. Kind-of-extends {@link CtfTestTrace}.
 *
 * To run tests using these, you first need to run the "get-traces.[xml|sh]"
 * script located under lttng/org.eclipse.linuxtools.ctf.core.tests/traces/ .
 *
 * @author Alexandre Montplaisir
 */
public enum CtfTmfTestTrace {
    /** Example kernel trace */
    KERNEL,
    /** Another kernel trace */
    TRACE2,
    /** Kernel trace with event contexts */
    KERNEL_VM,
    /** Trace synchronization: source trace */
    SYNC_SRC,
    /** Trace synchronization: destination trace */
    SYNC_DEST,
    /** UST trace with lots of lost events */
    HELLO_LOST,
    /** UST trace with lttng-ust-cyg-profile events (aka -finstrument-functions) */
    CYG_PROFILE,
    /** UST trace with lttng-ust-cyg-profile-fast events (no address in func_exit) */
    CYG_PROFILE_FAST;


    private final String fPath;
    private CtfTmfTraceStub fTrace = null;

    private CtfTmfTestTrace() {
        /* This makes my head spin */
        fPath = CtfTestTrace.valueOf(this.name()).getPath();
    }

    /**
     * @return The path of this trace
     */
    public String getPath() {
        return fPath;
    }

    /**
     * Return a CtfTmfTraceStub object of this test trace. It will be already
     * initTrace()'ed.
     *
     * Make sure you call {@link #exists()} before calling this!
     *
     * After being used by unit tests, traces must be properly disposed of by
     * calling the {@link CtfTmfTestTrace#dispose()} method.
     *
     * @return A CtfTmfTrace reference to this trace
     */
    public synchronized CtfTmfTrace getTrace() {
        if (fTrace != null) {
            fTrace.dispose();
        }
        fTrace = new CtfTmfTraceStub();
        try {
            fTrace.initTrace(null, fPath, CtfTmfEvent.class);
        } catch (TmfTraceException e) {
            /* Should not happen if tracesExist() passed */
            throw new RuntimeException(e);
        }
        return fTrace;
    }

    /**
     * Check if the trace actually exists on disk or not.
     *
     * @return If the trace is present
     */
    public boolean exists() {
        return CtfTestTrace.valueOf(this.name()).exists();
    }

    /**
     * Dispose of the trace
     */
    public void dispose() {
        if (fTrace != null) {
            fTrace.dispose();
            fTrace = null;
        }
    }
}
