/*******************************************************************************
 * Copyright (c) 2014, 2017 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Red Hat Inc. - modified for use in OSIO Rest Connector
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

public class IterationListResponse implements RestResponse<Iteration> {
	private Iteration[] data;
	
	private PagingLinks links;
	
	private SpaceListMeta meta;
	
	// for testing purposes only
	public IterationListResponse (Iteration[] data, PagingLinks links, SpaceListMeta meta) {
		this.data = data;
		this.links = links;
		this.meta = meta;
	}

	@Override
	public Iteration[] getArray() {
		return data;
	}
	
	public PagingLinks getLinks() {
		return links;
	}
	
	public SpaceListMeta getMeta() {
		return meta;
	}
}