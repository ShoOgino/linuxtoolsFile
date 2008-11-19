/*******************************************************************************
 * Copyright (c) 2004 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *******************************************************************************/ 


package org.eclipse.linuxtools.oprofile.launch;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class OprofileLaunchMessages
{

	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.oprofile.launch.oprofilelaunch"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private OprofileLaunchMessages()
	{
	}

	public static String getString(String key)
	{
		try
		{
			return RESOURCE_BUNDLE.getString(key);
		}
		catch (MissingResourceException e)
		{
			return '!' + key + '!';
		}
	}
}
