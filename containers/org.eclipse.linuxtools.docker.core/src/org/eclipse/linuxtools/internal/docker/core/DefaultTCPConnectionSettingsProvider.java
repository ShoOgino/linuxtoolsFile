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
package org.eclipse.linuxtools.internal.docker.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettingsProvider;

public class DefaultTCPConnectionSettingsProvider implements IDockerConnectionSettingsProvider {

	@Override
	public List<IDockerConnectionSettings> getConnectionSettings() {
		final TCPConnectionSettings tcp = new TCPConnectionSettings(
				"127.0.0.1:2375", null); //$NON-NLS-1$
		tcp.setName(tcp.getHost());
		DockerConnection conn = new DockerConnection.Builder().tcpConnection(tcp);
		try {
			conn.open(false);
			conn.close();
		} catch (DockerException e) {
			return Collections.emptyList();
		}
		return Arrays.asList(tcp);
	}

}
