/*******************************************************************************
 * Copyright (c) 2007 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools.ui.properties;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.ui.properties.AbstractCBuildPropertyTab;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.cdt.autotools.AutotoolsMakefileBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public class AutotoolsBuildPropertyPage extends AbstractCBuildPropertyTab {

	private String TRUE = "true"; // $NON-NLS-1$
	private String FALSE = "false"; // $NON-NLS-1$
	private String SCANNERMAKEW_LABEL = "ScannerMakeW.label"; // $NON-NLS-1$
	private String SCANNERMAKEW_TOOLTIP = "ScannerMakeW.tooltip"; // $NON-NLS-1$
	
	protected Button fCleanDelete;
	protected Button fCleanMake;
	protected Text   fCleanMakeTarget;
	protected Button fScannerMakeW;

	private IProject getProject() {
		return (IProject)getCfg().getManagedProject().getOwner();
	}

	public boolean canBeVisible() {
		return AutotoolsMakefileBuilder.hasTargetBuilder(getProject());
	}

	public void createControls(Composite parent) {
		super.createControls(parent);
		Composite composite= usercomp;
		// assume parent page uses griddata
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL);
		composite.setLayoutData(gd);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		//PixelConverter pc= new PixelConverter(composite);
		//layout.verticalSpacing= pc.convertHeightInCharsToPixels(1) / 2;
		composite.setLayout(layout);
		
		Group g = new Group(composite, SWT.SHADOW_ETCHED_IN);
		g.setText(AutotoolsPropertyMessages.getString("CleanBehavior.title"));
		gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		g.setLayoutData(gd);
		layout= new GridLayout();
		layout.numColumns= 2;
		g.setLayout(layout);
		
		fCleanDelete = new Button(g, SWT.RADIO);
		fCleanDelete.setText(AutotoolsPropertyMessages.getString("CleanDelete.label"));
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan = 2;
		fCleanDelete.setLayoutData(gd);
		fCleanMake = new Button(g, SWT.RADIO);
		fCleanMake.setText(AutotoolsPropertyMessages.getString("CleanMake.label"));
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan = 2;
		fCleanMake.setLayoutData(gd);
		
		Label label = new Label(g, SWT.LEFT);
		label.setText(AutotoolsPropertyMessages.getString("CleanMakeTarget.label"));
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		label.setLayoutData(gd);
		
		fCleanMakeTarget = new Text(g, SWT.SINGLE | SWT.BORDER);
		fCleanMakeTarget.setText(AutotoolsPropertyConstants.CLEAN_MAKE_TARGET_DEFAULT);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		fCleanMakeTarget.setLayoutData(gd);
		
		fCleanDelete.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				fCleanMake.setSelection(false);
				fCleanDelete.setSelection(true);
				fCleanMakeTarget.setEnabled(false);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
		});
		
		fCleanMake.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				fCleanDelete.setSelection(false);
				fCleanMake.setSelection(true);
				fCleanMakeTarget.setEnabled(true);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
		});
		
		fCleanMakeTarget.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (fCleanMakeTarget.getText().equals("")) { // $NON-NLS-1$
					// FIXME: should probably issue warning here, but how?
				}
			}
		});
		
		fScannerMakeW = new Button(composite, SWT.LEFT | SWT.CHECK); 
		fScannerMakeW.setText(AutotoolsPropertyMessages.getString(SCANNERMAKEW_LABEL));
		fScannerMakeW.setToolTipText(AutotoolsPropertyMessages.getString(SCANNERMAKEW_TOOLTIP));
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
		fScannerMakeW.setLayoutData(gd);
		
		initialize();
	}

	protected void performOK() {
		IProject project = getProject();
		if (fCleanDelete.getSelection()) {
			try {
				project.setPersistentProperty(AutotoolsPropertyConstants.CLEAN_DELETE, TRUE); 
			} catch (CoreException ce) {
				// FIXME: what can we do here?
			}
		} else {
			try {
				project.setPersistentProperty(AutotoolsPropertyConstants.CLEAN_DELETE, FALSE);
			} catch (CoreException ce) {
				// FIXME: what can we do here?
			}
			try {
				project.setPersistentProperty(AutotoolsPropertyConstants.CLEAN_MAKE_TARGET, fCleanMakeTarget.getText());
			} catch (CoreException ce) {
				// FIXME: what can we do here?
			}
		}
		boolean setScannerInfoDirty = false;
		try {
			// Get old scanner method setting to see if it has changed.
			String oldScannerMakeW = project.getPersistentProperty(AutotoolsPropertyConstants.SCANNER_USE_MAKE_W);
			if (fScannerMakeW.getSelection()) {
				project.setPersistentProperty(AutotoolsPropertyConstants.SCANNER_USE_MAKE_W, TRUE);
				if (oldScannerMakeW == null || !oldScannerMakeW.equals(TRUE))
					setScannerInfoDirty = true;
			} else {
				project.setPersistentProperty(AutotoolsPropertyConstants.SCANNER_USE_MAKE_W, null);
				if (oldScannerMakeW != null && oldScannerMakeW.equals(TRUE))
					setScannerInfoDirty = true;
			}
		} catch (CoreException ce) {
			ce.printStackTrace(); // FIXME: what can we do here?
		}
		// If the scanner info method changes, we must mark the current data as
		// dirty so it will be recalculated.
		if (setScannerInfoDirty) {
			try {
				project.setSessionProperty(AutotoolsPropertyConstants.SCANNER_INFO_DIRTY, Boolean.TRUE);
			} catch (CoreException ce2) {
				// FIXME: what can we do here?
			}
		}
	}

	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		performOK();
	}
	
	protected void performDefaults() {
		fCleanDelete.setSelection(false);
		fCleanMake.setSelection(true);
		fCleanMakeTarget.setText(AutotoolsPropertyConstants.CLEAN_MAKE_TARGET_DEFAULT);
		fCleanMakeTarget.setEnabled(true);
		fScannerMakeW.setSelection(true);
	}
	
	public void updateData(ICResourceDescription cfgd) {
		// what to do here?
	}
	
	public void updateButtons() {
		// what to do here?
	}

	public void setVisible (boolean b) {
		super.setVisible(b);
	}

	private void initialize() {
		IProject project = getProject();
		String cleanDelete = null;
		String cleanMakeTarget = null;
		String scannerMakeW = null;
		try {
			cleanDelete = project.getPersistentProperty(AutotoolsPropertyConstants.CLEAN_DELETE);
			cleanMakeTarget = project.getPersistentProperty(AutotoolsPropertyConstants.CLEAN_MAKE_TARGET);
			scannerMakeW = project.getPersistentProperty(AutotoolsPropertyConstants.SCANNER_USE_MAKE_W);
		} catch (CoreException e) {
			// do nothing
		}

		if (cleanMakeTarget == null) {
			cleanMakeTarget = AutotoolsPropertyConstants.CLEAN_MAKE_TARGET_DEFAULT;
		}
		fCleanMakeTarget.setText(cleanMakeTarget);

		if (cleanDelete == null || cleanDelete.equals(FALSE)) {
			fCleanDelete.setSelection(false);
			fCleanMake.setSelection(true);
			fCleanMakeTarget.setEnabled(true);
		} else {
			fCleanDelete.setSelection(true);
			fCleanMake.setSelection(false);
			fCleanMakeTarget.setEnabled(false);
		}
		
		if (scannerMakeW == null || !scannerMakeW.equals(TRUE)) {
			fScannerMakeW.setSelection(false);
		} else
			fScannerMakeW.setSelection(true);
	}
	
}
