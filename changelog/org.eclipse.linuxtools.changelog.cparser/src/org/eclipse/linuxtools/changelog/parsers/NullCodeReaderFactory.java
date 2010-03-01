/*******************************************************************************
 * Copyright (c) 2007, 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Red Hat Inc. - modified for usage in ChangeLog C Parser plug-in
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.parsers;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;

/**
 * A <code>ICodeReaderFactory</code> which creates dummy <code>CodeReader</code>s without content.
 *
 * @since 4.0
 */
@SuppressWarnings("deprecation")
public class NullCodeReaderFactory implements ICodeReaderFactory {

	private static final char[] EMPTY_CHARS = new char[0];
	private static final NullCodeReaderFactory INSTANCE= new NullCodeReaderFactory();

	public static NullCodeReaderFactory getInstance() {
		return INSTANCE;
	}

	private NullCodeReaderFactory() {
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#createCodeReaderForInclusion(java.lang.String)
	 */
	public CodeReader createCodeReaderForInclusion(String path) {
		return new CodeReader(path, EMPTY_CHARS);
	}

	
	public CodeReader createCodeReaderForInclusion(IIndexFileLocation ifl, String astPath) {
		return new CodeReader(astPath, EMPTY_CHARS);
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#createCodeReaderForTranslationUnit(java.lang.String)
	 */
	public CodeReader createCodeReaderForTranslationUnit(String path) {
		return new CodeReader(path, EMPTY_CHARS);
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#getCodeReaderCache()
	 */
	public ICodeReaderCache getCodeReaderCache() {
		return null;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#getUniqueIdentifier()
	 */
	public int getUniqueIdentifier() {
		// is this used somewhere?
		return 7;
	}

}
