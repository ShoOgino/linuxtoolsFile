/*******************************************************************************
 * Copyright (c) 2004-2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.propertypage;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.rpm.core.utils.RPMQuery;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;

public class SpecFileHeaderPage extends AbstractRPMPropertyPage {

	private static final String RPM_QI = Messages
			.getString("SpecFileHeaderPage.info"); //$NON-NLS-1$

	private static final int QI_FIELD_WIDTH = 80;

	private static final int QI_FIELD_HEIGHT = 40;

	private Text rpm_qiText;

	@Override
	protected void addFields(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		// RPM labels and text fields setup

		Label rpmDescriptionLabel = new Label(composite, SWT.NONE);
		rpmDescriptionLabel.setText(RPM_QI);
		rpm_qiText = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY
				| SWT.V_SCROLL | SWT.WRAP);
		GridData gdQI = new GridData();
		gdQI.widthHint = convertWidthInCharsToPixels(QI_FIELD_WIDTH);
		gdQI.heightHint = convertWidthInCharsToPixels(QI_FIELD_HEIGHT);
		rpm_qiText.setLayoutData(gdQI);

		// Populate RPM text field
		try {
			String rpm_qi = RPMQuery.getHeaderInfo((IFile) getElement());
			rpm_qiText.setText(rpm_qi);
		} catch (CoreException e) {
			StatusManager.getManager().handle(new StatusAdapter(e.getStatus()),
					StatusManager.LOG | StatusManager.SHOW);
		}
	}

}