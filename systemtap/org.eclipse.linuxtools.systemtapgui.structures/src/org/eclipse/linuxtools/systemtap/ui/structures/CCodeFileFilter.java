/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.structures;

import java.io.File;
import java.io.FileFilter;

public class CCodeFileFilter implements FileFilter {
	/**
	 * Checks a file type and only passes it (returns true) if it is either a directory, a .c, or a .h
	 * file type.
	 * 
	 * @param f The file to check.
	 * 
	 * @return A boolean value indicating whether or not to display the file.
	 */
	public boolean accept(File f) {
		if(null == f)
			return false;
		return f.isDirectory() ||
				f.getName().toLowerCase().endsWith(".c") ||
				f.getName().toLowerCase().endsWith(".h");
	}
	
	public String getDescription() {
		return ".c, .h files";
	}
}
