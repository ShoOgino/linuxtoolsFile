package org.eclipse.linuxtools.rpm.ui.editor.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.changelog.core.IParserChangeLogContrib;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

public class SpecfileChangelogParser implements IParserChangeLogContrib {

	public SpecfileChangelogParser() {
	}

	public String parseCurrentFunction(IEditorPart editor) throws CoreException {
		return "";
	}

	public String parseCurrentFunction(IEditorInput input, int offset) throws CoreException {
		return "";
	}

}
