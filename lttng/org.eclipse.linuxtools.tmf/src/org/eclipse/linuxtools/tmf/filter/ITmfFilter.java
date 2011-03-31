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

package org.eclipse.linuxtools.tmf.filter;

import org.eclipse.linuxtools.tmf.event.TmfEvent;


public interface ITmfFilter {

	/**
	 * Verify the filter conditions on an event
	 * 
	 * @param event The event to verify.
	 * @return True if the event matches the filter conditions.
	 */
	public boolean matches(TmfEvent event);
	
}
