/*******************************************************************************
 * Copyright (c) 2008 Alexander Kurtakov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.rpmlint.resolutions;

import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileElement;
import org.eclipse.swt.graphics.Image;

public class NoCleaningOfBuildroot extends AInsertLineResolution {

	public static final String ID = "no-cleaning-of-buildroot"; //$NON-NLS-1$

	public String getDescription() {
		return Messages.NoCleaningOfBuildroot_0;
	}

	public Image getImage() {
		return null;
	}

	public String getLabel() {
		return ID;
	}

	@Override
	public String getLineToInsert() {
		return "rm -Rf $RPM_BUILD_ROOT\n"; //$NON-NLS-1$
	}

	@Override
	public int getLineNumberForInsert(SpecfileEditor editor) {
		SpecfileElement[] sections = editor.getSpecfile().getSections();
		for (SpecfileElement section : sections) {
			if (section.getName().equals("install") //$NON-NLS-1$
					|| section.getName().equals("clean")) { //$NON-NLS-1$
				if (markerLine == section.getLineNumber()) {
					return section.getLineNumber() + 1;
				}
			}
		}
		return 0;
	}

}
