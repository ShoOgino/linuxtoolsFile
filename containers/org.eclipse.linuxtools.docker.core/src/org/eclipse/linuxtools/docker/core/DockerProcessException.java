/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat.
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

package org.eclipse.linuxtools.docker.core;

public class DockerProcessException extends DockerException {

	/**
	 * Constructor
	 * 
	 * @param message
	 *            the error message
	 */
	public DockerProcessException(String message) {
		super(message);
	}

	private static final long serialVersionUID = 7940420692807693369L;

}
