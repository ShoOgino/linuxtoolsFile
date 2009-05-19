/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package com.st.dataviewers.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;

import com.st.dataviewers.abstractviewers.AbstractSTViewer;
import com.st.dataviewers.abstractviewers.STDataViewersImages;
import com.st.dataviewers.abstractviewers.STDataViewersMessages;
import com.st.dataviewers.dialogs.STDataViewersHideShowColumnsDialog;

/**
 * This action allows the user to hide/show some columns 
 *
 */
public class STHideShowColAction extends Action {
	
	private final AbstractSTViewer stViewer;

	/**
	 * Constructor
	 * @param stViewer
	 */
	public STHideShowColAction(AbstractSTViewer stViewer) {
		super(STDataViewersMessages.hideshowAction_title);
		this.stViewer = stViewer;
		Image img = STDataViewersImages.getImage(STDataViewersImages.IMG_EDIT_PROPERTIES); 
		super.setImageDescriptor(ImageDescriptor.createFromImage(img));
		setEnabled(true);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run(){
		STDataViewersHideShowColumnsDialog dialog =
			new STDataViewersHideShowColumnsDialog(stViewer);
		
		if (dialog.open() == Window.OK && dialog.isDirty()) {
			if (dialog.getManager() != null) {
				stViewer.setHideShowManager(dialog.getManager());
			}
		}
	}
}

