/*******************************************************************************
 * Copyright (c) 2013, 2018 Frank Becker and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Red Hat Inc. - modified for use with OpenShift.io
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.core;

public class OSIORestResourceNotFoundException extends OSIORestException {

	private static final long serialVersionUID = 5227546210820677763L;

	public OSIORestResourceNotFoundException() {
	}

	public OSIORestResourceNotFoundException(String message) {
		super(message);
	}

	public OSIORestResourceNotFoundException(Throwable cause) {
		super(cause);
	}

	public OSIORestResourceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
