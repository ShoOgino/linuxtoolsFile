/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;

public class FlatBuildConfiguration implements IProjectConfiguration {

	private IProject project;
	
	public FlatBuildConfiguration(IProject project) {
		this.project = project;
	}

	public IContainer getBuildFolder() {
		return project;
	}

	public IContainer getRpmsFolder() {
		return project;
	}

	public IContainer getSourcesFolder() {
		return project;
	}

	public IContainer getSpecsFolder() {
		return project;
	}

	public IContainer getSrpmsFolder() {
		return project;
	}

}
