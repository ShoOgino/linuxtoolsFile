/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.memcheck;

public final class MemcheckCommandConstants {
	// Valgrind program arguments
	public static final String OPT_LEAKCHECK = "--leak-check"; //$NON-NLS-1$
	public static final String OPT_SHOWREACH = "--show-reachable"; //$NON-NLS-1$
	public static final String OPT_LEAKRES = "--leak-resolution"; //$NON-NLS-1$
	public static final String OPT_FREELIST = "--freelist-vol"; //$NON-NLS-1$
	public static final String OPT_GCCWORK = "--workaround-gcc296-bugs"; //$NON-NLS-1$
	public static final String OPT_PARTIAL = "--partial-loads-ok"; //$NON-NLS-1$
	public static final String OPT_UNDEF = "--undef-value-errors"; //$NON-NLS-1$
	public static final String OPT_ALIGNMENT = "--alignment"; //$NON-NLS-1$
	
	// VG >= 3.4.0
	public static final String OPT_TRACKORIGINS = "--track-origins"; //$NON-NLS-1$
}
