/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.component;

import java.lang.reflect.Array;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.linuxtools.tmf.Tracer;
import org.eclipse.linuxtools.tmf.event.TmfData;
import org.eclipse.linuxtools.tmf.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.request.TmfCoalescedDataRequest;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.request.TmfRequestExecutor;
import org.eclipse.linuxtools.tmf.signal.TmfEndSynchSignal;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.signal.TmfStartSynchSignal;
import org.eclipse.linuxtools.tmf.trace.ITmfContext;

/**
 * <b><u>TmfProvider</u></b>
 * <p>
 * The TmfProvider<T> is a provider for a data of type <T>.
 * <p>
 * This abstract class implements the housekeeking methods to register/
 * deregister the event provider and to handle generically the event requests.
 * <p>
 * The concrete class can either re-implement processRequest() entirely or
 * just implement the hooks (initializeContext() and getNext()).
 * <p>
 * TODO: Add support for providing multiple data types.
 */
public abstract class TmfDataProvider<T extends TmfData> extends TmfComponent implements ITmfDataProvider<T> {

	final protected Class<T> fType;
	final protected boolean  fLogData;
	final protected boolean  fLogException;

	public static final int DEFAULT_QUEUE_SIZE = 1000;
	protected final int fQueueSize;
	protected final BlockingQueue<T> fDataQueue;
	protected final TmfRequestExecutor fExecutor;

	private int fSignalDepth = 0;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------
	
	public TmfDataProvider(String name, Class<T> type) {
		this(name, type, DEFAULT_QUEUE_SIZE);
	}

	protected TmfDataProvider(String name, Class<T> type, int queueSize) {
		super(name);
		fType = type;
		fQueueSize = queueSize;
		fDataQueue = (queueSize > 1) ? new LinkedBlockingQueue<T>(fQueueSize) : new SynchronousQueue<T>();

//        Tracer.traceComponent(getName() + " created");

        fExecutor = new TmfRequestExecutor();
		fSignalDepth = 0;

		fLogData = Tracer.isEventTraced();
		fLogException = Tracer.isEventTraced();

		TmfProviderManager.register(fType, this);
//		Tracer.traceComponent(getName() + " started");
}
	
	public TmfDataProvider(TmfDataProvider<T> other) {
        super(other);
        fType = other.fType;
        fQueueSize = other.fQueueSize;
        fDataQueue = (fQueueSize > 1) ? new LinkedBlockingQueue<T>(fQueueSize) : new SynchronousQueue<T>();

        fExecutor = new TmfRequestExecutor();
        fSignalDepth = 0;

        fLogData = Tracer.isEventTraced();
		fLogException = Tracer.isEventTraced();
	}
	
	@Override
	public void dispose() {
		TmfProviderManager.deregister(fType, this);
		fExecutor.stop();
//		Tracer.traceComponent(getName() + " stopped");
		if (fClone != null) fClone.dispose();
		super.dispose();
	}

	public int getQueueSize() {
		return fQueueSize;
	}

	public Class<?> getType() {
		return fType;
	}

	// ------------------------------------------------------------------------
	// ITmfRequestHandler
	// ------------------------------------------------------------------------

//	public synchronized void sendRequest(final ITmfDataRequest<T> request, ExecutionType execType) {
//		sendRequest(request);
//	}

	public synchronized void sendRequest(final ITmfDataRequest<T> request) {
		sendRequest(request, ExecutionType.SHORT);
	}

	protected TmfDataProvider<T> fClone;
	public synchronized void sendRequest(final ITmfDataRequest<T> request, ExecutionType execType) {
		if (fClone == null || execType == ExecutionType.SHORT) {
			if (fSignalDepth > 0) {
				coalesceDataRequest(request);
			} else {
				queueRequest(request);
			}
		}
		else {
			fClone.sendRequest(request);
		}
	}

	/**
	 * This method queues the coalesced requests.
	 * 
	 * @param thread
	 */
	public synchronized void fireRequests() {
		for (TmfDataRequest<T> request : fPendingCoalescedRequests) {
			queueRequest(request);
		}
		fPendingCoalescedRequests.clear();

		if (fClone != null)
			fClone.fireRequests();
	}

	// ------------------------------------------------------------------------
	// Coalescing (primitive test...)
	// ------------------------------------------------------------------------

	protected Vector<TmfCoalescedDataRequest<T>> fPendingCoalescedRequests = new Vector<TmfCoalescedDataRequest<T>>();

	protected synchronized void newCoalescedDataRequest(ITmfDataRequest<T> request) {
		TmfCoalescedDataRequest<T> coalescedRequest =
			new TmfCoalescedDataRequest<T>(fType, request.getIndex(), request.getNbRequested(), request.getBlockize());
		coalescedRequest.addRequest(request);
		fPendingCoalescedRequests.add(coalescedRequest);
	}

	protected synchronized void coalesceDataRequest(ITmfDataRequest<T> request) {
		for (TmfCoalescedDataRequest<T> req : fPendingCoalescedRequests) {
			if (req.isCompatible(request)) {
				req.addRequest(request);
				return;
			}
		}
		newCoalescedDataRequest(request);
	}

	// ------------------------------------------------------------------------
	// Request processing
	// ------------------------------------------------------------------------

	protected void queueRequest(final ITmfDataRequest<T> request) {

//		final String provider = getName();
		final ITmfDataProvider<T> provider = this;

		// Process the request
		Thread thread = new Thread() {

			@Override
			public void run() {

				// Extract the generic information
				request.start();
				int blockSize   = request.getBlockize();
				int nbRequested = request.getNbRequested();
			 
				// Create the result buffer
				Vector<T> result = new Vector<T>();
				int nbRead = 0;

				// Initialize the execution
				ITmfContext context = armRequest(request);
				if (context == null) {
					request.cancel();
					return;
				}

				try {
					// Get the ordered events
//					Tracer.traceLog("Request #" + request.getRequestId() + " is serviced by " + provider);
					T data = getNext(context);
//					Tracer.traceLog("Request #" + request.getRequestId() + " read first event");
					while (data != null && !isCompleted(request, data, nbRead))
					{
						if (fLogData) Tracer.traceEvent(provider, request, data);
						result.add(data);
						if (++nbRead % blockSize == 0) {
							pushData(request, result);
						}
						// To avoid an unnecessary read passed the last data requested
						if (nbRead < nbRequested) {
							data = getNext(context);
							if (data == null || data.isNullRef()) {
//								Tracer.traceLog("Request #" + request.getRequestId() + " end of data");
							}
						}
					}
					pushData(request, result);
					request.done();
				}
				catch (Exception e) {
					e.printStackTrace();
					if (fLogException) Tracer.traceException(e);
					request.fail();
				}
			}
		};
		fExecutor.execute(thread);
        if (Tracer.isRequestTraced()) Tracer.traceRequest(request, "queued");
	}

	/**
	 * Format the result data and forwards it to the requester.
	 * Note: after handling, the data is *removed*.
	 * 
	 * @param request
	 * @param data
	 */
	@SuppressWarnings("unchecked")
	protected void pushData(ITmfDataRequest<T> request, Vector<T> data) {
		synchronized(request) {
			if (!request.isCompleted()) {
				T[] result = (T[]) Array.newInstance(fType, data.size());
				data.toArray(result);
				request.setData(result);
				request.handleData();
				data.removeAllElements();
			}
		}
	}

	/**
	 * Initialize the provider based on the request. The context is
	 * provider specific and will be updated by getNext().
	 * 
	 * @param request
	 * @return an application specific context; null if request can't be serviced
	 */
	public abstract ITmfContext armRequest(ITmfDataRequest<T> request);
	
	/**
	 * Return the next piece of data based on the context supplied. The context
	 * would typically be updated for the subsequent read.
	 * 
	 * @param context
	 * @return
	 */
	private final int TIMEOUT = 5000;
	public T getNext(ITmfContext context) throws InterruptedException {
		T event = fDataQueue.poll(TIMEOUT, TimeUnit.MILLISECONDS);
		if (event == null) {
			if (Tracer.isErrorTraced()) Tracer.traceError("Request timeout on read");
			throw new InterruptedException();
		}
		return event;
	}

	/**
	 * Makes the generated result data available for getNext()
	 * 
	 * @param data
	 */
	public void queueResult(T data) throws InterruptedException {
		boolean ok = fDataQueue.offer(data, TIMEOUT, TimeUnit.MILLISECONDS);
		if (!ok) {
			if (Tracer.isErrorTraced()) Tracer.traceError("Request timeout on write");
			throw new InterruptedException();
		}
	}

	/**
	 * Checks if the data meets the request completion criteria.
	 * 
	 * @param request
	 * @param data
	 * @return
	 */
	public boolean isCompleted(ITmfDataRequest<T> request, T data, int nbRead) {
		return request.isCompleted() || nbRead >= request.getNbRequested() || data.isNullRef();
	}

	// ------------------------------------------------------------------------
	// Signal handlers
	// ------------------------------------------------------------------------

	@TmfSignalHandler
	public void startSynch(TmfStartSynchSignal signal) {
		synchronized(this) {
			fSignalDepth++;
		}
	}

	@TmfSignalHandler
	public void endSynch(TmfEndSynchSignal signal) {
		synchronized(this) {
			fSignalDepth--;
			if (fSignalDepth == 0) {
				fireRequests();
			}
		}
	}

}
