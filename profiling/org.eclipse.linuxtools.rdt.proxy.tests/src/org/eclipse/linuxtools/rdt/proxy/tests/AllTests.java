/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.linuxtools.rdt.proxy.tests;

import org.eclipse.linuxtools.rdt.proxy.tests.CommandLauncherProxyTest;
import org.eclipse.linuxtools.rdt.proxy.tests.FileProxyTest;
import org.eclipse.linuxtools.rdt.proxy.tests.RemoteProxyManagerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({CommandLauncherProxyTest.class, FileProxyTest.class,
	RemoteProxyManagerTest.class
})
public class AllTests {

}
