/*******************************************************************************
 * Copyright (c) 2007 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools.ui.properties;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.linuxtools.cdt.autotools.AutotoolsPlugin;


public class AutotoolsPropertyConstants {
	
	static final String PREFIX = AutotoolsPlugin.getUniqueIdentifier() + "."; // $NON-NLS-1$
	public static final String AUTOMAKE_VERSION_STRING = "AutoconfEditorAutomakeVersion"; // $NON-NLS-1$
	public static final QualifiedName AUTOMAKE_VERSION = new QualifiedName(PREFIX, AUTOMAKE_VERSION_STRING);
	public static final String AUTOCONF_VERSION_STRING = "AutoconfEditorAutoconfVersion"; // $NON-NLS-1$
	public static final QualifiedName AUTOCONF_VERSION = new QualifiedName(PREFIX, AUTOCONF_VERSION_STRING);
	public static final String AUTOCONF_MACRO_VERSIONING = "AutoconfEditorMacroVersioning"; // $NON-NLS-1$
	public static final QualifiedName AUTOCONF_TOOL = new QualifiedName(PREFIX, "AutoconfToolPath"); // $NON-NLS-1$
	public static final QualifiedName AUTOMAKE_TOOL = new QualifiedName(PREFIX, "AutomakeToolPath"); // $NON-NLS-1$
	public static final QualifiedName ACLOCAL_TOOL = new QualifiedName(PREFIX, "AclocalToolPath");  // $NON-NLS-1$
	public static final QualifiedName CLEAN_DELETE = new QualifiedName(PREFIX, "CleanDelete"); // $NON-NLS-1$
	public static final QualifiedName CLEAN_MAKE_TARGET = new QualifiedName(PREFIX, "CleanMakeTarget"); // $NON-NLS-1$
	public static final QualifiedName SCANNER_USE_MAKE_W = new QualifiedName(PREFIX, "ScannerUseMakeW");
	public static final QualifiedName OPEN_INCLUDE = new QualifiedName(PREFIX, "IncludeResourceMapping"); // $NON-NLS-1$
	public static final QualifiedName OPEN_INCLUDE_P = new QualifiedName(PREFIX, "PersistentIncludeResourceMapping"); //$NON-NLS-1$
	public static final QualifiedName SCANNER_INFO_DIRTY = new QualifiedName(PREFIX, "ScannerInfoDirty"); // $NON-NLSp-1$
	
	static String[] fACVersions = {"2.13", "2.59", "2.61"}; // $NON-NLS-1$
	static final String LATEST_AC_VERSION = fACVersions[fACVersions.length - 1];
	
	static String[] fAMVersions = {"1.4-p6", "1.9.5", "1.9.6"}; // $NON-NLS-1$
	static final String LATEST_AM_VERSION = fAMVersions[fAMVersions.length - 1];
	
	public static final String CLEAN_MAKE_TARGET_DEFAULT = "distclean"; // $NON-NLS-1$
	public static final String TRUE = "true"; // $NON-NLS-1$
	public static final String FALSE = "false"; // $NON-NLS-1$
}
