/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.signal;

import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;

/**
 * Current experiment has been updated.
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfExperimentUpdatedSignal extends TmfSignal {

	private final TmfExperiment fExperiment;

    /**
     * Constructor
     *
     * @param source
     *            Object sending this signal
     * @param experiment
     *            The experiment that was updated
     */
    public TmfExperimentUpdatedSignal(Object source, TmfExperiment experiment) {
        super(source);
        fExperiment = experiment;
    }

    /**
     * @return The experiment
     */
    public TmfExperiment getExperiment() {
        return fExperiment;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
    @SuppressWarnings("nls")
	public String toString() {
		return "[TmfExperimentUpdatedSignal (" + fExperiment.toString() /*+ ", " + fTrace.toString()*/ + ")]";
	}

}
