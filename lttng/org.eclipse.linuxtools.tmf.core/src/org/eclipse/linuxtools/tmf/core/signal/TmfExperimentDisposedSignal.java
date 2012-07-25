/*******************************************************************************
 * Copyright (c) 2011 Ericsson
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

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;

/**
 * Experiemnt has been disposed
 *
 * @param <T> The experiment event type
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfExperimentDisposedSignal<T extends ITmfEvent> extends TmfSignal {

	private final TmfExperiment<T> fExperiment;

    /**
     * Constructor
     *
     * @param source
     *            Object sending this signal
     * @param experiment
     *            Experiment that was disposed
     */
	public TmfExperimentDisposedSignal(Object source, TmfExperiment<T> experiment) {
		super(source);
		fExperiment = experiment;
	}

    /**
     * @return Reference to the disposed experiment concerning this signal
     */
	public TmfExperiment<? extends ITmfEvent> getExperiment() {
		return fExperiment;
	}

	@Override
    @SuppressWarnings("nls")
	public String toString() {
		return "[TmfExperimentDisposedSignal (" + fExperiment.getName() + ")]";
	}
}
