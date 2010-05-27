/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *    Alphonse Van Assche
 *    Alexander Kurtakov - adapt to 3.5 API.
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.linuxtools.rpm.ui.editor.markers.SpecfileErrorHandler;
import org.eclipse.linuxtools.rpm.ui.editor.markers.SpecfileTaskHandler;
import org.eclipse.linuxtools.rpm.ui.editor.outline.SpecfileContentOutlinePage;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class SpecfileEditor extends TextEditor {

	private ColorManager colorManager;
	private SpecfileContentOutlinePage outlinePage;
	private IEditorInput input;
	private Specfile specfile;
	private SpecfileParser parser;
	private RpmMacroOccurrencesUpdater fOccurrencesUpdater;

	public SpecfileEditor() {
		super();
		colorManager = new ColorManager();
		parser = getParser();
		setSourceViewerConfiguration(new SpecfileConfiguration(colorManager,
				this));
		setKeyBindingScopes(new String[] { "org.eclipse.linuxtools.rpm.ui.specEditorScope" }); //$NON-NLS-1$
	}

	@Override
	public void dispose() {
		colorManager.dispose();
		// Set specfile field to null here is useful for test cases because
		// whether
		// the specfile in null SpecfileReconcilingStrategy#reconcile don't
		// update anything and thus it don't give false stacktraces.
		specfile = null;
		super.dispose();
	}

	@Override
	protected void doSetInput(IEditorInput newInput) throws CoreException {
		super.doSetInput(newInput);
		this.input = newInput;

		if (outlinePage != null)
			outlinePage.setInput(input);

		validateAndMark();
	}

	@Override
	protected void editorSaved() {
		super.editorSaved();

		// we validate and mark document here
		validateAndMark();

		if (outlinePage != null)
			outlinePage.update();
	}

	protected void validateAndMark() {
		try {
			IDocument document = getInputDocument();
			SpecfileErrorHandler specfileErrorHandler = new SpecfileErrorHandler(
					getInputFile(), document);
			specfileErrorHandler.removeExistingMarkers();
			SpecfileTaskHandler specfileTaskHandler = new SpecfileTaskHandler(
					getInputFile(), document);
			specfileTaskHandler.removeExistingMarkers();
			this.parser.setErrorHandler(specfileErrorHandler);
			this.parser.setTaskHandler(specfileTaskHandler);
			specfile = parser.parse(document);
		} catch (Exception e) {
			SpecfileLog.logError(e);
		}
	}

	/**
	 * Get a {@link IFile}, this implementation return <code>null</code> if the
	 * <code>IEditorInput</code> instance is not of type
	 * {@link IFileEditorInput}.
	 * 
	 * @return a <code>IFile</code> or <code>null</code>.
	 */
	protected IFile getInputFile() {
		if (input instanceof IFileEditorInput) {
			IFileEditorInput ife = (IFileEditorInput) input;
			IFile file = ife.getFile();
			return file;
		}
		return null;
	}

	public IDocument getInputDocument() {
		IDocument document = getDocumentProvider().getDocument(input);
		return document;
	}

	@Override
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			return getOutlinePage();
		}
		return super.getAdapter(required);
	}

	public SpecfileContentOutlinePage getOutlinePage() {
		if (outlinePage == null) {
			outlinePage = new SpecfileContentOutlinePage(this);
			if (getEditorInput() != null)
				outlinePage.setInput(getEditorInput());
		}
		return outlinePage;
	}

	public Specfile getSpecfile() {
		return specfile;
	}


	/*
	 * @see
	 * org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createPartControl
	 * (org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		fOccurrencesUpdater = new RpmMacroOccurrencesUpdater(this);
	}

	protected void setSpecfile(Specfile specfile) {
		this.specfile = specfile;
		if (fOccurrencesUpdater != null) {
			Shell shell = getSite().getShell();
			if (!(shell == null || shell.isDisposed())) {
				shell.getDisplay().asyncExec(new Runnable() {
					public void run() {
						fOccurrencesUpdater.update(getSourceViewer());
					}
				});
			}
		}

	}

	public final SpecfileParser getParser() {
		if (parser == null) {
			parser = new SpecfileParser();
		}
		return parser;
	}

	/**
	 * Get the spefile source viewer, this method is useful for test cases.
	 * 
	 * @return the specfile source viewer
	 */
	public SourceViewer getSpecfileSourceViewer() {
		return (SourceViewer) getSourceViewer();
	}

}
