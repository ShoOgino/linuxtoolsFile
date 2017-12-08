/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.mylyn.osio.rest.core.tests;

import static org.junit.Assert.assertEquals;

import java.io.FileReader;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.IOSIORestConstants;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.NullOperationMonitor;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestClient;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConfiguration;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConnector;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestGetUser;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Identity;
import org.eclipse.linuxtools.mylyn.osio.rest.test.support.OSIOTestRestRequestProvider;
import org.eclipse.linuxtools.mylyn.osio.rest.test.support.TestData;
import org.eclipse.linuxtools.mylyn.osio.rest.test.support.TestUtils;
import org.eclipse.mylyn.commons.repositories.core.RepositoryLocation;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class TestOSIORestGetUser {
	
	private TestOSIORestConnector connector;

	private TaskRepository repository;

	private OSIOTestRestRequestProvider requestProvider;
	
	private class TestOSIORestConnector extends OSIORestConnector {
		
		private OSIORestConfiguration config;
		
		public void setConfiguration (OSIORestConfiguration config) {
			this.config = config;
		}
		
		@Override
		public OSIORestConfiguration getRepositoryConfiguration(TaskRepository repository) throws CoreException {
			return config;
		}
	}
	
	@Before
	public void setUp() {
		connector = new TestOSIORestConnector();
		repository = new TaskRepository(connector.getConnectorKind(), "http://openshift.io/api");
		repository.setProperty(IOSIORestConstants.REPOSITORY_AUTH_ID, "user");
		repository.setProperty(IOSIORestConstants.REPOSITORY_AUTH_TOKEN, "xxxxxxTokenxxxxxx");
		requestProvider = new OSIOTestRestRequestProvider();
	}

	@Test
	public void testGetAuthUser() throws Exception {
		TestData testData = new TestData();
		TestUtils.initSpaces(requestProvider, testData);
		OSIORestClient client = connector.getClient(repository, requestProvider);
		OSIORestConfiguration config = client.getConfiguration(repository, new NullOperationMonitor());
		config.setSpaces(testData.spaceMap);
		connector.setConfiguration(config);
		RepositoryLocation location = client.getClient().getLocation();
		location.setProperty(IOSIORestConstants.REPOSITORY_AUTH_ID, "user");
		location.setProperty(IOSIORestConstants.REPOSITORY_AUTH_TOKEN, "xxxxxxTokenxxxxxx");

		OSIORestGetUser data = new OSIORestGetUser(client.getClient(), "USER-0001");
		
		String bundleLocation = Activator.getContext().getBundle().getLocation();
		int index = bundleLocation.indexOf('/');
		String fileName = bundleLocation.substring(index) + "/testjson/user.data";
		FileReader in = new FileReader(fileName);
		Identity user = data.testParseFromJson(in);
		
		assertEquals("User.org", user.getCompany());
		assertEquals("user@user.org", user.getEmail());
		assertEquals("User 1", user.getFullName());
		assertEquals("IDENTITY-0001", user.getIdentityID());
		assertEquals("https://www.gravatar.com/avatar/user.jpg", user.getImageURL());
		assertEquals("USER-0001", user.getUserID());
		assertEquals("user", user.getUsername());
		assertEquals("IDENTITY-0001", user.getId());
	}

}
