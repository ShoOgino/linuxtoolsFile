/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.callgraph.tests;

import junit.framework.TestCase;

import org.eclipse.linuxtools.callgraph.core.SystemTapTextView;

public class SystemTapGraphViewTest extends TestCase {
	private SystemTapTextView stapView = new SystemTapTextView();
	private String testText = "blah";
	
	//TODO: write some better tests here
	public void test() {
		System.out.println("\n\nLaunching RunSystemTapActionTest\n");

		
		stapView = (SystemTapTextView) TestHelper.makeView("org.eclipse.linuxtools.callgraph.core.systemtaptextview");
		if (stapView == null)
			try {
				throw new Exception("The SystemTapView is null");
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		
		stapView.println(testText);
		assertEquals(stapView.getText(), testText);
		
		stapView.clearAll();
		assertEquals(stapView.getText(), "");
	}
	
}
