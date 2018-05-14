/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

import java.util.Map;

public class WorkItemResponse implements IdNamed {
	
	private String type;
	
	private String id;
	
	private Map<String, Object> attributes;
	
	private WorkItemRelationships relationships;
	
	private GenericLinksForWorkItem links;
	
	// for testing purposes only
	public WorkItemResponse (String id, String type, Map<String, Object> attributes,
			WorkItemRelationships relationships, GenericLinksForWorkItem links) {
		this.id = id;
		this.type = type;
		this.attributes = attributes;
		this.relationships = relationships;
		this.links = links;
	}
	
	public String getName() {
		return (String)attributes.get("system.title"); //$NON-NLS-1$
	}
	
	public String getType() {
		return type;
	}
	
	public String getId() {
		return id;
	}
	
	public Map<String, Object> getAttributes() {
		return attributes;
	}
	
	public WorkItemRelationships getRelationships() {
		return relationships;
	}
	
	public GenericLinksForWorkItem getLinks() {
		return links;
	}
	
}
