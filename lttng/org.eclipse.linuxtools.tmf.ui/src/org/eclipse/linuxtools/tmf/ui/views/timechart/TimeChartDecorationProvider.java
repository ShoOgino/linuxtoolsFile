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

package org.eclipse.linuxtools.tmf.ui.views.timechart;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;

public class TimeChartDecorationProvider {

	private IResource fResource;
    private Set<Long> fBookmarksSet = new HashSet<Long>();
    private ITmfFilter fFilterFilter;
    private ITmfFilter fSearchFilter;

	public TimeChartDecorationProvider(IResource resource) {
	    fResource = resource;
	    refreshBookmarks();
    }

	public IResource getResource() {
		return fResource;
	}
	
	public boolean isBookmark(long rank) {
	    return fBookmarksSet.contains(rank);
    }
	
	public void refreshBookmarks() {
		try {
			fBookmarksSet.clear();
	        for (IMarker bookmark : fResource.findMarkers(IMarker.BOOKMARK, false, IResource.DEPTH_ZERO)) {
	        	int location = bookmark.getAttribute(IMarker.LOCATION, -1);
	        	if (location != -1) {
	        		Long rank = (long) location;
	        		fBookmarksSet.add(rank);
	        	}
	        }
        } catch (CoreException e) {
	        e.printStackTrace();
        }
    }

	public void filterApplied(ITmfFilter filter) {
		fFilterFilter = filter;
    }

	public boolean isVisible(TmfEvent event) {
		if (fFilterFilter != null) {
			return fFilterFilter.matches(event);
		}
		return true;
	}
	
	public void searchApplied(ITmfFilter filter) {
		fSearchFilter = filter;
    }
	
	public boolean isSearchMatch(TmfEvent event) {
		if (fSearchFilter != null) {
			return fSearchFilter.matches(event);
		}
		return false;
	}
	
}
