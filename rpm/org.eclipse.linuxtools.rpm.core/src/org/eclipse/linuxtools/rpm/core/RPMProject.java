/*******************************************************************************
 * Copyright (c) 2005, 2010 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.rpm.core.utils.RPM;
import org.eclipse.linuxtools.rpm.core.utils.RPMBuild;

public class RPMProject {

	private IProject project;
	private SourceRPM sourceRPM;
	private RPMConfiguration rpmConfig;

	public RPMProject(IProject project) throws CoreException {
		this.project = project;
		rpmConfig = new RPMConfiguration(this.project);
	}

	public IProject getProject() {
		return project;
	}

	public SourceRPM getSourceRPM() {
		return sourceRPM;
	}

	public void setSourceRPM(SourceRPM sourceRPM) throws CoreException {
		this.sourceRPM = sourceRPM;
		getProject()
				.setPersistentProperty(
						new QualifiedName(RPMCorePlugin.ID,
								IRPMConstants.SRPM_PROPERTY),
						sourceRPM.getFile().getName());
	}

	public RPMConfiguration getConfiguration() {
		return rpmConfig;
	}

	public IFile getSpecFile() {
		IFolder specsFolder = getConfiguration().getSpecsFolder();
		IFile file = null;
		try {
			file = specsFolder.getFile(specsFolder.members()[0].getName());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;
	}

	public void setSpecFile(IFile specFile) throws CoreException {
		getProject().setPersistentProperty(
				new QualifiedName(RPMCorePlugin.ID,
						IRPMConstants.SPEC_FILE_PROPERTY), specFile.getName());
	}

	public void importSourceRPM(File externalFile) throws CoreException {
		// Copy original SRPM to workspace
		IFile srpmFile = getConfiguration().getSrpmsFolder().getFile(
				externalFile.getName());
		try {
			srpmFile.create(new FileInputStream(externalFile), false, null);
		} catch (FileNotFoundException e) {
			String throw_message = Messages
					.getString("RPMCore.Error_trying_to_copy__") + //$NON-NLS-1$
					rpmConfig.getSpecsFolder().getLocation().toOSString();
			IStatus error = new Status(IStatus.ERROR, IRPMConstants.ERROR, 1,
					throw_message, null);
			throw new CoreException(error);
		}
		setSourceRPM(new SourceRPM(srpmFile));

		// Install the SRPM
		RPM rpm = new RPM(getConfiguration());
		rpm.install(getSourceRPM().getFile());
		getProject().refreshLocal(IResource.DEPTH_INFINITE, null);

		// Set the spec file
		IResource[] installedSpecs = getConfiguration().getSpecsFolder().members();
		if (installedSpecs.length != 1) {
			String throw_message = Messages
					.getString("RPMCore.spec_file_ambiguous") + //$NON-NLS-1$
					rpmConfig.getSpecsFolder().getLocation().toOSString();
			IStatus error = new Status(IStatus.ERROR, IRPMConstants.ERROR, 1,
					throw_message, null);
			throw new CoreException(error);
		}
		setSpecFile(getConfiguration().getSpecsFolder().getFile(
				installedSpecs[0].getName()));

		// Set the project nature
		RPMProjectNature.addRPMNature(getProject(), null);

	}

	public void buildAll(OutputStream outStream) throws CoreException {
		RPMBuild rpmbuild = new RPMBuild(getConfiguration());
		 rpmbuild.buildAll(getSpecFile(), outStream);

		getConfiguration().getBuildFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
		getConfiguration().getRpmsFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
		getConfiguration().getSrpmsFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
	}

	public void buildBinaryRPM(OutputStream out) throws CoreException {
		RPMBuild rpmbuild = new RPMBuild(getConfiguration());
		rpmbuild.buildBinary(getSpecFile(), out);

		getConfiguration().getBuildFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
		getConfiguration().getRpmsFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
	}

	public void buildSourceRPM(OutputStream out) throws CoreException {
		RPMBuild rpmbuild = new RPMBuild(getConfiguration());
		rpmbuild.buildSource(getSpecFile(), out);

		getConfiguration().getBuildFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
		getConfiguration().getSrpmsFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
	}

	public void buildPrep(OutputStream out) throws CoreException {
		RPMBuild rpmbuild = new RPMBuild(getConfiguration());
		rpmbuild.buildPrep(getSpecFile(), out);
		getConfiguration().getBuildFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
	}

}
