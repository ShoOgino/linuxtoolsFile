/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.docker.core;

import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.eclipse.linuxtools.internal.docker.core.DefaultDockerConnectionStorageManager;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerRefreshManager;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionStorageManagerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.spotify.docker.client.DockerClient;

/**
 * 
 */
public class DockerConnectionManagerTest {

	private final DockerConnectionManager dockerConnectionManager = DockerConnectionManager.getInstance();
	private final DockerContainerRefreshManager dockerContainersRefreshManager = DockerContainerRefreshManager
			.getInstance();

	@AfterClass
	public static void restoreDefaultConfig() {
		DockerConnectionManager.getInstance().setConnectionStorageManager(new DefaultDockerConnectionStorageManager());
	}

	@Before
	@After
	public void reset() {
		dockerContainersRefreshManager.reset();
	}
	
	@Test
	public void shouldRegisterConnectionOnRefreshContainersManager() {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		dockerConnectionManager
				.setConnectionStorageManager(MockDockerConnectionStorageManagerFactory.providing(dockerConnection));
		SWTUtils.syncExec(() -> dockerConnectionManager.reloadConnections());
		Assertions.assertThat(dockerContainersRefreshManager.getConnections()).isEmpty();
		// when
		dockerConnection.getContainers();
		// then
		Assertions.assertThat(dockerContainersRefreshManager.getConnections()).containsExactly(dockerConnection);
	}

	@Test
	public void shouldUnregisterConnectionOnRefreshContainersManager() {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		dockerConnectionManager
				.setConnectionStorageManager(MockDockerConnectionStorageManagerFactory.providing(dockerConnection));
		SWTUtils.syncExec(() -> dockerConnectionManager.reloadConnections());
		dockerConnection.getContainers();
		Assertions.assertThat(dockerContainersRefreshManager.getConnections()).containsExactly(dockerConnection);
		// when
		SWTUtils.syncExec(() -> dockerConnectionManager.removeConnection(dockerConnection));
		SWTUtils.wait(1, TimeUnit.SECONDS);
		// then
		Assertions.assertThat(dockerContainersRefreshManager.getConnections()).isEmpty();
	}
}
