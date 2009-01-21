/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.cachegrind.model;


public class CachegrindLine implements ICachegrindElement {
	protected CachegrindFunction parent;
	protected int line;
	protected long[] values;
	
	public CachegrindLine(CachegrindFunction parent, int line, long[] values) {
		this.parent = parent;
		this.line = line;
		this.values = values;
	}

	public ICachegrindElement[] getChildren() {
		return null;
	}
	
	public int getLine() {
		return line;
	}
	
	public long[] getValues() {
		return values;
	}

	public ICachegrindElement getParent() {
		return parent;
	}
	
}
