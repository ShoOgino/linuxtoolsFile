/*******************************************************************************
 * Copyright (c) 2015, 2018 Frank Becker and others.
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

import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpRequestBase;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;

@SuppressWarnings("restriction")
public abstract class OSIORestPatchRequest<T> extends OSIORestRequest<T> {

	public OSIORestPatchRequest(CommonHttpClient client, String urlSuffix, boolean authenticationRequired) {
		super(client, urlSuffix, authenticationRequired, false);
	}

	@Override
	protected HttpRequestBase createHttpRequestBase(String url) {
		HttpPatch request = new HttpPatch(url);
		request.setHeader(CONTENT_TYPE, APPLICATION_VND_JSON);
		return request;
	}

}