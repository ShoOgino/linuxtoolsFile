/*******************************************************************************
 * Copyright (c) 2015,2018 Red Hat.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.wizards;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IRegistry;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.RegistryInfo;
import org.eclipse.linuxtools.internal.docker.ui.databinding.BaseDatabindingModel;
import org.eclipse.linuxtools.internal.docker.ui.preferences.PreferenceConstants;

/**
 * Base {@link BaseDatabindingModel} for Push and Pull models.
 */
public abstract class ImagePullPushPageModel extends BaseDatabindingModel {

	public static final String SELECTED_IMAGE_NAME = "selectedImageName"; //$NON-NLS-1$

	public static final String SELECTED_REGISTRY = "selectedRegistry"; //$NON-NLS-1$

	public static final String TAG = "tag"; //$NON-NLS-1$

	private IRegistry selectedRegistry;

	private String selectedImageName;

	public ImagePullPushPageModel() {

	}

	/**
	 * Constructor
	 * 
	 * @param image
	 *            the selected {@link IDockerImage}
	 * @param selectedImageName
	 *            the selected repo/name/tag of the given image
	 */
	public ImagePullPushPageModel(final String selectedImageName) {
		setSelectedImageName(selectedImageName);
	}

	public IRegistry getSelectedRegistry() {
		return this.selectedRegistry;
	}

	public void setSelectedRegistry(final IRegistry selectedRegistry) {
		firePropertyChange(SELECTED_REGISTRY, this.selectedRegistry,
				this.selectedRegistry = selectedRegistry);
		// store the last registry used for future actions
		final IPreferenceStore store = Activator.getDefault()
				.getPreferenceStore();
		store.putValue(PreferenceConstants.LAST_REGISTRY_ACCOUNT,
				((RegistryInfo) selectedRegistry).getRegistryId());
	}

	public String getSelectedImageName() {
		return selectedImageName;
	}

	public void setSelectedImageName(final String selectedImageName) {
		firePropertyChange(SELECTED_IMAGE_NAME, this.selectedImageName,
				this.selectedImageName = selectedImageName);
	}

}
