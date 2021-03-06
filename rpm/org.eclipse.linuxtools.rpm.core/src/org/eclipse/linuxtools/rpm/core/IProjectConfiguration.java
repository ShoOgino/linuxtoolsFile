/*******************************************************************************
 * Copyright (c) 2011, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core;

import java.util.List;

import org.eclipse.core.resources.IContainer;

/**
 * Project configuration allowing to retrieve information needed for RPM builds.
 *
 */
public interface IProjectConfiguration {

    /**
     * Constant for the --define parameter.
     */
    String DEFINE = "--define"; //$NON-NLS-1$

    /**
     * Returns the folder to build into.
     *
     * @return The build folder.
     */
    IContainer getBuildFolder();

    /**
     * Returns the folder to put built binary rpms.
     *
     * @return The rpms folder.
     */
    IContainer getRpmsFolder();

    /**
     * Returns the folder to retrieve sources from.
     *
     * @return The sources folder.
     */
    IContainer getSourcesFolder();

    /**
     * Returns the folder to retrieve spec file from.
     *
     * @return The specs folder.
     */
    IContainer getSpecsFolder();

    /**
     * Returns the folder to put src.rpm.
     *
     * @return The source rpms folder.
     */
    IContainer getSrpmsFolder();


    /**
     * Returns list of RPM defines to be passed wherever needed so the project structure is respected.
     *
     * @return The defines setting various directories.
     */
    List<String> getConfigDefines();

}
