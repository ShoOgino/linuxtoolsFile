/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

public class WorkItemLinkTypeRelationships {
	
	private RelationWorkItemLinkCategory link_category;
	
	private RelationSpaces spaces;
	
	// for testing purposes only
	public WorkItemLinkTypeRelationships (RelationWorkItemLinkCategory link_category,
			RelationSpaces spaces) {
		this.link_category = link_category;
		this.spaces = spaces;
	}
	
	public RelationWorkItemLinkCategory getLinkCategory() {
		return link_category;
	}
	
	public RelationSpaces getSpaces() {
		return spaces;
	}

}
