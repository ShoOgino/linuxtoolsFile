/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat.
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

import java.io.File;
import java.net.URI;

import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings.BindingType;
import org.eclipse.linuxtools.docker.core.IRegistryAccount;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DefaultDockerClient.Builder;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.messages.RegistryAuth;

/**
 * Factory for {@link DockerClient}. Decoupling from {@link DockerConnection}
 * implementation to more easily introduce variants (eg, during tests)
 */
public class DockerClientFactory {

	/**
	 * Creates a new {@link DockerClient} from the given
	 * {@link IDockerConnectionSettings}.
	 * 
	 * @param connectionSettings
	 *            the connection settings
	 * @return the {@link DockerClient}
	 * @throws DockerCertificateException
	 *             if the path to Docker certificates is invalid (missing files)
	 */
	public DockerClient getClient(
			final IDockerConnectionSettings connectionSettings)
			throws DockerCertificateException {
		return getClient(connectionSettings, null);
	}

	/**
	 * Creates a new {@link DockerClient} from the given
	 * {@link IDockerConnectionSettings}.
	 * 
	 * @param connectionSettings
	 *            the connection settings
	 * @return the {@link DockerClient} or <code>null</code> if the connection
	 *         URI (Unix socker path or TCP host) was missing (ie,
	 *         <code>null</code> or empty)
	 * @throws DockerCertificateException
	 *             if the path to Docker certificates is invalid (missing files)
	 */
	public DockerClient getClient(
			final IDockerConnectionSettings connectionSettings,
			final IRegistryAccount registryAccount)
			throws DockerCertificateException {
		final Builder builder = DefaultDockerClient.builder();
		if (connectionSettings
				.getType() == BindingType.UNIX_SOCKET_CONNECTION) {
			final UnixSocketConnectionSettings unixSocketConnectionSettings = (UnixSocketConnectionSettings) connectionSettings;
			if (unixSocketConnectionSettings.hasPath()) {
				builder.uri(unixSocketConnectionSettings.getPath());
			}

		} else {
			final TCPConnectionSettings tcpConnectionSettings = (TCPConnectionSettings) connectionSettings;
			if (tcpConnectionSettings.hasHost()) {
				builder.uri(URI.create(tcpConnectionSettings.getHost()));
				if (tcpConnectionSettings.getPathToCertificates() != null
						&& !tcpConnectionSettings.getPathToCertificates()
								.isEmpty()) {
					builder.dockerCertificates(new DockerCertificates(new File(
							tcpConnectionSettings.getPathToCertificates())
									.toPath()));
				}
			}
		}
		// skip if no URI exists
		if (builder.uri() == null) {
			return null;
		}

		if (registryAccount != null) {
			builder.registryAuth(buildAuthentication(registryAccount));
		}
		return builder.build();
	}

	private RegistryAuth buildAuthentication(final IRegistryAccount info) {
		if (info.getUsername() != null && !info.getUsername().isEmpty()) {
			final RegistryAuth authAccount = RegistryAuth.builder()
					.serverAddress(info.getServerAddress())
					.username(info.getUsername()).email(info.getEmail())
					.password(info.getPassword() != null
							? new String(info.getPassword()) : null)
					.build();
			return authAccount;
		}
		return null;
	}

}
