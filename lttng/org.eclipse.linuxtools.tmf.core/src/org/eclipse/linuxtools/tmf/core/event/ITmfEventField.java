/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

/**
 * <b><u>ITmfEventField</u></b>
 * <p>
 */
public interface ITmfEventField extends Cloneable {

    /**
     * @return the field ID
     */
    public String getId();

    /**
     * @return the field value
     */
    public Object getValue();

    /**
     * @return the list of subfields (if any)
     */
    public ITmfEventField[] getSubFields();

    /**
     * @return a clone of the event type
     */
    public ITmfEventField clone();

}
