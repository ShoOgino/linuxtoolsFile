/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Patrick Tasse - Initial API and implementation
 * Francois Chouinard - Put in shape for 1.0
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.trace;

import java.util.Arrays;

/**
 * A convenience class to store trace location arrays. The main purpose is to
 * provide a Comparable implementation for TmfExperimentLocation.
 * 
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfLocationArray implements Comparable<TmfLocationArray>, Cloneable {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private TmfRankedLocation[] fLocations;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The standard constructor
     * 
     * @param locations the locations
     */
    public TmfLocationArray(TmfRankedLocation[] locations) {
        fLocations = locations;
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * The standard constructor
     * 
     * @param locations the locations
     */
    public TmfRankedLocation[] getLocations() {
        return fLocations;
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public TmfLocationArray clone() {
        TmfRankedLocation[] clones = new TmfRankedLocation[fLocations.length];
        for (int i = 0; i < fLocations.length; i++) {
            TmfRankedLocation location = fLocations[i];
            clones[i] = (location != null) ? location.clone() : null;
        }
        return new TmfLocationArray(clones);
    }

    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

    @Override
    public int compareTo(TmfLocationArray o) {
        for (int i = 0; i < fLocations.length; i++) {
            TmfRankedLocation l1 = fLocations[i];
            TmfRankedLocation l2 = o.fLocations[i];
            int result = l1.compareTo(l2);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(fLocations);
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TmfLocationArray other = (TmfLocationArray) obj;
        if (!Arrays.equals(fLocations, other.fLocations)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfLocationArray [locations=" + Arrays.toString(fLocations) + "]";
    }

}
