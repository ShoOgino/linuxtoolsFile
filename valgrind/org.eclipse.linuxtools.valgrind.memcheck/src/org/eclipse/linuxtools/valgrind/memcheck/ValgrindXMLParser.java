/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.valgrind.memcheck;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.valgrind.core.ValgrindPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ValgrindXMLParser {
	protected static final String END_TAG = "</valgrindoutput>"; //$NON-NLS-1$

	protected DocumentBuilder builder;
	protected ArrayList<ValgrindError> errors;

	public ValgrindXMLParser(InputStream in) throws ParserConfigurationException, IOException, CoreException, SAXException {
		builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		errors = new ArrayList<ValgrindError>();

		StringBuffer xmlBuf = new StringBuffer();
		StringBuffer plainBuf = new StringBuffer();
		separateOutput(in, xmlBuf, plainBuf);

		// any plaintext in memcheck output is an error
		String err = plainBuf.toString().trim();
		if (err.length() > 0) {
			throw new CoreException(new Status(IStatus.ERROR, ValgrindPlugin.PLUGIN_ID, err));
		}

		InputSource is = new InputSource(new ByteArrayInputStream(xmlBuf.toString().getBytes()));
		Document doc = builder.parse(is);

		NodeList nodes = doc.getElementsByTagName("error"); //$NON-NLS-1$
		for (int i = 0; i < nodes.getLength(); i++) {
			errors.add(new ValgrindError(nodes.item(i)));
		}
	}

	/*
	 * Currently only memcheck works with xml output so core messages such as fatal errors
	 * will result in malformed xml documents.
	 */
	protected void separateOutput(InputStream in, StringBuffer xmlBuf, StringBuffer plainBuf) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		boolean xml = false;
		String line;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("<?xml")) { //$NON-NLS-1$
				xml = true;
			}
			if (xml) {
				xmlBuf.append(line + "\n"); //$NON-NLS-1$
			}
			else {
				plainBuf.append(line + "\n"); //$NON-NLS-1$
			}
			if (line.equals(END_TAG)) {
				xml = false;
			}
		}

	}

	public ArrayList<ValgrindError> getErrors() {
		return errors;
	}
}
