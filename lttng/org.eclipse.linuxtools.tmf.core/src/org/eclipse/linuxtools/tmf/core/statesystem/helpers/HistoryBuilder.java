/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statesystem.helpers;

import java.io.IOException;

import org.eclipse.linuxtools.tmf.core.statesystem.StateHistorySystem;

/**
 * This is the high-level wrapper around the State History and its input and
 * storage plugins. Just create the object using the constructor then .run()
 * 
 * You can use one HistoryBuilder and it will instantiate everything underneath.
 * If you need more fine-grained control you can still ignore this and
 * instantiate everything manually.
 * 
 * @author alexmont
 * 
 */
public class HistoryBuilder implements Runnable {

    private final IStateChangeInput sci;
    private final StateHistorySystem shs;
    private final IStateHistoryBackend hb;

    private final Thread sciThread;

    /**
     * Instantiate a new HistoryBuilder helper.
     * 
     * @param stateChangeInput
     *            The input plugin to use. This is required.
     * @param backend
     *            The backend storage to use. Use "null" here if you want a
     *            state system with no history.
     * @throws IOException
     *             Is thrown if anything went wrong (usually with the storage
     *             backend)
     */
    public HistoryBuilder(IStateChangeInput stateChangeInput,
            IStateHistoryBackend backend) throws IOException {
        assert (stateChangeInput != null);
        assert (backend != null);

        sci = stateChangeInput;
        hb = backend;
        shs = new StateHistorySystem(hb, true);

        sci.assignTargetStateSystem(shs);
        sciThread = new Thread(sci, "Input Plugin"); //$NON-NLS-1$
    }

    /**
     * Factory-style method to open an existing history, you only have to
     * provide the already-instantiated IStateHistoryBackend object.
     * 
     * @param hb
     *            The history-backend object
     * @return A IStateSystemBuilder reference to the new state system. If you
     *         will only run queries on this history, you should *definitely*
     *         cast it to IStateSystemQuerier.
     * @throws IOException
     *             If there was something wrong.
     */
    public static IStateSystemBuilder openExistingHistory(
            IStateHistoryBackend hb) throws IOException {
        return new StateHistorySystem(hb, false);
    }

    @Override
    public void run() {
        sciThread.start();
    }

    /**
     * Since HistoryBuilder.run() simply starts the processing asynchronously,
     * you should call .close() when you know the state history is completely
     * built (or when the user closes the trace, whichever comes first).
     */
    public void close() {
        try {
            sciThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return a read/write reference to the state system object that was
     * created.
     * 
     * @return Reference to the state system, with access to everything.
     */
    public IStateSystemBuilder getStateSystemBuilder() {
        return shs;
    }

    /**
     * Return a read-only reference to the state system object that was created.
     * 
     * @return Reference to the state system, but only with the query methods
     *         available.
     */
    public IStateSystemQuerier getStateSystemQuerier() {
        return shs;
    }

}